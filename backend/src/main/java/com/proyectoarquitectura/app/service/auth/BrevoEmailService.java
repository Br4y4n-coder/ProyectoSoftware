package com.proyectoarquitectura.app.service.auth;

import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.Usuario;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class BrevoEmailService implements EmailService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;
    private final String senderEmail;
    private final String senderName;
    private final String frontendUrl;

    public BrevoEmailService(RestTemplate restTemplate,
                             @Value("${brevo.api-key:}") String apiKey,
                             @Value("${brevo.api-url}") String apiUrl,
                             @Value("${brevo.sender.email:}") String senderEmail,
                             @Value("${brevo.sender.name}") String senderName,
                             @Value("${app.frontend.url}") String frontendUrl) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void enviarVerificacionCorreo(Usuario u, String token) {
        String link = frontendUrl + "/auth/verify-email?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        String html = """
                <!DOCTYPE html>
                <html><body style="font-family: Arial, sans-serif; color:#111;">
                    <h2>Hola %s,</h2>
                    <p>Gracias por registrarte en SIT. Para activar tu cuenta haz clic en el siguiente enlace:</p>
                    <p><a href="%s">Verificar correo</a></p>
                    <p><code>%s</code></p>
                </body></html>
                """.formatted(escapar(u.getNombres()), link, link);
        enviarAsync(u.getCorreo(), nombreCompleto(u), "Verifica tu correo en SIT", html);
    }

    @Override
    public void enviarResetPassword(Usuario u, String token) {
        String link = frontendUrl + "/auth/reset-password?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        String html = """
                <!DOCTYPE html>
                <html><body style="font-family: Arial, sans-serif; color:#111;">
                    <h2>Hola %s,</h2>
                    <p>Recibimos una solicitud para restablecer tu contrasena en SIT.</p>
                    <p><a href="%s">Restablecer contrasena</a></p>
                    <p><code>%s</code></p>
                </body></html>
                """.formatted(escapar(u.getNombres()), link, link);
        enviarAsync(u.getCorreo(), nombreCompleto(u), "Restablece tu contrasena de SIT", html);
    }

    @Override
    @Async
    public void enviarAsync(String toEmail, String toName, String subject, String htmlContent) {
        enviar(toEmail, toName, subject, htmlContent);
    }

    @Override
    @Async
    public void notificarTicketCreado(Ticket ticket) {
        if (ticket.getCliente() == null) return;
        String html = """
                <h2>Ticket creado</h2>
                <p>Se registro el ticket <strong>%s</strong> con asunto: %s</p>
                <p>Estado: %s | Prioridad: %s</p>
                """.formatted(
                escapar(ticket.getCodigo()),
                escapar(ticket.getAsunto()),
                escapar(ticket.getEstado()),
                escapar(ticket.getPrioridad()));
        enviarAsync(ticket.getCliente().getCorreo(), nombreCompleto(ticket.getCliente()),
                "SIT - Ticket creado: " + ticket.getCodigo(), html);
    }

    @Override
    @Async
    public void notificarTicketAsignado(Ticket ticket) {
        if (ticket.getAgente() == null) return;
        String html = """
                <h2>Ticket asignado</h2>
                <p>Se te asigno el ticket <strong>%s</strong>: %s</p>
                <p>Prioridad: %s</p>
                """.formatted(
                escapar(ticket.getCodigo()),
                escapar(ticket.getAsunto()),
                escapar(ticket.getPrioridad()));
        enviarAsync(ticket.getAgente().getCorreo(), nombreCompleto(ticket.getAgente()),
                "SIT - Ticket asignado: " + ticket.getCodigo(), html);
    }

    @Override
    @Async
    public void notificarCambioEstado(Ticket ticket, String estadoAnterior) {
        String html = """
                <h2>Cambio de estado</h2>
                <p>El ticket <strong>%s</strong> cambio de <em>%s</em> a <em>%s</em>.</p>
                """.formatted(
                escapar(ticket.getCodigo()),
                escapar(estadoAnterior),
                escapar(ticket.getEstado()));

        if (ticket.getCliente() != null) {
            enviarAsync(ticket.getCliente().getCorreo(), nombreCompleto(ticket.getCliente()),
                    "SIT - Actualizacion ticket " + ticket.getCodigo(), html);
        }
        if (ticket.getAgente() != null) {
            enviarAsync(ticket.getAgente().getCorreo(), nombreCompleto(ticket.getAgente()),
                    "SIT - Actualizacion ticket " + ticket.getCodigo(), html);
        }
    }

    @Override
    public void enviar(String toEmail, String toName, String subject, String htmlContent) {
        if (apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            log.warn("[BREVO DESACTIVADO] No se envia correo a {} (subject={})", toEmail, subject);
            return;
        }

        Map<String, Object> body = Map.of(
                "sender", Map.of("name", senderName, "email", senderEmail),
                "to", List.of(Map.of("email", toEmail, "name", toName == null ? toEmail : toName)),
                "subject", subject,
                "htmlContent", htmlContent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    apiUrl, new HttpEntity<>(body, headers), Void.class);
            log.info("Correo enviado a {} (subject='{}', status={})", toEmail, subject, response.getStatusCode());
        } catch (RestClientResponseException e) {
            log.error("Brevo respondio {} al enviar a {}: {}",
                    e.getStatusCode(), toEmail, e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Error enviando correo a {}: {}", toEmail, e.getMessage(), e);
        }
    }

    private String nombreCompleto(Usuario u) {
        return u.getNombres() + " " + u.getApellidos();
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("<", "&lt;").replace(">", "&gt;");
    }
}
