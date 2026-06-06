package com.proyectoarquitectura.app.models.dto.metrics; 
 
public class TicketsResueltosPorAgenteResponse { 
    private String agenteNombre; 
    private Long cantidad; 
 
    public TicketsResueltosPorAgenteResponse() {} 
 
    public TicketsResueltosPorAgenteResponse(String agenteNombre, Long cantidad) { 
        this.agenteNombre = agenteNombre; 
        this.cantidad = cantidad; 
    } 
 
    public String getAgenteNombre() { return agenteNombre; } 
    public void setAgenteNombre(String agenteNombre) { this.agenteNombre = agenteNombre; } 
    public Long getCantidad() { return cantidad; } 
    public void setCantidad(Long cantidad) { this.cantidad = cantidad; } 
} 
