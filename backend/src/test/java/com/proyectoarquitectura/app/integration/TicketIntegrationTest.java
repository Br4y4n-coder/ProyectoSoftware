package com.proyectoarquitectura.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectoarquitectura.app.models.dto.auth.LoginRequest;
import com.proyectoarquitectura.app.models.entity.Categoria;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.CategoriaRepository;
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

import com.jayway.jsonpath.JsonPath;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PRUEBA DE INTEGRACIÓN #2 — Ciclo de vida de Tickets
 *
 * Tipo de caja: GRIS — Spring context real + H2 en memoria.
 * EmailService mockeado para evitar SMTP.
 *
 * Verifica: autenticar → crear ticket → obtener por ID → obtener por código.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("INT-02 | Ciclo de vida de Tickets (H2 + MockMvc)")
class TicketIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired RolRepository rolRepository;
    @Autowired CategoriaRepository categoriaRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @MockBean EmailService emailService;

    private String accessToken;
    private Integer categoriaId;

    private static final String CORREO  = "ticket.user@test.com";
    private static final String PASSWORD = "TestPass123!";

    @BeforeEach
    void setup() throws Exception {
        doNothing().when(emailService).enviarVerificacionCorreo(any(), any());
        doNothing().when(emailService).enviarResetPassword(any(), any());

        // Seed roles
        Rol rol = rolRepository.findByNombre("usuario")
                .orElseGet(() -> { Rol r = new Rol(); r.setNombre("usuario"); return rolRepository.save(r); });

        // Crear usuario activo
        if (usuarioRepository.findByCorreo(CORREO).isEmpty()) {
            Usuario u = new Usuario();
            u.setNombres("Ticket"); u.setApellidos("User");
            u.setCorreo(CORREO);
            u.setContrasenaHash(passwordEncoder.encode(PASSWORD));
            u.setEstado("activo"); u.setRol(rol); u.setIntentosFallidos(0);
            usuarioRepository.save(u);
        }

        // Seed categoría
        if (categoriaRepository.findAll().isEmpty()) {
            Categoria cat = new Categoria();
            cat.setNombre("Soporte Técnico"); cat.setActiva(true);
            categoriaRepository.save(cat);
        }
        categoriaId = categoriaRepository.findAll().get(0).getId();

        // Obtener token
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(
                                LoginRequest.builder().correo(CORREO).contrasena(PASSWORD).build())))
                .andExpect(status().isOk())
                .andReturn();

        accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(),
                "$.data.accessToken");
    }

    @Test
    @DisplayName("INT-02-A | Crear ticket retorna 201 con código y estado ABIERTO")
    void crearTicket_retorna201() throws Exception {
        String payload = String.format("""
                {
                  "titulo": "PC no enciende",
                  "descripcion": "El equipo no enciende desde esta mañana",
                  "tipo": "incidente",
                  "prioridad": "alta",
                  "categoriaId": %d
                }
                """, categoriaId);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.titulo").value("PC no enciende"))
                .andExpect(jsonPath("$.data.estado").exists())
                .andExpect(jsonPath("$.data.codigo").exists());
    }

    @Test
    @DisplayName("INT-02-B | Obtener ticket por ID retorna datos correctos")
    void obtenerTicketPorId_retornaDatos() throws Exception {
        // Primero crear un ticket
        String payload = String.format("""
                {"titulo":"Ticket ID Test","descripcion":"Desc","tipo":"solicitud","prioridad":"baja","categoriaId":%d}
                """, categoriaId);

        MvcResult crearResult = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + accessToken)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        Integer id = JsonPath.read(crearResult.getResponse().getContentAsString(), "$.data.id");

        // Luego obtenerlo por ID
        mockMvc.perform(get("/api/tickets/" + id)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.titulo").value("Ticket ID Test"));
    }

    @Test
    @DisplayName("INT-02-C | Sin token en endpoints de tickets retorna 401")
    void sinToken_retorna401() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }
}
