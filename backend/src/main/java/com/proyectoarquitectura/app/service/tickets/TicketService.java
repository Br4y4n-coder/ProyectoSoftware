package com.proyectoarquitectura.app.service.tickets;

import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketService {

    TicketResponse crear(CreateTicketRequest req, Integer clienteId);

    TicketResponse obtenerPorId(Integer id);

    TicketResponse obtenerPorCodigo(String codigo);

    Page<TicketResponse> listar(Pageable pageable);

    Page<TicketResponse> listarPorCliente(Integer clienteId, Pageable pageable);

    Page<TicketResponse> listarPorAgente(Integer agenteId, Pageable pageable);

    Page<TicketResponse> listarPorEstado(String estado, Pageable pageable);

    TicketResponse asignarAgente(Integer ticketId, Integer agenteId, Integer usuarioActorId);

    TicketResponse cambiarEstado(Integer ticketId, String nuevoEstado, Integer usuarioActorId);

    TicketResponse actualizar(Integer ticketId, ActualizarTicketRequest req, Integer usuarioActorId);

    ValidacionTicketActivoResponse validarTicketActivo(String area, Integer clienteId);

    Page<TicketResponse> buscar(String estado,
                                String prioridad,
                                String tipo,
                                LocalDateTime fechaDesde,
                                LocalDateTime fechaHasta,
                                Integer usuarioId,
                                Integer agenteId,
                                Pageable pageable);

    List<TicketHistoryResponse> obtenerHistorial(Integer ticketId);
}
