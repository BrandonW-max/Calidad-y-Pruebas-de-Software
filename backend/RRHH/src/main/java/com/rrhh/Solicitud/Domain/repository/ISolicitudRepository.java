package com.rrhh.Solicitud.Domain.repository;

import com.rrhh.Solicitud.Domain.entities.Solicitud;

import java.util.List;
import java.util.Optional;

public interface ISolicitudRepository {

    Solicitud guardar(Solicitud solicitud);

    Optional<Solicitud> buscarPorId(Integer idSolicitud);

    List<Solicitud> buscarPorEmpleado(Long idEmpleado);

    List<Solicitud> buscarPorEstado(String estado);

    List<Solicitud> buscarTodas();
}