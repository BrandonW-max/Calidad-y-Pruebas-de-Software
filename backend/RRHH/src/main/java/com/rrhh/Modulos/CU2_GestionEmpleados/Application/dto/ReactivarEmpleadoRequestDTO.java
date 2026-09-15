package com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto;

import jakarta.validation.constraints.NotBlank;

public class ReactivarEmpleadoRequestDTO {

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    public ReactivarEmpleadoRequestDTO() {
    }

    public ReactivarEmpleadoRequestDTO(String motivo) {
        this.motivo = motivo;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}