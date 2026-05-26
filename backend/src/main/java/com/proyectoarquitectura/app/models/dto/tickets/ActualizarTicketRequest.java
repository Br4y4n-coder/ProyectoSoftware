package com.proyectoarquitectura.app.models.dto.tickets;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActualizarTicketRequest {

    @Size(max = 200)
    private String asunto;

    private String descripcion;

    @Pattern(regexp = "alta|media|baja", message = "prioridad debe ser: alta, media o baja")
    private String prioridad;
}
