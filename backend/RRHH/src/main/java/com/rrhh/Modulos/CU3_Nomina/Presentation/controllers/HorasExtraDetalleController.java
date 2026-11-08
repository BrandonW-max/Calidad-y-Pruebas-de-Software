package com.rrhh.Modulos.CU3_Nomina.Presentation.controllers;

import com.rrhh.Modulos.CU3_Nomina.Application.services.LiquidacionHorasExtraService;
import com.rrhh.Modulos.CU3_Nomina.Presentation.routes.NominaApiRoutes;
import com.rrhh.Shared.config.RRHHProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(HorasExtraDetalleController.class);

    private final LiquidacionHorasExtraService liquidacionService;
    private final RRHHProperties rrhhProperties;

    public HorasExtraDetalleController(LiquidacionHorasExtraService liquidacionService, RRHHProperties rrhhProperties) {
        this.liquidacionService = liquidacionService;
        this.rrhhProperties = rrhhProperties;
    }

    @GetMapping("/horas-extra-detalle")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<Map<String, Object>>> detalle(@RequestParam Long idEmpleado,
                                                             @RequestParam String periodo) {
        log.debug("horas-extra-detalle idEmpleado={} periodo={}", idEmpleado, periodo);
        return ResponseEntity.ok(liquidacionService.detalle(idEmpleado, periodo));
    }

    // Umbral de horas dobles configurado (RNF46): la vista previa lo usa para calcular igual que el backend
    @GetMapping("/horas-extra-umbral")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<Map<String, Object>> umbral() {
        return ResponseEntity.ok(Map.of("umbralHorasDobles", rrhhProperties.getUmbralHorasDobles()));
    }
}
