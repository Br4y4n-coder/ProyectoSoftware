package com.proyectoarquitectura.app.repository;

import com.proyectoarquitectura.app.models.entity.TicketHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketHistoryRepository extends JpaRepository<TicketHistory, Integer> {

    List<TicketHistory> findByTicketIdOrderByFechaHoraDesc(Integer ticketId);
}
