package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto;

import com.rrhh.Shared.persistence.JustificacionTardanzaModel;

import java.time.LocalDateTime;

public class JustificacionResponseDTO {

    private Integer idJustificacion;
    private Integer idRegistroAsistencia;
    private Long idEmpleado;
    private String motivo;
    private String evidenciaUrl;
    private String estado;
    private LocalDateTime fechaJustificacion;
    private Long revisadoPor;
    private LocalDateTime fechaRevision;
    private String comentarioRevision;
    private String horaEntrada;
    private String fechaAsistencia;
    private String horaEntradaEsperada;
    private Integer minutosTardanza;

    public static JustificacionResponseDTO from(JustificacionTardanzaModel m) {
        JustificacionResponseDTO dto = new JustificacionResponseDTO();
        dto.idJustificacion = m.getIdJustificacion();
        dto.idRegistroAsistencia = m.getIdRegistroAsistencia();
        dto.idEmpleado = m.getIdEmpleado();
        dto.motivo = m.getMotivo();
        dto.evidenciaUrl = m.getEvidenciaUrl();
        dto.estado = m.getEstado();
        dto.fechaJustificacion = m.getFechaJustificacion();
        dto.revisadoPor = m.getRevisadoPor();
        dto.fechaRevision = m.getFechaRevision();
        dto.comentarioRevision = m.getComentarioRevision();
        dto.horaEntrada = m.getHoraEntrada();
        dto.fechaAsistencia = m.getFechaAsistencia();
        dto.horaEntradaEsperada = m.getHoraEntradaEsperada();
        dto.minutosTardanza = m.getMinutosTardanza();
        return dto;
    }

    public Integer getIdJustificacion() { return idJustificacion; }
    public Integer getIdRegistroAsistencia() { return idRegistroAsistencia; }
    public Long getIdEmpleado() { return idEmpleado; }
    public String getMotivo() { return motivo; }
    public String getEvidenciaUrl() { return evidenciaUrl; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaJustificacion() { return fechaJustificacion; }
    public Long getRevisadoPor() { return revisadoPor; }
    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public String getComentarioRevision() { return comentarioRevision; }
    public String getHoraEntrada() { return horaEntrada; }
    public String getFechaAsistencia() { return fechaAsistencia; }
    public String getHoraEntradaEsperada() { return horaEntradaEsperada; }
    public Integer getMinutosTardanza() { return minutosTardanza; }
}
