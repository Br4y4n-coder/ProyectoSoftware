package com.proyectoarquitectura.app.service.configuracion;

import com.proyectoarquitectura.app.models.dto.configuracion.ConfiguracionResponse;
import com.proyectoarquitectura.app.models.entity.Configuracion;
import com.proyectoarquitectura.app.repository.ConfiguracionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — ConfiguracionService
 *
 * JUnit 5 + Mockito puro (sin contexto Spring).
 * Se mockea ConfiguracionRepository, única dependencia del constructor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracionService — Pruebas Unitarias")
class ConfiguracionServiceTest {

    @Mock
    private ConfiguracionRepository configuracionRepository;

    private ConfiguracionService configuracionService;

    @BeforeEach
    void setUp() {
        configuracionService = new ConfiguracionService(configuracionRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Configuracion config(Integer id, String clave, String valor) {
        return Configuracion.builder()
                .id(id)
                .clave(clave)
                .valor(valor)
                .descripcion("desc " + clave)
                .creadoEn(LocalDateTime.of(2026, 1, 1, 0, 0))
                .actualizadoEn(LocalDateTime.of(2026, 1, 2, 0, 0))
                .build();
    }

    // ─── obtenerConfiguraciones() ───────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerConfiguraciones()")
    class ObtenerConfiguracionesTests {

        @Test
        @DisplayName("PU-CFG-01 | Retorna un mapa clave→valor con todas las configuraciones")
        void retornaMapaClaveValor() {
            when(configuracionRepository.findAll()).thenReturn(List.of(
                    config(1, "smtp.host", "mail.test.com"),
                    config(2, "app.nombre", "HelpDesk")));

            Map<String, String> resultado = configuracionService.obtenerConfiguraciones();

            assertThat(resultado)
                    .hasSize(2)
                    .containsEntry("smtp.host", "mail.test.com")
                    .containsEntry("app.nombre", "HelpDesk");
            verify(configuracionRepository).findAll();
        }

        @Test
        @DisplayName("PU-CFG-02 | Sin configuraciones retorna mapa vacío")
        void sinConfiguracionesRetornaMapaVacio() {
            when(configuracionRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(configuracionService.obtenerConfiguraciones()).isEmpty();
        }
    }

    // ─── listarTodas() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarTodas()")
    class ListarTodasTests {

        @Test
        @DisplayName("PU-CFG-03 | Retorna las configuraciones mapeadas a ConfiguracionResponse")
        void retornaConfiguracionesMapeadas() {
            when(configuracionRepository.findAll())
                    .thenReturn(List.of(config(1, "smtp.host", "mail.test.com")));

            List<ConfiguracionResponse> resultado = configuracionService.listarTodas();

            assertThat(resultado).hasSize(1);
            ConfiguracionResponse r = resultado.get(0);
            assertThat(r.getId()).isEqualTo(1);
            assertThat(r.getClave()).isEqualTo("smtp.host");
            assertThat(r.getValor()).isEqualTo("mail.test.com");
            assertThat(r.getDescripcion()).isEqualTo("desc smtp.host");
            assertThat(r.getCreadoEn()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
            assertThat(r.getActualizadoEn()).isEqualTo(LocalDateTime.of(2026, 1, 2, 0, 0));
        }

        @Test
        @DisplayName("PU-CFG-04 | Sin configuraciones retorna lista vacía")
        void sinConfiguracionesRetornaListaVacia() {
            when(configuracionRepository.findAll()).thenReturn(Collections.emptyList());

            assertThat(configuracionService.listarTodas()).isEmpty();
        }
    }

    // ─── guardarConfiguraciones() ───────────────────────────────────────────────

    @Nested
    @DisplayName("guardarConfiguraciones()")
    class GuardarConfiguracionesTests {

        @Test
        @DisplayName("PU-CFG-05 | Clave existente actualiza el valor de la entidad encontrada")
        void claveExistenteActualizaValor() {
            Configuracion existente = config(1, "smtp.host", "viejo.test.com");
            when(configuracionRepository.findByClave("smtp.host"))
                    .thenReturn(Optional.of(existente));
            when(configuracionRepository.save(any(Configuracion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            configuracionService.guardarConfiguraciones(Map.of("smtp.host", "nuevo.test.com"));

            assertThat(existente.getValor()).isEqualTo("nuevo.test.com");
            assertThat(existente.getClave()).isEqualTo("smtp.host");
            verify(configuracionRepository).save(existente);
        }

        @Test
        @DisplayName("PU-CFG-06 | Clave nueva crea una Configuracion nueva y la guarda")
        void claveNuevaCreaConfiguracion() {
            when(configuracionRepository.findByClave("tema.color"))
                    .thenReturn(Optional.empty());
            when(configuracionRepository.save(any(Configuracion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            configuracionService.guardarConfiguraciones(Map.of("tema.color", "azul"));

            ArgumentCaptor<Configuracion> captor = ArgumentCaptor.forClass(Configuracion.class);
            verify(configuracionRepository).save(captor.capture());
            Configuracion guardada = captor.getValue();
            assertThat(guardada.getId()).isNull(); // entidad nueva, sin id
            assertThat(guardada.getClave()).isEqualTo("tema.color");
            assertThat(guardada.getValor()).isEqualTo("azul");
        }

        @Test
        @DisplayName("PU-CFG-07 | Varias entradas generan un save por cada clave")
        void variasEntradasGeneranUnSavePorClave() {
            Map<String, String> entradas = new LinkedHashMap<>();
            entradas.put("a", "1");
            entradas.put("b", "2");
            when(configuracionRepository.findByClave("a"))
                    .thenReturn(Optional.of(config(1, "a", "viejo")));
            when(configuracionRepository.findByClave("b"))
                    .thenReturn(Optional.empty());
            when(configuracionRepository.save(any(Configuracion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            configuracionService.guardarConfiguraciones(entradas);

            verify(configuracionRepository, times(2)).save(any(Configuracion.class));
            verify(configuracionRepository).findByClave("a");
            verify(configuracionRepository).findByClave("b");
        }

        @Test
        @DisplayName("PU-CFG-08 | Mapa vacío no interactúa con el repositorio")
        void mapaVacioNoGuardaNada() {
            configuracionService.guardarConfiguraciones(Collections.emptyMap());

            verifyNoInteractions(configuracionRepository);
        }
    }
}
