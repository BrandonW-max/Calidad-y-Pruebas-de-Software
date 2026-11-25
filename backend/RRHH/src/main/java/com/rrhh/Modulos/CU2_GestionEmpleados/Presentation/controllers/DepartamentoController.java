package com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.controllers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DepartamentoDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.routes.DepartamentoApiRoutes;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(DepartamentoApiRoutes.BASE)
public class DepartamentoController {

    private final IDepartamentoRepository departamentoRepository;
    private final JpaContratoRepository jpaContratoRepository;

    public DepartamentoController(IDepartamentoRepository departamentoRepository,
                                  JpaContratoRepository jpaContratoRepository) {
        this.departamentoRepository = departamentoRepository;
        this.jpaContratoRepository = jpaContratoRepository;
    }

    @GetMapping(DepartamentoApiRoutes.CARGOS)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public List<String> cargos(@RequestParam(defaultValue = "") String departamento) {
        return jpaContratoRepository.buscarCargosActivos(departamento);
    }

    @GetMapping(DepartamentoApiRoutes.LISTAR)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public List<DepartamentoDTO> listar() {
        return departamentoRepository.findAll()
                .stream()
                .map(d -> new DepartamentoDTO(d.getIdDpto(), d.getNombre(), d.getCodDpto()))
                .collect(Collectors.toList());
    }
}
