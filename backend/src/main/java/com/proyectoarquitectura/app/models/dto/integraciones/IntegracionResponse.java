package com.proyectoarquitectura.app.models.dto.integraciones;

import com.proyectoarquitectura.app.models.entity.Integracion;
import java.time.LocalDateTime;

public class IntegracionResponse {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String tipo;
    private String configuracion;
    private Boolean conectado;
    private Boolean activo;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    public IntegracionResponse() {}

    public static IntegracionResponse from(Integracion integ) {
        IntegracionResponse response = new IntegracionResponse();
        response.setId(integ.getId());
        response.setNombre(integ.getNombre());
        response.setDescripcion(integ.getDescripcion());
        response.setTipo(integ.getTipo());
        response.setConfiguracion(integ.getConfiguracion());
        response.setConectado(integ.getConectado());
        response.setActivo(integ.getActivo());
        response.setCreadoEn(integ.getCreadoEn());
        response.setActualizadoEn(integ.getActualizadoEn());
        return response;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getConfiguracion() { return configuracion; }
    public void setConfiguracion(String configuracion) { this.configuracion = configuracion; }
    public Boolean getConectado() { return conectado; }
    public void setConectado(Boolean conectado) { this.conectado = conectado; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(LocalDateTime actualizadoEn) { this.actualizadoEn = actualizadoEn; }
}