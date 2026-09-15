package com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.controllers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.FamiliarRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.PerfilNominaRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.BeneficiosEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.routes.BeneficiosApiRoutes;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(BeneficiosApiRoutes.BASE)
@PreAuthorize("hasRole('RRHH')")
public class BeneficiosEmpleadoController {

    private final BeneficiosEmpleadoService beneficiosEmpleadoService;

    public BeneficiosEmpleadoController(BeneficiosEmpleadoService beneficiosEmpleadoService) {
        this.beneficiosEmpleadoService = beneficiosEmpleadoService;
    }

    @GetMapping
    public Map<String, Object> obtener(@PathVariable Long idEmpleado) {
        return beneficiosEmpleadoService.obtener(idEmpleado);
    }

    @PutMapping
    public Map<String, Object> guardarPerfil(@PathVariable Long idEmpleado, @RequestBody PerfilNominaRequestDTO dto) {
        return beneficiosEmpleadoService.guardarPerfil(idEmpleado, dto);
    }

    @PostMapping(BeneficiosApiRoutes.FAMILIARES)
    public Map<String, Object> agregarFamiliar(@PathVariable Long idEmpleado, @RequestBody FamiliarRequestDTO dto) {
        return beneficiosEmpleadoService.agregarFamiliar(idEmpleado, dto);
    }

    @PutMapping(BeneficiosApiRoutes.FAMILIAR_POR_ID)
    public Map<String, Object> actualizarFamiliar(@PathVariable Long idEmpleado, @PathVariable Integer idFamiliar, @RequestBody FamiliarRequestDTO dto) {
        return beneficiosEmpleadoService.actualizarFamiliar(idEmpleado, idFamiliar, dto);
    }

    @DeleteMapping(BeneficiosApiRoutes.FAMILIAR_POR_ID)
    public ResponseEntity<Void> desactivarFamiliar(@PathVariable Long idEmpleado, @PathVariable Integer idFamiliar) {
        beneficiosEmpleadoService.desactivarFamiliar(idEmpleado, idFamiliar);
        return ResponseEntity.noContent().build();
    }
}
