package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class RegistroAsistenciaResponseDTO {

    private Integer idRegistro;
    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private BigDecimal horasTrabajadas;
    private Integer minutosTardanza;
    private Integer minutosExtra;
    private String estado;
    private String tipoUltimoRegistro;
    private String tipoRegistro;
    private String observacion;
    private Long idEmpleado;

    public static RegistroAsistenciaResponseDTO from(RegistroAsistenciaModel m) {
        RegistroAsistenciaResponseDTO dto = new RegistroAsistenciaResponseDTO();
        dto.idRegistro = m.getIdRegistro();
        dto.fecha = m.getFecha();
        dto.horaEntrada = m.getHoraEntrada();
        dto.horaSalida = m.getHoraSalida();
        dto.horasTrabajadas = m.getHorasTrabajadas();
        dto.minutosTardanza = m.getMinutosTardanza();
        dto.minutosExtra = m.getMinutosExtra();
        dto.estado = m.getEstado();
        dto.tipoUltimoRegistro = m.getTipoUltimoRegistro();
        dto.tipoRegistro = m.getTipoRegistro();
        dto.observacion = m.getObservacion();
        dto.idEmpleado = m.getEmpleado() != null ? m.getEmpleado().getIdEmpleado() : null;
        return dto;
    }

    public Integer getIdRegistro() { return idRegistro; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraEntrada() { return horaEntrada; }
    public LocalTime getHoraSalida() { return horaSalida; }
    public BigDecimal getHorasTrabajadas() { return horasTrabajadas; }
    public Integer getMinutosTardanza() { return minutosTardanza; }
    public Integer getMinutosExtra() { return minutosExtra; }
    public String getEstado() { return estado; }
    public String getTipoUltimoRegistro() { return tipoUltimoRegistro; }
    public String getTipoRegistro() { return tipoRegistro; }
    public String getObservacion() { return observacion; }
    public Long getIdEmpleado() { return idEmpleado; }
}
