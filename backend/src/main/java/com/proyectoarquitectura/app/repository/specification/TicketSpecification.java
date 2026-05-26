package com.proyectoarquitectura.app.repository.specification;

import com.proyectoarquitectura.app.models.entity.Ticket;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.stream.Stream;

public final class TicketSpecification {

    private TicketSpecification() {
    }

    public static Specification<Ticket> conEstado(String estado) {
        return (root, query, cb) ->
                estado == null || estado.isBlank()
                        ? null
                        : cb.equal(cb.lower(root.get("estado")), estado.toLowerCase());
    }

    public static Specification<Ticket> conPrioridad(String prioridad) {
        return (root, query, cb) ->
                prioridad == null || prioridad.isBlank()
                        ? null
                        : cb.equal(cb.lower(root.get("prioridad")), prioridad.toLowerCase());
    }

    public static Specification<Ticket> conTipo(String tipo) {
        return (root, query, cb) ->
                tipo == null || tipo.isBlank()
                        ? null
                        : cb.equal(cb.lower(root.get("tipo")), tipo.toLowerCase());
    }

    public static Specification<Ticket> conFechaDesde(LocalDateTime desde) {
        return (root, query, cb) ->
                desde == null
                        ? null
                        : cb.greaterThanOrEqualTo(root.get("fechaCreacion"), desde);
    }

    public static Specification<Ticket> conFechaHasta(LocalDateTime hasta) {
        return (root, query, cb) ->
                hasta == null
                        ? null
                        : cb.lessThanOrEqualTo(root.get("fechaCreacion"), hasta);
    }

    public static Specification<Ticket> conClienteId(Integer usuarioId) {
        return (root, query, cb) ->
                usuarioId == null
                        ? null
                        : cb.equal(root.get("cliente").get("id"), usuarioId);
    }

    public static Specification<Ticket> conAgenteId(Integer agenteId) {
        return (root, query, cb) ->
                agenteId == null
                        ? null
                        : cb.equal(root.get("agente").get("id"), agenteId);
    }

    public static Specification<Ticket> buscar(String estado,
                                             String prioridad,
                                             String tipo,
                                             LocalDateTime fechaDesde,
                                             LocalDateTime fechaHasta,
                                             Integer usuarioId,
                                             Integer agenteId) {
        return Stream.of(
                        conEstado(estado),
                        conPrioridad(prioridad),
                        conTipo(tipo),
                        conFechaDesde(fechaDesde),
                        conFechaHasta(fechaHasta),
                        conClienteId(usuarioId),
                        conAgenteId(agenteId))
                .reduce(Specification::and)
                .orElse(Specification.unrestricted());
    }
}
