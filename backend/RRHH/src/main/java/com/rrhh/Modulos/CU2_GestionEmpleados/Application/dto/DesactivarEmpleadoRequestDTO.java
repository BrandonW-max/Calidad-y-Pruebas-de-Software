package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import jakarta.validation.constraints.NotBlank;

public class DesactivarEmpleadoRequestDTO {

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    public DesactivarEmpleadoRequestDTO() {
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}