package com.proyectoarquitectura.app.models.dto.tickets;

import com.proyectoarquitectura.app.models.entity.Ticket;
import java.time.LocalDateTime;

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
    private Short valoracion;

    public TicketResponse() {}

    public static TicketResponse from(Ticket t) {
        TicketResponse response = new TicketResponse();
        response.setId(t.getId());
        response.setCodigo(t.getCodigo());
        response.setAsunto(t.getAsunto());
        response.setDescripcion(t.getDescripcion());
        response.setTipo(t.getTipo());
        response.setPrioridad(t.getPrioridad());
        response.setEstado(t.getEstado());
        
        if (t.getCategoria() != null) {
            response.setCategoriaId(t.getCategoria().getId());
            response.setCategoriaNombre(t.getCategoria().getNombre());
        }
        
        if (t.getCliente() != null) {
            response.setClienteId(t.getCliente().getId());
            response.setClienteNombre(t.getCliente().getNombres() + " " + t.getCliente().getApellidos());
        }
        
        if (t.getAgente() != null) {
            response.setAgenteId(t.getAgente().getId());
            response.setAgenteNombre(t.getAgente().getNombres() + " " + t.getAgente().getApellidos());
        }
        
        response.setFechaCreacion(t.getFechaCreacion());
        response.setFechaInicioAtencion(t.getFechaInicioAtencion());
        response.setFechaCierre(t.getFechaCierre());
        response.setFechaVencimientoSla(t.getFechaVencimientoSla());
        response.setTiempoResolucionMinutos(t.getTiempoResolucionMinutos());
        response.setValoracion(t.getValoracion());

        return response;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    
    public String getCategoriaNombre() { return categoriaNombre; }
    public void setCategoriaNombre(String categoriaNombre) { this.categoriaNombre = categoriaNombre; }
    
    public Integer getClienteId() { return clienteId; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    
    public Integer getAgenteId() { return agenteId; }
    public void setAgenteId(Integer agenteId) { this.agenteId = agenteId; }
    
    public String getAgenteNombre() { return agenteNombre; }
    public void setAgenteNombre(String agenteNombre) { this.agenteNombre = agenteNombre; }
    
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public LocalDateTime getFechaInicioAtencion() { return fechaInicioAtencion; }
    public void setFechaInicioAtencion(LocalDateTime fechaInicioAtencion) { this.fechaInicioAtencion = fechaInicioAtencion; }
    
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    
    public LocalDateTime getFechaVencimientoSla() { return fechaVencimientoSla; }
    public void setFechaVencimientoSla(LocalDateTime fechaVencimientoSla) { this.fechaVencimientoSla = fechaVencimientoSla; }
    
    public Integer getTiempoResolucionMinutos() { return tiempoResolucionMinutos; }
    public void setTiempoResolucionMinutos(Integer tiempoResolucionMinutos) { this.tiempoResolucionMinutos = tiempoResolucionMinutos; }

    public Short getValoracion() { return valoracion; }
    public void setValoracion(Short valoracion) { this.valoracion = valoracion; }
}