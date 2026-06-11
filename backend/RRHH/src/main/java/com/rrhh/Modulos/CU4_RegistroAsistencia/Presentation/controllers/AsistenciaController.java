package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.AsistenciaService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.JustificacionService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaManualRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.config.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/asistencia")
@CrossOrigin(origins = "*")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final JustificacionService justificacionService;

    public AsistenciaController(AsistenciaService asistenciaService,
                                JustificacionService justificacionService) {
        this.asistenciaService = asistenciaService;
        this.justificacionService = justificacionService;
    }

    @PostMapping("/marcar")
    public ResponseEntity<MarcarAsistenciaResponseDTO> marcarAsistencia(
            @RequestBody MarcarAsistenciaRequestDTO request,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        MarcarAsistenciaResponseDTO response = asistenciaService.marcarAsistencia(request, ip);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/manual")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<MarcarAsistenciaResponseDTO> registrarManual(
            @RequestBody MarcarAsistenciaManualRequestDTO request,
            @AuthenticationPrincipal AuthenticatedUser user,
            HttpServletRequest httpRequest) {
        String ip = httpRequest.getRemoteAddr();
        MarcarAsistenciaResponseDTO response = asistenciaService.registrarManual(request, user.getIdUsuario(), ip);
        if (!response.isSuccess()) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/justificar")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH')")
    public ResponseEntity<?> justificarTardanza(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long idRegistro = Long.valueOf(body.get("idRegistroAsistencia").toString());
        String motivo = body.get("motivo").toString();
        String evidencia = body.getOrDefault("evidenciaUrl", "").toString();
        JustificacionTardanzaModel j = justificacionService.registrar(idRegistro.intValue(), user.getIdUsuario(), motivo, evidencia);
        return ResponseEntity.ok(j);
    }

    @PutMapping("/justificar/{id}/revisar")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> revisarJustificacion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser user) {
        String decision = body.get("decision").toString();
        String comentario = body.getOrDefault("comentario", "").toString();
        JustificacionTardanzaModel j = justificacionService.revisar(id, decision, user.getIdUsuario(), comentario);
        return ResponseEntity.ok(j);
    }

    @GetMapping("/justificaciones/pendientes")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<JustificacionTardanzaModel>> listarPendientes() {
        return ResponseEntity.ok(justificacionService.listarPendientes());
    }

    @GetMapping("/justificaciones/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH')")
    public ResponseEntity<List<JustificacionTardanzaModel>> listarPorEmpleado(
            @PathVariable Long idEmpleado) {
        return ResponseEntity.ok(justificacionService.listarPorEmpleado(idEmpleado));
    }
}
