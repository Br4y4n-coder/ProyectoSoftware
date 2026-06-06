package com.proyectoarquitectura.app.service.usuarios;

import com.proyectoarquitectura.app.exception.BusinessException;
import com.proyectoarquitectura.app.exception.NotFoundException;
import com.proyectoarquitectura.app.models.dto.auth.UsuarioResponse;
import com.proyectoarquitectura.app.models.dto.usuarios.ActualizarUsuarioRequest;
import com.proyectoarquitectura.app.models.entity.Rol;
import com.proyectoarquitectura.app.models.entity.Usuario;
import com.proyectoarquitectura.app.repository.RolRepository;
import com.proyectoarquitectura.app.repository.UsuarioRepository;
import com.proyectoarquitectura.app.security.SecurityAuditLogger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Override
    @Transactional
    public UsuarioResponse cambiarEstado(Integer usuarioId, String nuevoEstado, Integer adminId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));

        if (usuarioId.equals(adminId)) {
            throw BusinessException.badRequest("No puedes cambiar el estado de tu propia cuenta");
        }

        String estadoAnterior = usuario.getEstado();
        usuario.setEstado(nuevoEstado.toLowerCase());
        usuario = usuarioRepository.save(usuario);

        SecurityAuditLogger.cambioRol(
                usuario.getId(),
                usuario.getCorreo(),
                "estado:" + estadoAnterior,
                "estado:" + nuevoEstado,
                adminId);

        return UsuarioResponse.from(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Integer usuarioId, ActualizarUsuarioRequest req, Integer adminId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado: " + usuarioId));

        if (req.getNombres() != null && !req.getNombres().isBlank()) {
            usuario.setNombres(req.getNombres().trim());
        }
        if (req.getApellidos() != null && !req.getApellidos().isBlank()) {
            usuario.setApellidos(req.getApellidos().trim());
        }
        if (req.getTelefono() != null) {
            usuario.setTelefono(req.getTelefono().trim());
        }
        if (req.getNivelAgente() != null) {
            usuario.setNivelAgente(req.getNivelAgente());
        }
        if (req.getEstado() != null && !req.getEstado().isBlank()) {
            if (usuarioId.equals(adminId)) {
                throw BusinessException.badRequest("No puedes cambiar el estado de tu propia cuenta");
            }
            usuario.setEstado(req.getEstado().toLowerCase());
        }

        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Override
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(UsuarioResponse::from);
    }
}