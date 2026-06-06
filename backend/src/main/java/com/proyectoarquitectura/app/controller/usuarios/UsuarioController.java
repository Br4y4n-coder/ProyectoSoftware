package com.proyectoarquitectura.app.controller.usuarios;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.dto.usuarios.CambiarEstadoUsuarioRequest;
import com.proyectoarquitectura.app.models.dto.usuarios.CambiarRolRequest;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.usuarios.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema. Solo accesible por ADMINISTRADOR.")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar usuarios", description = "Retorna todos los usuarios registrados en el sistema con paginación.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioResponse>>> listarUsuarios(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<UsuarioResponse> data = usuarioService.listarUsuarios(pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

    @Operation(summary = "Cambiar rol de usuario", description = "Asigna un nuevo rol al usuario (ADMINISTRADOR, AGENTE, CLIENTE).")
    @PatchMapping("/{id}/rol")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarRol(@PathVariable Integer id,
                                                                   @Valid @RequestBody CambiarRolRequest req,
                                                                   @AuthenticationPrincipal CustomUserDetails me) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        UsuarioResponse data = usuarioService.cambiarRol(id, req.getRol(), me.getUsuario().getId());
        return ResponseEntity.ok(ApiResponse.<UsuarioResponse>builder()
                .status(200)
                .message("Rol actualizado")
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Cambiar estado de usuario", description = "Cambia el estado del usuario (activo, pendiente, suspendido, rechazado, eliminado).")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(@PathVariable Integer id,
                                                                      @Valid @RequestBody CambiarEstadoUsuarioRequest req,
                                                                      @AuthenticationPrincipal CustomUserDetails me) {
        if (me == null) throw AuthException.unauthorized("No autenticado");
        UsuarioResponse data = usuarioService.cambiarEstado(id, req.getEstado(), me.getUsuario().getId());
        return ResponseEntity.ok(ok(200, "Estado actualizado", data));
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