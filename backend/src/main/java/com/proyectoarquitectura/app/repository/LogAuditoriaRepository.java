package com.proyectoarquitectura.app.repository;

import com.proyectoarquitectura.app.models.entity.LogAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    Page<LogAuditoria> findByUsuarioContainingIgnoreCase(String usuario, Pageable pageable);
    Page<LogAuditoria> findByAccion(String accion, Pageable pageable);
}