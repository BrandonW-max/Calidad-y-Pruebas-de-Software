package com.rrhh.Modulos.CU3_Nomina.Application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AjustarNominaRequestDTO {

    @NotNull(message = "Error: campo requerido")
    @DecimalMin(value = "0.0", inclusive = true, message = "Error: el valor debe ser mayor o igual a 0")
    private BigDecimal sueldoBase;
    private BigDecimal totalHorasTrabajadas;
    private BigDecimal totalHorasExtra;
    private Integer totalMinutosTardanza;
    private Integer cantidadGuardias;
    private String tipoPensionAplicada;

    public BigDecimal getSueldoBase() { return sueldoBase; }
    public void setSueldoBase(BigDecimal sueldoBase) { this.sueldoBase = sueldoBase; }

    public BigDecimal getTotalHorasTrabajadas() { return totalHorasTrabajadas; }
    public void setTotalHorasTrabajadas(BigDecimal totalHorasTrabajadas) { this.totalHorasTrabajadas = totalHorasTrabajadas; }

    public BigDecimal getTotalHorasExtra() { return totalHorasExtra; }
    public void setTotalHorasExtra(BigDecimal totalHorasExtra) { this.totalHorasExtra = totalHorasExtra; }

    public Integer getTotalMinutosTardanza() { return totalMinutosTardanza; }
    public void setTotalMinutosTardanza(Integer totalMinutosTardanza) { this.totalMinutosTardanza = totalMinutosTardanza; }

    public Integer getCantidadGuardias() { return cantidadGuardias; }
    public void setCantidadGuardias(Integer cantidadGuardias) { this.cantidadGuardias = cantidadGuardias; }

    public String getTipoPensionAplicada() { return tipoPensionAplicada; }
    public void setTipoPensionAplicada(String tipoPensionAplicada) { this.tipoPensionAplicada = tipoPensionAplicada; }
}
