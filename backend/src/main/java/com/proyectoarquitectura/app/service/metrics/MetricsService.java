package com.proyectoarquitectura.app.service.metrics;

import com.proyectoarquitectura.app.models.dto.metrics.TicketMetricDTO;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsResueltosPorAgenteResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface MetricsService {
    List<TicketsPorEstadoResponse> contarTicketsPorEstado();
    TiempoPromedioResolucionResponse promedioTiempoResolucionDesde(LocalDateTime fechaInicio);
    List<TicketsResueltosPorAgenteResponse> contarTicketsResueltosPorAgente();
    List<TicketsPorPrioridadResponse> ticketsPorPrioridad();
    List<TicketMetricDTO> obtenerMetricasAvanzadas(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}