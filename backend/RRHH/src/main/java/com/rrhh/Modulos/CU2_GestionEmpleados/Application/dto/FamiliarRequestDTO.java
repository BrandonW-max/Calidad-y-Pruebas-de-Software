package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.time.LocalDate;

public class FamiliarRequestDTO {
    @NotBlank(message = "Los nombres del familiar son obligatorios")
    public String nombres;
    public String apellidos;
    public String parentesco;
    public String numeroDi;
    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
    public LocalDate fechaNacimiento;
    public Boolean esDerechohabiente;
    public Boolean elegibleAsignacionFamiliar;
    public Boolean validado;
    public LocalDate coberturaDesde;
    public LocalDate coberturaHasta;
}
