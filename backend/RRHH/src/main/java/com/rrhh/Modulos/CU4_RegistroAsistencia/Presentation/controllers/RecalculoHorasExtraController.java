package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.RecalculoHorasExtraService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes.AsistenciaApiRoutes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.Map;

@RestController
@RequestMapping(AsistenciaApiRoutes.BASE)
public class RecalculoHorasExtraController {

    private final RecalculoHorasExtraService recalculoService;

    public RecalculoHorasExtraController(RecalculoHorasExtraService recalculoService) {
        this.recalculoService = recalculoService;
    }

    @PostMapping("/recalcular-horas-extra")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<Map<String, Long>> recalcular(
            @RequestParam String mes,
            @RequestParam(defaultValue = "false") boolean aplicar) {
        return ResponseEntity.ok(recalculoService.recalcular(YearMonth.parse(mes), aplicar));
    }
}
