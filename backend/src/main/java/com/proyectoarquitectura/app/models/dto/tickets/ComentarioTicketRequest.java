package com.proyectoarquitectura.app.models.dto.tickets;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioTicketRequest {

    @NotBlank(message = "el comentario no puede estar vacío")
    @Size(max = 2000)
    private String texto;
}
