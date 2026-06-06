package com.proyectoarquitectura.app.controller.exportacion;

import com.proyectoarquitectura.app.service.exportacion.ExportacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Tag(name = "Exportación", description = "Exportación de datos del sistema en formato CSV y JSON. Solo accesible por ADMINISTRADOR.")
@SecurityRequirement(name = "Bearer Authentication")
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

    @Operation(summary = "Exportar tickets en CSV", description = "Descarga todos los tickets del sistema en formato CSV.")
    @GetMapping("/tickets/csv")
    public ResponseEntity<byte[]> exportarTicketsCSV() {
        byte[] data = exportacionService.exportarTicketsCSV();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets_" + obtenerTimestamp() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @Operation(summary = "Exportar usuarios en CSV", description = "Descarga todos los usuarios del sistema en formato CSV.")
    @GetMapping("/usuarios/csv")
    public ResponseEntity<byte[]> exportarUsuariosCSV() {
        byte[] data = exportacionService.exportarUsuariosCSV();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios_" + obtenerTimestamp() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @Operation(summary = "Exportar auditoría en CSV", description = "Descarga los logs de auditoría en formato CSV.")
    @GetMapping("/auditoria/csv")
    public ResponseEntity<byte[]> exportarAuditoriaCSV() {
        byte[] data = exportacionService.exportarAuditoriaCSV();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria_" + obtenerTimestamp() + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @Operation(summary = "Exportar tickets en JSON", description = "Descarga todos los tickets del sistema en formato JSON.")
    @GetMapping("/tickets/json")
    public ResponseEntity<String> exportarTicketsJSON() {
        String data = exportacionService.exportarTicketsJSON();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tickets_" + obtenerTimestamp() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @Operation(summary = "Exportar usuarios en JSON", description = "Descarga todos los usuarios del sistema en formato JSON.")
    @GetMapping("/usuarios/json")
    public ResponseEntity<String> exportarUsuariosJSON() {
        String data = exportacionService.exportarUsuariosJSON();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=usuarios_" + obtenerTimestamp() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @Operation(summary = "Exportar auditoría en JSON", description = "Descarga los logs de auditoría en formato JSON.")
    @GetMapping("/auditoria/json")
    public ResponseEntity<String> exportarAuditoriaJSON() {
        String data = exportacionService.exportarAuditoriaJSON();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria_" + obtenerTimestamp() + ".json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }
}