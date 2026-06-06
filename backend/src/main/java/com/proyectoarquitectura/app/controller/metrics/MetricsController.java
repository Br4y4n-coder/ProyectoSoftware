package com.proyectoarquitectura.app.controller.metrics;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsResueltosPorAgenteResponse;
import com.proyectoarquitectura.app.service.metrics.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Métricas", description = "Estadísticas y métricas del sistema para el dashboard.")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @Operation(summary = "Tickets por estado", description = "Retorna el conteo de tickets agrupados por estado.")
    @GetMapping("/tickets-por-estado")
    public ResponseEntity<ApiResponse<List<TicketsPorEstadoResponse>>> getTicketsPorEstado() {
        List<TicketsPorEstadoResponse> data = metricsService.contarTicketsPorEstado();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Tiempo promedio de resolución", description = "Calcula el tiempo promedio en horas para resolver tickets desde la fecha indicada (por defecto últimos 30 días).")
    @GetMapping("/tiempo-promedio-resolucion")
    public ResponseEntity<ApiResponse<TiempoPromedioResolucionResponse>> getTiempoPromedioResolucion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio) {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now().minusDays(30);
        }
        TiempoPromedioResolucionResponse data = metricsService.promedioTiempoResolucionDesde(fechaInicio);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Tickets resueltos por agente", description = "Retorna el ranking de agentes por cantidad de tickets resueltos.")
    @GetMapping("/tickets-resueltos-por-agente")
    public ResponseEntity<ApiResponse<List<TicketsResueltosPorAgenteResponse>>> getTicketsPorAgente() {
        List<TicketsResueltosPorAgenteResponse> data = metricsService.contarTicketsResueltosPorAgente();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Tickets por prioridad", description = "Retorna el conteo de tickets agrupados por nivel de prioridad.")
    @GetMapping("/tickets-por-prioridad")
    public ResponseEntity<ApiResponse<List<TicketsPorPrioridadResponse>>> getTicketsPorPrioridad() {
        List<TicketsPorPrioridadResponse> data = metricsService.ticketsPorPrioridad();
        return ResponseEntity.ok(ok(200, "OK", data));
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