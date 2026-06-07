package com.proyectoarquitectura.app.controller.exportacion;

import com.proyectoarquitectura.app.service.exportacion.ExportacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — ExportacionController
 * JUnit 5 + Mockito puro (sin contexto Spring).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ExportacionController — Pruebas Unitarias")
class ExportacionControllerUnitTest {

    @Mock private ExportacionService exportacionService;

    private ExportacionController controller;

    @BeforeEach
    void setUp() {
        controller = new ExportacionController(exportacionService);
    }

    @Test
    @DisplayName("PU-EXP-01 | exportarTicketsCSV() retorna 200 con bytes, header y content-type CSV")
    void exportarTicketsCsv() {
        byte[] esperado = "ID,Codigo".getBytes(StandardCharsets.UTF_8);
        when(exportacionService.exportarTicketsCSV()).thenReturn(esperado);

        ResponseEntity<byte[]> resp = controller.exportarTicketsCSV();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(esperado);
        assertThat(resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=tickets_").contains(".csv");
        assertThat(resp.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/csv"));
        verify(exportacionService).exportarTicketsCSV();
    }

    @Test
    @DisplayName("PU-EXP-02 | exportarUsuariosCSV() retorna 200 con bytes y nombre de archivo de usuarios")
    void exportarUsuariosCsv() {
        byte[] esperado = "ID,Nombres".getBytes(StandardCharsets.UTF_8);
        when(exportacionService.exportarUsuariosCSV()).thenReturn(esperado);

        ResponseEntity<byte[]> resp = controller.exportarUsuariosCSV();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(esperado);
        assertThat(resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=usuarios_").contains(".csv");
        verify(exportacionService).exportarUsuariosCSV();
    }

    @Test
    @DisplayName("PU-EXP-03 | exportarAuditoriaCSV() retorna 200 con bytes y nombre de archivo de auditoría")
    void exportarAuditoriaCsv() {
        byte[] esperado = "ID,Usuario".getBytes(StandardCharsets.UTF_8);
        when(exportacionService.exportarAuditoriaCSV()).thenReturn(esperado);

        ResponseEntity<byte[]> resp = controller.exportarAuditoriaCSV();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isSameAs(esperado);
        assertThat(resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=auditoria_").contains(".csv");
        verify(exportacionService).exportarAuditoriaCSV();
    }

    @Test
    @DisplayName("PU-EXP-04 | exportarTicketsJSON() retorna 200 con JSON y content-type application/json")
    void exportarTicketsJson() {
        when(exportacionService.exportarTicketsJSON()).thenReturn("[{\"id\":1}]");

        ResponseEntity<String> resp = controller.exportarTicketsJSON();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("[{\"id\":1}]");
        assertThat(resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=tickets_").contains(".json");
        assertThat(resp.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        verify(exportacionService).exportarTicketsJSON();
    }

    @Test
    @DisplayName("PU-EXP-05 | exportarUsuariosJSON() retorna 200 con JSON de usuarios")
    void exportarUsuariosJson() {
        when(exportacionService.exportarUsuariosJSON()).thenReturn("[]");

        ResponseEntity<String> resp = controller.exportarUsuariosJSON();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("[]");
        assertThat(resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=usuarios_").contains(".json");
        verify(exportacionService).exportarUsuariosJSON();
    }

    @Test
    @DisplayName("PU-EXP-06 | exportarAuditoriaJSON() retorna 200 con JSON de auditoría")
    void exportarAuditoriaJson() {
        when(exportacionService.exportarAuditoriaJSON()).thenReturn("[{\"id\":9}]");

        ResponseEntity<String> resp = controller.exportarAuditoriaJSON();

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isEqualTo("[{\"id\":9}]");
        assertThat(resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("attachment; filename=auditoria_").contains(".json");
        verify(exportacionService).exportarAuditoriaJSON();
    }
}
