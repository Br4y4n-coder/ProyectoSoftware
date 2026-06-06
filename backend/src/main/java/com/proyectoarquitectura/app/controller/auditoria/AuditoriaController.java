package com.proyectoarquitectura.app.controller.auditoria;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auditoria.LogAuditoriaResponse;
import com.proyectoarquitectura.app.service.auditoria.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Auditoría", description = "Consulta de logs de auditoría del sistema. Solo accesible por ADMINISTRADOR.")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @Operation(summary = "Listar logs de auditoría", description = "Retorna todos los registros de auditoría paginados.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> listar(
            @PageableDefault(size = 20, sort = "fechaHora") Pageable pageable) {
        Page<LogAuditoriaResponse> data = auditoriaService.listarTodos(pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Buscar logs por usuario", description = "Filtra los logs de auditoría por nombre o email del usuario.")
    @GetMapping("/buscar/usuario")
    public ResponseEntity<ApiResponse<Page<LogAuditoriaResponse>>> buscarPorUsuario(
            @RequestParam String usuario,
            @PageableDefault(size = 20, sort = "fechaHora") Pageable pageable) {
        Page<LogAuditoriaResponse> data = auditoriaService.buscarPorUsuario(usuario, pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Buscar logs por acción", description = "Filtra los logs de auditoría por tipo de acción realizada.")
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