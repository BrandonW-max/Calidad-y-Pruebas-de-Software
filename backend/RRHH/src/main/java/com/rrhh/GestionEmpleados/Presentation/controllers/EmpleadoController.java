package com.rrhh.GestionEmpleados.Presentation.controllers;

import com.rrhh.GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.BuscarEmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;

import com.rrhh.GestionEmpleados.Application.services.IEmpleadoService;
import com.rrhh.GestionEmpleados.Presentation.routes.EmpleadoApiRoutes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EmpleadoApiRoutes.EMPLEADO_BASE)
public class EmpleadoController {

    @Autowired
    private IEmpleadoService empleadoService;

    @PostMapping(EmpleadoApiRoutes.REGISTRAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarEmpleado(
            @RequestBody RegistrarEmpleadoRequestDTO dto
    ) {
        return empleadoService.registrarEmpleado(dto);
    }

    @GetMapping(EmpleadoApiRoutes.LISTAR)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public List<BuscarEmpleadoResponseDTO> listarEmpleados() {
        return empleadoService.listarEmpleados();
    }

    @GetMapping(EmpleadoApiRoutes.BUSCAR)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public List<BuscarEmpleadoResponseDTO> buscarEmpleados(
            @RequestParam String filtro
    ) {
        return empleadoService.buscarEmpleados(filtro);
    }

    @GetMapping(EmpleadoApiRoutes.BUSCAR_AVANZADO)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public List<BuscarEmpleadoResponseDTO> buscarEmpleadosAvanzado(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer idDpto,
            @RequestParam(required = false) String cargo
    ) {
        return empleadoService.buscarEmpleadosAvanzado(
                estado,
                idDpto,
                cargo
        );
    }

    @PutMapping(EmpleadoApiRoutes.ACTUALIZAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO actualizarEmpleado(
            @PathVariable Long idEmpleado,
            @RequestBody ActualizarEmpleadoRequestDTO dto
    ) {
        return empleadoService.actualizarEmpleado(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.DESACTIVAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO desactivarEmpleado(
            @PathVariable Long idEmpleado,
            @RequestBody DesactivarEmpleadoRequestDTO dto
    ) {
        return empleadoService.desactivarEmpleado(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.SANCION)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarSancion(
            @PathVariable Long idEmpleado,
            @RequestBody RegistrarSancionRequestDTO dto
    ) {
        return empleadoService.registrarSancion(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.REACTIVAR)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO reactivarEmpleado(
            @PathVariable Long idEmpleado,
            @RequestBody ReactivarEmpleadoRequestDTO dto
    ) {
        return empleadoService.reactivarEmpleado(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.ASCENSO)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarAscenso(
            @PathVariable Long idEmpleado,
            @RequestBody RegistrarAscensoRequestDTO dto
    ) {
        return empleadoService.registrarAscenso(idEmpleado, dto);
    }

    @PutMapping(EmpleadoApiRoutes.CAMBIO_SALARIAL)
    @PreAuthorize("hasRole('RRHH')")
    public EmpleadoResponseDTO registrarCambioSalarial(
            @PathVariable Long idEmpleado,
            @RequestBody CambioSalarialRequestDTO dto
    ) {
        return empleadoService.registrarCambioSalarial(idEmpleado, dto);
    }
}