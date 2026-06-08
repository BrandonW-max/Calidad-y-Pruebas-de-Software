package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;

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

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.factory.IContratoFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rrhh.Shared.security.HashService;

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
    private HashService hashService;

    @Autowired
    private AuditoriaEmpleadoService auditoriaEmpleadoService;

    @Autowired
    private IContratoFactory contratoFactory;

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

        Contrato contrato = contratoFactory.crearIndefinido(
                empleadoGuardado.getIdEmpleado(),
                departamento.getIdDpto(),
                departamento.getNombre(),
                dto.getCargo(),
                dto.getFechaInicio(),
                dto.getFechaFin(),
                dto.getSueldo()
        );

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
        usuario.setContrasenia(hashService.encriptar(dto.getNumeroDi()));
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
                        + contrato.getCargo()
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
                contrato.getCargo(),
                departamento.getNombre(),
                rol.getNombreRol(),
                empleadoGuardado.getEstado()
        );
    }
}
