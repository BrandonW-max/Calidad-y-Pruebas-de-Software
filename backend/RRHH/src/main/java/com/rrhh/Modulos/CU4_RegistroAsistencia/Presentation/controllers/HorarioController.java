package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.HorarioService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.Horario;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes.HorarioApiRoutes;
import com.rrhh.Shared.persistence.HorarioModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(HorarioApiRoutes.BASE)
@PreAuthorize("hasRole('RRHH')")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public ResponseEntity<List<HorarioModel>> listar() {
        return ResponseEntity.ok(horarioService.listar());
    }

    @GetMapping(HorarioApiRoutes.BUSCAR_ID)
    public ResponseEntity<HorarioModel> buscar(@PathVariable Integer id) {
        HorarioModel h = horarioService.buscarPorId(id);
        return h != null ? ResponseEntity.ok(h) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<HorarioModel> guardar(@RequestBody Horario horario) {
        return ResponseEntity.ok(horarioService.guardar(horario));
    }

    @PutMapping(HorarioApiRoutes.BUSCAR_ID)
    public ResponseEntity<HorarioModel> actualizar(@PathVariable Integer id, @RequestBody Horario horario) {
        horario.setIdHorario(id);
        return ResponseEntity.ok(horarioService.guardar(horario));
    }

    @DeleteMapping(HorarioApiRoutes.BUSCAR_ID)
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        horarioService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    /** Asigna (o reasigna) un horario a un empleado. Desactiva la asignación anterior. */
    @PostMapping(HorarioApiRoutes.ASIGNAR)
    public ResponseEntity<?> asignarHorario(@RequestBody Map<String, Object> body) {
        Long      idEmpleado = ((Number) body.get("idEmpleado")).longValue();
        Integer   idHorario  = ((Number) body.get("idHorario")).intValue();
        LocalDate fechaDesde = body.get("fechaDesde") != null
                ? LocalDate.parse((String) body.get("fechaDesde")) : LocalDate.now();
        LocalDate fechaHasta = body.get("fechaHasta") != null
                ? LocalDate.parse((String) body.get("fechaHasta")) : null;
        Boolean esTemporal   = body.get("esTemporal") instanceof Boolean
                ? (Boolean) body.get("esTemporal") : false;

        return ResponseEntity.ok(
                horarioService.asignarHorario(idEmpleado, idHorario, fechaDesde, fechaHasta, esTemporal));
    }

    /** Historial de asignaciones de horario de un empleado. */
    @GetMapping(HorarioApiRoutes.ASIGNACIONES_POR_EMPLEADO)
    public ResponseEntity<List<Map<String, Object>>> asignacionesPorEmpleado(
            @PathVariable Long idEmpleado) {
        return ResponseEntity.ok(horarioService.listarAsignacionesPorEmpleado(idEmpleado));
    }

    /** Empleados asignados actualmente a un horario concreto. */
    @GetMapping(HorarioApiRoutes.EMPLEADOS_POR_HORARIO)
    public ResponseEntity<List<Map<String, Object>>> empleadosPorHorario(@PathVariable Integer id) {
        return ResponseEntity.ok(horarioService.getEmpleadosPorHorario(id));
    }

    /** Resumen de asignaciones activas (una por empleado) para poblar la tabla. */
    @GetMapping(HorarioApiRoutes.ASIGNACIONES_ACTIVAS)
    public ResponseEntity<List<Map<String, Object>>> asignacionesActivas() {
        return ResponseEntity.ok(horarioService.getAsignacionesActivasResumen());
    }
}
