package com.proyectoarquitectura.app.service.usuarios;

import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    UsuarioResponse cambiarRol(Integer usuarioId, String nuevoRol, Integer adminId);
    
    Page<UsuarioResponse> listarUsuarios(Pageable pageable);
}