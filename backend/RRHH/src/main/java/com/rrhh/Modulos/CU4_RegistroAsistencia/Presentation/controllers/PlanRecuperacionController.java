package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.PlanRecuperacionConsultaService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes.AsistenciaApiRoutes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(AsistenciaApiRoutes.BASE)
public class PlanRecuperacionController {

    private final PlanRecuperacionConsultaService consultaService;

    public PlanRecuperacionController(PlanRecuperacionConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping("/planes-recuperacion")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<Map<String, Object>>> listar(@RequestParam(defaultValue = "") String estado) {
        return ResponseEntity.ok(consultaService.listar(estado));
    }
}
