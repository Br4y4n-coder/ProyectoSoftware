package com.proyectoarquitectura.app.controller.categorias;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.categorias.CategoriaResponse;
import com.proyectoarquitectura.app.service.categorias.CategoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — CategoriaController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoriaController — Pruebas Unitarias")
class CategoriaControllerUnitTest {

    @Mock private CategoriaService categoriaService;

    private CategoriaController controller;

    @BeforeEach
    void setUp() {
        controller = new CategoriaController(categoriaService);
    }

    @Test
    @DisplayName("PU-CAT-01 | listar() retorna 200 con la lista del servicio")
    void listarRetorna200ConLista() {
        CategoriaResponse cat = mock(CategoriaResponse.class);
        List<CategoriaResponse> esperado = List.of(cat);
        when(categoriaService.listarTodas()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<CategoriaResponse>>> resp = controller.listar();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(200);
        assertThat(resp.getBody().getMessage()).isEqualTo("OK");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(categoriaService).listarTodas();
    }

    @Test
    @DisplayName("PU-CAT-02 | listarActivas() retorna 200 con las categorías activas")
    void listarActivasRetorna200() {
        List<CategoriaResponse> esperado = List.of(mock(CategoriaResponse.class), mock(CategoriaResponse.class));
        when(categoriaService.listarActivas()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<CategoriaResponse>>> resp = controller.listarActivas();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(categoriaService).listarActivas();
    }

    @Test
    @DisplayName("PU-CAT-03 | obtenerPorId() retorna 200 con la categoría encontrada")
    void obtenerPorIdRetorna200() {
        CategoriaResponse esperado = mock(CategoriaResponse.class);
        when(categoriaService.obtenerPorId(7)).thenReturn(esperado);

        ResponseEntity<ApiResponse<CategoriaResponse>> resp = controller.obtenerPorId(7);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        assertThat(resp.getBody().getTimestamp()).isNotNull();
        verify(categoriaService).obtenerPorId(7);
    }
}
