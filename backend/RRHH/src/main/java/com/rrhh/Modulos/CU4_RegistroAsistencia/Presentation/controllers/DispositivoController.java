package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.OtpService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes.DispositivoApiRoutes;
import com.rrhh.Shared.persistence.DispositivoEmpleadoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(DispositivoApiRoutes.BASE)
public class DispositivoController {

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
        String dni = body.get("numeroDi");
        if (dni == null || dni.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "DNI requerido."));
        }

        EmpleadoModel emp = empleadoRepository.findByNumeroDi(dni)
                .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Empleado no encontrado con DNI: " + dni));

        if (!"ACTIVO".equals(emp.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Empleado inactivo o desvinculado."));
        }

        if (emp.getCorreo() == null || emp.getCorreo().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "El empleado no tiene correo registrado."));
        }

        OtpService.OtpResult otp = otpService.generarOtp(dni);
        emailService.enviarCodigoOtp(emp.getCorreo(), emp.getNombres() + " " + emp.getApellidos(), otp.codigo());

        return ResponseEntity.ok(Map.of("sessionId", otp.sessionId()));
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
                    .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Empleado no encontrado con DNI: " + dni));
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
     * Confirma que el DNI corresponde a un empleado activo, previo a enviar el OTP.
     * No devuelve datos personales (nombre/apellido): la app no los usa en este paso
     * y exponerlos permitiría enumerar empleados solo con el DNI.
     */
    @GetMapping(DispositivoApiRoutes.POR_DNI)
    public ResponseEntity<Map<String, Object>> getEmpleadoPorDni(@PathVariable String numeroDi) {
        Optional<EmpleadoModel> opt = empleadoRepository.findByNumeroDi(numeroDi);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        if (!"ACTIVO".equals(opt.get().getEstado())) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(Map.of("existe", true));
    }
}
