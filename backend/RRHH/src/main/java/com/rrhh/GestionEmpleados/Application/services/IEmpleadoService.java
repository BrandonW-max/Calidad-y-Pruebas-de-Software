package com.rrhh.GestionEmpleados.Application.services;

import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.CambioSalarialRequestDTO;

import java.util.List;

public interface IEmpleadoService {

    EmpleadoResponseDTO registrarEmpleado(
            RegistrarEmpleadoRequestDTO dto
    );

    List<BuscarEmpleadoResponseDTO> listarEmpleados();

    List<BuscarEmpleadoResponseDTO> buscarEmpleados(String filtro);
    EmpleadoResponseDTO actualizarEmpleado(
        Long idEmpleado,
        ActualizarEmpleadoRequestDTO dto
    );

    EmpleadoResponseDTO desactivarEmpleado(
            Long idEmpleado,
            DesactivarEmpleadoRequestDTO dto
    );
    EmpleadoResponseDTO registrarSancion(
        Long idEmpleado,
        RegistrarSancionRequestDTO dto
);
EmpleadoResponseDTO reactivarEmpleado(
        Long idEmpleado,
        ReactivarEmpleadoRequestDTO dto
);
EmpleadoResponseDTO registrarAscenso(
        Long idEmpleado,
        RegistrarAscensoRequestDTO dto
);
EmpleadoResponseDTO registrarCambioSalarial(
        Long idEmpleado,
        CambioSalarialRequestDTO dto
);
List<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
        String estado,
        Integer idDpto,
        String cargo
);
}