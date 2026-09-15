package com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.controllers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DepartamentoDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.routes.DepartamentoApiRoutes;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(DepartamentoApiRoutes.BASE)
public class DepartamentoController {

    private final IDepartamentoRepository departamentoRepository;

    public DepartamentoController(IDepartamentoRepository departamentoRepository) {
        this.departamentoRepository = departamentoRepository;
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
