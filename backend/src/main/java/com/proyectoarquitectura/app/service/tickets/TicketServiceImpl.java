package com.proyectoarquitectura.app.service.tickets;

import com.proyectoarquitectura.app.exception.NotFoundException;
import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.TicketHistory;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.TicketHistoryRepository;
import com.proyectoarquitectura.app.repository.TicketRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.service.auth.EmailService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TicketServiceImpl implements TicketService {

    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final EmailService emailService;

    public TicketServiceImpl(UsuarioRepository usuarioRepository,
                             TicketRepository ticketRepository,
                             TicketHistoryRepository ticketHistoryRepository,
                             EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.ticketHistoryRepository = ticketHistoryRepository;
        this.emailService = emailService;
    }

    @Override
    public TicketResponse crear(CreateTicketRequest req, Integer clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado: " + clienteId));

        Ticket ticket = new Ticket();
        ticket.setCodigo("TKT-" + System.currentTimeMillis());
        ticket.setAsunto(req.getAsunto());
        ticket.setDescripcion(req.getDescripcion());
        ticket.setTipo(req.getTipo());
        ticket.setPrioridad(req.getPrioridad());
        ticket.setEstado("abierto");
        ticket.setCliente(cliente);
        ticket.setFechaCreacion(LocalDateTime.now());
        ticket.setActualizadoEn(LocalDateTime.now());

        ticket = ticketRepository.save(ticket);
        registrarHistorial(ticket.getId(), "creacion", null, ticket.getCodigo(), clienteId);
        emailService.notificarTicketCreado(ticket);
        return TicketResponse.from(ticket);
    }

    @Override
    public TicketResponse obtenerPorId(Integer id) {
        return TicketResponse.from(obtenerTicket(id));
    }

    @Override
    public TicketResponse obtenerPorCodigo(String codigo) {
        Ticket ticket = ticketRepository.findByCodigo(codigo)
            .orElseThrow(() -> new NotFoundException("Ticket no encontrado: " + codigo));
        return TicketResponse.from(ticket);
    }

    @Override
    public Page<TicketResponse> listar(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(TicketResponse::from);
    }

    @Override
    public Page<TicketResponse> listarPorCliente(Integer clienteId, Pageable pageable) {
        return ticketRepository.findByClienteId(clienteId, pageable).map(TicketResponse::from);
    }

    @Override
    public Page<TicketResponse> listarPorAgente(Integer agenteId, Pageable pageable) {
        return ticketRepository.findByAgenteId(agenteId, pageable).map(TicketResponse::from);
    }

    @Override
    public Page<TicketResponse> listarPorEstado(String estado, Pageable pageable) {
        return ticketRepository.findByEstado(estado, pageable).map(TicketResponse::from);
    }

    @Override
    public TicketResponse asignarAgente(Integer ticketId, Integer agenteId, Integer usuarioActorId) {
        Ticket ticket = obtenerTicket(ticketId);
        Usuario agente = usuarioRepository.findById(agenteId)
            .orElseThrow(() -> new NotFoundException("Agente no encontrado: " + agenteId));

        String agenteAnterior = ticket.getAgente() != null
                ? ticket.getAgente().getNombres() + " " + ticket.getAgente().getApellidos()
                : null;

        ticket.setAgente(agente);
        ticket.setFechaInicioAtencion(LocalDateTime.now());
        ticket.setActualizadoEn(LocalDateTime.now());

        ticket = ticketRepository.save(ticket);
        registrarHistorial(ticketId, "agente", agenteAnterior,
                agente.getNombres() + " " + agente.getApellidos(), usuarioActorId);
        emailService.notificarTicketAsignado(ticket);
        return TicketResponse.from(ticket);
    }

    @Override
    public TicketResponse cambiarEstado(Integer ticketId, String nuevoEstado, Integer usuarioActorId) {
        Ticket ticket = obtenerTicket(ticketId);

        String estadoAnterior = ticket.getEstado();
        ticket.setEstado(nuevoEstado);
        if ("cerrado".equalsIgnoreCase(nuevoEstado) || "resuelto".equalsIgnoreCase(nuevoEstado)) {
            ticket.setFechaCierre(LocalDateTime.now());
        }
        ticket.setActualizadoEn(LocalDateTime.now());

        ticket = ticketRepository.save(ticket);
        if (!nuevoEstado.equalsIgnoreCase(estadoAnterior)) {
            registrarHistorial(ticketId, "estado", estadoAnterior, nuevoEstado, usuarioActorId);
            emailService.notificarCambioEstado(ticket, estadoAnterior);
        }
        return TicketResponse.from(ticket);
    }

    @Override
    public TicketResponse actualizar(Integer ticketId, ActualizarTicketRequest req, Integer usuarioActorId) {
        Ticket ticket = obtenerTicket(ticketId);

        if (req.getAsunto() != null && !Objects.equals(req.getAsunto(), ticket.getAsunto())) {
            registrarHistorial(ticketId, "asunto", ticket.getAsunto(), req.getAsunto(), usuarioActorId);
            ticket.setAsunto(req.getAsunto());
        }
        if (req.getDescripcion() != null && !Objects.equals(req.getDescripcion(), ticket.getDescripcion())) {
            registrarHistorial(ticketId, "descripcion", ticket.getDescripcion(), req.getDescripcion(), usuarioActorId);
            ticket.setDescripcion(req.getDescripcion());
        }
        if (req.getPrioridad() != null && !Objects.equals(req.getPrioridad(), ticket.getPrioridad())) {
            registrarHistorial(ticketId, "prioridad", ticket.getPrioridad(), req.getPrioridad(), usuarioActorId);
            ticket.setPrioridad(req.getPrioridad());
        }

        ticket.setActualizadoEn(LocalDateTime.now());

        ticket = ticketRepository.save(ticket);
        return TicketResponse.from(ticket);
    }

    @Override
    public ValidacionTicketActivoResponse validarTicketActivo(String area, Integer clienteId) {
        ValidacionTicketActivoResponse response = new ValidacionTicketActivoResponse();
        response.setTieneTicketActivo(false);
        return response;
    }

    @Override
    public Page<TicketResponse> buscar(String estado, String prioridad, String tipo,
                                       LocalDateTime fechaDesde, LocalDateTime fechaHasta,
                                       Integer usuarioId, Integer agenteId, Pageable pageable) {
        Specification<Ticket> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (estado != null && !estado.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("estado")), estado.toLowerCase()));
            }
            if (prioridad != null && !prioridad.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("prioridad")), prioridad.toLowerCase()));
            }
            if (tipo != null && !tipo.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("tipo")), tipo.toLowerCase()));
            }
            if (fechaDesde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaDesde));
            }
            if (fechaHasta != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaHasta));
            }
            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("cliente").get("id"), usuarioId));
            }
            if (agenteId != null) {
                predicates.add(cb.equal(root.get("agente").get("id"), agenteId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return ticketRepository.findAll(spec, pageable).map(TicketResponse::from);
    }

    @Override
    public List<TicketHistoryResponse> obtenerHistorial(Integer ticketId) {
        // Valida que el ticket exista para responder 404 en vez de lista vacía engañosa
        obtenerTicket(ticketId);
        return ticketHistoryRepository.findByTicketIdOrderByFechaHoraDesc(ticketId)
                .stream()
                .map(TicketHistoryResponse::from)
                .toList();
    }

    /* ---------- Helpers ---------- */

    private Ticket obtenerTicket(Integer id) {
        return ticketRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Ticket no encontrado: " + id));
    }

    private void registrarHistorial(Integer ticketId, String campo, String valorAnterior,
                                    String valorNuevo, Integer actorId) {
        Usuario actor = actorId != null
                ? usuarioRepository.findById(actorId).orElse(null)
                : null;
        ticketHistoryRepository.save(TicketHistory.builder()
                .ticketId(ticketId)
                .campoModificado(campo)
                .valorAnterior(valorAnterior)
                .valorNuevo(valorNuevo)
                .usuario(actor)
                .build());
    }
}
