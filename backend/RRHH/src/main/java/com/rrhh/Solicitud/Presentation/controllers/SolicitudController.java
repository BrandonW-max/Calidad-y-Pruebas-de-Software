package com.rrhh.Solicitud.Presentation.controllers;

import com.rrhh.Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Solicitud.Application.dto.SolicitudResponseDTO;
import com.rrhh.Solicitud.Application.services.ISolicitudService;
import com.rrhh.Solicitud.Presentation.routes.SolicitudApiRoutes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SolicitudController {

    private final ISolicitudService solicitudService;

    public SolicitudController(ISolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @PostMapping(SolicitudApiRoutes.REGISTRAR)
    @PreAuthorize("hasRole('EMPLEADO')")
    public ResponseEntity<?> registrar(
            @RequestBody RegistrarSolicitudRequestDTO request,
            Authentication authentication) {
        try {
            Long idEmpleado = Long.parseLong(authentication.getName());
            SolicitudResponseDTO response = solicitudService.registrarSolicitud(request, idEmpleado);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(SolicitudApiRoutes.REVISAR_RRHH)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> revisarRrhh(
            @RequestBody RevisionRrhhRequestDTO request,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            SolicitudResponseDTO response = solicitudService.revisarSolicitudRrhh(request, idUsuario);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(SolicitudApiRoutes.DECIDIR_GERENCIA)
    @PreAuthorize("hasRole('GERENCIA')")
    public ResponseEntity<?> decidirGerencia(
            @RequestBody DecisionGerenciaRequestDTO request,
            Authentication authentication) {
        try {
            Long idUsuario = Long.parseLong(authentication.getName());
            SolicitudResponseDTO response = solicitudService.decidirSolicitudGerencia(request, idUsuario);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(SolicitudApiRoutes.POR_ESTADO)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(solicitudService.listarPorEstado(estado));
    }

    @GetMapping(SolicitudApiRoutes.POR_EMPLEADO)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<List<SolicitudResponseDTO>> listarPorEmpleado(@PathVariable Long idEmpleado) {
        return ResponseEntity.ok(solicitudService.listarPorEmpleado(idEmpleado));
    }

    @GetMapping(SolicitudApiRoutes.BUSCAR_ID)
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA', 'EMPLEADO')")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(solicitudService.buscarPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}