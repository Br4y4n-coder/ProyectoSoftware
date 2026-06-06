package com.proyectoarquitectura.app.controller.auditoria;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auditoria.LogAuditoriaResponse;
import com.proyectoarquitectura.app.service.auditoria.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> listar(
            @PageableDefault(size = 20, sort = "fechaHora") Pageable pageable) {
        Page<LogAuditoriaResponse> data = auditoriaService.listarTodos(pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping("/buscar/usuario")
    public ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> buscarPorUsuario(
            @RequestParam String usuario,
            @PageableDefault(size = 20, sort = "fechaHora") Pageable pageable) {
        Page<LogAuditoriaResponse> data = auditoriaService.buscarPorUsuario(usuario, pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping("/buscar/accion")
    public ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> buscarPorAccion(
            @RequestParam String accion,
            @PageableDefault(size = 20, sort = "fechaHora") Pageable pageable) {
        Page<LogAuditoriaResponse> data = auditoriaService.buscarPorAccion(accion, pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    private <T> ApiResponse<T> ok(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}