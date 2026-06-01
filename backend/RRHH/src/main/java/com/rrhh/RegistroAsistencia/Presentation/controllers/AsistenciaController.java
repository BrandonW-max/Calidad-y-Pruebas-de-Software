package com.rrhh.RegistroAsistencia.Presentation.controllers;

import com.rrhh.RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.RegistroAsistencia.Application.services.AsistenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/asistencia")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @PostMapping("/marcar")
    public ResponseEntity<MarcarAsistenciaResponseDTO> marcarAsistencia(@RequestBody MarcarAsistenciaRequestDTO request) {
        MarcarAsistenciaResponseDTO response = asistenciaService.marcarAsistencia(request);

        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }
}