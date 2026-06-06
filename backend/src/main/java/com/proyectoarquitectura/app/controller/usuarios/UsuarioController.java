package com.proyectoarquitectura.app.controller.usuarios;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.dto.usuarios.CambiarRolRequest;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.usuarios.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioResponse>>> listarUsuarios(
            @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        Page<UsuarioResponse> data = usuarioService.listarUsuarios(pageable);
        return ResponseEntity.ok(ok(200, "OK", data));
    }

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

    private <T> ApiResponse<T> ok(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build();
    }
}