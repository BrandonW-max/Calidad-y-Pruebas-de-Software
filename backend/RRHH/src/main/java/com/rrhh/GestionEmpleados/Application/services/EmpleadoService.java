package com.rrhh.GestionEmpleados.Application.services;

import com.rrhh.AutenticacionYRol.Application.services.RolAsignacionService;
import com.rrhh.AutenticacionYRol.Infrastructure.interfaces.JpaRolRepository;
import com.rrhh.AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;

import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository;
import com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaDepartamentoRepository;
import com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;

import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.DepartamentoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RolModel;
import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmpleadoService implements IEmpleadoService {

    @Autowired
    private JpaEmpleadoRepository empleadoRepository;

    @Autowired
    private JpaContratoRepository contratoRepository;

    @Autowired
    private JpaDepartamentoRepository departamentoRepository;

    @Autowired
    private JpaRolRepository rolRepository;

    @Autowired
    private JpaUsuarioRepository usuarioRepository;

    @Autowired
    private RolAsignacionService rolAsignacionService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public EmpleadoResponseDTO registrarEmpleado(RegistrarEmpleadoRequestDTO dto) {

        // =========================
        // VALIDAR DUPLICIDAD DNI / CE
        // =========================

        if (empleadoRepository.existsByNumeroDi(dto.getNumeroDi())) {
            return new EmpleadoResponseDTO(
                    false,
                    "Ya existe un empleado con ese documento de identidad",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        // =========================
        // BUSCAR DEPARTAMENTO
        // =========================

        DepartamentoModel departamento = departamentoRepository
                .findById(dto.getIdDpto())
                .orElseThrow(() -> new RuntimeException("Departamento no encontrado"));

        // =========================
        // REGISTRAR EMPLEADO
        // =========================

        EmpleadoModel empleado = new EmpleadoModel();

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

        EmpleadoModel empleadoGuardado =
                empleadoRepository.save(empleado);

        // =========================
        // REGISTRAR CONTRATO
        // =========================

        ContratoModel contrato = new ContratoModel();

        contrato.setEmpleado(empleadoGuardado);
        contrato.setDepartamento(departamento);
        contrato.setCargo(dto.getCargo());
        contrato.setTipoContrato(dto.getTipoContrato());
        contrato.setFechaInicio(dto.getFechaInicio());
        contrato.setFechaFin(dto.getFechaFin());
        contrato.setSueldo(dto.getSueldo());
        contrato.setEstado("ACTIVO");

        contratoRepository.save(contrato);

        // =========================
        // DETERMINAR ROL AUTOMÁTICO
        // =========================

        String nombreRol = rolAsignacionService.determinarRol(
                departamento.getNombre(),
                dto.getCargo()
        );
        System.out.println("==== DEBUG ASIGNACION ROL ====");
        System.out.println("Departamento recibido: " + departamento.getNombre());
        System.out.println("Cargo recibido: " + dto.getCargo());
        System.out.println("Rol calculado: " + nombreRol);
        System.out.println("==============================");

        RolModel rol = rolRepository
                .findByNombreRol(nombreRol)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + nombreRol));

        // =========================
        // CREAR USUARIO DEL SISTEMA
        // =========================

        UsuarioModel usuario = new UsuarioModel();

        usuario.setNombreUsuario(dto.getNombreUsuario());
        usuario.setCorreoInst(dto.getCorreoInst());
        usuario.setContrasenia(
                passwordEncoder.encode(dto.getContrasenia())
        );

        usuario.setEmpleado(empleadoGuardado);
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setDosfaActivo(false);

        usuarioRepository.save(usuario);

        // =========================
        // RESPUESTA
        // =========================

        return new EmpleadoResponseDTO(
                true,
                "Empleado registrado correctamente",
                empleadoGuardado.getIdEmpleado(),
                empleadoGuardado.getNombres() + " " + empleadoGuardado.getApellidos(),
                contrato.getCargo(),
                departamento.getNombre(),
                rol.getNombreRol(),
                empleadoGuardado.getEstado()
        );
    }
}