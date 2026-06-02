package com.rrhh.Reporte.Presentation.controllers;

import com.rrhh.GestionEmpleados.Infrastructure.export.GestionEmpleadosExportFactory;
import com.rrhh.Nomina.Infrastructure.export.NominaExportFactory;
import com.rrhh.RegistroAsistencia.Infrastructure.export.RegistroAsistenciaExportFactory;
import com.rrhh.Reporte.Application.dto.*;
import com.rrhh.Reporte.Application.services.IReporteService;
import com.rrhh.Reporte.Presentation.routes.ReporteApiRoutes;
import com.rrhh.Solicitud.Infrastructure.export.SolicitudExportFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            ReporteResponseDTO dto = reporteService.generarReporteEmpleados(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("EMPLEADOS", formato,
                        dto.getDatos(), GestionEmpleadosExportFactory.HEADERS,
                        GestionEmpleadosExportFactory.KEYS);
                return buildFileResponse(archivo, "reporte_empleados", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(ReporteApiRoutes.ASISTENCIA)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteAsistencia(
            @RequestBody FiltroAsistenciaDTO filtro,
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            ReporteResponseDTO dto = reporteService.generarReporteAsistencia(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("ASISTENCIA", formato,
                        dto.getDatos(), RegistroAsistenciaExportFactory.HEADERS,
                        RegistroAsistenciaExportFactory.KEYS);
                return buildFileResponse(archivo, "reporte_asistencia", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(ReporteApiRoutes.NOMINA)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteNomina(
            @RequestBody FiltroNominaDTO filtro,
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            ReporteResponseDTO dto = reporteService.generarReporteNomina(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("NOMINA", formato,
                        dto.getDatos(), NominaExportFactory.HEADERS,
                        NominaExportFactory.KEYS);
                return buildFileResponse(archivo, "reporte_nomina", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(ReporteApiRoutes.SOLICITUDES)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> reporteSolicitudes(
            @RequestBody FiltroSolicitudDTO filtro,
            @RequestParam(required = false) String formato,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            ReporteResponseDTO dto = reporteService.generarReporteSolicitudes(filtro, idUsuario);
            if (formato != null) {
                byte[] archivo = reporteService.exportarReporte("SOLICITUDES", formato,
                        dto.getDatos(), SolicitudExportFactory.HEADERS,
                        SolicitudExportFactory.KEYS);
                return buildFileResponse(archivo, "reporte_solicitudes", formato);
            }
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] archivo, String nombre, String formato) {
        String extension;
        MediaType mediaType;
        if ("PDF".equalsIgnoreCase(formato)) {
            extension = ".pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            extension = ".xlsx";
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDispositionFormData("attachment", nombre + extension);
        headers.setContentLength(archivo.length);

        return ResponseEntity.ok().headers(headers).body(archivo);
    }
}