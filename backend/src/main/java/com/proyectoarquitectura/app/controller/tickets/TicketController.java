package com.proyectoarquitectura.app.controller.tickets;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.tickets.AsignarAgenteRequest;
import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CambiarEstadoRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.tickets.TicketService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> crear(@Valid @RequestBody CreateTicketRequest req,
                                                             @AuthenticationPrincipal CustomUserDetails me) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        TicketResponse data = ticketService.crear(req, me.getUsuario().getId());
        return ResponseEntity.status(201).body(ok(201, "Ticket creado", data));
    }

    @GetMapping("/validar-activo")
    public ResponseEntity<ApiResponse<ValidacionTicketActivoResponse>> validarActivo(
            @RequestParam String area,
            @AuthenticationPrincipal CustomUserDetails me) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        ValidacionTicketActivoResponse data = ticketService.validarTicketActivo(area, me.getUsuario().getId());
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','AGENTE')")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> buscar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaHasta,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer agenteId,
            @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {
        Page<TicketResponse> data = ticketService.buscar(
                estado, prioridad, tipo, fechaDesde, fechaHasta, usuarioId, agenteId, pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping("/mios")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> mios(@AuthenticationPrincipal CustomUserDetails me,
                                                                  @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        Page<TicketResponse> data = ticketService.listarPorCliente(me.getUsuario().getId(), pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','AGENTE')")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer agenteId,
            @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {

        Page<TicketResponse> data;
        if (agenteId != null) {
            data = ticketService.listarPorAgente(agenteId, pageable);
        } else if (estado != null) {
            data = ticketService.listarPorEstado(estado, pageable);
        } else {
            data = ticketService.listar(pageable);
        }
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<TicketResponse>> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ok(200, "OK", ticketService.obtenerPorCodigo(codigo)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(ok(200, "OK", ticketService.obtenerPorId(id)));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<TicketHistoryResponse>>> historial(@PathVariable Integer id) {
        return ResponseEntity.ok(ok(200, "OK", ticketService.obtenerHistorial(id)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','AGENTE')")
    public ResponseEntity<ApiResponse<TicketResponse>> actualizar(@PathVariable Integer id,
                                                                  @Valid @RequestBody ActualizarTicketRequest req,
                                                                  @AuthenticationPrincipal CustomUserDetails me) {
        Integer actor = me != null ? me.getUsuario().getId() : null;
        return ResponseEntity.ok(ok(200, "Ticket actualizado",
                ticketService.actualizar(id, req, actor)));
    }

    @PatchMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','AGENTE')")
    public ResponseEntity<ApiResponse<TicketResponse>> asignar(@PathVariable Integer id,
                                                               @Valid @RequestBody AsignarAgenteRequest req,
                                                               @AuthenticationPrincipal CustomUserDetails me) {
        Integer actor = me != null ? me.getUsuario().getId() : null;
        return ResponseEntity.ok(ok(200, "Agente asignado",
                ticketService.asignarAgente(id, req.getAgenteId(), actor)));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','AGENTE')")
    public ResponseEntity<ApiResponse<TicketResponse>> cambiarEstado(@PathVariable Integer id,
                                                                     @Valid @RequestBody CambiarEstadoRequest req,
                                                                     @AuthenticationPrincipal CustomUserDetails me) {
        Integer actor = me != null ? me.getUsuario().getId() : null;
        return ResponseEntity.ok(ok(200, "Estado actualizado",
                ticketService.cambiarEstado(id, req.getEstado(), actor)));
    }

    private <T> ApiResponse<T> ok(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}
