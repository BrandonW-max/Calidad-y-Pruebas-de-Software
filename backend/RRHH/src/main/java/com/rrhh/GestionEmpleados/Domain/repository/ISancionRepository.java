package com.rrhh.GestionEmpleados.Domain.repository;

import com.rrhh.GestionEmpleados.Domain.entities.Sancion;

import java.time.LocalDate;
import java.util.List;

public interface ISancionRepository {

    Sancion save(Sancion sancion);

    List<Sancion> findActivasConBloqueoByEmpleado(Long idEmpleado);

    List<Sancion> findActivasVencidasConBloqueo(LocalDate fechaActual);
}