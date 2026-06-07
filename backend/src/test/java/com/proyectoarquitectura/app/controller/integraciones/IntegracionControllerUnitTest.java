package com.proyectoarquitectura.app.controller.integraciones;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.integraciones.IntegracionResponse;
import com.proyectoarquitectura.app.service.integraciones.IntegracionService;
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
 * PRUEBAS UNITARIAS — IntegracionController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("IntegracionController — Pruebas Unitarias")
class IntegracionControllerUnitTest {

    @Mock private IntegracionService integracionService;

    private IntegracionController controller;

    @BeforeEach
    void setUp() {
        controller = new IntegracionController(integracionService);
    }

    @Test
    @DisplayName("PU-INT-01 | listar() retorna 200 con las integraciones del servicio")
    void listarRetorna200() {
        List<IntegracionResponse> esperado = List.of(mock(IntegracionResponse.class));
        when(integracionService.listarTodas()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<IntegracionResponse>>> resp = controller.listar();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("OK");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(integracionService).listarTodas();
    }

    @Test
    @DisplayName("PU-INT-02 | conectar() retorna 200 con mensaje 'Integración conectada'")
    void conectarRetorna200() {
        IntegracionResponse esperado = mock(IntegracionResponse.class);
        when(integracionService.conectar(3)).thenReturn(esperado);

        ResponseEntity<ApiResponse<IntegracionResponse>> resp = controller.conectar(3);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Integración conectada");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(integracionService).conectar(3);
    }

    @Test
    @DisplayName("PU-INT-03 | desconectar() retorna 200 con mensaje 'Integración desconectada'")
    void desconectarRetorna200() {
        IntegracionResponse esperado = mock(IntegracionResponse.class);
        when(integracionService.desconectar(5)).thenReturn(esperado);

        ResponseEntity<ApiResponse<IntegracionResponse>> resp = controller.desconectar(5);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Integración desconectada");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(integracionService).desconectar(5);
    }
}
