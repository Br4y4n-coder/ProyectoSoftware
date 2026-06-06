package com.proyectoarquitectura.app.controller.categorias;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.categorias.CategoriaResponse;
import com.proyectoarquitectura.app.service.categorias.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listar() {
        List<CategoriaResponse> data = categoriaService.listarTodas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @GetMapping("/activas")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarActivas() {
        List<CategoriaResponse> data = categoriaService.listarActivas();
        return ResponseEntity.ok(ok(200, "OK", data));
    }

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