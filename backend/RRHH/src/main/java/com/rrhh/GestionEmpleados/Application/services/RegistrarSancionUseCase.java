package com.rrhh.GestionEmpleados.Application.services;

import com.rrhh.GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.GestionEmpleados.Application.dto.RegistrarSancionRequestDTO;
import com.rrhh.GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrarSancionUseCase {

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private ISancionRepository sancionRepository;

    @Autowired
    private IUsuarioGestionRepository usuarioRepository;

    @Autowired
    private IContratoRepository contratoRepository;

    @Autowired
    private AuditoriaEmpleadoService auditoriaEmpleadoService;

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            RegistrarSancionRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (!empleado.puedeSerSancionado()) {
            return new EmpleadoResponseDTO(
                    false,
                    "No se puede registrar sanción a un empleado desvinculado",
                    empleado.getIdEmpleado(),
                    empleado.getNombres() + " " + empleado.getApellidos(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    empleado.getEstado()
            );
        }

        if (dto == null || dto.getMotivo() == null || dto.getMotivo().isBlank()) {
            return new EmpleadoResponseDTO(
                    false,
                    "Debe ingresar un motivo para la sanción",
                    empleado.getIdEmpleado(),
                    empleado.getNombres() + " " + empleado.getApellidos(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    empleado.getEstado()
            );
        }

        String estadoAnterior = empleado.getEstado();

        Sancion sancion = new Sancion();

        sancion.setIdEmpleado(empleado.getIdEmpleado());
        sancion.setMotivo(dto.getMotivo());
        sancion.setDescripcion(dto.getDescripcion());
        sancion.setFechaInicio(dto.getFechaInicio());
        sancion.setFechaFin(dto.getFechaFin());
        sancion.setBloqueaAcceso(Boolean.TRUE.equals(dto.getBloquearAcceso()));
        sancion.setEstado("ACTIVA");

        Sancion sancionGuardada =
                sancionRepository.save(sancion);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        if (usuario != null && Boolean.TRUE.equals(dto.getBloquearAcceso())) {
            usuario.desactivar();
            usuarioRepository.save(usuario);

            empleado.suspender();
            empleado = empleadoRepository.save(empleado);
        }

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        String valorNuevo =
                "Sancion ID: " + sancionGuardada.getIdSancion()
                        + " | Motivo: " + sancionGuardada.getMotivo()
                        + " | Descripción: " + sancionGuardada.getDescripcion()
                        + " | Inicio: " + sancionGuardada.getFechaInicio()
                        + " | Fin: " + sancionGuardada.getFechaFin()
                        + " | Bloquea acceso: " + sancionGuardada.getBloqueaAcceso()
                        + " | Estado empleado nuevo: " + empleado.getEstado();

        auditoriaEmpleadoService.registrar(
                empleado.getIdEmpleado(),
                "SANCION_EMPLEADO",
                "EMPLEADO",
                "Estado anterior: " + estadoAnterior,
                valorNuevo
        );

        return new EmpleadoResponseDTO(
                true,
                "Sanción registrada correctamente",
                empleado.getIdEmpleado(),
                empleado.getNombres() + " " + empleado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                empleado.getEstado()
        );
    }
}