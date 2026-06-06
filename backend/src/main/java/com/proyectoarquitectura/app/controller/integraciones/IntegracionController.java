package com.proyectoarquitectura.app.controller.integraciones;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.integraciones.IntegracionResponse;
import com.proyectoarquitectura.app.service.integraciones.IntegracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Tag(name = "Integraciones", description = "Gestión de integraciones con servicios de terceros. Solo accesible por ADMINISTRADOR.")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/integraciones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class IntegracionController {

    private final IntegracionService integracionService;

    public IntegracionController(IntegracionService integracionService) {
        this.integracionService = integracionService;
    }

    @Operation(summary = "Listar integraciones", description = "Retorna todas las integraciones configuradas con su estado de conexión.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<IntegracionResponse>>> listar() {
        List<IntegracionResponse> data = integracionService.listarTodas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Conectar integración", description = "Activa la conexión de una integración por su ID.")
    @PostMapping("/{id}/conectar")
    public ResponseEntity<ApiResponse<IntegracionResponse>> conectar(@PathVariable Integer id) {
        IntegracionResponse data = integracionService.conectar(id);
        return ResponseEntity.ok(ok(200, "Integración conectada", data));
    }

    @Operation(summary = "Desconectar integración", description = "Desactiva la conexión de una integración por su ID.")
    @PostMapping("/{id}/desconectar")
    public ResponseEntity<ApiResponse<IntegracionResponse>> desconectar(@PathVariable Integer id) {
        IntegracionResponse data = integracionService.desconectar(id);
        return ResponseEntity.ok(ok(200, "Integración desconectada", data));
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