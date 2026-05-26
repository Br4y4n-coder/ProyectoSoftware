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
public class CambiarRolRequest {

    @NotBlank
    @Pattern(regexp = "usuario|agente|administrador", message = "rol invalido")
    private String rol;
}
