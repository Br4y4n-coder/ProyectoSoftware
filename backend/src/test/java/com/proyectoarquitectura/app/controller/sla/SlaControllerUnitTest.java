package com.proyectoarquitectura.app.controller.sla;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.sla.SlaReglaResponse;
import com.proyectoarquitectura.app.service.sla.SlaService;
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
 * PRUEBAS UNITARIAS — SlaController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SlaController — Pruebas Unitarias")
class SlaControllerUnitTest {

    @Mock private SlaService slaService;

    private SlaController controller;

    @BeforeEach
    void setUp() {
        controller = new SlaController(slaService);
    }

    @Test
    @DisplayName("PU-SLA-01 | listar() retorna 200 con las reglas SLA del servicio")
    void listarRetorna200ConReglas() {
        List<SlaReglaResponse> esperado = List.of(mock(SlaReglaResponse.class));
        when(slaService.listarTodas()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<SlaReglaResponse>>> resp = controller.listar();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(200);
        assertThat(resp.getBody().getMessage()).isEqualTo("OK");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(slaService).listarTodas();
    }

    @Test
    @DisplayName("PU-SLA-02 | listar() retorna 200 con lista vacía cuando no hay reglas")
    void listarRetorna200ConListaVacia() {
        when(slaService.listarTodas()).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<SlaReglaResponse>>> resp = controller.listar();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isEmpty();
    }
}
