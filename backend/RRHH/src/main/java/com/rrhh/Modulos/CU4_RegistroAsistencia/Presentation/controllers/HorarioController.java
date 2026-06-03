package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.HorarioService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.Horario;
import com.rrhh.Shared.persistence.HorarioModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/horarios")
@CrossOrigin(origins = "*")
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

    @GetMapping("/{id}")
    public ResponseEntity<HorarioModel> buscar(@PathVariable Integer id) {
        HorarioModel h = horarioService.buscarPorId(id);
        return h != null ? ResponseEntity.ok(h) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<HorarioModel> guardar(@RequestBody Horario horario) {
        return ResponseEntity.ok(horarioService.guardar(horario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioModel> actualizar(@PathVariable Integer id, @RequestBody Horario horario) {
        horario.setIdHorario(id);
        return ResponseEntity.ok(horarioService.guardar(horario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        horarioService.eliminar(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/asignar")
    public ResponseEntity<?> asignarHorario(@RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(horarioService.asignarHorario(
                body.get("idEmpleado"), body.get("idHorario")));
    }
}
