package com.rrhh.GestionEmpleados.Application.services;

import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;

public interface IEmpleadoService {

    EmpleadoResponseDTO registrarEmpleado(
            RegistrarEmpleadoRequestDTO dto
    );
}