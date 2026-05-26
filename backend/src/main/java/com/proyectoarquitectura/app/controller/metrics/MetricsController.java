package com.proyectoarquitectura.app.controller.metrics;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorAgenteResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;
import com.proyectoarquitectura.app.service.metrics.MetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/metrics")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping("/tickets-por-estado")
    public ResponseEntity<ApiResponse<TicketsPorEstadoResponse>> ticketsPorEstado() {
        return ResponseEntity.ok(ok(metricsService.ticketsPorEstado()));
    }

    @GetMapping("/tiempo-promedio-resolucion")
    public ResponseEntity<ApiResponse<TiempoPromedioResolucionResponse>> tiempoPromedioResolucion() {
        return ResponseEntity.ok(ok(metricsService.tiempoPromedioResolucion()));
    }

    @GetMapping("/tickets-por-agente")
    public ResponseEntity<ApiResponse<TicketsPorAgenteResponse>> ticketsPorAgente() {
        return ResponseEntity.ok(ok(metricsService.ticketsPorAgente()));
    }

    @GetMapping("/tickets-por-prioridad")
    public ResponseEntity<ApiResponse<TicketsPorPrioridadResponse>> ticketsPorPrioridad() {
        return ResponseEntity.ok(ok(metricsService.ticketsPorPrioridad()));
    }

    private <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .message("OK")
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}
