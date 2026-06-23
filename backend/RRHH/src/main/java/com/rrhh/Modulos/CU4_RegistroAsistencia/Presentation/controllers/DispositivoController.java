package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import com.rrhh.Shared.persistence.DispositivoEmpleadoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/dispositivos")
public class DispositivoController {

    private final JpaDispositivoEmpleadoRepository dispositivoRepository;
    private final JpaEmpleadoRepository empleadoRepository;

    public DispositivoController(JpaDispositivoEmpleadoRepository dispositivoRepository,
                                 JpaEmpleadoRepository empleadoRepository) {
        this.dispositivoRepository = dispositivoRepository;
        this.empleadoRepository    = empleadoRepository;
    }

    /** Vincula o actualiza el celular de un empleado (acepta idEmpleado o numeroDi) */
    @PostMapping("/registrar")
    public ResponseEntity<Map<String, Object>> registrar(@RequestBody Map<String, Object> body) {
        String token = body.get("deviceToken").toString();

        EmpleadoModel emp;
        if (body.containsKey("numeroDi")) {
            String dni = body.get("numeroDi").toString();
            emp = empleadoRepository.findByNumeroDi(dni)
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado con DNI: " + dni));
        } else {
            Long idEmpleado = Long.valueOf(body.get("idEmpleado").toString());
            emp = empleadoRepository.findById(java.util.Objects.requireNonNull(idEmpleado))
                    .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));
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

    /** Busca empleado por DNI — usado por la app móvil al registrarse */
    @GetMapping("/dni/{numeroDi}")
    public ResponseEntity<Map<String, Object>> getEmpleadoPorDni(@PathVariable String numeroDi) {
        Optional<EmpleadoModel> opt = empleadoRepository.findByNumeroDi(numeroDi);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmpleadoModel e = opt.get();
        if (!"ACTIVO".equals(e.getEstado())) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(Map.of(
                "idEmpleado", e.getIdEmpleado(),
                "nombres",    e.getNombres(),
                "apellidos",  e.getApellidos()
        ));
    }

    /** La app consulta datos del empleado para mostrar su nombre en la pantalla */
    @GetMapping("/empleado/{idEmpleado}")
    public ResponseEntity<Map<String, Object>> getEmpleado(@PathVariable Long idEmpleado) {
        Optional<EmpleadoModel> opt = empleadoRepository.findById(java.util.Objects.requireNonNull(idEmpleado));
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        EmpleadoModel e = opt.get();
        return ResponseEntity.ok(Map.of(
                "idEmpleado", e.getIdEmpleado(),
                "nombres",    e.getNombres(),
                "apellidos",  e.getApellidos(),
                "estado",     e.getEstado() != null ? e.getEstado() : "ACTIVO"
        ));
    }
}
