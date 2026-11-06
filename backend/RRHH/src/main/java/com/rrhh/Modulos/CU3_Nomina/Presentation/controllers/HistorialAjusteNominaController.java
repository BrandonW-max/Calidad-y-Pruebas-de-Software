package com.rrhh.Modulos.CU3_Nomina.Presentation.controllers;

import com.rrhh.Modulos.CU3_Nomina.Application.services.HistorialAjusteNominaConsultaService;
import com.rrhh.Modulos.CU3_Nomina.Presentation.routes.NominaApiRoutes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(NominaApiRoutes.BASE)
public class HistorialAjusteNominaController {

    private final HistorialAjusteNominaConsultaService consultaService;

    public HistorialAjusteNominaController(HistorialAjusteNominaConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    @GetMapping("/historial-ajustes")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<Map<String, Object>>> listar(@RequestParam(defaultValue = "") String periodo) {
        return ResponseEntity.ok(consultaService.listar(periodo));
    }
}
