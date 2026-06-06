package com.proyectoarquitectura.app.controller.auth;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auth.AuthResponse;
import com.proyectoarquitectura.app.models.dto.auth.ForgotPasswordRequest;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
import com.proyectoarquitectura.app.models.dto.auth.RefreshTokenRequest;
import com.proyectoarquitectura.app.models.dto.auth.RegisterRequest;
import com.proyectoarquitectura.app.models.dto.auth.ResetPasswordRequest;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@Tag(name = "Autenticación", description = "Registro, login, verificación de correo y recuperación de contraseña")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Registrar usuario", description = "Crea una nueva cuenta. Se envía un correo de verificación al email proporcionado.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UsuarioResponse>> register(@Valid @RequestBody RegisterRequest req) {
        UsuarioResponse u = authService.register(req);
        return ResponseEntity.status(201).body(ApiResponse.<UsuarioResponse>builder()
                .status(201)
                .message("Usuario registrado. Revisa tu correo para verificar la cuenta.")
                .data(u)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Verificar correo electrónico", description = "Activa la cuenta usando el token recibido por email.")
    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Object>> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Correo verificado correctamente")
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica al usuario y retorna los tokens JWT de acceso y refresco.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest req,
                                                           HttpServletRequest httpReq) {
        AuthResponse data = authService.login(req, httpReq);
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(200)
                .message("Login exitoso")
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Renovar token", description = "Genera un nuevo access token usando un refresh token válido.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest req) {
        AuthResponse data = authService.refresh(req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.<AuthResponse>builder()
                .status(200)
                .message("Token renovado")
                .data(data)
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el refresh token del usuario.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(@RequestBody(required = false) RefreshTokenRequest req) {
        authService.logout(req == null ? null : req.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Sesion cerrada")
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Solicitar restablecimiento de contraseña", description = "Envía un enlace de reset al correo registrado si existe en el sistema.")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Object>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authService.forgotPassword(req.getCorreo());
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Si el correo existe, recibirás un enlace para restablecer tu contraseña")
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Restablecer contraseña", description = "Actualiza la contraseña usando el token enviado por email.")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.resetPassword(req.getToken(), req.getNuevaContrasena());
        return ResponseEntity.ok(ApiResponse.builder()
                .status(200)
                .message("Contraseña actualizada correctamente")
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }

    @Operation(summary = "Obtener usuario autenticado", description = "Retorna los datos del usuario actual junto con sus roles.")
    @SecurityRequirement(name = "Bearer Authentication")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> me(@AuthenticationPrincipal CustomUserDetails details) {
        if (details == null) {
            return ResponseEntity.status(401).body(ApiResponse.<Map<String, Object>>builder()
                    .status(401)
                    .message("No autenticado")
                    .timestamp(Instant.now().toEpochMilli())
                    .build());
        }
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .status(200)
                .message("OK")
                .data(Map.of(
                        "usuario", UsuarioResponse.from(details.getUsuario()),
                        "authorities", details.getAuthorities()
                ))
                .timestamp(Instant.now().toEpochMilli())
                .build());
    }
}
