package com.proyectoarquitectura.app.controller.auditoria;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auditoria.LogAuditoriaResponse;
import com.proyectoarquitectura.app.service.auditoria.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — AuditoriaController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuditoriaController — Pruebas Unitarias")
class AuditoriaControllerUnitTest {

    @Mock private AuditoriaService auditoriaService;

    private AuditoriaController controller;
    private final Pageable pageable = PageRequest.of(0, 10);

    @BeforeEach
    void setUp() {
        controller = new AuditoriaController(auditoriaService);
    }

    @Test
    @DisplayName("PU-AUD-01 | listar() retorna 200 con la página de logs")
    void listarRetorna200() {
        Page<LogAuditoriaResponse> esperado = new PageImpl<>(List.of(mock(LogAuditoriaResponse.class)));
        when(auditoriaService.listarTodos(pageable)).thenReturn(esperado);

        ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> resp = controller.listar(pageable);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(auditoriaService).listarTodos(pageable);
    }

    @Test
    @DisplayName("PU-AUD-02 | buscarPorUsuario() retorna 200 filtrando por usuario")
    void buscarPorUsuarioRetorna200() {
        Page<LogAuditoriaResponse> esperado = new PageImpl<>(List.of(mock(LogAuditoriaResponse.class)));
        when(auditoriaService.buscarPorUsuario("juan", pageable)).thenReturn(esperado);

        ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> resp =
                controller.buscarPorUsuario("juan", pageable);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(auditoriaService).buscarPorUsuario("juan", pageable);
    }

    @Test
    @DisplayName("PU-AUD-03 | buscarPorAccion() retorna 200 filtrando por acción")
    void buscarPorAccionRetorna200() {
        Page<LogAuditoriaResponse> esperado = new PageImpl<>(List.of(mock(LogAuditoriaResponse.class)));
        when(auditoriaService.buscarPorAccion("LOGIN", pageable)).thenReturn(esperado);

        ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> resp =
                controller.buscarPorAccion("LOGIN", pageable);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(auditoriaService).buscarPorAccion("LOGIN", pageable);
    }
}
