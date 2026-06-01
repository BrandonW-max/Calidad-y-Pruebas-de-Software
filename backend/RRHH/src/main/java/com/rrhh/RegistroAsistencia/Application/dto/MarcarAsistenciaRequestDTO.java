package com.rrhh.RegistroAsistencia.Application.dto;

public class MarcarAsistenciaRequestDTO {

    private Integer idEmpleado;
    private boolean huellaValida;

    public MarcarAsistenciaRequestDTO() {
    }

    public Integer getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Integer idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    public boolean isHuellaValida() {
        return huellaValida;
    }

    public void setHuellaValida(boolean huellaValida) {
        this.huellaValida = huellaValida;
    }
}