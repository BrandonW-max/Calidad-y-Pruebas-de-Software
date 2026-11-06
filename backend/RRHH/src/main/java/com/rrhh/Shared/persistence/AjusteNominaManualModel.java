package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ajuste_nomina_manual")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AjusteNominaManualModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ajuste")
    private Integer idAjuste;

    @Column(name = "id_empleado", nullable = false)
    private Long idEmpleado;

    @Column(name = "periodo", nullable = false, length = 20)
    private String periodo;

    @Column(name = "total_horas_trabajadas")
    private BigDecimal totalHorasTrabajadas;

    @Column(name = "total_horas_extra")
    private BigDecimal totalHorasExtra;

    @Column(name = "total_minutos_tardanza")
    private Integer totalMinutosTardanza;

    @Column(name = "cantidad_guardias")
    private Integer cantidadGuardias;

    @Column(name = "tipo_pension_aplicada", length = 10)
    private String tipoPensionAplicada;

    @Column(name = "actualizado_por")
    private Long actualizadoPor;

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl;
}
