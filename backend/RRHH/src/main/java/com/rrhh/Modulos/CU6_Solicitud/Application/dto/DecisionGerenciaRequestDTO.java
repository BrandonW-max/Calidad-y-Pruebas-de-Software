package com.rrhh.Modulos.CU6_Solicitud.Application.dto;

import jakarta.validation.constraints.NotNull;

public class DecisionGerenciaRequestDTO {

    @NotNull(message = "El id de la solicitud es obligatorio")
    private Integer idSolicitud;
    private boolean aprobado;
    private String respuesta;

    public DecisionGerenciaRequestDTO() {}

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public boolean isAprobado() { return aprobado; }
    public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
}