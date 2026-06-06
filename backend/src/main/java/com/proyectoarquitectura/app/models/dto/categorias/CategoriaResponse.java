package com.proyectoarquitectura.app.models.dto.categorias;

import com.proyectoarquitectura.app.models.entity.Categoria;
import java.time.LocalDateTime;

public class CategoriaResponse {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String colorHex;
    private String icono;
    private Integer areaId;
    private String areaNombre;
    private Boolean activo;
    private LocalDateTime creadoEn;

    public CategoriaResponse() {}

    public static CategoriaResponse from(Categoria cat) {
        CategoriaResponse response = new CategoriaResponse();
        response.setId(cat.getId());
        response.setNombre(cat.getNombre());
        response.setDescripcion(cat.getDescripcion());
        response.setColorHex(cat.getColorHex());
        response.setIcono(cat.getIcono());
        response.setActivo(cat.getActivo());
        response.setCreadoEn(cat.getCreadoEn());
        if (cat.getArea() != null) {
            response.setAreaId(cat.getArea().getId());
            response.setAreaNombre(cat.getArea().getNombre());
        }
        return response;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }
    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }
    public String getAreaNombre() { return areaNombre; }
    public void setAreaNombre(String areaNombre) { this.areaNombre = areaNombre; }
    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public void setCreadoEn(LocalDateTime creadoEn) { this.creadoEn = creadoEn; }
}