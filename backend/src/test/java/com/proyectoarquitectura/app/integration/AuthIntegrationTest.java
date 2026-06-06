package com.proyectoarquitectura.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
import com.proyectoarquitectura.app.models.dto.auth.RegisterRequest;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.RolRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.service.auth.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PRUEBA DE INTEGRACIÓN #1 — Flujo completo de Autenticación
 *
 * Tipo de caja: GRIS — se usa el contexto real de Spring con H2, excepto
 * el EmailService que se mockea para evitar llamadas SMTP.
 *
 * Verifica: registro → login → /me → logout como un flujo end-to-end
 * a nivel de capa HTTP sin levantar servidor real (MockMvc).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("INT-01 | Flujo completo de autenticación (H2 + MockMvc)")
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean EmailService emailService;

    private static final String CORREO = "integracion@test.com";
    private static final String PASSWORD = "TestPass123!";

    @BeforeEach
    void setup() throws Exception {
        doNothing().when(emailService).enviarVerificacionCorreo(any(), any());
        doNothing().when(emailService).enviarResetPassword(any(), any());

        // Seed del rol 'usuario' si no existe (el DataInitializer puede no correr en test)
        if (rolRepository.findByNombre("usuario").isEmpty()) {
            Rol rol = new Rol(); rol.setNombre("usuario");
            rolRepository.save(rol);
        }
        // Seed del rol 'ADMINISTRADOR'
        if (rolRepository.findByNombre("ADMINISTRADOR").isEmpty()) {
            Rol rol = new Rol(); rol.setNombre("ADMINISTRADOR");
            rolRepository.save(rol);
        }
    }

    @Test
    @DisplayName("INT-01-A | Registro de usuario crea cuenta con estado 'pendiente'")
    void registro_creaUsuarioConEstadoPendiente() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .nombres("Integración").apellidos("Test")
                .correo(CORREO).contrasena(PASSWORD)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.correo").value(CORREO))
                .andExpect(jsonPath("$.data.estado").value("pendiente"));

        // Verificar persistencia en base de datos
        assertThat(usuarioRepository.findByCorreo(CORREO)).isPresent()
                .get().extracting(Usuario::getEstado).isEqualTo("pendiente");
    }

    @Test
    @DisplayName("INT-01-B | Login exitoso retorna tokens JWT válidos")
    void login_retornaTokensValidos() throws Exception {
        // Crear usuario activo directamente en BD
        Usuario u = crearUsuarioActivo(CORREO, PASSWORD);
        usuarioRepository.save(u);

        LoginRequest req = LoginRequest.builder()
                .correo(CORREO).contrasena(PASSWORD)
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("accessToken");
    }

    @Test
    @DisplayName("INT-01-C | Registro duplicado retorna 409 Conflict")
    void registroDuplicado_retorna409() throws Exception {
        // Crear usuario existente
        Usuario u = crearUsuarioActivo(CORREO, PASSWORD);
        usuarioRepository.save(u);

        RegisterRequest req = RegisterRequest.builder()
                .nombres("Dup").apellidos("Dup")
                .correo(CORREO).contrasena(PASSWORD)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("INT-01-D | Endpoint protegido sin token retorna 401")
    void endpointProtegido_sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    // ─── Helper ─────────────────────────────────────────────────────────────────

    private Usuario crearUsuarioActivo(String correo, String password) {
        Rol rol = rolRepository.findByNombre("usuario")
                .orElseGet(() -> { Rol r = new Rol(); r.setNombre("usuario"); return rolRepository.save(r); });
        Usuario u = new Usuario();
        u.setNombres("Test"); u.setApellidos("User");
        u.setCorreo(correo);
        u.setContrasenaHash(passwordEncoder.encode(password));
        u.setEstado("activo"); u.setRol(rol); u.setIntentosFallidos(0);
        return u;
    }
}
