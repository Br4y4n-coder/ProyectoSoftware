package com.proyectoarquitectura.app.repository;

import com.proyectoarquitectura.app.models.entity.Ticket;
import com.proyectoarquitectura.app.models.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    
    Page<Ticket> findByEstado(String estado, Pageable pageable);
    
    Page<Ticket> findByClienteId(Integer clienteId, Pageable pageable);
    
    Page<Ticket> findByAgenteId(Integer agenteId, Pageable pageable);
    
    java.util.Optional<Ticket> findByCodigo(String codigo);
}