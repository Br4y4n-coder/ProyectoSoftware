package com.proyectoarquitectura.app.service.auth;

import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.Usuario;

public interface EmailService {

    void enviarVerificacionCorreo(Usuario usuario, String token);

    void enviarResetPassword(Usuario usuario, String token);

    void enviar(String toEmail, String toName, String subject, String htmlContent);

    void enviarAsync(String toEmail, String toName, String subject, String htmlContent);

    void notificarTicketCreado(Ticket ticket);

    void notificarTicketAsignado(Ticket ticket);

    void notificarCambioEstado(Ticket ticket, String estadoAnterior);
}
