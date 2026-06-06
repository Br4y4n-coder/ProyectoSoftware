package com.proyectoarquitectura.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PRUEBA DE INTEGRACIÓN #3 — Gestión de Usuarios (rol ADMINISTRADOR)
 *
 * Tipo de caja: GRIS — Spring context real + H2.
 * Verifica control de acceso por roles y CRUD de usuarios.
 *
 * Escenarios:
 *  A. Admin lista usuarios → 200 OK con paginación
 *  B. Admin cambia rol de usuario → 200 OK
 *  C. Usuario normal accede a /api/usuarios → 403 Forbidden
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("INT-03 | Gestión de Usuarios — Control de Acceso por Rol (H2 + MockMvc)")
class UsuarioIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockitoBean EmailService emailService;

    private String adminToken;
    private String userToken;
    private Integer usuarioNormalId;

    private static final String ADMIN_CORREO  = "admin.int@test.com";
    private static final String USER_CORREO   = "user.int@test.com";
    private static final String PASSWORD      = "TestPass123!";

    @BeforeEach
    void setup() throws Exception {
        doNothing().when(emailService).enviarVerificacionCorreo(any(), any());

        // Seed roles
        Rol rolAdmin = rolRepository.findByNombre("ADMINISTRADOR")
                .orElseGet(() -> { Rol r = new Rol(); r.setNombre("ADMINISTRADOR"); return rolRepository.save(r); });
        Rol rolUser = rolRepository.findByNombre("usuario")
                .orElseGet(() -> { Rol r = new Rol(); r.setNombre("usuario"); return rolRepository.save(r); });
        Rol rolAgente = rolRepository.findByNombre("agente")
                .orElseGet(() -> { Rol r = new Rol(); r.setNombre("agente"); return rolRepository.save(r); });

        // Crear admin
        Usuario admin = new Usuario();
        admin.setNombres("Admin"); admin.setApellidos("Test");
        admin.setCorreo(ADMIN_CORREO);
        admin.setContrasenaHash(passwordEncoder.encode(PASSWORD));
        admin.setEstado("activo"); admin.setRol(rolAdmin); admin.setIntentosFallidos(0);
        usuarioRepository.save(admin);

        // Crear usuario normal
        Usuario user = new Usuario();
        user.setNombres("Normal"); user.setApellidos("User");
        user.setCorreo(USER_CORREO);
        user.setContrasenaHash(passwordEncoder.encode(PASSWORD));
        user.setEstado("activo"); user.setRol(rolUser); user.setIntentosFallidos(0);
        Usuario savedUser = usuarioRepository.save(user);
        usuarioNormalId = savedUser.getId();

        // Obtener token admin
        adminToken = obtenerToken(ADMIN_CORREO, PASSWORD);
        // Obtener token usuario normal
        userToken = obtenerToken(USER_CORREO, PASSWORD);
    }

    @Test
    @DisplayName("INT-03-A | Admin puede listar todos los usuarios con paginación")
    void admin_listaUsuarios_retorna200() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").isNumber());
    }

    @Test
    @DisplayName("INT-03-B | Admin cambia rol de usuario → 200 OK con nuevo rol")
    void admin_cambiaRol_retorna200() throws Exception {
        String payload = "{\"rol\":\"agente\"}";

        mockMvc.perform(patch("/api/usuarios/" + usuarioNormalId + "/rol")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminToken)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rol").value("agente"));
    }

    @Test
    @DisplayName("INT-03-C | Usuario sin rol ADMINISTRADOR accede a /api/usuarios → 403 Forbidden")
    void usuarioNormal_listaUsuarios_retorna403() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    // ─── Helper ─────────────────────────────────────────────────────────────────

    private String obtenerToken(String correo, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                LoginRequest.builder().correo(correo).contrasena(password).build())))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }
}
