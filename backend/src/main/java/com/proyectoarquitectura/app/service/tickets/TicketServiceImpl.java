package com.proyectoarquitectura.app.service.tickets;

import com.proyectoarquitectura.app.exception.BusinessException;
import com.proyectoarquitectura.app.exception.NotFoundException;
import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import com.proyectoarquitectura.app.models.entity.Area;
import com.proyectoarquitectura.app.models.entity.Categoria;
import com.proyectoarquitectura.app.models.entity.SlaRegla;
import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.TicketHistory;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.AreaRepository;
import com.proyectoarquitectura.app.repository.CategoriaRepository;
import com.proyectoarquitectura.app.repository.SlaReglaRepository;
import com.proyectoarquitectura.app.repository.TicketHistoryRepository;
import com.proyectoarquitectura.app.repository.TicketRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.repository.specification.TicketSpecification;
import com.proyectoarquitectura.app.service.auth.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final SlaReglaRepository slaReglaRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final AreaRepository areaRepository;
    private final EmailService emailService;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UsuarioRepository usuarioRepository,
                             CategoriaRepository categoriaRepository,
                             SlaReglaRepository slaReglaRepository,
                             TicketHistoryRepository ticketHistoryRepository,
                             AreaRepository areaRepository,
                             EmailService emailService) {
        this.ticketRepository = ticketRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.slaReglaRepository = slaReglaRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.areaRepository = areaRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public TicketResponse crear(CreateTicketRequest req, Integer clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new NotFoundException("Cliente no encontrado"));

        Categoria categoria = categoriaRepository.findById(req.getCategoriaId())
                .orElseThrow(() -> new NotFoundException("Categoria no encontrada"));

        if (categoria.getArea() == null) {
            throw BusinessException.badRequest("La categoria no tiene area asociada");
        }

        validarSinTicketActivoEnArea(clienteId, categoria.getArea());

        SlaRegla sla = slaReglaRepository
                .findFirstByPrioridadAndAplicaRolIdAndActivoTrueOrderByIdAsc(req.getPrioridad(), cliente.getRol().getId())
                .orElse(null);

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime venceSla = sla != null
                ? ahora.plusHours(sla.getTiempoResolucionHoras())
                : null;

        String codigoTemporal = "TMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Ticket t = Ticket.builder()
                .codigo(codigoTemporal)
                .asunto(req.getAsunto().trim())
                .descripcion(req.getDescripcion().trim())
                .tipo(req.getTipo())
                .prioridad(req.getPrioridad() == null ? "media" : req.getPrioridad())
                .estado("abierto")
                .categoria(categoria)
                .cliente(cliente)
                .slaRegla(sla)
                .fechaVencimientoSla(venceSla)
                .build();

        t = ticketRepository.save(t);
        t.setCodigo(String.format("TKT-%04d", t.getId()));

        log.info("Ticket creado id={} codigo={} cliente={}", t.getId(), t.getCodigo(), cliente.getCorreo());
        emailService.notificarTicketCreado(t);
        return TicketResponse.from(t);
    }

    @Override
    @Transactional(readOnly = true)
    public ValidacionTicketActivoResponse validarTicketActivo(String area, Integer clienteId) {
        Area areaEntidad = resolverArea(area);
        Optional<Ticket> activo = ticketRepository.findTicketActivoPorClienteYArea(
                clienteId, areaEntidad.getId(), areaEntidad.getNombre());

        if (activo.isPresent()) {
            return ValidacionTicketActivoResponse.builder()
                    .tieneTicketActivo(true)
                    .ticketActivo(TicketResponse.from(activo.get()))
                    .build();
        }
        return ValidacionTicketActivoResponse.builder()
                .tieneTicketActivo(false)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse obtenerPorId(Integer id) {
        return TicketResponse.from(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse obtenerPorCodigo(String codigo) {
        Ticket t = ticketRepository.findByCodigo(codigo)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado: " + codigo));
        return TicketResponse.from(t);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> listar(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> listarPorCliente(Integer clienteId, Pageable pageable) {
        return ticketRepository.findByClienteId(clienteId, pageable).map(TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> listarPorAgente(Integer agenteId, Pageable pageable) {
        return ticketRepository.findByAgenteId(agenteId, pageable).map(TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> listarPorEstado(String estado, Pageable pageable) {
        return ticketRepository.findByEstado(estado, pageable).map(TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> buscar(String estado,
                                       String prioridad,
                                       String tipo,
                                       LocalDateTime fechaDesde,
                                       LocalDateTime fechaHasta,
                                       Integer usuarioId,
                                       Integer agenteId,
                                       Pageable pageable) {
        Specification<Ticket> spec = TicketSpecification.buscar(
                estado, prioridad, tipo, fechaDesde, fechaHasta, usuarioId, agenteId);
        return ticketRepository.findAll(spec, pageable).map(TicketResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> obtenerHistorial(Integer ticketId) {
        buscar(ticketId);
        return ticketHistoryRepository.findByTicketIdOrderByFechaHoraDesc(ticketId).stream()
                .map(TicketHistoryResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse asignarAgente(Integer ticketId, Integer agenteId, Integer usuarioActorId) {
        Ticket t = buscar(ticketId);
        Usuario agente = usuarioRepository.findById(agenteId)
                .orElseThrow(() -> new NotFoundException("Agente no encontrado"));

        if (agente.getRol() == null || !"agente".equalsIgnoreCase(agente.getRol().getNombre())) {
            throw BusinessException.badRequest("El usuario " + agenteId + " no tiene rol agente");
        }
        if (!"activo".equalsIgnoreCase(agente.getEstado())) {
            throw BusinessException.badRequest("El agente no esta activo");
        }
        if ("cerrado".equalsIgnoreCase(t.getEstado()) || "cancelado".equalsIgnoreCase(t.getEstado())) {
            throw BusinessException.badRequest("No se puede asignar un ticket cerrado o cancelado");
        }

        String agenteAnterior = t.getAgente() != null ? String.valueOf(t.getAgente().getId()) : null;
        t.setAgente(agente);
        if ("abierto".equalsIgnoreCase(t.getEstado())) {
            t.setEstado("asignado");
        }
        ticketRepository.save(t);

        registrarHistorial(t.getId(), "asignacion", agenteAnterior, String.valueOf(agenteId), usuarioActorId);
        emailService.notificarTicketAsignado(t);

        log.info("Ticket {} asignado a agente {}", t.getCodigo(), agente.getCorreo());
        return TicketResponse.from(t);
    }

    @Override
    @Transactional
    public TicketResponse cambiarEstado(Integer ticketId, String nuevoEstado, Integer usuarioActorId) {
        Ticket t = buscar(ticketId);
        String anterior = t.getEstado();

        if (anterior.equalsIgnoreCase(nuevoEstado)) {
            return TicketResponse.from(t);
        }
        validarTransicion(anterior, nuevoEstado);

        t.setEstado(nuevoEstado);
        LocalDateTime ahora = LocalDateTime.now();

        if ("en_proceso".equalsIgnoreCase(nuevoEstado)
                && ("abierto".equalsIgnoreCase(anterior) || "asignado".equalsIgnoreCase(anterior))) {
            if (t.getFechaInicioAtencion() == null) {
                t.setFechaInicioAtencion(ahora);
            }
        }
        if ("resuelto".equalsIgnoreCase(nuevoEstado) || "cerrado".equalsIgnoreCase(nuevoEstado)) {
            if (t.getFechaCierre() == null) {
                t.setFechaCierre(ahora);
            }
            calcularTiempoResolucion(t, ahora);
        }

        ticketRepository.save(t);
        registrarHistorial(t.getId(), "estado", anterior, nuevoEstado, usuarioActorId);
        emailService.notificarCambioEstado(t, anterior);

        log.info("Ticket {} estado: {} -> {}", t.getCodigo(), anterior, nuevoEstado);
        return TicketResponse.from(t);
    }

    @Override
    @Transactional
    public TicketResponse actualizar(Integer ticketId, ActualizarTicketRequest req, Integer usuarioActorId) {
        Ticket t = buscar(ticketId);

        if (req.getAsunto() != null && !req.getAsunto().isBlank()
                && !Objects.equals(t.getAsunto(), req.getAsunto().trim())) {
            registrarHistorial(t.getId(), "asunto", t.getAsunto(), req.getAsunto().trim(), usuarioActorId);
            t.setAsunto(req.getAsunto().trim());
        }
        if (req.getDescripcion() != null && !req.getDescripcion().isBlank()
                && !Objects.equals(t.getDescripcion(), req.getDescripcion().trim())) {
            registrarHistorial(t.getId(), "descripcion", t.getDescripcion(), req.getDescripcion().trim(), usuarioActorId);
            t.setDescripcion(req.getDescripcion().trim());
        }
        if (req.getPrioridad() != null && !req.getPrioridad().equalsIgnoreCase(t.getPrioridad())) {
            registrarHistorial(t.getId(), "prioridad", t.getPrioridad(), req.getPrioridad(), usuarioActorId);
            t.setPrioridad(req.getPrioridad());
        }

        return TicketResponse.from(ticketRepository.save(t));
    }

    private void calcularTiempoResolucion(Ticket t, LocalDateTime fin) {
        LocalDateTime desde;
        if (t.getFechaInicioAtencion() != null) {
            desde = t.getFechaInicioAtencion();
        } else if (t.getAgente() != null) {
            desde = t.getFechaCreacion();
        } else {
            desde = t.getFechaCreacion();
        }
        if (desde != null) {
            long minutos = Duration.between(desde, fin).toMinutes();
            t.setTiempoResolucionMinutos((int) Math.max(minutos, 0));
        }
    }

    private void validarSinTicketActivoEnArea(Integer clienteId, Area area) {
        Optional<Ticket> activo = ticketRepository.findTicketActivoPorClienteYArea(
                clienteId, area.getId(), area.getNombre());
        if (activo.isPresent()) {
            throw BusinessException.badRequest(
                    "Ya existe un ticket activo en el area '" + area.getNombre()
                            + "': " + activo.get().getCodigo());
        }
    }

    private Area resolverArea(String area) {
        if (area == null || area.isBlank()) {
            throw BusinessException.badRequest("El parametro area es obligatorio");
        }
        try {
            Integer areaId = Integer.parseInt(area.trim());
            return areaRepository.findById(areaId)
                    .orElseThrow(() -> new NotFoundException("Area no encontrada: " + areaId));
        } catch (NumberFormatException e) {
            return areaRepository.findByNombre(area.trim())
                    .orElseThrow(() -> new NotFoundException("Area no encontrada: " + area));
        }
    }

    private void registrarHistorial(Integer ticketId,
                                    String campo,
                                    String valorAnterior,
                                    String valorNuevo,
                                    Integer actorId) {
        try {
            Usuario actor = actorId != null ? usuarioRepository.findById(actorId).orElse(null) : null;
            TicketHistory h = TicketHistory.builder()
                    .ticketId(ticketId)
                    .campoModificado(campo)
                    .valorAnterior(valorAnterior)
                    .valorNuevo(valorNuevo)
                    .usuario(actor)
                    .build();
            ticketHistoryRepository.save(h);
        } catch (Exception e) {
            log.warn("No se pudo registrar historial del ticket {}: {}", ticketId, e.getMessage());
        }
    }

    private Ticket buscar(Integer id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Ticket no encontrado: " + id));
    }

    private void validarTransicion(String desde, String hacia) {
        if ("cerrado".equalsIgnoreCase(desde) && !"reabierto".equalsIgnoreCase(hacia)) {
            throw BusinessException.badRequest("Un ticket cerrado solo puede pasar a reabierto");
        }
        if ("cancelado".equalsIgnoreCase(desde)) {
            throw BusinessException.badRequest("Un ticket cancelado no puede cambiar de estado");
        }
    }
}
