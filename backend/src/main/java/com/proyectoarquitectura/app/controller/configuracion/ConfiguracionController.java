package com.proyectoarquitectura.app.controller.configuracion;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.configuracion.ConfiguracionResponse;
import com.proyectoarquitectura.app.service.configuracion.ConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Tag(name = "Configuración", description = "Gestión de parámetros de configuración del sistema. Solo accesible por ADMINISTRADOR.")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/configuracion")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    public ConfiguracionController(ConfiguracionService configuracionService) {
        this.configuracionService = configuracionService;
    }

    @Operation(summary = "Listar configuraciones", description = "Retorna todas las entradas de configuración del sistema.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ConfiguracionResponse>>> listar() {
        List<ConfiguracionResponse> data = configuracionService.listarTodas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Obtener configuraciones como mapa", description = "Retorna todas las configuraciones en formato clave-valor.")
    @GetMapping("/mapa")
    public ResponseEntity<ApiResponse<Map<String, String>>> obtenerMapa() {
        Map<String, String> data = configuracionService.obtenerConfiguraciones();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Guardar configuraciones", description = "Persiste un mapa de clave-valor con los parámetros de configuración del sistema.")
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