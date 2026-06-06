package com.proyectoarquitectura.app.controller.integraciones;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.integraciones.IntegracionResponse;
import com.proyectoarquitectura.app.service.integraciones.IntegracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/integraciones")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class IntegracionController {

    private final IntegracionService integracionService;

    public IntegracionController(IntegracionService integracionService) {
        this.integracionService = integracionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<IntegracionResponse>>> listar() {
        List<IntegracionResponse> data = integracionService.listarTodas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @PostMapping("/{id}/conectar")
    public ResponseEntity<ApiResponse<IntegracionResponse>> conectar(@PathVariable Integer id) {
        IntegracionResponse data = integracionService.conectar(id);
        return ResponseEntity.ok(ok(200, "Integración conectada", data));
    }

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