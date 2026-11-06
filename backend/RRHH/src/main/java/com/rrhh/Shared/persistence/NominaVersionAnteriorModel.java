package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "nomina_version_anterior")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NominaVersionAnteriorModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_version_anterior")
    private Integer idVersionAnterior;

    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @Column(name = "periodo", nullable = false, length = 20)
    private String periodo;

    @Column(name = "version")
    private Integer version;

    @Column(name = "sueldo_base")
    private BigDecimal sueldoBase;

    @Column(name = "total_horas_extra")
    private BigDecimal totalHorasExtra;

    @Column(name = "total_minutos_tardanza")
    private Integer totalMinutosTardanza;

    @Column(name = "descuento_tardanzas")
    private BigDecimal descuentoTardanzas;

    @Column(name = "descuento_ley")
    private BigDecimal descuentoLey;

    @Column(name = "sueldo_bruto")
    private BigDecimal sueldoBruto;

    @Column(name = "sueldo_neto")
    private BigDecimal sueldoNeto;

    @Column(name = "tipo_pension_aplicada", length = 10)
    private String tipoPensionAplicada;

    @Column(name = "reemplazada_el")
    private LocalDateTime reemplazadaEl;
}
