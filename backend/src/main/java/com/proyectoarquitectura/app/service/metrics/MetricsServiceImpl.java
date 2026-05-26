package com.proyectoarquitectura.app.service.metrics;

import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorAgenteResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorAgenteResponse.AgenteResueltosItem;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;
import com.proyectoarquitectura.app.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MetricsServiceImpl implements MetricsService {

    private final TicketRepository ticketRepository;

    public MetricsServiceImpl(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public TicketsPorEstadoResponse ticketsPorEstado() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Object[] row : ticketRepository.contarTicketsPorEstado()) {
            mapa.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return TicketsPorEstadoResponse.builder().conteoPorEstado(mapa).build();
    }

    @Override
    @Transactional(readOnly = true)
    public TiempoPromedioResolucionResponse tiempoPromedioResolucion() {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        Double promedio = ticketRepository.promedioTiempoResolucionDesde(desde);
        return TiempoPromedioResolucionResponse.builder()
                .promedioMinutos(promedio == null ? 0.0 : promedio)
                .diasAnalizados(30)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketsPorAgenteResponse ticketsPorAgente() {
        List<AgenteResueltosItem> items = ticketRepository.contarTicketsResueltosPorAgente().stream()
                .map(row -> AgenteResueltosItem.builder()
                        .agenteId((Integer) row[0])
                        .agenteNombre(row[1] + " " + row[2])
                        .cantidadResueltos((Long) row[3])
                        .build())
                .collect(Collectors.toList());
        return TicketsPorAgenteResponse.builder().agentes(items).build();
    }

    @Override
    @Transactional(readOnly = true)
    public TicketsPorPrioridadResponse ticketsPorPrioridad() {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Object[] row : ticketRepository.contarTicketsPorPrioridad()) {
            mapa.put(String.valueOf(row[0]), (Long) row[1]);
        }
        return TicketsPorPrioridadResponse.builder().conteoPorPrioridad(mapa).build();
    }
}
