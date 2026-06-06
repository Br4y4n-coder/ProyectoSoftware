package com.proyectoarquitectura.app.repository;

import com.proyectoarquitectura.app.models.entity.Integracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntegracionRepository extends JpaRepository<Integracion, Integer> {
    Optional<Integracion> findByNombre(String nombre);
    List<Integracion> findByActivoTrue();
    List<Integracion> findByConectadoTrue();
}