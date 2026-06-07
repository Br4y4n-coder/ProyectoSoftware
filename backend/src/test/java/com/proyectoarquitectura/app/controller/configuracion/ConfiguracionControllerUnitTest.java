package com.proyectoarquitectura.app.controller.configuracion;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.configuracion.ConfiguracionResponse;
import com.proyectoarquitectura.app.service.configuracion.ConfiguracionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — ConfiguracionController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConfiguracionController — Pruebas Unitarias")
class ConfiguracionControllerUnitTest {

    @Mock private ConfiguracionService configuracionService;

    private ConfiguracionController controller;

    @BeforeEach
    void setUp() {
        controller = new ConfiguracionController(configuracionService);
    }

    @Test
    @DisplayName("PU-CFG-01 | listar() retorna 200 con todas las configuraciones")
    void listarRetorna200() {
        List<ConfiguracionResponse> esperado = List.of(mock(ConfiguracionResponse.class));
        when(configuracionService.listarTodas()).thenReturn(esperado);

        ResponseEntity<ApiResponse<List<ConfiguracionResponse>>> resp = controller.listar();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(configuracionService).listarTodas();
    }

    @Test
    @DisplayName("PU-CFG-02 | obtenerMapa() retorna 200 con el mapa clave-valor")
    void obtenerMapaRetorna200() {
        Map<String, String> esperado = Map.of("clave1", "valor1", "clave2", "valor2");
        when(configuracionService.obtenerConfiguraciones()).thenReturn(esperado);

        ResponseEntity<ApiResponse<Map<String, String>>> resp = controller.obtenerMapa();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(configuracionService).obtenerConfiguraciones();
    }

    @Test
    @DisplayName("PU-CFG-03 | guardar() delega en el servicio y retorna 200 con data nula")
    void guardarRetorna200() {
        Map<String, String> entrada = Map.of("smtp.host", "mail.test.com");

        ResponseEntity<ApiResponse<Void>> resp = controller.guardar(entrada);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Configuración guardada");
        assertThat(resp.getBody().getData()).isNull();
        verify(configuracionService).guardarConfiguraciones(entrada);
    }
}
