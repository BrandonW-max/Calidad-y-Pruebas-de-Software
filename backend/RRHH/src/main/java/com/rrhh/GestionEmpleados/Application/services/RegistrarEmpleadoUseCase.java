package com.rrhh.GestionEmpleados.Application.services;

import com.rrhh.AutenticacionYRol.Application.services.RolAsignacionService;

import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;

import com.rrhh.GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.GestionEmpleados.Domain.entities.Departamento;
import com.rrhh.GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.GestionEmpleados.Domain.entities.Rol;
import com.rrhh.GestionEmpleados.Domain.entities.UsuarioGestion;

import com.rrhh.GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarEmpleadoUseCase {

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private IContratoRepository contratoRepository;

    @Autowired
    private IDepartamentoRepository departamentoRepository;

    @Autowired
    private IRolRepository rolRepository;

    @Autowired
    private IUsuarioGestionRepository usuarioRepository;

    @Autowired
    private NombreUsuarioService nombreUsuarioService;

    @Autowired
    private RolAsignacionService rolAsignacionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditoriaEmpleadoService auditoriaEmpleadoService;

    @Transactional
    public EmpleadoResponseDTO ejecutar(RegistrarEmpleadoRequestDTO dto) {

        if (empleadoRepository.existsByNumeroDi(dto.getNumeroDi())) {
            return new EmpleadoResponseDTO(
                    false,
                    "Ya existe un empleado con ese documento de identidad",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        Departamento departamento =
                departamentoRepository.findById(dto.getIdDpto());

        if (departamento == null) {
            throw new RuntimeException("Departamento no encontrado");
        }

        Empleado empleado = new Empleado();

        empleado.setNombres(dto.getNombres());
        empleado.setApellidos(dto.getApellidos());
        empleado.setDocIdentidad(dto.getDocIdentidad());
        empleado.setNumeroDi(dto.getNumeroDi());
        empleado.setFechaNac(dto.getFechaNac());
        empleado.setSexo(dto.getSexo());
        empleado.setEstadoCivil(dto.getEstadoCivil());
        empleado.setDireccion(dto.getDireccion());
        empleado.setCorreo(dto.getCorreo());
        empleado.setTelefono(dto.getTelefono());
        empleado.setEstado("ACTIVO");

        Empleado empleadoGuardado =
                empleadoRepository.save(empleado);

        Contrato contrato = new Contrato();

        contrato.setIdEmpleado(empleadoGuardado.getIdEmpleado());
        contrato.setIdDpto(departamento.getIdDpto());
        contrato.setNombreDepartamento(departamento.getNombre());
        contrato.setCargo(dto.getCargo());
        contrato.setTipoContrato(dto.getTipoContrato());
        contrato.setFechaInicio(dto.getFechaInicio());
        contrato.setFechaFin(dto.getFechaFin());
        contrato.setSueldo(dto.getSueldo());
        contrato.setEstado("ACTIVO");

        Contrato contratoGuardado =
                contratoRepository.save(contrato);

        String nombreRol =
                rolAsignacionService.determinarRol(
                        departamento.getNombre(),
                        dto.getCargo()
                );

        Rol rol =
                rolRepository.findByNombreRol(nombreRol);

        if (rol == null) {
            throw new RuntimeException("Rol no encontrado: " + nombreRol);
        }

        String nombreUsuarioGenerado =
                nombreUsuarioService.generarNombreUsuarioUnico(
                        dto.getNombres(),
                        dto.getApellidos()
                );

        String correoInstitucionalGenerado =
                nombreUsuarioService.generarCorreoInstitucional(
                        nombreUsuarioGenerado
                );

        UsuarioGestion usuario = new UsuarioGestion();

        usuario.setNombreUsuario(nombreUsuarioGenerado);
        usuario.setCorreoInst(correoInstitucionalGenerado);
        usuario.setContrasenia(passwordEncoder.encode(dto.getContrasenia()));
        usuario.setActivo(true);
        usuario.setDosfaActivo(false);
        usuario.setIdEmpleado(empleadoGuardado.getIdEmpleado());
        usuario.setIdRol(rol.getIdRol());
        usuario.setNombreRol(rol.getNombreRol());

        usuarioRepository.save(usuario);

        auditoriaEmpleadoService.registrar(
                empleadoGuardado.getIdEmpleado(),
                "REGISTRO_EMPLEADO",
                "EMPLEADO",
                null,
                "Empleado registrado: "
                        + empleadoGuardado.getNombres()
                        + " "
                        + empleadoGuardado.getApellidos()
                        + " | Usuario: "
                        + nombreUsuarioGenerado
                        + " | Correo institucional: "
                        + correoInstitucionalGenerado
                        + " | Cargo: "
                        + contratoGuardado.getCargo()
                        + " | Departamento: "
                        + departamento.getNombre()
                        + " | Rol: "
                        + rol.getNombreRol()
        );

        return new EmpleadoResponseDTO(
                true,
                "Empleado registrado correctamente",
                empleadoGuardado.getIdEmpleado(),
                empleadoGuardado.getNombres() + " " + empleadoGuardado.getApellidos(),
                nombreUsuarioGenerado,
                correoInstitucionalGenerado,
                contratoGuardado.getCargo(),
                departamento.getNombre(),
                rol.getNombreRol(),
                empleadoGuardado.getEstado()
        );
    }
}