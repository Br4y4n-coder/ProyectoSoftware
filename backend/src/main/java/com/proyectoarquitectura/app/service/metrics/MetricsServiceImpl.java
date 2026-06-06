package com.proyectoarquitectura.app.service.metrics;

import com.proyectoarquitectura.app.models.dto.metrics.TicketMetricDTO;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsResueltosPorAgenteResponse;
import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final TicketRepository ticketRepository;

    public MetricsServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public List<TicketsPorEstadoResponse> contarTicketsPorEstado() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Long> conteo = new HashMap<>();
        for (Ticket t : tickets) {
            conteo.put(t.getEstado(), conteo.getOrDefault(t.getEstado(), 0L) + 1);
        }
        
        List<TicketsPorEstadoResponse> respuesta = new ArrayList<>();
        for (Map.Entry<String, Long> entry : conteo.entrySet()) {
            respuesta.add(new TicketsPorEstadoResponse(entry.getKey(), entry.getValue()));
        }
        return respuesta;
    }

    @Override
    public TiempoPromedioResolucionResponse promedioTiempoResolucionDesde(LocalDateTime fechaInicio) {
        List<Ticket> tickets = ticketRepository.findAll();
        double promedio = 0.0;
        long count = 0;
        for (Ticket t : tickets) {
            if (t.getFechaCierre() != null) {
                promedio += java.time.Duration.between(t.getFechaCreacion(), t.getFechaCierre()).toHours();
                count++;
            }
        }
        promedio = count > 0 ? promedio / count : 0.0;
        return new TiempoPromedioResolucionResponse(promedio, (long) tickets.size());
    }

    @Override
    public List<TicketsResueltosPorAgenteResponse> contarTicketsResueltosPorAgente() {
        return new ArrayList<>();
    }

    @Override
    public List<TicketsPorPrioridadResponse> ticketsPorPrioridad() {
        List<Ticket> tickets = ticketRepository.findAll();
        Map<String, Long> conteo = new HashMap<>();
        for (Ticket t : tickets) {
            conteo.put(t.getPrioridad(), conteo.getOrDefault(t.getPrioridad(), 0L) + 1);
        }
        
        List<TicketsPorPrioridadResponse> respuesta = new ArrayList<>();
        for (Map.Entry<String, Long> entry : conteo.entrySet()) {
            respuesta.add(new TicketsPorPrioridadResponse(entry.getKey(), entry.getValue()));
        }
        return respuesta;
    }

    @Override
    public List<TicketMetricDTO> obtenerMetricasAvanzadas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return new ArrayList<>();
    }
}