package com.proyectoarquitectura.app.service.auditoria;

import com.proyectoarquitectura.app.models.dto.auditoria.LogAuditoriaResponse;
import com.proyectoarquitectura.app.models.entity.LogAuditoria;
import com.proyectoarquitectura.app.repository.LogAuditoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;

    public AuditoriaService(LogAuditoriaRepository logAuditoriaRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    @Transactional
    public void registrar(String usuario, String accion, String detalles, String ip) {
        LogAuditoria log = LogAuditoria.builder()
                .usuario(usuario)
                .accion(accion)
                .detalles(detalles)
                .ip(ip)
                .build();
        logAuditoriaRepository.save(log);
    }

    public Page<LogAuditoriaResponse> listarTodos(Pageable pageable) {
        return logAuditoriaRepository.findAll(pageable).map(LogAuditoriaResponse::from);
    }

    public Page<LogAuditoriaResponse> buscarPorUsuario(String usuario, Pageable pageable) {
        return logAuditoriaRepository.findByUsuarioContainingIgnoreCase(usuario, pageable)
                .map(LogAuditoriaResponse::from);
    }

    public Page<LogAuditoriaResponse> buscarPorAccion(String accion, Pageable pageable) {
        return logAuditoriaRepository.findByAccion(accion, pageable)
                .map(LogAuditoriaResponse::from);
    }
}