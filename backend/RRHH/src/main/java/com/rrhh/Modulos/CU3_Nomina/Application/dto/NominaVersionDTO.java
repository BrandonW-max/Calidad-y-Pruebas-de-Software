package com.rrhh.Modulos.CU3_Nomina.Application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NominaVersionDTO {

    private Integer version;
    private boolean vigente;
    private BigDecimal sueldoBase;
    private BigDecimal totalHorasExtra;
    private Integer totalMinutosTardanza;
    private BigDecimal descuentoTardanzas;
    private BigDecimal descuentoLey;
    private BigDecimal sueldoBruto;
    private BigDecimal sueldoNeto;
    private String tipoPensionAplicada;
    private LocalDateTime reemplazadoEl;
}
