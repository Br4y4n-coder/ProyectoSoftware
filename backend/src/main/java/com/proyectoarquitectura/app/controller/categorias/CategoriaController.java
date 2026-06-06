package com.proyectoarquitectura.app.controller.categorias;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.categorias.CategoriaResponse;
import com.proyectoarquitectura.app.service.categorias.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Tag(name = "Categorías", description = "Gestión de categorías para clasificar los tickets de soporte.")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(summary = "Listar todas las categorías", description = "Retorna todas las categorías incluyendo las inactivas.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listar() {
        List<CategoriaResponse> data = categoriaService.listarTodas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Listar categorías activas", description = "Retorna solo las categorías habilitadas para seleccionar al crear un ticket.")
    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarActivas() {
        List<CategoriaResponse> data = categoriaService.listarActivas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Obtener categoría por ID", description = "Retorna el detalle de una categoría específica.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoriaResponse>> obtenerPorId(@PathVariable Integer id) {
        CategoriaResponse data = categoriaService.obtenerPorId(id);
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