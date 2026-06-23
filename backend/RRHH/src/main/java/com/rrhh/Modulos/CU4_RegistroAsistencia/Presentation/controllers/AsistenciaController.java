package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.AsistenciaService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.EvidenciaStorageService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.JustificacionService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaManualRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.config.security.AuthenticatedUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/asistencia")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;
    private final JustificacionService justificacionService;
    private final JpaUsuarioRepository usuarioRepository;
    private final JpaDispositivoEmpleadoRepository dispositivoRepository;
    private final EvidenciaStorageService storageService;

    public AsistenciaController(AsistenciaService asistenciaService,
                                JustificacionService justificacionService,
                                JpaUsuarioRepository usuarioRepository,
                                JpaDispositivoEmpleadoRepository dispositivoRepository,
                                EvidenciaStorageService storageService) {
        this.asistenciaService      = asistenciaService;
        this.justificacionService   = justificacionService;
        this.usuarioRepository      = usuarioRepository;
        this.dispositivoRepository  = dispositivoRepository;
        this.storageService         = storageService;
    }

    @PostMapping(value = "/upload-evidencia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH', 'GERENCIA')")
    public ResponseEntity<Map<String, String>> uploadEvidencia(
            @RequestParam("file") MultipartFile file) throws IOException {
        String url = storageService.guardar(file);
        return ResponseEntity.ok(Map.of("url", url));
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

    @PostMapping("/justificar")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH')")
    public ResponseEntity<?> justificarTardanza(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser user) {
        Long idRegistro = Long.valueOf(Objects.requireNonNull(body.get("idRegistroAsistencia"), "idRegistroAsistencia es requerido").toString());
        String motivo = Objects.requireNonNull(body.get("motivo"), "motivo es requerido").toString();
        String evidencia = Objects.toString(body.get("evidenciaUrl"), "");
        Long idEmpleado = Long.valueOf(Objects.requireNonNull(body.get("idEmpleado"), "idEmpleado es requerido").toString());
        validarEmpleadoPropio(user, idEmpleado);
        JustificacionTardanzaModel j = justificacionService.registrar(idRegistro.intValue(), idEmpleado, motivo, evidencia);
        return ResponseEntity.ok(j);
    }

    @PutMapping("/justificar/{id}/revisar")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<?> revisarJustificacion(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal AuthenticatedUser user) {
        String decision = Objects.requireNonNull(body.get("decision"), "decision es requerido").toString();
        String comentario = Objects.toString(body.get("comentario"), "");
        JustificacionTardanzaModel j = justificacionService.revisar(id, decision, user.getIdUsuario(), comentario);
        return ResponseEntity.ok(j);
    }

    @GetMapping("/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<List<RegistroAsistenciaModel>> listarPorEmpleado(
            @PathVariable Integer idEmpleado,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarEmpleadoPropio(user, idEmpleado.longValue());
        return ResponseEntity.ok(asistenciaService.listarPorEmpleado(idEmpleado));
    }

    @GetMapping("/horario/{idEmpleado}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH', 'GERENCIA')")
    public ResponseEntity<Map<String, Object>> getHorarioEmpleado(@PathVariable Integer idEmpleado) {
        Optional<HorarioModel> horarioOpt = asistenciaService.getHorarioEmpleado(idEmpleado);
        if (horarioOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "horaEntrada", "08:00",
                "horaSalida", "17:00",
                "nombreTurno", "Turno General",
                "tolerancia", 10
            ));
        }
        HorarioModel h = horarioOpt.get();
        return ResponseEntity.ok(Map.of(
            "horaEntrada", h.getHoraEntrada().toString().substring(0, 5),
            "horaSalida", h.getHoraSalida().toString().substring(0, 5),
            "nombreTurno", h.getNombreTurno() != null ? h.getNombreTurno() : "Turno General",
            "tolerancia", h.getTolerancia() != null ? h.getTolerancia() : 10
        ));
    }

    @GetMapping("/hoy/{idEmpleado}")
    @PreAuthorize("hasAnyRole('RRHH', 'EMPLEADO')")
    public ResponseEntity<RegistroAsistenciaModel> buscarHoy(
            @PathVariable Integer idEmpleado,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarEmpleadoPropio(user, idEmpleado.longValue());
        Optional<RegistroAsistenciaModel> registro = asistenciaService.buscarHoy(idEmpleado);
        return registro.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.noContent().build());
    }

    /** Estado de asistencia de hoy para la app móvil (sin cookie de sesión) */
    @GetMapping("/biometrico/estado/{idEmpleado}")
    public ResponseEntity<Map<String, Object>> estadoBiometrico(
            @PathVariable Long idEmpleado,
            @RequestParam String deviceToken) {
        boolean valido = dispositivoRepository.existsByEmpleadoIdEmpleadoAndDeviceToken(idEmpleado, deviceToken);
        if (!valido) return ResponseEntity.status(403).build();

        Optional<RegistroAsistenciaModel> reg = asistenciaService.buscarHoy(idEmpleado.intValue());
        boolean tieneEntrada = reg.isPresent() && reg.get().getHoraEntrada() != null;
        boolean tieneSalida  = reg.isPresent() && reg.get().getHoraSalida()  != null;

        Map<String, Object> estado = new java.util.HashMap<>();
        estado.put("tieneEntrada", tieneEntrada);
        estado.put("tieneSalida",  tieneSalida);
        estado.put("horaEntrada",  tieneEntrada ? reg.get().getHoraEntrada().toString().substring(0, 5) : null);
        estado.put("horaSalida",   tieneSalida  ? reg.get().getHoraSalida().toString().substring(0, 5)  : null);
        estado.put("estado",       reg.map(RegistroAsistenciaModel::getEstado).orElse(null));
        return ResponseEntity.ok(estado);
    }

    @PostMapping("/biometrico")
    public ResponseEntity<MarcarAsistenciaResponseDTO> marcarBiometrico(
            @RequestBody Map<String, Object> body,
            HttpServletRequest httpRequest) {
        Long idEmpleado  = Long.valueOf(Objects.requireNonNull(body.get("idEmpleado")).toString());
        String token     = Objects.requireNonNull(body.get("deviceToken")).toString();

        boolean valido = dispositivoRepository.existsByEmpleadoIdEmpleadoAndDeviceToken(idEmpleado, token);
        if (!valido) {
            return ResponseEntity.status(403)
                    .body(new MarcarAsistenciaResponseDTO(false, "Dispositivo no autorizado."));
        }

        // Bloquear si ya tiene entrada Y salida (registro del día completado)
        Optional<RegistroAsistenciaModel> reg = asistenciaService.buscarHoy(idEmpleado.intValue());
        if (reg.isPresent() && reg.get().getHoraEntrada() != null && reg.get().getHoraSalida() != null) {
            return ResponseEntity.badRequest()
                    .body(new MarcarAsistenciaResponseDTO(false, "Ya tienes entrada y salida registradas hoy."));
        }

        MarcarAsistenciaRequestDTO req = new MarcarAsistenciaRequestDTO();
        req.setIdEmpleado(idEmpleado.intValue());

        MarcarAsistenciaResponseDTO response = asistenciaService.marcarAsistencia(req, httpRequest.getRemoteAddr());
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
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

    @GetMapping("/justificaciones/pendientes")
    @PreAuthorize("hasRole('RRHH')")
    public ResponseEntity<List<JustificacionTardanzaModel>> listarPendientes() {
        return ResponseEntity.ok(justificacionService.listarPendientes());
    }

    @GetMapping("/justificaciones/empleado/{idEmpleado}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'RRHH')")
    public ResponseEntity<List<JustificacionTardanzaModel>> listarPorEmpleado(
            @PathVariable Long idEmpleado,
            @AuthenticationPrincipal AuthenticatedUser user) {
        validarEmpleadoPropio(user, idEmpleado);
        return ResponseEntity.ok(justificacionService.listarPorEmpleado(idEmpleado));
    }

    private void validarEmpleadoPropio(AuthenticatedUser user, Long idEmpleado) {
        if (user == null || !"EMPLEADO".equals(user.getRol())) return;

        UsuarioModel usuario = usuarioRepository.findById(Objects.requireNonNull(user.getIdUsuario()))
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException("Usuario no encontrado."));
        if (usuario.getEmpleado() == null
                || !idEmpleado.equals(usuario.getEmpleado().getIdEmpleado())) {
            throw new org.springframework.security.access.AccessDeniedException("No puede acceder a la asistencia de otro empleado.");
        }
    }
}
