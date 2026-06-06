package com.proyectoarquitectura.app.models.dto.metrics;

public class TicketsPorEstadoResponse {
    private String estado;
    private Long cantidad;

    public TicketsPorEstadoResponse() {}

    public TicketsPorEstadoResponse(String estado, Long cantidad) {
        this.estado = estado;
        this.cantidad = cantidad;
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Long getCantidad() { return cantidad; }
    public void setCantidad(Long cantidad) { this.cantidad = cantidad; }
}