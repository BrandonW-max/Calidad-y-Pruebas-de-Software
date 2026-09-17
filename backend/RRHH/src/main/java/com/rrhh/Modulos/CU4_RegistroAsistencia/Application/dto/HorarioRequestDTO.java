package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public class HorarioRequestDTO {

    @NotBlank(message = "El nombre del turno es obligatorio")
    private String nombreTurno;

    @NotNull(message = "La hora de entrada es obligatoria")
    private LocalTime horaEntrada;

    @NotNull(message = "La hora de salida es obligatoria")
    private LocalTime horaSalida;

    @Min(value = 0, message = "La tolerancia no puede ser negativa")
    private Integer tolerancia = 10;

    public String getNombreTurno() { return nombreTurno; }
    public void setNombreTurno(String nombreTurno) { this.nombreTurno = nombreTurno; }

    public LocalTime getHoraEntrada() { return horaEntrada; }
    public void setHoraEntrada(LocalTime horaEntrada) { this.horaEntrada = horaEntrada; }

    public LocalTime getHoraSalida() { return horaSalida; }
    public void setHoraSalida(LocalTime horaSalida) { this.horaSalida = horaSalida; }

    public Integer getTolerancia() { return tolerancia; }
    public void setTolerancia(Integer tolerancia) { this.tolerancia = tolerancia; }
}
