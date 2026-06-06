package com.proyectoarquitectura.app.models.dto.metrics;

public class TicketsPorPrioridadResponse {
    private String prioridad;
    private Long cantidad;

    public TicketsPorPrioridadResponse() {}

    public TicketsPorPrioridadResponse(String prioridad, Long cantidad) {
        this.prioridad = prioridad;
        this.cantidad = cantidad;
    }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public Long getCantidad() { return cantidad; }
    public void setCantidad(Long cantidad) { this.cantidad = cantidad; }
}