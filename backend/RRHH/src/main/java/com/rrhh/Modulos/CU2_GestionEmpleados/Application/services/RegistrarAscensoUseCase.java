package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.catalogo.CatalogoCargos;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.config.RRHHProperties;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarAscensoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final IRolRepository rolRepository;

    private final RolAsignacionService rolAsignacionService;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    private final RRHHProperties rrhhProperties;

    private final com.rrhh.Shared.service.CambioSueldoService cambioSueldo;

    public RegistrarAscensoUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IUsuarioGestionRepository usuarioRepository, IRolRepository rolRepository, RolAsignacionService rolAsignacionService, AuditoriaEmpleadoService auditoriaEmpleadoService, RRHHProperties rrhhProperties, com.rrhh.Shared.service.CambioSueldoService cambioSueldo) {
        this.cambioSueldo = cambioSueldo;
        this.rrhhProperties = rrhhProperties;
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.rolAsignacionService = rolAsignacionService;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            RegistrarAscensoRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new com.rrhh.Shared.exception.RecursoNoEncontradoException("Error: empleado no encontrado");
        }

        if (!empleado.puedeRecibirAscenso()) {
            throw new IllegalStateException("Error: el empleado esta inactivo");
        }

        if (dto == null || dto.getNuevoCargo() == null || dto.getNuevoCargo().isBlank()) {
            throw new IllegalArgumentException("El cargo es obligatorio");
        }

        if (dto.getNuevoSueldo() != null
                && dto.getNuevoSueldo().compareTo(rrhhProperties.getSalarioMinimo()) < 0) {
            throw new IllegalArgumentException("Error: el sueldo debe ser mayor o igual a "
                    + rrhhProperties.getSalarioMinimo().stripTrailingZeros().toPlainString());
        }

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        if (contrato == null) {
            throw new RuntimeException("El empleado no tiene contrato activo");
        }

        // El ascenso no cambia de departamento: el cargo nuevo debe pertenecer al actual
        String departamento = contrato.getNombreDepartamento();
        String nuevoCargo = dto.getNuevoCargo();

        if (departamento == null || departamento.isBlank()) {
            throw new IllegalArgumentException("El empleado no tiene departamento asignado. Asígnele un departamento antes del ascenso.");
        }

        if (nuevoCargo.equals(contrato.getCargo())) {
            throw new IllegalArgumentException("El empleado ya tiene el cargo " + nuevoCargo);
        }

        if (!CatalogoCargos.pertenece(departamento, nuevoCargo)) {
            throw new IllegalArgumentException("El cargo '" + nuevoCargo + "' no pertenece al departamento " + departamento);
        }

        String cargoAnterior = contrato.getCargo();
        String departamentoAnterior = contrato.getNombreDepartamento();

        contrato.cambiarCargo(nuevoCargo);

        BigDecimal sueldoAnterior = contrato.getSueldo();

        if (dto.getNuevoSueldo() != null
                && (sueldoAnterior == null || sueldoAnterior.compareTo(dto.getNuevoSueldo()) != 0)) {
            contrato.cambiarSueldo(dto.getNuevoSueldo());
            cambioSueldo.registrar(idEmpleado,
                    dto.getFechaAscenso() != null ? dto.getFechaAscenso() : java.time.LocalDate.now(),
                    sueldoAnterior, dto.getNuevoSueldo(), null);
        }

        Contrato contratoActualizado =
                contratoRepository.save(contrato);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        String rolAnterior = null;
        String rolNuevo = null;

        if (usuario != null) {

            rolAnterior = usuario.getNombreRol();

            String nombreRolCalculado =
                    rolAsignacionService.determinarRol(
                            departamento,
                            nuevoCargo
                    );

            Rol rol =
                    rolRepository.findByNombreRol(nombreRolCalculado);

            if (rol == null) {
                throw new com.rrhh.Shared.exception.RecursoNoEncontradoException("Rol no encontrado: " + nombreRolCalculado);
            }

            usuario.setIdRol(rol.getIdRol());
            usuario.setNombreRol(rol.getNombreRol());

            usuarioRepository.save(usuario);

            rolNuevo = rol.getNombreRol();
        }

        String motivo = "Sin motivo especificado";

        if (dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            motivo = dto.getMotivo();
        }

        String fechaAscenso = dto.getFechaAscenso() != null
                ? dto.getFechaAscenso().toString()
                : "No especificada";

        String valorAnterior =
                "Cargo anterior: " + cargoAnterior
                        + " | Departamento anterior: " + departamentoAnterior
                        + " | Rol anterior: " + rolAnterior
                        + " | Sueldo anterior: " + sueldoAnterior;

        String valorNuevo =
                "Nuevo cargo: " + contratoActualizado.getCargo()
                        + " | Nuevo departamento: " + contratoActualizado.getNombreDepartamento()
                        + " | Nuevo rol: " + rolNuevo
                        + " | Fecha ascenso: " + fechaAscenso
                        + " | Nuevo sueldo: " + contratoActualizado.getSueldo()
                        + " | Motivo: " + motivo;

        auditoriaEmpleadoService.registrar(
                empleado.getIdEmpleado(),
                "ASCENSO_EMPLEADO",
                "EMPLEADO",
                valorAnterior,
                valorNuevo
        );

        return new EmpleadoResponseDTO(
                true,
                "Ascenso registrado correctamente",
                empleado.getIdEmpleado(),
                empleado.getNombres() + " " + empleado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contratoActualizado.getCargo(),
                contratoActualizado.getNombreDepartamento(),
                rolNuevo,
                empleado.getEstado()
        );
    }
}
