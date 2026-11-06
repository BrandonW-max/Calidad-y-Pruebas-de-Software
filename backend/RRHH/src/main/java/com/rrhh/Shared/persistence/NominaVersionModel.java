package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_version_nomina")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NominaVersionModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_version")
    private Integer idVersion;

    @Column(name = "id_nomina", nullable = false)
    private Integer idNomina;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "sueldo_base")
    private BigDecimal sueldoBase;

    @Column(name = "total_horas_trabajadas")
    private BigDecimal totalHorasTrabajadas;

    @Column(name = "total_horas_extra")
    private BigDecimal totalHorasExtra;

    @Column(name = "total_minutos_tardanza")
    private Integer totalMinutosTardanza;

    @Column(name = "bonif_familiar")
    private BigDecimal bonifFamiliar;

    @Column(name = "bonif_turno_nocturno")
    private BigDecimal bonifTurnoNocturno;

    @Column(name = "bonif_guardia")
    private BigDecimal bonifGuardia;

    @Column(name = "bonif_riesgo")
    private BigDecimal bonifRiesgo;

    @Column(name = "descuento_tardanzas")
    private BigDecimal descuentoTardanzas;

    @Column(name = "descuento_ley")
    private BigDecimal descuentoLey;

    @Column(name = "sueldo_bruto")
    private BigDecimal sueldoBruto;

    @Column(name = "sueldo_neto", nullable = false)
    private BigDecimal sueldoNeto;

    @Column(name = "tipo_pension_aplicada", length = 10)
    private String tipoPensionAplicada;

    @Column(name = "reemplazado_por")
    private Long reemplazadoPor;

    @Column(name = "reemplazado_el")
    private LocalDateTime reemplazadoEl;
}
