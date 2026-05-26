package com.proyectoarquitectura.app.controller.usuarios;

import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.dto.usuarios.CambiarRolRequest;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.usuarios.UsuarioService;
import jakarta.validation.Valid;
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
}
