package com.proyectoarquitectura.app.models.dto.configuracion;

import com.proyectoarquitectura.app.models.entity.Configuracion;
import java.time.LocalDateTime;

public class ConfiguracionResponse {
    private Integer id;
    private String clave;
    private String valor;
    private String descripcion;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public ConfiguracionResponse() {}

    public static ConfiguracionResponse from(Configuracion config) {
        ConfiguracionResponse response = new ConfiguracionResponse();
        response.setId(config.getId());
        response.setClave(config.getClave());
        response.setValor(config.getValor());
        response.setDescripcion(config.getDescripcion());
        response.setCreadoEn(config.getCreadoEn());
        response.setActualizadoEn(config.getActualizadoEn());
        return response;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}