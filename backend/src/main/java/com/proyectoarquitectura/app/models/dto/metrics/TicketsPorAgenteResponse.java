package com.proyectoarquitectura.app.models.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketsPorAgenteResponse {

    private List<AgenteResueltosItem> agentes;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AgenteResueltosItem {
        private Integer agenteId;
        private String agenteNombre;
        private Long cantidadResueltos;
    }
}
