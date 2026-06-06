package com.proyectoarquitectura.app.controller.exportacion;

import com.proyectoarquitectura.app.service.exportacion.ExportacionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/exportar")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ExportacionController {

    private final ExportacionService exportacionService;

    public ExportacionController(ExportacionService exportacionService) {
        this.exportacionService = exportacionService;
    }

    private String obtenerTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    // Exportar Tickets CSV
    @GetMapping("/tickets/csv")
    public ResponseEntity<byte[]> exportarTicketsCSV() {
        byte[] data = exportacionService.exportarTicketsCSV();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets_" + obtenerTimestamp() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    // Exportar Usuarios CSV
    @GetMapping("/usuarios/csv")
    public ResponseEntity<byte[]> exportarUsuariosCSV() {
        byte[] data = exportacionService.exportarUsuariosCSV();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios_" + obtenerTimestamp() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    // Exportar Auditoría CSV
    @GetMapping("/auditoria/csv")
    public ResponseEntity<byte[]> exportarAuditoriaCSV() {
        byte[] data = exportacionService.exportarAuditoriaCSV();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria_" + obtenerTimestamp() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    // Exportar Tickets JSON
    @GetMapping("/tickets/json")
    public ResponseEntity<String> exportarTicketsJSON() {
        String data = exportacionService.exportarTicketsJSON();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets_" + obtenerTimestamp() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    // Exportar Usuarios JSON
    @GetMapping("/usuarios/json")
    public ResponseEntity<String> exportarUsuariosJSON() {
        String data = exportacionService.exportarUsuariosJSON();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios_" + obtenerTimestamp() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    // Exportar Auditoría JSON
    @GetMapping("/auditoria/json")
    public ResponseEntity<String> exportarAuditoriaJSON() {
        String data = exportacionService.exportarAuditoriaJSON();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria_" + obtenerTimestamp() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }
}