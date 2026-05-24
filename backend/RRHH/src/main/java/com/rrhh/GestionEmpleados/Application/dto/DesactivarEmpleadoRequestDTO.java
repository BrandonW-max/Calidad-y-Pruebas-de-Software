package com.rrhh.GestionEmpleados.Application.dto;

public class DesactivarEmpleadoRequestDTO {

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