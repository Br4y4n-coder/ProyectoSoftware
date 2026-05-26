package com.proyectoarquitectura.app.models.dto.tickets;

import com.proyectoarquitectura.app.models.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponse {

    private Integer id;
    private String codigo;
    private String asunto;
    private String descripcion;
    private String tipo;
    private String prioridad;
    private String estado;

    private Integer categoriaId;
    private String categoriaNombre;

    private Integer clienteId;
    private String clienteNombre;

    private Integer agenteId;
    private String agenteNombre;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaInicioAtencion;
    private LocalDateTime fechaCierre;
    private LocalDateTime fechaVencimientoSla;
    private Integer tiempoResolucionMinutos;

    public static TicketResponse from(Ticket t) {
        return TicketResponse.builder()
                .id(t.getId())
                .codigo(t.getCodigo())
                .asunto(t.getAsunto())
                .descripcion(t.getDescripcion())
                .tipo(t.getTipo())
                .prioridad(t.getPrioridad())
                .estado(t.getEstado())
                .categoriaId(t.getCategoria() != null ? t.getCategoria().getId() : null)
                .categoriaNombre(t.getCategoria() != null ? t.getCategoria().getNombre() : null)
                .clienteId(t.getCliente() != null ? t.getCliente().getId() : null)
                .clienteNombre(t.getCliente() != null
                        ? t.getCliente().getNombres() + " " + t.getCliente().getApellidos()
                        : null)
                .agenteId(t.getAgente() != null ? t.getAgente().getId() : null)
                .agenteNombre(t.getAgente() != null
                        ? t.getAgente().getNombres() + " " + t.getAgente().getApellidos()
                        : null)
                .fechaCreacion(t.getFechaCreacion())
                .fechaInicioAtencion(t.getFechaInicioAtencion())
                .fechaCierre(t.getFechaCierre())
                .fechaVencimientoSla(t.getFechaVencimientoSla())
                .tiempoResolucionMinutos(t.getTiempoResolucionMinutos())
                .build();
    }
}
