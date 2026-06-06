package com.proyectoarquitectura.app.models.dto.metrics;

public class TicketMetricDTO {
    private String nombre;
    private Long valor;

    public TicketMetricDTO() {}

    public TicketMetricDTO(String nombre, Long valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Long getValor() { return valor; }
    public void setValor(Long valor) { this.valor = valor; }
}