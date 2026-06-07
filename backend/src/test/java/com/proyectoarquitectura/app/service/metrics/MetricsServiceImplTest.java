package com.proyectoarquitectura.app.service.metrics;

import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorEstadoResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TicketsPorPrioridadResponse;
import com.proyectoarquitectura.app.models.dto.metrics.TiempoPromedioResolucionResponse;
import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — MetricsServiceImpl
 *
 * JUnit 5 + Mockito puro (sin contexto Spring).
 * Se mockea TicketRepository, única dependencia del constructor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsServiceImpl — Pruebas Unitarias")
class MetricsServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    private MetricsServiceImpl metricsService;

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 1, 1, 0, 0);

    @BeforeEach
    void setUp() {
        metricsService = new MetricsServiceImpl(ticketRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Ticket ticket(String estado, String prioridad) {
        return Ticket.builder()
                .id(1)
                .codigo("TKT-1")
                .asunto("Asunto")
                .descripcion("Descripción")
                .tipo("incidente")
                .estado(estado)
                .prioridad(prioridad)
                .fechaCreacion(BASE)
                .build();
    }

    private Ticket ticketCerrado(LocalDateTime creacion, LocalDateTime cierre) {
        Ticket t = ticket("cerrado", "media");
        t.setFechaCreacion(creacion);
        t.setFechaCierre(cierre);
        return t;
    }

    // ─── contarTicketsPorEstado() ───────────────────────────────────────────────

    @Nested
    @DisplayName("contarTicketsPorEstado()")
    class ContarPorEstadoTests {

        @Test
        @DisplayName("PU-MET-01 | Agrupa y cuenta tickets por estado")
        void agrupaYCuentaPorEstado() {
            when(ticketRepository.findAll()).thenReturn(List.of(
                    ticket("abierto", "alta"),
                    ticket("abierto", "media"),
                    ticket("cerrado", "baja")));

            List<TicketsPorEstadoResponse> resultado = metricsService.contarTicketsPorEstado();

            assertThat(resultado).hasSize(2);
            assertThat(resultado)
                    .anySatisfy(r -> {
                        if ("abierto".equals(r.getEstado())) {
                            assertThat(r.getCantidad()).isEqualTo(2L);
                        }
                    })
                    .extracting(TicketsPorEstadoResponse::getEstado)
                    .containsExactlyInAnyOrder("abierto", "cerrado");
            // Verificación explícita de cada conteo
            long abiertos = resultado.stream()
                    .filter(r -> "abierto".equals(r.getEstado()))
                    .mapToLong(TicketsPorEstadoResponse::getCantidad).sum();
            long cerrados = resultado.stream()
                    .filter(r -> "cerrado".equals(r.getEstado()))
                    .mapToLong(TicketsPorEstadoResponse::getCantidad).sum();
            assertThat(abiertos).isEqualTo(2L);
            assertThat(cerrados).isEqualTo(1L);
            verify(ticketRepository).findAll();
        }

        @Test
        @DisplayName("PU-MET-02 | Sin tickets retorna lista vacía")
        void sinTicketsRetornaListaVacia() {
            when(ticketRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(metricsService.contarTicketsPorEstado()).isEmpty();
        }
    }

    // ─── promedioTiempoResolucionDesde() ────────────────────────────────────────

    @Nested
    @DisplayName("promedioTiempoResolucionDesde()")
    class PromedioResolucionTests {

        @Test
        @DisplayName("PU-MET-03 | Calcula el promedio en horas solo con tickets cerrados")
        void calculaPromedioConTicketsCerrados() {
            // Cerrado 1: 10 horas | Cerrado 2: 20 horas | Abierto: se ignora en el promedio
            Ticket cerrado1 = ticketCerrado(BASE, BASE.plusHours(10));
            Ticket cerrado2 = ticketCerrado(BASE, BASE.plusHours(20));
            Ticket abierto = ticket("abierto", "alta"); // sin fechaCierre
            when(ticketRepository.findAll()).thenReturn(List.of(cerrado1, cerrado2, abierto));

            TiempoPromedioResolucionResponse resultado =
                    metricsService.promedioTiempoResolucionDesde(BASE.minusDays(30));

            assertThat(resultado.getPromedioHoras()).isEqualTo(15.0);
            // totalTickets cuenta TODOS los tickets, no solo los cerrados
            assertThat(resultado.getTotalTickets()).isEqualTo(3L);
        }

        @Test
        @DisplayName("PU-MET-04 | Sin tickets cerrados el promedio es 0.0")
        void sinCerradosPromedioCero() {
            when(ticketRepository.findAll())
                    .thenReturn(List.of(ticket("abierto", "alta")));

            TiempoPromedioResolucionResponse resultado =
                    metricsService.promedioTiempoResolucionDesde(BASE);

            assertThat(resultado.getPromedioHoras()).isEqualTo(0.0);
            assertThat(resultado.getTotalTickets()).isEqualTo(1L);
        }

        @Test
        @DisplayName("PU-MET-05 | Sin tickets retorna promedio 0.0 y total 0")
        void sinTicketsRetornaCeros() {
            when(ticketRepository.findAll()).thenReturn(Collections.emptyList());

            TiempoPromedioResolucionResponse resultado =
                    metricsService.promedioTiempoResolucionDesde(BASE);

            assertThat(resultado.getPromedioHoras()).isEqualTo(0.0);
            assertThat(resultado.getTotalTickets()).isEqualTo(0L);
        }
    }

    // ─── contarTicketsResueltosPorAgente() ──────────────────────────────────────

    @Nested
    @DisplayName("contarTicketsResueltosPorAgente()")
    class ResueltosPorAgenteTests {

        @Test
        @DisplayName("PU-MET-06 | Implementación actual retorna lista vacía sin tocar el repositorio")
        void retornaListaVaciaSinTocarRepositorio() {
            assertThat(metricsService.contarTicketsResueltosPorAgente()).isEmpty();
            verifyNoInteractions(ticketRepository);
        }
    }

    // ─── ticketsPorPrioridad() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("ticketsPorPrioridad()")
    class PorPrioridadTests {

        @Test
        @DisplayName("PU-MET-07 | Agrupa y cuenta tickets por prioridad")
        void agrupaYCuentaPorPrioridad() {
            when(ticketRepository.findAll()).thenReturn(List.of(
                    ticket("abierto", "alta"),
                    ticket("cerrado", "alta"),
                    ticket("abierto", "baja")));

            List<TicketsPorPrioridadResponse> resultado = metricsService.ticketsPorPrioridad();

            assertThat(resultado).hasSize(2);
            assertThat(resultado)
                    .extracting(TicketsPorPrioridadResponse::getPrioridad)
                    .containsExactlyInAnyOrder("alta", "baja");
            long altas = resultado.stream()
                    .filter(r -> "alta".equals(r.getPrioridad()))
                    .mapToLong(TicketsPorPrioridadResponse::getCantidad).sum();
            long bajas = resultado.stream()
                    .filter(r -> "baja".equals(r.getPrioridad()))
                    .mapToLong(TicketsPorPrioridadResponse::getCantidad).sum();
            assertThat(altas).isEqualTo(2L);
            assertThat(bajas).isEqualTo(1L);
            verify(ticketRepository).findAll();
        }

        @Test
        @DisplayName("PU-MET-08 | Sin tickets retorna lista vacía")
        void sinTicketsRetornaListaVacia() {
            when(ticketRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(metricsService.ticketsPorPrioridad()).isEmpty();
        }
    }

    // ─── obtenerMetricasAvanzadas() ─────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerMetricasAvanzadas()")
    class MetricasAvanzadasTests {

        @Test
        @DisplayName("PU-MET-09 | Implementación actual retorna lista vacía sin tocar el repositorio")
        void retornaListaVaciaSinTocarRepositorio() {
            assertThat(metricsService.obtenerMetricasAvanzadas(BASE, BASE.plusDays(1))).isEmpty();
            verifyNoInteractions(ticketRepository);
        }
    }
}
