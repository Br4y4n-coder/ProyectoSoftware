package com.proyectoarquitectura.app.repository;

import com.proyectoarquitectura.app.models.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByCodigo(String codigo);

    Page<Ticket> findByClienteId(Integer clienteId, Pageable pageable);

    Page<Ticket> findByAgenteId(Integer agenteId, Pageable pageable);

    Page<Ticket> findByEstado(String estado, Pageable pageable);

    List<Ticket> findByEstadoIn(List<String> estados);

    List<Ticket> findByPrioridadAndEstadoIn(String prioridad, List<String> estados);

    long countByEstado(String estado);

    long countByAgenteIdAndEstado(Integer agenteId, String estado);

    @Query("""
            SELECT t FROM Ticket t
            JOIN FETCH t.categoria c
            JOIN FETCH c.area a
            WHERE t.cliente.id = :clienteId
              AND LOWER(t.estado) IN ('abierto','asignado','en_proceso')
              AND (a.id = :areaId OR LOWER(a.nombre) = LOWER(:areaNombre))
            ORDER BY t.fechaCreacion DESC
            """)
    Optional<Ticket> findTicketActivoPorClienteYArea(@Param("clienteId") Integer clienteId,
                                                       @Param("areaId") Integer areaId,
                                                       @Param("areaNombre") String areaNombre);

    @Query("""
            SELECT t.estado, COUNT(t)
            FROM Ticket t
            GROUP BY t.estado
            """)
    List<Object[]> contarTicketsPorEstado();

    @Query("""
            SELECT AVG(t.tiempoResolucionMinutos)
            FROM Ticket t
            WHERE LOWER(t.estado) IN ('resuelto','cerrado')
              AND t.tiempoResolucionMinutos IS NOT NULL
              AND t.fechaCierre >= :desde
            """)
    Double promedioTiempoResolucionDesde(@Param("desde") LocalDateTime desde);

    @Query("""
            SELECT t.agente.id, t.agente.nombres, t.agente.apellidos, COUNT(t)
            FROM Ticket t
            WHERE t.agente IS NOT NULL
              AND LOWER(t.estado) IN ('resuelto','cerrado')
            GROUP BY t.agente.id, t.agente.nombres, t.agente.apellidos
            ORDER BY COUNT(t) DESC
            """)
    List<Object[]> contarTicketsResueltosPorAgente();

    @Query("""
            SELECT t.prioridad, COUNT(t)
            FROM Ticket t
            GROUP BY t.prioridad
            """)
    List<Object[]> contarTicketsPorPrioridad();

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.estado IN ('abierto','en_proceso')
              AND t.fechaVencimientoSla IS NOT NULL
              AND t.fechaVencimientoSla > :ahora
              AND t.fechaVencimientoSla < :limite
            ORDER BY t.fechaVencimientoSla ASC
            """)
    List<Ticket> findTicketsEnRiesgoSla(@Param("ahora") LocalDateTime ahora,
                                        @Param("limite") LocalDateTime limite);

    @Query("""
            SELECT t FROM Ticket t
            WHERE t.estado IN ('abierto','en_proceso')
              AND t.fechaVencimientoSla IS NOT NULL
              AND t.fechaVencimientoSla < :ahora
            """)
    List<Ticket> findTicketsVencidos(@Param("ahora") LocalDateTime ahora);
}
