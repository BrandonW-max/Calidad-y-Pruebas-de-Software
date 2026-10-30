package com.rrhh.Modulos.CU3_Nomina.Presentation.controllers;

import com.rrhh.Modulos.CU3_Nomina.Application.services.LiquidacionHorasExtraService;
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
public class HorasExtraDetalleController {

    private final LiquidacionHorasExtraService liquidacionService;

    public HorasExtraDetalleController(LiquidacionHorasExtraService liquidacionService) {
        this.liquidacionService = liquidacionService;
    }

    @GetMapping("/horas-extra-detalle")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<Map<String, Object>>> detalle(@RequestParam Long idEmpleado,
                                                             @RequestParam String periodo) {
        return ResponseEntity.ok(liquidacionService.detalle(idEmpleado, periodo));
    }
}
