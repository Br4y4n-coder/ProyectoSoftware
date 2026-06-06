package com.proyectoarquitectura.app.service.tickets;

import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.TicketRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.service.auth.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final UsuarioRepository usuarioRepository;
    private final TicketRepository ticketRepository;
    private final EmailService emailService;

    public TicketServiceImpl(UsuarioRepository usuarioRepository, TicketRepository ticketRepository,
                             EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.ticketRepository = ticketRepository;
        this.emailService = emailService;
    }

    @Override
    public TicketResponse crear(CreateTicketRequest req, Integer clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        
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
        emailService.notificarTicketCreado(ticket);
        return TicketResponse.from(ticket);
    }

    @Override
    public TicketResponse obtenerPorId(Integer id) {
        Ticket ticket = ticketRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        return TicketResponse.from(ticket);
    }

    @Override
    public TicketResponse obtenerPorCodigo(String codigo) {
        Ticket ticket = ticketRepository.findByCodigo(codigo)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
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
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        Usuario agente = usuarioRepository.findById(agenteId)
            .orElseThrow(() -> new RuntimeException("Agente no encontrado"));
        
        ticket.setAgente(agente);
        ticket.setFechaInicioAtencion(LocalDateTime.now());
        ticket.setActualizadoEn(LocalDateTime.now());

        ticket = ticketRepository.save(ticket);
        emailService.notificarTicketAsignado(ticket);
        return TicketResponse.from(ticket);
    }

    @Override
    public TicketResponse cambiarEstado(Integer ticketId, String nuevoEstado, Integer usuarioActorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        String estadoAnterior = ticket.getEstado();
        ticket.setEstado(nuevoEstado);
        if ("cerrado".equalsIgnoreCase(nuevoEstado) || "resuelto".equalsIgnoreCase(nuevoEstado)) {
            ticket.setFechaCierre(LocalDateTime.now());
        }
        ticket.setActualizadoEn(LocalDateTime.now());

        ticket = ticketRepository.save(ticket);
        if (!nuevoEstado.equalsIgnoreCase(estadoAnterior)) {
            emailService.notificarCambioEstado(ticket, estadoAnterior);
        }
        return TicketResponse.from(ticket);
    }

    @Override
    public TicketResponse actualizar(Integer ticketId, ActualizarTicketRequest req, Integer usuarioActorId) {
        Ticket ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new RuntimeException("Ticket no encontrado"));
        
        if (req.getAsunto() != null) ticket.setAsunto(req.getAsunto());
        if (req.getDescripcion() != null) ticket.setDescripcion(req.getDescripcion());
        if (req.getPrioridad() != null) ticket.setPrioridad(req.getPrioridad());
        
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
        if (estado != null) {
            return ticketRepository.findByEstado(estado, pageable).map(TicketResponse::from);
        }
        return ticketRepository.findAll(pageable).map(TicketResponse::from);
    }

    @Override
    public List<TicketHistoryResponse> obtenerHistorial(Integer ticketId) {
        return Collections.emptyList();
    }
}