package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.catalogo.CatalogoCargos;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActualizarEmpleadoUseCase {

    private final IEmpleadoRepository empleadoRepository;

    private final IContratoRepository contratoRepository;

    private final IDepartamentoRepository departamentoRepository;

    private final IUsuarioGestionRepository usuarioRepository;

    private final IRolRepository rolRepository;

    private final RolAsignacionService rolAsignacionService;

    private final AuditoriaEmpleadoService auditoriaEmpleadoService;

    private final com.rrhh.Shared.service.CambioSueldoService cambioSueldo;

    public ActualizarEmpleadoUseCase(IEmpleadoRepository empleadoRepository, IContratoRepository contratoRepository, IDepartamentoRepository departamentoRepository, IUsuarioGestionRepository usuarioRepository, IRolRepository rolRepository, RolAsignacionService rolAsignacionService, AuditoriaEmpleadoService auditoriaEmpleadoService, com.rrhh.Shared.service.CambioSueldoService cambioSueldo) {
        this.cambioSueldo = cambioSueldo;
        this.empleadoRepository = empleadoRepository;
        this.contratoRepository = contratoRepository;
        this.departamentoRepository = departamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.rolAsignacionService = rolAsignacionService;
        this.auditoriaEmpleadoService = auditoriaEmpleadoService;
    }

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            ActualizarEmpleadoRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new com.rrhh.Shared.exception.RecursoNoEncontradoException("Empleado no encontrado");
        }

        if (!empleado.puedeSerEditado()) {
            throw new IllegalStateException("No se puede editar un empleado desvinculado");
        }

        String valorAnterior =
                "fechaNac=" + empleado.getFechaNac()
                        + ", sexo=" + empleado.getSexo()
                        + ", estadoCivil=" + empleado.getEstadoCivil()
                        + ", direccion=" + empleado.getDireccion()
                        + ", correo=" + empleado.getCorreo()
                        + ", telefono=" + empleado.getTelefono();

        if (dto.getFechaNac() != null) {
            empleado.setFechaNac(dto.getFechaNac());
        }

        if (dto.getSexo() != null && !dto.getSexo().isBlank()) {
            empleado.setSexo(dto.getSexo());
        }

        if (dto.getEstadoCivil() != null && !dto.getEstadoCivil().isBlank()) {
            empleado.setEstadoCivil(dto.getEstadoCivil());
        }

        if (dto.getDireccion() != null && !dto.getDireccion().isBlank()) {
            empleado.setDireccion(dto.getDireccion());
        }

        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()) {
            empleado.setCorreo(dto.getCorreo());
        }

        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            empleado.setTelefono(dto.getTelefono());
        }

        Empleado empleadoActualizado =
                empleadoRepository.save(empleado);

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        String cambiosContrato = "";

        if (contrato != null) {
            // Solo se valida el par departamento + cargo cuando alguno de los dos cambia
            boolean cambiaDpto = dto.getIdDpto() != null && !dto.getIdDpto().equals(contrato.getIdDpto());
            boolean cambiaCargo = dto.getCargo() != null && !dto.getCargo().isBlank()
                    && !dto.getCargo().equals(contrato.getCargo());

            Departamento dep = null;

            if (cambiaDpto) {
                dep = departamentoRepository.findById(dto.getIdDpto());
                if (dep == null) {
                    throw new com.rrhh.Shared.exception.RecursoNoEncontradoException("Departamento no encontrado");
                }
            }

            String departamento = dep != null ? dep.getNombre() : contrato.getNombreDepartamento();
            String cargo = cambiaCargo ? dto.getCargo() : contrato.getCargo();

            if (cambiaDpto || cambiaCargo) {
                validarCargoDelDepartamento(departamento, cargo);
            }

            if (cambiaDpto) {
                contrato.cambiarDepartamento(dep.getIdDpto(), dep.getNombre());
                cambiosContrato += ", idDpto=" + dto.getIdDpto();
            }

            if (cambiaCargo) {
                contrato.cambiarCargo(cargo);
                cambiosContrato += ", cargo=" + cargo;
            }

            if (dto.getSueldo() != null && dto.getSueldo().compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.math.BigDecimal sueldoAnterior = contrato.getSueldo();
                contrato.cambiarSueldo(dto.getSueldo());
                cambioSueldo.registrar(idEmpleado, java.time.LocalDate.now(), sueldoAnterior, dto.getSueldo(), null);
                cambiosContrato += ", sueldo=" + dto.getSueldo();
            }

            if (dto.getTipoContrato() != null && !dto.getTipoContrato().isBlank()) {
                contrato.setTipoContrato(dto.getTipoContrato());
                cambiosContrato += ", tipoContrato=" + dto.getTipoContrato();
            }

            if (!cambiosContrato.isEmpty()) {
                contratoRepository.save(contrato);
            }

            // El rol depende de departamento y cargo: si cambió alguno, se recalcula
            if (usuario != null && (cambiaDpto || cambiaCargo)) {
                String rolNuevo = actualizarRol(usuario, departamento, cargo);
                cambiosContrato += ", rol=" + rolNuevo;
            }
        }

        String valorNuevo =
                "fechaNac=" + empleadoActualizado.getFechaNac()
                        + ", sexo=" + empleadoActualizado.getSexo()
                        + ", estadoCivil=" + empleadoActualizado.getEstadoCivil()
                        + ", direccion=" + empleadoActualizado.getDireccion()
                        + ", correo=" + empleadoActualizado.getCorreo()
                        + ", telefono=" + empleadoActualizado.getTelefono()
                        + cambiosContrato;

        auditoriaEmpleadoService.registrar(
                empleadoActualizado.getIdEmpleado(),
                "ACTUALIZACION_EMPLEADO",
                "EMPLEADO",
                valorAnterior,
                valorNuevo
        );

        return new EmpleadoResponseDTO(
                true,
                "Empleado actualizado correctamente",
                empleadoActualizado.getIdEmpleado(),
                empleadoActualizado.getNombres() + " " + empleadoActualizado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                empleadoActualizado.getEstado()
        );
    }

    private void validarCargoDelDepartamento(String departamento, String cargo) {
        if (departamento == null || departamento.isBlank()) {
            throw new IllegalArgumentException("El empleado no tiene departamento asignado. Seleccione un departamento.");
        }
        if (!CatalogoCargos.pertenece(departamento, cargo)) {
            throw new IllegalArgumentException("El cargo '" + cargo + "' no pertenece al departamento " + departamento);
        }
    }

    private String actualizarRol(UsuarioGestion usuario, String departamento, String cargo) {
        String nombreRol = rolAsignacionService.determinarRol(departamento, cargo);
        Rol rol = rolRepository.findByNombreRol(nombreRol);
        if (rol == null) {
            throw new com.rrhh.Shared.exception.RecursoNoEncontradoException("Rol no encontrado: " + nombreRol);
        }
        usuario.setIdRol(rol.getIdRol());
        usuario.setNombreRol(rol.getNombreRol());
        usuarioRepository.save(usuario);
        return rol.getNombreRol();
    }
}
