package com.rrhh.Shared.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "departamento")
public class DepartamentoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_dpto")
    private Integer idDpto;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "cod_dpto", unique = true, length = 20)
    private String codDpto;

    @Column(name = "ubicacion", length = 100)
    private String ubicacion;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_el", updatable = false)
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    // ================= RELACIONES =================

    @OneToMany(mappedBy = "departamento", fetch = FetchType.LAZY)
    private List<EmpleadoModel> empleados;

    // ================= CONSTRUCTORES =================

    public DepartamentoModel() {
    }

    public DepartamentoModel(
            Integer idDpto,
            String nombre,
            String codDpto,
            String ubicacion,
            Boolean activo,
            LocalDateTime creadoEl,
            LocalDateTime actualizadoEl,
            List<EmpleadoModel> empleados
    ) {
        this.idDpto = idDpto;
        this.nombre = nombre;
        this.codDpto = codDpto;
        this.ubicacion = ubicacion;
        this.activo = activo;
        this.creadoEl = creadoEl;
        this.actualizadoEl = actualizadoEl;
        this.empleados = empleados;
    }

    // ================= GETTERS Y SETTERS =================

    public Integer getIdDpto() {
        return idDpto;
    }

    public void setIdDpto(Integer idDpto) {
        this.idDpto = idDpto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodDpto() {
        return codDpto;
    }

    public void setCodDpto(String codDpto) {
        this.codDpto = codDpto;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreadoEl() {
        return creadoEl;
    }

    public void setCreadoEl(LocalDateTime creadoEl) {
        this.creadoEl = creadoEl;
    }

    public LocalDateTime getActualizadoEl() {
        return actualizadoEl;
    }

    public void setActualizadoEl(LocalDateTime actualizadoEl) {
        this.actualizadoEl = actualizadoEl;
    }

    public List<EmpleadoModel> getEmpleados() {
        return empleados;
    }

    public void setEmpleados(List<EmpleadoModel> empleados) {
        this.empleados = empleados;
    }
}