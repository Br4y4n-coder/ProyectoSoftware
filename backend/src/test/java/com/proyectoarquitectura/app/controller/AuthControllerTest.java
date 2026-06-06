package com.proyectoarquitectura.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoarquitectura.app.exception.AuthException;
import com.proyectoarquitectura.app.models.dto.auth.AuthResponse;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
import com.proyectoarquitectura.app.models.dto.auth.RegisterRequest;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.security.JwtService;
import com.proyectoarquitectura.app.service.auth.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PRUEBAS DE CONTROLADOR — AuthController
 *
 * Tipo de caja: GRIS — se conoce la estructura del controlador y se prueba
 * la capa HTTP (códigos de estado, cuerpos de respuesta) mockeando el servicio.
 *
 * Herramienta: @SpringBootTest + MockMvc + @MockBean (AuthService).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController — Pruebas de Caja Gris (MockMvc)")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;

    @MockBean AuthService authService;
    @MockBean JwtService jwtService;

    // ─── POST /api/auth/register ─────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("CG-AUTH-01 | Payload válido → 201 Created con datos del usuario")
        void registroValido_retorna201() throws Exception {
            RegisterRequest req = RegisterRequest.builder()
                    .nombres("María").apellidos("García")
                    .correo("maria@test.com").contrasena("Segura123!")
                    .build();
            UsuarioResponse resp = UsuarioResponse.builder()
                    .id(1).nombres("María").apellidos("García")
                    .correo("maria@test.com").estado("pendiente").rol("usuario")
                    .build();
            when(authService.register(any())).thenReturn(resp);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.correo").value("maria@test.com"))
                    .andExpect(jsonPath("$.data.estado").value("pendiente"));
        }

        @Test
        @DisplayName("CG-AUTH-02 | Correo vacío → 400 Bad Request (validación Bean)")
        void correoVacio_retorna400() throws Exception {
            RegisterRequest req = RegisterRequest.builder()
                    .nombres("X").apellidos("Y").correo("").contrasena("pass1234")
                    .build();

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("CG-AUTH-03 | Correo duplicado → 409 Conflict")
        void correoDuplicado_retorna409() throws Exception {
            RegisterRequest req = RegisterRequest.builder()
                    .nombres("X").apellidos("Y").correo("dup@test.com").contrasena("pass1234")
                    .build();
            when(authService.register(any())).thenThrow(AuthException.conflict("El correo ya esta registrado"));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }
    }

    // ─── POST /api/auth/login ────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("CG-AUTH-04 | Credenciales válidas → 200 OK con tokens JWT")
        void loginValido_retorna200ConTokens() throws Exception {
            LoginRequest req = LoginRequest.builder()
                    .correo("juan@test.com").contrasena("pass1234")
                    .build();
            AuthResponse authResp = new AuthResponse();
            authResp.setAccessToken("access-tok");
            authResp.setRefreshToken("refresh-tok");
            authResp.setTokenType("Bearer");
            authResp.setExpiresInMs(86400000L);
            authResp.setUsuario(UsuarioResponse.builder()
                    .id(1).correo("juan@test.com").rol("usuario").estado("activo").build());

            when(authService.login(any(), any())).thenReturn(authResp);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.accessToken").value("access-tok"))
                    .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("CG-AUTH-05 | Credenciales inválidas → 401 Unauthorized")
        void loginInvalido_retorna401() throws Exception {
            LoginRequest req = LoginRequest.builder()
                    .correo("bad@test.com").contrasena("wrong")
                    .build();
            when(authService.login(any(), any()))
                    .thenThrow(AuthException.unauthorized("Credenciales invalidas"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("CG-AUTH-06 | Correo con formato inválido → 400 Bad Request")
        void correoInvalidoFormato_retorna400() throws Exception {
            LoginRequest req = LoginRequest.builder()
                    .correo("no-es-un-email").contrasena("pass1234")
                    .build();

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ─── POST /api/auth/logout ───────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/logout")
    class Logout {

        @Test
        @DisplayName("CG-AUTH-07 | Logout con body → 200 OK")
        void logoutConBody_retorna200() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"any-token\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Sesion cerrada"));
        }

        @Test
        @DisplayName("CG-AUTH-08 | Logout sin body → 200 OK (body es opcional)")
        void logoutSinBody_retorna200() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ─── POST /api/auth/forgot-password ─────────────────────────────────────

    @Nested
    @DisplayName("POST /api/auth/forgot-password")
    class ForgotPassword {

        @Test
        @DisplayName("CG-AUTH-09 | Solicitud de reset → 200 OK (respuesta genérica)")
        void forgotPassword_retorna200() throws Exception {
            mockMvc.perform(post("/api/auth/forgot-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"correo\":\"user@test.com\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }
}
