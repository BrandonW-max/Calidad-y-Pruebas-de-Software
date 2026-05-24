package com.rrhh.GestionEmpleados.Application.services;

import com.rrhh.GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpleadoService implements IEmpleadoService {

    @Autowired
    private RegistrarEmpleadoUseCase registrarEmpleadoUseCase;

    @Autowired
    private BuscarEmpleadoUseCase buscarEmpleadoUseCase;

    @Autowired
    private ActualizarEmpleadoUseCase actualizarEmpleadoUseCase;

    @Autowired
    private DesactivarEmpleadoUseCase desactivarEmpleadoUseCase;

    @Autowired
    private RegistrarSancionUseCase registrarSancionUseCase;

    @Autowired
    private ReactivarEmpleadoUseCase reactivarEmpleadoUseCase;

    @Autowired
    private RegistrarAscensoUseCase registrarAscensoUseCase;

    @Autowired
    private CambioSalarialUseCase cambioSalarialUseCase;

    @Override
    public EmpleadoResponseDTO registrarEmpleado(
            RegistrarEmpleadoRequestDTO dto
    ) {
        return registrarEmpleadoUseCase.ejecutar(dto);
    }

    @Override
    public List<BuscarEmpleadoResponseDTO> listarEmpleados() {
        return buscarEmpleadoUseCase.listar();
    }

    @Override
    public List<BuscarEmpleadoResponseDTO> buscarEmpleados(
            String filtro
    ) {
        return buscarEmpleadoUseCase.buscarPorFiltro(filtro);
    }

    @Override
    public List<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
            String estado,
            Integer idDpto,
            String cargo
    ) {
        return buscarEmpleadoUseCase.buscarAvanzado(
                estado,
                idDpto,
                cargo
        );
    }

    @Override
    public EmpleadoResponseDTO actualizarEmpleado(
            Long idEmpleado,
            ActualizarEmpleadoRequestDTO dto
    ) {
        return actualizarEmpleadoUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO desactivarEmpleado(
            Long idEmpleado,
            DesactivarEmpleadoRequestDTO dto
    ) {
        return desactivarEmpleadoUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO registrarSancion(
            Long idEmpleado,
            RegistrarSancionRequestDTO dto
    ) {
        return registrarSancionUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO reactivarEmpleado(
            Long idEmpleado,
            ReactivarEmpleadoRequestDTO dto
    ) {
        return reactivarEmpleadoUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO registrarAscenso(
            Long idEmpleado,
            RegistrarAscensoRequestDTO dto
    ) {
        return registrarAscensoUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }

    @Override
    public EmpleadoResponseDTO registrarCambioSalarial(
            Long idEmpleado,
            CambioSalarialRequestDTO dto
    ) {
        return cambioSalarialUseCase.ejecutar(
                idEmpleado,
                dto
        );
    }
}