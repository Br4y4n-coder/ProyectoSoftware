package com.proyectoarquitectura.app.service.integraciones;

import com.proyectoarquitectura.app.models.dto.integraciones.IntegracionResponse;
import com.proyectoarquitectura.app.models.entity.Integracion;
import com.proyectoarquitectura.app.repository.IntegracionRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — IntegracionService
 *
 * JUnit 5 + Mockito puro (sin contexto Spring).
 * Se mockea IntegracionRepository, única dependencia del constructor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IntegracionService — Pruebas Unitarias")
class IntegracionServiceTest {

    @Mock
    private IntegracionRepository integracionRepository;

    private IntegracionService integracionService;

    @BeforeEach
    void setUp() {
        integracionService = new IntegracionService(integracionRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Integracion integracion(Integer id, boolean conectado) {
        return Integracion.builder()
                .id(id)
                .nombre("Slack")
                .descripcion("Notificaciones a Slack")
                .tipo("mensajeria")
                .configuracion("{\"webhook\":\"https://example\"}")
                .conectado(conectado)
                .activo(true)
                .creadoEn(LocalDateTime.of(2000, 1, 1, 0, 0))
                // Fecha antigua: garantiza que now() siempre será posterior
                .actualizadoEn(LocalDateTime.of(2000, 1, 2, 0, 0))
                .build();
    }

    // ─── listarTodas() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarTodas()")
    class ListarTodasTests {

        @Test
        @DisplayName("PU-INT-01 | Retorna todas las integraciones mapeadas a IntegracionResponse")
        void retornaTodasLasIntegraciones() {
            when(integracionRepository.findAll())
                    .thenReturn(List.of(integracion(1, true), integracion(2, false)));

            List<IntegracionResponse> resultado = integracionService.listarTodas();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getId()).isEqualTo(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Slack");
            assertThat(resultado.get(0).getTipo()).isEqualTo("mensajeria");
            assertThat(resultado.get(0).getConectado()).isTrue();
            assertThat(resultado.get(1).getConectado()).isFalse();
            verify(integracionRepository).findAll();
        }

        @Test
        @DisplayName("PU-INT-02 | Sin integraciones retorna lista vacía")
        void sinIntegracionesRetornaListaVacia() {
            when(integracionRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(integracionService.listarTodas()).isEmpty();
        }
    }

    // ─── conectar() ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("conectar()")
    class ConectarTests {

        @Test
        @DisplayName("PU-INT-03 | Conectar integración existente marca conectado=true y actualiza fecha")
        void conectarIntegracionExistente() {
            Integracion integ = integracion(1, false);
            LocalDateTime fechaAnterior = integ.getActualizadoEn();
            when(integracionRepository.findById(1)).thenReturn(Optional.of(integ));
            when(integracionRepository.save(any(Integracion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            IntegracionResponse resultado = integracionService.conectar(1);

            assertThat(resultado.getConectado()).isTrue();
            // La fecha de actualización fue refrescada por el servicio
            assertThat(integ.getActualizadoEn()).isNotNull().isAfter(fechaAnterior);
            verify(integracionRepository).save(integ);
        }

        @Test
        @DisplayName("PU-INT-04 | Conectar integración inexistente lanza RuntimeException")
        void conectarIntegracionInexistente() {
            when(integracionRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> integracionService.conectar(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Integración no encontrada");
            verify(integracionRepository, never()).save(any());
        }
    }

    // ─── desconectar() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("desconectar()")
    class DesconectarTests {

        @Test
        @DisplayName("PU-INT-05 | Desconectar integración existente marca conectado=false y actualiza fecha")
        void desconectarIntegracionExistente() {
            Integracion integ = integracion(2, true);
            LocalDateTime fechaAnterior = integ.getActualizadoEn();
            when(integracionRepository.findById(2)).thenReturn(Optional.of(integ));
            when(integracionRepository.save(any(Integracion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            IntegracionResponse resultado = integracionService.desconectar(2);

            assertThat(resultado.getConectado()).isFalse();
            assertThat(integ.getActualizadoEn()).isNotNull().isAfter(fechaAnterior);
            verify(integracionRepository).save(integ);
        }

        @Test
        @DisplayName("PU-INT-06 | Desconectar integración inexistente lanza RuntimeException")
        void desconectarIntegracionInexistente() {
            when(integracionRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> integracionService.desconectar(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Integración no encontrada");
            verify(integracionRepository, never()).save(any());
        }
    }
}
