package com.rrhh.Nomina.Application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class NominaResponseDTO {

    private Integer idNomina;
    private String periodo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private LocalDate fechaEmision;
    private String nombreEmpleado;
    private Long idEmpleado;

    private BigDecimal sueldoBase;
    private BigDecimal totalHorasTrabajadas;
    private BigDecimal totalHorasExtra;
    private Integer totalMinutosTardanza;

    private BigDecimal bonifFamiliar;
    private BigDecimal bonifTurnoNocturno;
    private BigDecimal bonifGuardia;
    private BigDecimal bonifRiesgo;
    private BigDecimal bonifCargo;

    private BigDecimal descuentoTardanzas;
    private BigDecimal descuentoLey;
    private BigDecimal asignacionFamiliar;

    private BigDecimal sueldoBruto;
    private BigDecimal sueldoNeto;
    private String estadoPago;

    private List<DetalleNominaResponseDTO> detalles;

    public NominaResponseDTO() {}

    public Integer getIdNomina() { return idNomina; }
    public void setIdNomina(Integer idNomina) { this.idNomina = idNomina; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String nombreEmpleado) { this.nombreEmpleado = nombreEmpleado; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public BigDecimal getSueldoBase() { return sueldoBase; }
    public void setSueldoBase(BigDecimal sueldoBase) { this.sueldoBase = sueldoBase; }

    public BigDecimal getTotalHorasTrabajadas() { return totalHorasTrabajadas; }
    public void setTotalHorasTrabajadas(BigDecimal totalHorasTrabajadas) { this.totalHorasTrabajadas = totalHorasTrabajadas; }

    public BigDecimal getTotalHorasExtra() { return totalHorasExtra; }
    public void setTotalHorasExtra(BigDecimal totalHorasExtra) { this.totalHorasExtra = totalHorasExtra; }

    public Integer getTotalMinutosTardanza() { return totalMinutosTardanza; }
    public void setTotalMinutosTardanza(Integer totalMinutosTardanza) { this.totalMinutosTardanza = totalMinutosTardanza; }

    public BigDecimal getBonifFamiliar() { return bonifFamiliar; }
    public void setBonifFamiliar(BigDecimal bonifFamiliar) { this.bonifFamiliar = bonifFamiliar; }

    public BigDecimal getBonifTurnoNocturno() { return bonifTurnoNocturno; }
    public void setBonifTurnoNocturno(BigDecimal bonifTurnoNocturno) { this.bonifTurnoNocturno = bonifTurnoNocturno; }

    public BigDecimal getBonifGuardia() { return bonifGuardia; }
    public void setBonifGuardia(BigDecimal bonifGuardia) { this.bonifGuardia = bonifGuardia; }

    public BigDecimal getBonifRiesgo() { return bonifRiesgo; }
    public void setBonifRiesgo(BigDecimal bonifRiesgo) { this.bonifRiesgo = bonifRiesgo; }

    public BigDecimal getBonifCargo() { return bonifCargo; }
    public void setBonifCargo(BigDecimal bonifCargo) { this.bonifCargo = bonifCargo; }

    public BigDecimal getDescuentoTardanzas() { return descuentoTardanzas; }
    public void setDescuentoTardanzas(BigDecimal descuentoTardanzas) { this.descuentoTardanzas = descuentoTardanzas; }

    public BigDecimal getDescuentoLey() { return descuentoLey; }
    public void setDescuentoLey(BigDecimal descuentoLey) { this.descuentoLey = descuentoLey; }

    public BigDecimal getAsignacionFamiliar() { return asignacionFamiliar; }
    public void setAsignacionFamiliar(BigDecimal asignacionFamiliar) { this.asignacionFamiliar = asignacionFamiliar; }

    public BigDecimal getSueldoBruto() { return sueldoBruto; }
    public void setSueldoBruto(BigDecimal sueldoBruto) { this.sueldoBruto = sueldoBruto; }

    public BigDecimal getSueldoNeto() { return sueldoNeto; }
    public void setSueldoNeto(BigDecimal sueldoNeto) { this.sueldoNeto = sueldoNeto; }

    public String getEstadoPago() { return estadoPago; }
    public void setEstadoPago(String estadoPago) { this.estadoPago = estadoPago; }

    public List<DetalleNominaResponseDTO> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleNominaResponseDTO> detalles) { this.detalles = detalles; }
}