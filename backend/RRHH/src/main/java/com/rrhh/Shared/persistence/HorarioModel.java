package com.rrhh.Shared.persistence;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "horario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HorarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_horario")
    private Integer idHorario;

    @Column(name = "nombre_turno", nullable = false)
    private String nombreTurno;

    @Column(name = "hora_entrada", nullable = false)
    private LocalTime horaEntrada;

    @Column(name = "hora_salida", nullable = false)
    private LocalTime horaSalida;

    @Column(name = "tolerancia")
    private Integer tolerancia = 10;

    @Column(name = "umbral_extra")
    private Integer umbralExtra = 60;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "creado_el")
    private LocalDateTime creadoEl = LocalDateTime.now();

    @Column(name = "actualizado_el")
    private LocalDateTime actualizadoEl = LocalDateTime.now();

    // =========================
    // RELACIONES
    // =========================

    @OneToMany(mappedBy = "horario", fetch = FetchType.LAZY)
    private List<AsignacionHorarioModel> asignacionesHorario;

    @OneToMany(mappedBy = "horario", fetch = FetchType.LAZY)
    private List<RegistroAsistenciaModel> registrosAsistencia;
}