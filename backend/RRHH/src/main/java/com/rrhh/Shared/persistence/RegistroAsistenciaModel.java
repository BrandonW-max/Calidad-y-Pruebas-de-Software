package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;

@Entity
@Table(name = "registro_asistencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroAsistenciaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_registro")
    private Integer idRegistro;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "minutos_tardanza")
    private Integer minutosTardanza = 0;

    @Column(name = "minutos_extra")
    private Integer minutosExtra = 0;

    @Column(name = "estado_asistencia")
    private String estadoAsistencia;

    @Column(name = "creado_el")
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empleado")
    private EmpleadoModel empleado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario")
    private HorarioModel horario;
}