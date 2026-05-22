package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "nomina")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NominaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nomina")
    private Integer idNomina;

    @Column(name = "periodo", nullable = false)
    private String periodo;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "sueldo_base")
    private BigDecimal sueldoBase;

    @Column(name = "sueldo_neto")
    private BigDecimal sueldoNeto;

    @Column(name = "estado_pago")
    private String estadoPago = "PENDIENTE";

    @Column(name = "creado_el")
    private LocalDateTime creadoEl = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private EmpleadoModel empleado;
}