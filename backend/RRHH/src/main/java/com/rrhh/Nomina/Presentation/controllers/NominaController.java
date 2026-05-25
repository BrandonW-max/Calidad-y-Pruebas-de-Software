package com.rrhh.Nomina.Presentation.controllers;

import com.rrhh.Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Nomina.Application.services.INominaService;
import com.rrhh.Nomina.Presentation.routes.NominaApiRoutes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class NominaController {

    private final INominaService nominaService;

    public NominaController(INominaService nominaService) {
        this.nominaService = nominaService;
    }

    @PostMapping(NominaApiRoutes.CALCULAR)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> calcularNomina(
            @RequestBody CalcularNominaRequestDTO request,
            Authentication authentication) {
        try {
            Long idUsuario = obtenerIdUsuario(authentication);
            List<NominaResponseDTO> resultado = nominaService.calcularNomina(request, idUsuario);
            return ResponseEntity.ok(resultado);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping(NominaApiRoutes.BUSCAR_ID)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        try {
            NominaResponseDTO nomina = nominaService.buscarNominaPorId(id);
            return ResponseEntity.ok(nomina);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(NominaApiRoutes.POR_PERIODO)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<NominaResponseDTO>> buscarPorPeriodo(
            @PathVariable String periodo) {
        return ResponseEntity.ok(nominaService.buscarPorPeriodo(periodo));
    }

    @GetMapping(NominaApiRoutes.POR_EMPLEADO)
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<List<NominaResponseDTO>> buscarPorEmpleado(
            @PathVariable Long idEmpleado) {
        return ResponseEntity.ok(nominaService.buscarPorEmpleado(idEmpleado));
    }

    @GetMapping(NominaApiRoutes.POR_NOMBRE)
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> buscarPorNombre(@RequestParam String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return ResponseEntity.badRequest().body("El parámetro 'nombre' es obligatorio.");
        }
        List<NominaResponseDTO> resultados = nominaService.buscarPorNombreEmpleado(nombre);
        return ResponseEntity.ok(resultados);
    }

    private Long obtenerIdUsuario(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}