package com.proyectoarquitectura.app.service.categorias;

import com.proyectoarquitectura.app.models.dto.categorias.CategoriaResponse;
import com.proyectoarquitectura.app.models.entity.Area;
import com.proyectoarquitectura.app.models.entity.Categoria;
import com.proyectoarquitectura.app.repository.CategoriaRepository;
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
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — CategoriaService
 *
 * JUnit 5 + Mockito puro (sin contexto Spring).
 * Se mockea CategoriaRepository, única dependencia del constructor.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaService — Pruebas Unitarias")
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    private CategoriaService categoriaService;

    @BeforeEach
    void setUp() {
        categoriaService = new CategoriaService(categoriaRepository);
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Categoria categoriaConArea() {
        Area area = Area.builder().id(5).nombre("Soporte").build();
        return Categoria.builder()
                .id(1)
                .nombre("Hardware")
                .descripcion("Problemas de hardware")
                .colorHex("#FF0000")
                .icono("cpu")
                .area(area)
                .activo(true)
                .creadoEn(LocalDateTime.of(2026, 1, 15, 10, 0))
                .build();
    }

    private Categoria categoriaSinArea() {
        return Categoria.builder()
                .id(2)
                .nombre("Software")
                .descripcion("Problemas de software")
                .colorHex("#00FF00")
                .icono("code")
                .area(null)
                .activo(false)
                .creadoEn(LocalDateTime.of(2026, 2, 1, 8, 30))
                .build();
    }

    // ─── listarTodas() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarTodas()")
    class ListarTodasTests {

        @Test
        @DisplayName("PU-CAT-01 | Retorna todas las categorías mapeadas a CategoriaResponse")
        void retornaTodasLasCategorias() {
            when(categoriaRepository.findAll())
                    .thenReturn(List.of(categoriaConArea(), categoriaSinArea()));

            List<CategoriaResponse> resultado = categoriaService.listarTodas();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getId()).isEqualTo(1);
            assertThat(resultado.get(0).getNombre()).isEqualTo("Hardware");
            // La categoría con área debe traer los datos del área
            assertThat(resultado.get(0).getAreaId()).isEqualTo(5);
            assertThat(resultado.get(0).getAreaNombre()).isEqualTo("Soporte");
            // La categoría sin área deja los campos de área en null
            assertThat(resultado.get(1).getAreaId()).isNull();
            assertThat(resultado.get(1).getAreaNombre()).isNull();
            verify(categoriaRepository).findAll();
        }

        @Test
        @DisplayName("PU-CAT-02 | Sin categorías retorna lista vacía")
        void sinCategoriasRetornaListaVacia() {
            when(categoriaRepository.findAll()).thenReturn(Collections.emptyList());

            List<CategoriaResponse> resultado = categoriaService.listarTodas();

            assertThat(resultado).isEmpty();
        }
    }

    // ─── listarActivas() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("listarActivas()")
    class ListarActivasTests {

        @Test
        @DisplayName("PU-CAT-03 | Retorna solo las categorías activas mapeadas")
        void retornaSoloActivas() {
            Categoria activa = categoriaConArea(); // activo = true
            when(categoriaRepository.findByActivoTrue()).thenReturn(List.of(activa));

            List<CategoriaResponse> resultado = categoriaService.listarActivas();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getActivo()).isTrue();
            assertThat(resultado.get(0).getNombre()).isEqualTo("Hardware");
            verify(categoriaRepository).findByActivoTrue();
            verify(categoriaRepository, never()).findAll();
        }

        @Test
        @DisplayName("PU-CAT-04 | Sin categorías activas retorna lista vacía")
        void sinActivasRetornaListaVacia() {
            when(categoriaRepository.findByActivoTrue()).thenReturn(Collections.emptyList());

            assertThat(categoriaService.listarActivas()).isEmpty();
        }
    }

    // ─── obtenerPorId() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("obtenerPorId()")
    class ObtenerPorIdTests {

        @Test
        @DisplayName("PU-CAT-05 | Id existente retorna la categoría mapeada")
        void idExistenteRetornaCategoria() {
            Categoria cat = categoriaConArea();
            when(categoriaRepository.findById(1)).thenReturn(Optional.of(cat));

            CategoriaResponse resultado = categoriaService.obtenerPorId(1);

            assertThat(resultado.getId()).isEqualTo(1);
            assertThat(resultado.getNombre()).isEqualTo("Hardware");
            assertThat(resultado.getDescripcion()).isEqualTo("Problemas de hardware");
            assertThat(resultado.getColorHex()).isEqualTo("#FF0000");
            assertThat(resultado.getIcono()).isEqualTo("cpu");
            assertThat(resultado.getAreaId()).isEqualTo(5);
            assertThat(resultado.getCreadoEn()).isEqualTo(LocalDateTime.of(2026, 1, 15, 10, 0));
        }

        @Test
        @DisplayName("PU-CAT-06 | Id inexistente lanza RuntimeException con el id en el mensaje")
        void idInexistenteLanzaExcepcion() {
            when(categoriaRepository.findById(99)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoriaService.obtenerPorId(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Categoría no encontrada")
                    .hasMessageContaining("99");
        }
    }
}
