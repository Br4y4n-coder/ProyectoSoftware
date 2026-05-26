package com.proyectoarquitectura.app.service.usuarios;

import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;

public interface UsuarioService {

    UsuarioResponse cambiarRol(Integer usuarioId, String nuevoRol, Integer adminId);
}
