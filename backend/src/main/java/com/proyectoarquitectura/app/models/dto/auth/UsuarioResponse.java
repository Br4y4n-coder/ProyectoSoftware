package com.proyectoarquitectura.app.models.dto.auth;

import com.proyectoarquitectura.app.models.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioResponse {
    private Integer id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
    private String estado;
    private String area;
    private Integer nivelAgente;
    private LocalDateTime ultimoAcceso;

    public static UsuarioResponse from(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombres(u.getNombres())
                .apellidos(u.getApellidos())
                .correo(u.getCorreo())
                .rol(u.getRol() != null ? u.getRol().getNombre() : null)
                .estado(u.getEstado())
                .area(u.getArea() != null ? u.getArea().getNombre() : null)
                .nivelAgente(u.getNivelAgente())
                .ultimoAcceso(u.getUltimoAcceso())
                .build();
    }
}
