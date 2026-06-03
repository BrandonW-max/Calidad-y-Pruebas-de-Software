
package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ReactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Sancion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.ISancionRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReactivarEmpleadoUseCase {

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

    @Autowired
    private JpaUsuarioRepository jpaUsuarioRepository;

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            ReactivarEmpleadoRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (empleado.estaDesvinculado()) {
            return new EmpleadoResponseDTO(
                    false,
                    "No se puede reactivar un empleado desvinculado",
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

        String motivo = "Reactivación manual";

        if (dto != null && dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            motivo = dto.getMotivo();
        }

        List<Sancion> sancionesActivas =
                sancionRepository.findActivasConBloqueoByEmpleado(idEmpleado);

        for (Sancion sancion : sancionesActivas) {
            sancion.finalizar();
            sancionRepository.save(sancion);
        }

        empleado.activar();

        Empleado empleadoGuardado =
                empleadoRepository.save(empleado);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        if (usuario != null) {
            usuario.activar();
            usuarioRepository.save(usuario);

            jpaUsuarioRepository.findByEmpleadoIdEmpleado(idEmpleado).ifPresent(model -> {
                model.setTokenInvalidoDesde(null);
                jpaUsuarioRepository.save(model);
            });
        }

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        auditoriaEmpleadoService.registrar(
                empleadoGuardado.getIdEmpleado(),
                "REACTIVACION_EMPLEADO",
                "EMPLEADO",
                "Estado anterior: " + estadoAnterior,
                "Estado nuevo: ACTIVO | Motivo: " + motivo
        );

        return new EmpleadoResponseDTO(
                true,
                "Empleado reactivado correctamente. Acceso al sistema habilitado.",
                empleadoGuardado.getIdEmpleado(),
                empleadoGuardado.getNombres() + " " + empleadoGuardado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contrato != null ? contrato.getCargo() : null,
                contrato != null ? contrato.getNombreDepartamento() : null,
                usuario != null ? usuario.getNombreRol() : null,
                empleadoGuardado.getEstado()
        );
    }
}