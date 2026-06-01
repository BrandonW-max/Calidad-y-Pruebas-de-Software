package com.rrhh.Solicitud.Domain.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Solicitud {

    private Integer idSolicitud;
    private String tipoSolicitud;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer diasSolicitados;
    private String motivo;
    private String estado;
    private String respuesta;
    private String observacionRrhh;
    private String archivoAdjunto;
    private LocalDateTime fechaRevision;
    private LocalDateTime fechaDecision;
    private LocalDateTime creadoEl;
    private LocalDateTime actualizadoEl;

    private Long idEmpleado;
    private Long revisadoPor;
    private Long aprobadoPor;

    public Solicitud() {}

    public Integer getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(Integer idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getTipoSolicitud() { return tipoSolicitud; }
    public void setTipoSolicitud(String tipoSolicitud) { this.tipoSolicitud = tipoSolicitud; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public Integer getDiasSolicitados() { return diasSolicitados; }
    public void setDiasSolicitados(Integer diasSolicitados) { this.diasSolicitados = diasSolicitados; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    public String getObservacionRrhh() { return observacionRrhh; }
    public void setObservacionRrhh(String observacionRrhh) { this.observacionRrhh = observacionRrhh; }

    public String getArchivoAdjunto() { return archivoAdjunto; }
    public void setArchivoAdjunto(String archivoAdjunto) { this.archivoAdjunto = archivoAdjunto; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }

    public LocalDateTime getFechaDecision() { return fechaDecision; }
    public void setFechaDecision(LocalDateTime fechaDecision) { this.fechaDecision = fechaDecision; }

    public LocalDateTime getCreadoEl() { return creadoEl; }
    public void setCreadoEl(LocalDateTime creadoEl) { this.creadoEl = creadoEl; }

    public LocalDateTime getActualizadoEl() { return actualizadoEl; }
    public void setActualizadoEl(LocalDateTime actualizadoEl) { this.actualizadoEl = actualizadoEl; }

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }

    public Long getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(Long revisadoPor) { this.revisadoPor = revisadoPor; }

    public Long getAprobadoPor() { return aprobadoPor; }
    public void setAprobadoPor(Long aprobadoPor) { this.aprobadoPor = aprobadoPor; }

    public static SolicitudBuilder builder() {
        return new SolicitudBuilder();
    }

    private Solicitud(SolicitudBuilder builder) {
        this.idSolicitud = builder.idSolicitud;
        this.tipoSolicitud = builder.tipoSolicitud;
        this.fechaInicio = builder.fechaInicio;
        this.fechaFin = builder.fechaFin;
        this.diasSolicitados = builder.diasSolicitados;
        this.motivo = builder.motivo;
        this.estado = builder.estado;
        this.respuesta = builder.respuesta;
        this.observacionRrhh = builder.observacionRrhh;
        this.archivoAdjunto = builder.archivoAdjunto;
        this.fechaRevision = builder.fechaRevision;
        this.fechaDecision = builder.fechaDecision;
        this.creadoEl = builder.creadoEl;
        this.actualizadoEl = builder.actualizadoEl;
        this.idEmpleado = builder.idEmpleado;
        this.revisadoPor = builder.revisadoPor;
        this.aprobadoPor = builder.aprobadoPor;
    }

    public static final class SolicitudBuilder {
        private Integer idSolicitud;
        private String tipoSolicitud;
        private java.time.LocalDate fechaInicio;
        private java.time.LocalDate fechaFin;
        private Integer diasSolicitados;
        private String motivo;
        private String estado;
        private String respuesta;
        private String observacionRrhh;
        private String archivoAdjunto;
        private java.time.LocalDateTime fechaRevision;
        private java.time.LocalDateTime fechaDecision;
        private java.time.LocalDateTime creadoEl;
        private java.time.LocalDateTime actualizadoEl;
        private Long idEmpleado;
        private Long revisadoPor;
        private Long aprobadoPor;

        public SolicitudBuilder idSolicitud(Integer idSolicitud) {
            this.idSolicitud = idSolicitud;
            return this;
        }

        public SolicitudBuilder tipoSolicitud(String tipoSolicitud) {
            this.tipoSolicitud = tipoSolicitud;
            return this;
        }

        public SolicitudBuilder fechaInicio(java.time.LocalDate fechaInicio) {
            this.fechaInicio = fechaInicio;
            return this;
        }

        public SolicitudBuilder fechaFin(java.time.LocalDate fechaFin) {
            this.fechaFin = fechaFin;
            return this;
        }

        public SolicitudBuilder diasSolicitados(Integer diasSolicitados) {
            this.diasSolicitados = diasSolicitados;
            return this;
        }

        public SolicitudBuilder motivo(String motivo) {
            this.motivo = motivo;
            return this;
        }

        public SolicitudBuilder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public SolicitudBuilder respuesta(String respuesta) {
            this.respuesta = respuesta;
            return this;
        }

        public SolicitudBuilder observacionRrhh(String observacionRrhh) {
            this.observacionRrhh = observacionRrhh;
            return this;
        }

        public SolicitudBuilder archivoAdjunto(String archivoAdjunto) {
            this.archivoAdjunto = archivoAdjunto;
            return this;
        }

        public SolicitudBuilder fechaRevision(java.time.LocalDateTime fechaRevision) {
            this.fechaRevision = fechaRevision;
            return this;
        }

        public SolicitudBuilder fechaDecision(java.time.LocalDateTime fechaDecision) {
            this.fechaDecision = fechaDecision;
            return this;
        }

        public SolicitudBuilder creadoEl(java.time.LocalDateTime creadoEl) {
            this.creadoEl = creadoEl;
            return this;
        }

        public SolicitudBuilder actualizadoEl(java.time.LocalDateTime actualizadoEl) {
            this.actualizadoEl = actualizadoEl;
            return this;
        }

        public SolicitudBuilder idEmpleado(Long idEmpleado) {
            this.idEmpleado = idEmpleado;
            return this;
        }

        public SolicitudBuilder revisadoPor(Long revisadoPor) {
            this.revisadoPor = revisadoPor;
            return this;
        }

        public SolicitudBuilder aprobadoPor(Long aprobadoPor) {
            this.aprobadoPor = aprobadoPor;
            return this;
        }

        public Solicitud build() {
            return new Solicitud(this);
        }
    }
}