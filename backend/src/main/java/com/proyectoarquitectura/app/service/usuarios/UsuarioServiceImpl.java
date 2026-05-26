package com.proyectoarquitectura.app.service.usuarios;

import com.proyectoarquitectura.app.exception.BusinessException;
import com.proyectoarquitectura.app.exception.NotFoundException;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.RolRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.security.SecurityAuditLogger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional
    public UsuarioResponse cambiarRol(Integer usuarioId, String nuevoRol, Integer adminId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));

        Rol rol = rolRepository.findByNombre(nuevoRol.toLowerCase())
                .orElseThrow(() -> BusinessException.badRequest("Rol no valido: " + nuevoRol));

        String rolAnterior = usuario.getRol() != null ? usuario.getRol().getNombre() : null;
        usuario.setRol(rol);
        usuario = usuarioRepository.save(usuario);

        SecurityAuditLogger.cambioRol(
                usuario.getId(),
                usuario.getCorreo(),
                rolAnterior,
                rol.getNombre(),
                adminId);

        return UsuarioResponse.from(usuario);
    }
}
