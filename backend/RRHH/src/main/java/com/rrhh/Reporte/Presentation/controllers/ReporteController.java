package com.rrhh.Reporte.Presentation.controllers;

import com.rrhh.Reporte.Application.dto.*;
import com.rrhh.Reporte.Application.services.IReporteService;
import com.rrhh.Reporte.Presentation.routes.ReporteApiRoutes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class ReporteController {

    private final IReporteService reporteService;

    public ReporteController(IReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @PostMapping(ReporteApiRoutes.EMPLEADOS)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteEmpleados(
            @RequestBody FiltroEmpleadoDTO filtro,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            return ResponseEntity.ok(reporteService.generarReporteEmpleados(filtro, idUsuario));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(ReporteApiRoutes.ASISTENCIA)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteAsistencia(
            @RequestBody FiltroAsistenciaDTO filtro,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            return ResponseEntity.ok(reporteService.generarReporteAsistencia(filtro, idUsuario));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(ReporteApiRoutes.NOMINA)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteNomina(
            @RequestBody FiltroNominaDTO filtro,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            return ResponseEntity.ok(reporteService.generarReporteNomina(filtro, idUsuario));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(ReporteApiRoutes.SOLICITUDES)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteSolicitudes(
            @RequestBody FiltroSolicitudDTO filtro,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            return ResponseEntity.ok(reporteService.generarReporteSolicitudes(filtro, idUsuario));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}