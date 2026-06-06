package com.proyectoarquitectura.app.models.dto.usuarios;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CambiarEstadoUsuarioRequest {

    @NotBlank
    @Pattern(regexp = "activo|pendiente|suspendido|rechazado|eliminado", message = "estado invalido")
    private String estado;
}
