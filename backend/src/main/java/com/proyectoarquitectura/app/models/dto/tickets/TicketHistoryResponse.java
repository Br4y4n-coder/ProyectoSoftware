package com.proyectoarquitectura.app.models.dto.tickets;

import com.proyectoarquitectura.app.models.entity.TicketHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketHistoryResponse {

    private Integer id;
    private Integer ticketId;
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private Integer usuarioId;
    private String usuarioNombre;
    private LocalDateTime fechaHora;

    public static TicketHistoryResponse from(TicketHistory h) {
        return TicketHistoryResponse.builder()
                .id(h.getId())
                .ticketId(h.getTicketId())
                .campoModificado(h.getCampoModificado())
                .valorAnterior(h.getValorAnterior())
                .valorNuevo(h.getValorNuevo())
                .usuarioId(h.getUsuario() != null ? h.getUsuario().getId() : null)
                .usuarioNombre(h.getUsuario() != null
                        ? h.getUsuario().getNombres() + " " + h.getUsuario().getApellidos()
                        : null)
                .fechaHora(h.getFechaHora())
                .build();
    }
}
