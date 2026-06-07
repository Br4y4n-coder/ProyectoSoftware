package com.proyectoarquitectura.app.controller.metrics;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsResueltosPorAgenteResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;
import com.proyectoarquitectura.app.service.metrics.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — MetricsController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsController — Pruebas Unitarias")
class MetricsControllerUnitTest {

    @Mock private MetricsService metricsService;

    private MetricsController controller;

    @BeforeEach
    void setUp() {
        controller = new MetricsController(metricsService);
    }

    @Test
    @DisplayName("PU-MET-01 | getTicketsPorEstado() retorna 200 con la lista del servicio")
    void ticketsPorEstadoRetorna200() {
        List<TicketsPorEstadoResponse> esperado = List.of(mock(TicketsPorEstadoResponse.class));
        when(metricsService.contarTicketsPorEstado()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<TicketsPorEstadoResponse>>> resp = controller.getTicketsPorEstado();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(metricsService).contarTicketsPorEstado();
    }

    @Test
    @DisplayName("PU-MET-02 | getTiempoPromedioResolucion() con fecha explícita usa esa fecha")
    void tiempoPromedioConFechaExplicita() {
        TiempoPromedioResolucionResponse esperado = mock(TiempoPromedioResolucionResponse.class);
        LocalDateTime fecha = LocalDateTime.of(2026, 1, 1, 0, 0);
        when(metricsService.promedioTiempoResolucionDesde(fecha)).thenReturn(esperado);

        ResponseEntity<ApiResponse<TiempoPromedioResolucionResponse>> resp =
                controller.getTiempoPromedioResolucion(fecha);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(metricsService).promedioTiempoResolucionDesde(fecha);
    }

    @Test
    @DisplayName("PU-MET-03 | getTiempoPromedioResolucion() sin fecha usa por defecto los últimos 30 días")
    void tiempoPromedioSinFechaUsaDefecto() {
        TiempoPromedioResolucionResponse esperado = mock(TiempoPromedioResolucionResponse.class);
        when(metricsService.promedioTiempoResolucionDesde(any(LocalDateTime.class))).thenReturn(esperado);

        ResponseEntity<ApiResponse<TiempoPromedioResolucionResponse>> resp =
                controller.getTiempoPromedioResolucion(null);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(metricsService).promedioTiempoResolucionDesde(captor.capture());
        // La fecha por defecto debe ser aproximadamente hace 30 días
        assertThat(captor.getValue())
                .isBefore(LocalDateTime.now().minusDays(29))
                .isAfter(LocalDateTime.now().minusDays(31));
    }

    @Test
    @DisplayName("PU-MET-04 | getTicketsPorAgente() retorna 200 con el ranking de agentes")
    void ticketsPorAgenteRetorna200() {
        List<TicketsResueltosPorAgenteResponse> esperado = List.of(mock(TicketsResueltosPorAgenteResponse.class));
        when(metricsService.contarTicketsResueltosPorAgente()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<TicketsResueltosPorAgenteResponse>>> resp = controller.getTicketsPorAgente();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(metricsService).contarTicketsResueltosPorAgente();
    }

    @Test
    @DisplayName("PU-MET-05 | getTicketsPorPrioridad() retorna 200 con el conteo por prioridad")
    void ticketsPorPrioridadRetorna200() {
        List<TicketsPorPrioridadResponse> esperado = List.of(mock(TicketsPorPrioridadResponse.class));
        when(metricsService.ticketsPorPrioridad()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<TicketsPorPrioridadResponse>>> resp = controller.getTicketsPorPrioridad();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(metricsService).ticketsPorPrioridad();
    }
}
