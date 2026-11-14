package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RegistrarAscensoRequestDTO {

    @NotBlank(message = "El nuevo cargo es obligatorio")
    private String nuevoCargo;
    @NotNull(message = "La fecha de ascenso es obligatoria")
    private LocalDate fechaAscenso;
    private String motivo;
    // Opcional: si no se envía, el empleado conserva su sueldo actual
    private BigDecimal nuevoSueldo;

    public RegistrarAscensoRequestDTO() {
    }

    public RegistrarAscensoRequestDTO(
            String nuevoCargo,
            LocalDate fechaAscenso,
            String motivo
    ) {
        this.nuevoCargo = nuevoCargo;
        this.fechaAscenso = fechaAscenso;
        this.motivo = motivo;
    }

    public String getNuevoCargo() {
        return nuevoCargo;
    }

    public void setNuevoCargo(String nuevoCargo) {
        this.nuevoCargo = nuevoCargo;
    }

    public LocalDate getFechaAscenso() {
        return fechaAscenso;
    }

    public void setFechaAscenso(LocalDate fechaAscenso) {
        this.fechaAscenso = fechaAscenso;
    }

    public String getMotivo() {
        return motivo;
    }

    public BigDecimal getNuevoSueldo() {
        return nuevoSueldo;
    }

    public void setNuevoSueldo(BigDecimal nuevoSueldo) {
        this.nuevoSueldo = nuevoSueldo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
