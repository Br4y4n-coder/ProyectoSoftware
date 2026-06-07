package com.proyectoarquitectura.app.controller.tickets;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.tickets.AsignarAgenteRequest;
import com.proyectoarquitectura.app.models.dto.tickets.ActualizarTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CambiarEstadoRequest;
import com.proyectoarquitectura.app.models.dto.tickets.ComentarioTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.CreateTicketRequest;
import com.proyectoarquitectura.app.models.dto.tickets.TicketHistoryResponse;
import com.proyectoarquitectura.app.models.dto.tickets.TicketResponse;
import com.proyectoarquitectura.app.models.dto.tickets.ValidacionTicketActivoResponse;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.tickets.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Tickets", description = "Gestión del ciclo de vida de tickets de soporte")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Operation(summary = "Crear ticket", description = "Crea un nuevo ticket de soporte para el usuario autenticado.")
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> crear(@Valid @RequestBody CreateTicketRequest req,
                                                             @AuthenticationPrincipal CustomUserDetails me) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        TicketResponse data = ticketService.crear(req, me.getUsuario().getId());
        return ResponseEntity.status(201).body(ok(201, "Ticket creado", data));
    }

    @Operation(summary = "Validar ticket activo", description = "Verifica si el usuario ya tiene un ticket activo en el área indicada.")
    @GetMapping("/validar-activo")
    public ResponseEntity<ApiResponse<ValidacionTicketActivoResponse>> validarActivo(
            @RequestParam String area,
            @AuthenticationPrincipal CustomUserDetails me) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        ValidacionTicketActivoResponse data = ticketService.validarTicketActivo(area, me.getUsuario().getId());
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Buscar tickets con filtros", description = "Búsqueda avanzada de tickets por estado, prioridad, tipo, fechas, usuario o agente. Requiere rol ADMINISTRADOR o AGENTE.")
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

    @Operation(summary = "Listar mis tickets", description = "Retorna los tickets creados por el usuario autenticado.")
    @GetMapping("/mios")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> mios(@AuthenticationPrincipal CustomUserDetails me,
                                                                  @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        Page<TicketResponse> data = ticketService.listarPorCliente(me.getUsuario().getId(), pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Listar todos los tickets", description = "Lista todos los tickets con filtro opcional por estado o agente. Requiere rol ADMINISTRADOR o AGENTE.")
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

    @Operation(summary = "Obtener ticket por código", description = "Retorna el detalle completo de un ticket usando su código único (ej: TK-0001).")
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<ApiResponse<TicketResponse>> obtenerPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(ok(200, "OK", ticketService.obtenerPorCodigo(codigo)));
    }

    @Operation(summary = "Obtener ticket por ID", description = "Retorna el detalle completo de un ticket por su ID numérico.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(ok(200, "OK", ticketService.obtenerPorId(id)));
    }

    @Operation(summary = "Historial de un ticket", description = "Retorna la línea de tiempo de cambios y comentarios del ticket.")
    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<TicketHistoryResponse>>> historial(@PathVariable Integer id) {
        return ResponseEntity.ok(ok(200, "OK", ticketService.obtenerHistorial(id)));
    }

    @Operation(summary = "Actualizar ticket", description = "Modifica los campos del ticket. Requiere rol ADMINISTRADOR o AGENTE.")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','AGENTE')")
    public ResponseEntity<ApiResponse<TicketResponse>> actualizar(@PathVariable Integer id,
                                                                  @Valid @RequestBody ActualizarTicketRequest req,
                                                                  @AuthenticationPrincipal CustomUserDetails me) {
        Integer actor = me != null ? me.getUsuario().getId() : null;
        return ResponseEntity.ok(ok(200, "Ticket actualizado",
                ticketService.actualizar(id, req, actor)));
    }

    @Operation(summary = "Asignar agente a ticket", description = "Asigna un agente responsable al ticket. Requiere rol ADMINISTRADOR o AGENTE.")
    @PatchMapping("/{id}/asignar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','AGENTE')")
    public ResponseEntity<ApiResponse<TicketResponse>> asignar(@PathVariable Integer id,
                                                               @Valid @RequestBody AsignarAgenteRequest req,
                                                               @AuthenticationPrincipal CustomUserDetails me) {
        Integer actor = me != null ? me.getUsuario().getId() : null;
        return ResponseEntity.ok(ok(200, "Agente asignado",
                ticketService.asignarAgente(id, req.getAgenteId(), actor)));
    }

    @Operation(summary = "Comentar un ticket", description = "Agrega un comentario o nota al historial del ticket.")
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<ApiResponse<TicketHistoryResponse>> comentar(@PathVariable Integer id,
                                                                       @Valid @RequestBody ComentarioTicketRequest req,
                                                                       @AuthenticationPrincipal CustomUserDetails me) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        TicketHistoryResponse data = ticketService.comentar(id, req.getTexto(), me.getUsuario().getId());
        return ResponseEntity.status(201).body(ok(201, "Comentario agregado", data));
    }

    @Operation(summary = "Cambiar estado del ticket", description = "Cambia el estado del ticket (ABIERTO, EN_PROGRESO, RESUELTO, CERRADO). Requiere rol ADMINISTRADOR o AGENTE.")
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
