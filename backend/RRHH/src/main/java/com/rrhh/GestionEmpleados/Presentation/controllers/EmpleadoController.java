
package com.rrhh.GestionEmpleados.Presentation.controllers;

import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Application.services.IEmpleadoService;
import com.rrhh.GestionEmpleados.Presentation.routes.EmpleadoApiRoutes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(EmpleadoApiRoutes.EMPLEADO_BASE)
public class EmpleadoController {

    @Autowired
    private IEmpleadoService empleadoService;

    @PostMapping(EmpleadoApiRoutes.REGISTRAR)
    public EmpleadoResponseDTO registrarEmpleado(
            @RequestBody RegistrarEmpleadoRequestDTO dto
    ) {
        return empleadoService.registrarEmpleado(dto);
    }
}