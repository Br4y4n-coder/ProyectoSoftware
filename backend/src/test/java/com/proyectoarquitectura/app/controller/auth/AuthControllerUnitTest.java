package com.proyectoarquitectura.app.controller.auth;

import com.proyectoarquitectura.app.models.dto.ApiResponse;
import com.proyectoarquitectura.app.models.dto.auth.AuthResponse;
import com.proyectoarquitectura.app.models.dto.auth.ForgotPasswordRequest;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
import com.proyectoarquitectura.app.models.dto.auth.RefreshTokenRequest;
import com.proyectoarquitectura.app.models.dto.auth.RegisterRequest;
import com.proyectoarquitectura.app.models.dto.auth.ResetPasswordRequest;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.security.CustomUserDetails;
import com.proyectoarquitectura.app.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — AuthController
 * JUnit 5 + Mockito puro (sin contexto Spring), invocando los métodos
 * del controlador directamente con AuthService mockeado.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController — Pruebas Unitarias")
class AuthControllerUnitTest {

    @Mock private AuthService authService;
    @Mock private HttpServletRequest httpRequest;

    private AuthController controller;

    @BeforeEach
    void setUp() {
        controller = new AuthController(authService);
    }

    private CustomUserDetails principal() {
        Rol rol = new Rol();
        rol.setNombre("cliente");
        Usuario u = new Usuario();
        u.setId(1);
        u.setNombres("Juan");
        u.setApellidos("Pérez");
        u.setCorreo("juan@test.com");
        u.setContrasenaHash("hash");
        u.setEstado("activo");
        u.setRol(rol);
        return new CustomUserDetails(u);
    }

    @Test
    @DisplayName("PU-AUTHC-01 | register() retorna 201 con el usuario creado")
    void registerRetorna201() {
        RegisterRequest req = RegisterRequest.builder()
                .nombres("Juan").apellidos("Pérez")
                .correo("juan@test.com").contrasena("Segura123!")
                .build();
        UsuarioResponse esperado = UsuarioResponse.builder().id(1).correo("juan@test.com").build();
        when(authService.register(req)).thenReturn(esperado);

        ResponseEntity<ApiResponse<UsuarioResponse>> resp = controller.register(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(201);
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(authService).register(req);
    }

    @Test
    @DisplayName("PU-AUTHC-02 | verifyEmail() retorna 200 y delega el token al servicio")
    void verifyEmailRetorna200() {
        ResponseEntity<ApiResponse<Object>> resp = controller.verifyEmail("tok-123");

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Correo verificado correctamente");
        verify(authService).verifyEmail("tok-123");
    }

    @Test
    @DisplayName("PU-AUTHC-03 | login() retorna 200 con los tokens generados")
    void loginRetorna200() {
        LoginRequest req = LoginRequest.builder().correo("juan@test.com").contrasena("pass").build();
        AuthResponse esperado = AuthResponse.builder().accessToken("at").refreshToken("rt").build();
        when(authService.login(req, httpRequest)).thenReturn(esperado);

        ResponseEntity<ApiResponse<AuthResponse>> resp = controller.login(req, httpRequest);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Login exitoso");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(authService).login(req, httpRequest);
    }

    @Test
    @DisplayName("PU-AUTHC-04 | refresh() retorna 200 con el token renovado")
    void refreshRetorna200() {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("rt-1").build();
        AuthResponse esperado = AuthResponse.builder().accessToken("at-2").build();
        when(authService.refresh("rt-1")).thenReturn(esperado);

        ResponseEntity<ApiResponse<AuthResponse>> resp = controller.refresh(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Token renovado");
        assertThat(resp.getBody().getData()).isSameAs(esperado);
        verify(authService).refresh("rt-1");
    }

    @Test
    @DisplayName("PU-AUTHC-05 | logout() con body retorna 200 y delega el refresh token")
    void logoutConBodyRetorna200() {
        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("rt-1").build();

        ResponseEntity<ApiResponse<Object>> resp = controller.logout(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Sesion cerrada");
        verify(authService).logout("rt-1");
    }

    @Test
    @DisplayName("PU-AUTHC-06 | logout() sin body retorna 200 pasando token nulo")
    void logoutSinBodyRetorna200() {
        ResponseEntity<ApiResponse<Object>> resp = controller.logout(null);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        verify(authService).logout(isNull());
    }

    @Test
    @DisplayName("PU-AUTHC-07 | forgotPassword() retorna 200 con mensaje neutro")
    void forgotPasswordRetorna200() {
        ForgotPasswordRequest req = ForgotPasswordRequest.builder().correo("juan@test.com").build();

        ResponseEntity<ApiResponse<Object>> resp = controller.forgotPassword(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).contains("Si el correo existe");
        verify(authService).forgotPassword("juan@test.com");
    }

    @Test
    @DisplayName("PU-AUTHC-08 | resetPassword() retorna 200 y delega token y nueva contraseña")
    void resetPasswordRetorna200() {
        ResetPasswordRequest req = ResetPasswordRequest.builder()
                .token("tok-9").nuevaContrasena("Nueva123!").build();

        ResponseEntity<ApiResponse<Object>> resp = controller.resetPassword(req);

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getMessage()).isEqualTo("Contraseña actualizada correctamente");
        verify(authService).resetPassword("tok-9", "Nueva123!");
    }

    @Test
    @DisplayName("PU-AUTHC-09 | me() sin autenticación retorna 401")
    void meSinAutenticacionRetorna401() {
        ResponseEntity<ApiResponse<Map<String, Object>>> resp = controller.me(null);

        assertThat(resp.getStatusCode().value()).isEqualTo(401);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getStatus()).isEqualTo(401);
        assertThat(resp.getBody().getMessage()).isEqualTo("No autenticado");
        verifyNoInteractions(authService);
    }

    @Test
    @DisplayName("PU-AUTHC-10 | me() autenticado retorna 200 con usuario y authorities")
    void meAutenticadoRetorna200() {
        ResponseEntity<ApiResponse<Map<String, Object>>> resp = controller.me(principal());

        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody()).isNotNull();
        Map<String, Object> data = resp.getBody().getData();
        assertThat(data).containsKeys("usuario", "authorities");
        UsuarioResponse usuario = (UsuarioResponse) data.get("usuario");
        assertThat(usuario.getCorreo()).isEqualTo("juan@test.com");
        assertThat(usuario.getRol()).isEqualTo("cliente");
        assertThat(data.get("authorities").toString()).contains("ROLE_CLIENTE");
    }
}
