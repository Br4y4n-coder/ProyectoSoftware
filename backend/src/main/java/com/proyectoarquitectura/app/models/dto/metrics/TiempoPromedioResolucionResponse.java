package com.proyectoarquitectura.app.models.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TiempoPromedioResolucionResponse {

    private Double promedioMinutos;
    private Integer diasAnalizados;
}
