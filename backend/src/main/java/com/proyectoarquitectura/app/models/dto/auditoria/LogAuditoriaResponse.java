package com.proyectoarquitectura.app.models.dto.auditoria;

import com.proyectoarquitectura.app.models.entity.LogAuditoria;
import java.time.LocalDateTime;

public class LogAuditoriaResponse {
    private Long id;
    private String usuario;
    private String accion;
    private String detalles;
    private String ip;
    private LocalDateTime fechaHora;

    public LogAuditoriaResponse() {}

    public static LogAuditoriaResponse from(LogAuditoria log) {
        LogAuditoriaResponse response = new LogAuditoriaResponse();
        response.setId(log.getId());
        response.setUsuario(log.getUsuario());
        response.setAccion(log.getAccion());
        response.setDetalles(log.getDetalles());
        response.setIp(log.getIp());
        response.setFechaHora(log.getFechaHora());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getAccion() { return accion; }
    public void setAccion(String accion) { this.accion = accion; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}