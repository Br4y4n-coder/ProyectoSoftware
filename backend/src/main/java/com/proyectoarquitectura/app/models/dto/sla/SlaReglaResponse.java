package com.proyectoarquitectura.app.models.dto.sla;

import com.proyectoarquitectura.app.models.entity.SlaRegla;
import java.time.LocalDateTime;

public class SlaReglaResponse {
    private Integer id;
    private String nombre;
    private String prioridad;
    private Integer tiempoRespuestaHoras;
    private Integer tiempoResolucionHoras;
    private String aplicaRolNombre;
    private Integer aplicaRolId;
    private Boolean activo;
    private LocalDateTime creadoEn;

    public SlaReglaResponse() {}

    public static SlaReglaResponse from(SlaRegla sla) {
        SlaReglaResponse response = new SlaReglaResponse();
        response.setId(sla.getId());
        response.setNombre(sla.getNombre());
        response.setPrioridad(sla.getPrioridad());
        response.setTiempoRespuestaHoras(sla.getTiempoRespuestaHoras());
        response.setTiempoResolucionHoras(sla.getTiempoResolucionHoras());
        if (sla.getAplicaRol() != null) {
            response.setAplicaRolNombre(sla.getAplicaRol().getNombre());
            response.setAplicaRolId(sla.getAplicaRol().getId());
        }
        response.setActivo(sla.getActivo());
        response.setCreadoEn(sla.getCreadoEn());
        return response;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    
    public Integer getTiempoRespuestaHoras() { return tiempoRespuestaHoras; }
    public void setTiempoRespuestaHoras(Integer tiempoRespuestaHoras) { this.tiempoRespuestaHoras = tiempoRespuestaHoras; }
    
    public Integer getTiempoResolucionHoras() { return tiempoResolucionHoras; }
    public void setTiempoResolucionHoras(Integer tiempoResolucionHoras) { this.tiempoResolucionHoras = tiempoResolucionHoras; }
    
    public String getAplicaRolNombre() { return aplicaRolNombre; }
    public void setAplicaRolNombre(String aplicaRolNombre) { this.aplicaRolNombre = aplicaRolNombre; }
    
    public Integer getAplicaRolId() { return aplicaRolId; }
    public void setAplicaRolId(Integer aplicaRolId) { this.aplicaRolId = aplicaRolId; }
    
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}