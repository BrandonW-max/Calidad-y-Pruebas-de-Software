package com.rrhh.Solicitud.Application.services;

import com.rrhh.Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Solicitud.Application.dto.SolicitudResponseDTO;

import java.util.List;

public interface ISolicitudService {

    SolicitudResponseDTO registrarSolicitud(RegistrarSolicitudRequestDTO request, Long idEmpleado);

    SolicitudResponseDTO revisarSolicitudRrhh(RevisionRrhhRequestDTO request, Long idUsuarioRrhh);

    SolicitudResponseDTO decidirSolicitudGerencia(DecisionGerenciaRequestDTO request, Long idUsuarioGerencia);

    List<SolicitudResponseDTO> listarPorEstado(String estado);

    List<SolicitudResponseDTO> listarPorEmpleado(Long idEmpleado);

    SolicitudResponseDTO buscarPorId(Integer idSolicitud);
}