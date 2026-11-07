package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.OtpService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes.DispositivoApiRoutes;
import com.rrhh.Shared.persistence.DispositivoEmpleadoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(DispositivoApiRoutes.BASE)
public class DispositivoController {

    private static final Logger log = LoggerFactory.getLogger(DispositivoController.class);

    private final JpaDispositivoEmpleadoRepository dispositivoRepository;
    private final JpaEmpleadoRepository empleadoRepository;
    private final OtpService otpService;
    private final EmailService emailService;

    public DispositivoController(JpaDispositivoEmpleadoRepository dispositivoRepository,
                                 JpaEmpleadoRepository empleadoRepository,
                                 OtpService otpService,
                                 EmailService emailService) {
        this.dispositivoRepository = dispositivoRepository;
        this.empleadoRepository    = empleadoRepository;
        this.otpService            = otpService;
        this.emailService          = emailService;
    }

    /** Envía un código OTP al correo del empleado para verificar su identidad */
    @PostMapping(DispositivoApiRoutes.ENVIAR_OTP)
    public ResponseEntity<Map<String, Object>> enviarOtp(@RequestBody Map<String, String> body) {
        String dni = body != null ? body.get("numeroDi") : null;
        if (dni == null || dni.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "DNI requerido."));
        }
        if (!dni.matches("^\\d{8}$")) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Formato de DNI inválido. Debe contener 8 dígitos numéricos."));
        }

        Optional<EmpleadoModel> empOpt = empleadoRepository.findByNumeroDi(dni);

        // Anti-enumeración de empleados (CWE-203 / CWE-862):
        // Respuesta uniforme e indistinguible tanto si el empleado existe como si no existe o está inactivo.
        if (empOpt.isEmpty() || !"ACTIVO".equals(empOpt.get().getEstado())
                || empOpt.get().getCorreo() == null || empOpt.get().getCorreo().isBlank()) {
            return ResponseEntity.ok(Map.of(
                    "sessionId", java.util.UUID.randomUUID().toString(),
                    "mensaje", "Si el documento corresponde a un empleado activo, se envió un código OTP a su correo corporativo."
            ));
        }

        EmpleadoModel emp = empOpt.get();
        OtpService.OtpResult otp = otpService.generarOtp(dni);
        try {
            emailService.enviarCodigoOtp(emp.getCorreo(), emp.getNombres() + " " + emp.getApellidos(), otp.codigo());
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo OTP: {}", e.getMessage());
        }

        return ResponseEntity.ok(Map.of(
                "sessionId", otp.sessionId(),
                "mensaje", "Si el documento corresponde a un empleado activo, se envió un código OTP a su correo corporativo."
        ));
    }

    /** Vincula o actualiza el celular de un empleado, previa verificación OTP */
    @PostMapping(DispositivoApiRoutes.REGISTRAR)
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Map<String, Object> body) {
        String token = body.get("deviceToken").toString();
        String sessionId = body.getOrDefault("sessionId", "").toString();
        String codigo = body.getOrDefault("codigo", "").toString();

        if (sessionId.isBlank() || codigo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "sessionId y codigo son requeridos."));
        }

        OtpService.VerificacionResult verificacion = otpService.verificarOtp(sessionId, codigo);
        switch (verificacion) {
            case INVALIDO:
                return ResponseEntity.status(401).body(Map.of("mensaje", "Código OTP inválido."));
            case EXPIRADO_O_INEXISTENTE:
                return ResponseEntity.status(401).body(Map.of("mensaje", "Código OTP expirado o inválido. Solicita uno nuevo."));
            case SIN_INTENTOS:
                return ResponseEntity.status(429).body(Map.of("mensaje", "Demasiados intentos fallidos. Solicita un nuevo código."));
            case OK:
                break;
        }

        EmpleadoModel emp;
        if (body.containsKey("numeroDi")) {
            String dni = body.get("numeroDi").toString();
            emp = empleadoRepository.findByNumeroDi(dni)
                    .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("No se encontró un empleado activo con el documento indicado."));
        } else {
            Long idEmpleado = Long.valueOf(body.get("idEmpleado").toString());
            emp = empleadoRepository.findById(java.util.Objects.requireNonNull(idEmpleado))
                    .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Empleado no encontrado"));
        }

        if (!"ACTIVO".equals(emp.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Empleado inactivo o desvinculado."));
        }

        DispositivoEmpleadoModel dispositivo = dispositivoRepository
                .findByEmpleadoIdEmpleado(emp.getIdEmpleado())
                .orElse(new DispositivoEmpleadoModel());

        dispositivo.setEmpleado(emp);
        dispositivo.setDeviceToken(token);
        dispositivoRepository.save(dispositivo);

        return ResponseEntity.ok(Map.of(
                "mensaje",    "Dispositivo registrado correctamente.",
                "idEmpleado", emp.getIdEmpleado(),
                "nombres",    emp.getNombres(),
                "apellidos",  emp.getApellidos()
        ));
    }

    /**
     * Consulta interna/administrativa para verificar si un DNI corresponde a un empleado activo.
     * Restringido exclusivamente a roles con privilegios (RRHH, GERENCIA).
     * No devuelve datos personales del empleado.
     */
    @PreAuthorize("hasAnyRole('RRHH', 'GERENCIA')")
    @GetMapping(DispositivoApiRoutes.POR_DNI)
    public ResponseEntity<Map<String, Object>> getEmpleadoPorDni(@PathVariable String numeroDi) {
        if (numeroDi == null || !numeroDi.matches("^\\d{8}$")) {
            log.warn("Consulta de DNI rechazada por formato inválido: {}", enmascararDni(numeroDi));
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Formato de DNI inválido. Debe contener 8 dígitos numéricos."));
        }

        Optional<EmpleadoModel> opt = empleadoRepository.findByNumeroDi(numeroDi);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (!"ACTIVO".equals(opt.get().getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Empleado inactivo o desvinculado."));
        }

        return ResponseEntity.ok(Map.of("existe", true));
    }

    private String enmascararDni(String dni) {
        if (dni == null || dni.length() < 4) return "****";
        return dni.substring(0, 2) + "****" + dni.substring(dni.length() - 2);
    }
}
