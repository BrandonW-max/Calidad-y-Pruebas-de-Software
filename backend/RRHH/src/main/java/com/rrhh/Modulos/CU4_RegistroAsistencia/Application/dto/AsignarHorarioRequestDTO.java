package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AsignarHorarioRequestDTO {

    @NotNull(message = "El empleado es obligatorio")
    private Long idEmpleado;

    @NotNull(message = "El horario es obligatorio")
    private Integer idHorario;

    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private Boolean esTemporal = false;

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Integer getIdHorario() { return idHorario; }
    public void setIdHorario(Integer idHorario) { this.idHorario = idHorario; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }

    public Boolean getEsTemporal() { return esTemporal; }
    public void setEsTemporal(Boolean esTemporal) { this.esTemporal = esTemporal; }
}
