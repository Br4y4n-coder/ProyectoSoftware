package com.proyectoarquitectura.app.models.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketsPorPrioridadResponse {

    private Map<String, Long> conteoPorPrioridad;
}
