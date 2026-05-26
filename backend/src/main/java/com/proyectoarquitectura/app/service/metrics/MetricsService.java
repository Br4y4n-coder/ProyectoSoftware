package com.proyectoarquitectura.app.service.metrics;

import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorAgenteResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;

public interface MetricsService {

    TicketsPorEstadoResponse ticketsPorEstado();

    TiempoPromedioResolucionResponse tiempoPromedioResolucion();

    TicketsPorAgenteResponse ticketsPorAgente();

    TicketsPorPrioridadResponse ticketsPorPrioridad();
}
