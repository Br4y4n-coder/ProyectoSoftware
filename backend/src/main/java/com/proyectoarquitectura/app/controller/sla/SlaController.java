package com.proyectoarquitectura.app.controller.sla;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.sla.SlaReglaResponse;
import com.proyectoarquitectura.app.service.sla.SlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Tag(name = "SLA", description = "Gestión de reglas de nivel de servicio (SLA). Solo accesible por ADMINISTRADOR.")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/sla")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class SlaController {

    private final SlaService slaService;

    public SlaController(SlaService slaService) {
        this.slaService = slaService;
    }

    @Operation(summary = "Listar reglas SLA", description = "Retorna todas las reglas de nivel de servicio configuradas.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SlaReglaResponse>>> listar() {
        List<SlaReglaResponse> data = slaService.listarTodas();
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