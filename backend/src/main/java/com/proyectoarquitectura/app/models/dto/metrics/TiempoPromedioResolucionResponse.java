package com.proyectoarquitectura.app.models.dto.metrics;

public class TiempoPromedioResolucionResponse {
    private Double promedioHoras;
    private Long totalTickets;

    public TiempoPromedioResolucionResponse() {}

    public TiempoPromedioResolucionResponse(Double promedioHoras, Long totalTickets) {
        this.promedioHoras = promedioHoras;
        this.totalTickets = totalTickets;
    }

    public Double getPromedioHoras() { return promedioHoras; }
    public void setPromedioHoras(Double promedioHoras) { this.promedioHoras = promedioHoras; }
    public Long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(Long totalTickets) { this.totalTickets = totalTickets; }
}