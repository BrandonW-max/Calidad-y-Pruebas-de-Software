package com.rrhh.GestionEmpleados.Domain.repository;

import com.rrhh.GestionEmpleados.Domain.entities.Rol;

public interface IRolRepository {

    Rol findByNombreRol(String nombreRol);
}