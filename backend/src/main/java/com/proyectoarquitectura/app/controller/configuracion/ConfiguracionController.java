package com.proyectoarquitectura.app.controller.configuracion;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.configuracion.ConfiguracionResponse;
import com.proyectoarquitectura.app.service.configuracion.ConfiguracionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/configuracion")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConfiguracionResponse>>> listar() {
        List<ConfiguracionResponse> data = configuracionService.listarTodas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping("/mapa")
    public ResponseEntity<ApiResponse<Map<String, String>>> obtenerMapa() {
        Map<String, String> data = configuracionService.obtenerConfiguraciones();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> guardar(@RequestBody Map<String, String> configuraciones) {
        configuracionService.guardarConfiguraciones(configuraciones);
        return ResponseEntity.ok(ok(200, "Configuración guardada", null));
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