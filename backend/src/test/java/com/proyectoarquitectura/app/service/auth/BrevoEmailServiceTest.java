package com.proyectoarquitectura.app.service.auth;

import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS — BrevoEmailService
 *
 * Tipo de caja: BLANCA — se conoce la implementación interna y se prueban
 * todas las ramas de lógica condicional del servicio.
 *
 * Herramienta: JUnit 5 + Mockito (sin contexto Spring).
 * Nota: al no haber proxy de Spring, los metodos @Async se ejecutan en forma
 * sincrona, lo que hace las pruebas deterministas.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("BrevoEmailService — Pruebas Unitarias (Caja Blanca)")
class BrevoEmailServiceTest {

    private static final String API_KEY      = "api-key-123";
    private static final String API_URL      = "https://api.brevo.test/v3/smtp/email";
    private static final String SENDER_EMAIL = "noreply@sit.test";
    private static final String SENDER_NAME  = "SIT";
    private static final String FRONTEND_URL = "https://front.sit.test";

    @Mock private RestTemplate restTemplate;

    @Captor private ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor;

    private BrevoEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new BrevoEmailService(
                restTemplate, API_KEY, API_URL, SENDER_EMAIL, SENDER_NAME, FRONTEND_URL);
        when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    private Usuario usuario(Integer id, String nombres, String apellidos, String correo) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNombres(nombres);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        return u;
    }

    private Ticket ticketBase() {
        return Ticket.builder()
                .id(1)
                .codigo("TKT-1")
                .asunto("Falla de red")
                .descripcion("Sin conexion")
                .tipo("problema")
                .prioridad("alta")
                .estado("abierto")
                .build();
    }

    private Map<String, Object> cuerpoCapturado() {
        verify(restTemplate, atLeastOnce())
                .postForEntity(eq(API_URL), entityCaptor.capture(), eq(Void.class));
        return entityCaptor.getValue().getBody();
    }

    // ─── enviar() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("enviar()")
    class EnviarTests {

        @Test
        @DisplayName("PU-MAIL-01 | Con apiKey vacia no se llama al RestTemplate")
        void apiKeyVaciaNoEnvia() {
            BrevoEmailService sinApiKey = new BrevoEmailService(
                    restTemplate, "", API_URL, SENDER_EMAIL, SENDER_NAME, FRONTEND_URL);

            sinApiKey.enviar("a@test.com", "Ana", "Asunto", "<p>hola</p>");

            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("PU-MAIL-02 | Con senderEmail vacio tampoco se envia")
        void senderEmailVacioNoEnvia() {
            BrevoEmailService sinSender = new BrevoEmailService(
                    restTemplate, API_KEY, API_URL, "", SENDER_NAME, FRONTEND_URL);

            sinSender.enviar("a@test.com", "Ana", "Asunto", "<p>hola</p>");

            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("PU-MAIL-03 | Con datos completos hace postForEntity con header api-key y cuerpo correcto")
        void datosCompletosEnvia() {
            emailService.enviar("ana@test.com", "Ana Gil", "Hola Ana", "<p>contenido</p>");

            verify(restTemplate).postForEntity(eq(API_URL), entityCaptor.capture(), eq(Void.class));
            HttpEntity<Map<String, Object>> entidad = entityCaptor.getValue();

            assertThat(entidad.getHeaders().getFirst("api-key")).isEqualTo(API_KEY);
            Map<String, Object> body = entidad.getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("subject")).isEqualTo("Hola Ana");
            assertThat(body.get("htmlContent")).isEqualTo("<p>contenido</p>");

            @SuppressWarnings("unchecked")
            Map<String, Object> sender = (Map<String, Object>) body.get("sender");
            assertThat(sender.get("email")).isEqualTo(SENDER_EMAIL);
            assertThat(sender.get("name")).isEqualTo(SENDER_NAME);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> to = (List<Map<String, Object>>) body.get("to");
            assertThat(to.get(0).get("email")).isEqualTo("ana@test.com");
            assertThat(to.get(0).get("name")).isEqualTo("Ana Gil");
        }

        @Test
        @DisplayName("PU-MAIL-04 | Si toName es null se usa el correo como nombre del destinatario")
        void toNameNullUsaCorreo() {
            emailService.enviar("solo@test.com", null, "Asunto", "<p>x</p>");

            Map<String, Object> body = cuerpoCapturado();
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> to = (List<Map<String, Object>>) body.get("to");
            assertThat(to.get(0).get("name")).isEqualTo("solo@test.com");
        }

        @Test
        @DisplayName("PU-MAIL-05 | RestClientResponseException del RestTemplate no se propaga")
        void restClientExceptionNoPropaga() {
            when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), eq(Void.class)))
                    .thenThrow(new RestClientResponseException(
                            "Bad Request", 400, "Bad Request", null, null, null));

            assertThatCode(() -> emailService.enviar("x@test.com", "X", "S", "<p>h</p>"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PU-MAIL-06 | Una excepcion generica del RestTemplate tampoco se propaga")
        void excepcionGenericaNoPropaga() {
            when(restTemplate.postForEntity(eq(API_URL), any(HttpEntity.class), eq(Void.class)))
                    .thenThrow(new RuntimeException("timeout"));

            assertThatCode(() -> emailService.enviar("x@test.com", "X", "S", "<p>h</p>"))
                    .doesNotThrowAnyException();
        }
    }

    // ─── notificarTicketCreado() ────────────────────────────────────────────────

    @Nested
    @DisplayName("notificarTicketCreado()")
    class NotificarTicketCreadoTests {

        @Test
        @DisplayName("PU-MAIL-07 | Ticket sin cliente no genera ningun envio")
        void sinClienteNoEnvia() {
            Ticket ticket = ticketBase(); // cliente null

            assertThatCode(() -> emailService.notificarTicketCreado(ticket))
                    .doesNotThrowAnyException();
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("PU-MAIL-08 | Ticket con cliente envia correo con el codigo en el asunto")
        void conClienteEnvia() {
            Ticket ticket = ticketBase();
            ticket.setCliente(usuario(1, "Laura", "Gomez", "laura@test.com"));

            emailService.notificarTicketCreado(ticket);

            Map<String, Object> body = cuerpoCapturado();
            assertThat((String) body.get("subject")).contains("Ticket creado").contains("TKT-1");
            assertThat((String) body.get("htmlContent")).contains("TKT-1").contains("Falla de red");
        }
    }

    // ─── notificarTicketAsignado() ──────────────────────────────────────────────

    @Nested
    @DisplayName("notificarTicketAsignado()")
    class NotificarTicketAsignadoTests {

        @Test
        @DisplayName("PU-MAIL-09 | Ticket sin agente no genera ningun envio")
        void sinAgenteNoEnvia() {
            Ticket ticket = ticketBase();
            ticket.setCliente(usuario(1, "Laura", "Gomez", "laura@test.com"));

            assertThatCode(() -> emailService.notificarTicketAsignado(ticket))
                    .doesNotThrowAnyException();
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("PU-MAIL-10 | Con agente y sin cliente envia solo un correo (al agente)")
        void conAgenteSinClienteEnviaUno() {
            Ticket ticket = ticketBase();
            ticket.setAgente(usuario(2, "Pedro", "Ruiz", "pedro@test.com"));

            emailService.notificarTicketAsignado(ticket);

            verify(restTemplate, times(1))
                    .postForEntity(eq(API_URL), any(HttpEntity.class), eq(Void.class));
        }

        @Test
        @DisplayName("PU-MAIL-11 | Con agente y cliente envia dos correos")
        void conAgenteYClienteEnviaDos() {
            Ticket ticket = ticketBase();
            ticket.setAgente(usuario(2, "Pedro", "Ruiz", "pedro@test.com"));
            ticket.setCliente(usuario(1, "Laura", "Gomez", "laura@test.com"));

            emailService.notificarTicketAsignado(ticket);

            verify(restTemplate, times(2))
                    .postForEntity(eq(API_URL), any(HttpEntity.class), eq(Void.class));
        }
    }

    // ─── notificarCambioEstado() ────────────────────────────────────────────────

    @Nested
    @DisplayName("notificarCambioEstado()")
    class NotificarCambioEstadoTests {

        @Test
        @DisplayName("PU-MAIL-12 | Sin cliente ni agente no genera envios")
        void sinDestinatariosNoEnvia() {
            Ticket ticket = ticketBase();

            assertThatCode(() -> emailService.notificarCambioEstado(ticket, "abierto"))
                    .doesNotThrowAnyException();
            verifyNoInteractions(restTemplate);
        }

        @Test
        @DisplayName("PU-MAIL-13 | Con cliente y agente envia dos correos con el cambio de estado")
        void conClienteYAgenteEnviaDos() {
            Ticket ticket = ticketBase();
            ticket.setEstado("cerrado");
            ticket.setCliente(usuario(1, "Laura", "Gomez", "laura@test.com"));
            ticket.setAgente(usuario(2, "Pedro", "Ruiz", "pedro@test.com"));

            emailService.notificarCambioEstado(ticket, "abierto");

            verify(restTemplate, times(2))
                    .postForEntity(eq(API_URL), entityCaptor.capture(), eq(Void.class));
            Map<String, Object> body = entityCaptor.getValue().getBody();
            assertThat(body).isNotNull();
            assertThat((String) body.get("htmlContent"))
                    .contains("abierto").contains("cerrado").contains("TKT-1");
        }
    }

    // ─── enviarVerificacionCorreo() / enviarResetPassword() ────────────────────

    @Nested
    @DisplayName("enviarVerificacionCorreo() y enviarResetPassword()")
    class CorreosDeCuentaTests {

        @Test
        @DisplayName("PU-MAIL-14 | Verificacion de correo arma el link con el token y envia")
        void verificacionCorreoArmaLink() {
            Usuario u = usuario(1, "Carlos", "Lopez", "carlos@test.com");

            emailService.enviarVerificacionCorreo(u, "tok-verify-123");

            Map<String, Object> body = cuerpoCapturado();
            assertThat(body.get("subject")).isEqualTo("Verifica tu correo en SIT");
            assertThat((String) body.get("htmlContent"))
                    .contains(FRONTEND_URL + "/auth/verify-email?token=tok-verify-123")
                    .contains("Carlos");
        }

        @Test
        @DisplayName("PU-MAIL-15 | Reset de contrasena arma el link con el token y envia")
        void resetPasswordArmaLink() {
            Usuario u = usuario(1, "Carlos", "Lopez", "carlos@test.com");

            emailService.enviarResetPassword(u, "tok-reset-456");

            Map<String, Object> body = cuerpoCapturado();
            assertThat(body.get("subject")).isEqualTo("Restablece tu contrasena de SIT");
            assertThat((String) body.get("htmlContent"))
                    .contains(FRONTEND_URL + "/auth/reset-password?token=tok-reset-456")
                    .contains("Carlos");
        }
    }
}
