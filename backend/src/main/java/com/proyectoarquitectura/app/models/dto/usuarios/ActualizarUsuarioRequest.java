package com.proyectoarquitectura.app.models.dto.usuarios;

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
public class ActualizarUsuarioRequest {

    @Size(max = 80)
    private String nombres;

    @Size(max = 80)
    private String apellidos;

    @Size(max = 20)
    private String telefono;

    @Pattern(regexp = "activo|pendiente|suspendido|rechazado|eliminado", message = "estado invalido")
    private String estado;

    private Integer nivelAgente;
}
