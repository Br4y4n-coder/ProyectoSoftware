package com.proyectoarquitectura.app.controller;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — HealthChecksController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthChecksController — Pruebas Unitarias")
class HealthChecksControllerUnitTest {

    @Mock private JdbcTemplate jdbcTemplate;

    private HealthChecksController controller;

    @BeforeEach
    void setUp() {
        controller = new HealthChecksController(jdbcTemplate);
    }

    @Test
    @DisplayName("PU-HLT-01 | Base de datos responde 1 → 200 con estado UP")
    void baseDeDatosOperativaRetornaUp() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);

        ResponseEntity<ApiResponse<Map<String, Object>>> resp = controller.healthDatabase();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(200);
        assertThat(resp.getBody().getMessage()).isEqualTo("Base de datos operativa");
        assertThat(resp.getBody().getData()).containsEntry(HealthChecksController.DATABASE, "UP");
        assertThat(resp.getBody().getData()).containsKey("responseTimeMs");
    }

    @Test
    @DisplayName("PU-HLT-02 | Base de datos responde null → 500 con estado DOWN")
    void respuestaNulaRetorna500Down() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(null);

        ResponseEntity<ApiResponse<Map<String, Object>>> resp = controller.healthDatabase();

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Respuesta inválida de la base de datos");
        assertThat(resp.getBody().getData()).containsEntry(HealthChecksController.DATABASE, "DOWN");
    }

    @Test
    @DisplayName("PU-HLT-03 | Base de datos responde un valor distinto de 1 → 500 con estado DOWN")
    void respuestaInvalidaRetorna500Down() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(0);

        ResponseEntity<ApiResponse<Map<String, Object>>> resp = controller.healthDatabase();

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).containsEntry(HealthChecksController.DATABASE, "DOWN");
    }

    @Test
    @DisplayName("PU-HLT-04 | Excepción de conexión → 500 con mensaje de error de conexión")
    void excepcionDeConexionRetorna500() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new RuntimeException("Connection refused"));

        ResponseEntity<ApiResponse<Map<String, Object>>> resp = controller.healthDatabase();

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Error de conexión con la base de datos");
        assertThat(resp.getBody().getData()).containsEntry(HealthChecksController.DATABASE, "DOWN");
    }

    @Test
    @DisplayName("PU-HLT-05 | Respuesta lenta (> 2000 ms) → 200 con estado DEGRADED")
    void respuestaLentaRetornaDegraded() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenAnswer(inv -> {
            Thread.sleep(2100);
            return 1;
        });

        ResponseEntity<ApiResponse<Map<String, Object>>> resp = controller.healthDatabase();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).containsEntry(HealthChecksController.DATABASE, "DEGRADED");
    }
}
