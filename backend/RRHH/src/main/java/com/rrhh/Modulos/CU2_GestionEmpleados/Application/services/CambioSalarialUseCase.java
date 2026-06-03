package com.rrhh.Modulos.CU2_GestionEmpleados.Application.services;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.UsuarioGestion;

import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CambioSalarialUseCase {

    @Autowired
    private IEmpleadoRepository empleadoRepository;

    @Autowired
    private IContratoRepository contratoRepository;

    @Autowired
    private IUsuarioGestionRepository usuarioRepository;

    @Autowired
    private AuditoriaEmpleadoService auditoriaEmpleadoService;

    @Value("${rrhh.salario-minimo}")
    private BigDecimal salarioMinimo;

    @Transactional
    public EmpleadoResponseDTO ejecutar(
            Long idEmpleado,
            CambioSalarialRequestDTO dto
    ) {

        Empleado empleado =
                empleadoRepository.findById(idEmpleado);

        if (empleado == null) {
            throw new RuntimeException("Empleado no encontrado");
        }

        if (!empleado.puedeRecibirCambioSalarial()) {
            return new EmpleadoResponseDTO(
                    false,
                    "No se puede registrar cambio salarial a un empleado que no está activo",
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

        if (dto == null || dto.getNuevoSueldo() == null) {
            return new EmpleadoResponseDTO(
                    false,
                    "Debe ingresar el nuevo sueldo",
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

        if (dto.getNuevoSueldo().compareTo(salarioMinimo) < 0) {
            return new EmpleadoResponseDTO(
                    false,
                    "El nuevo sueldo debe ser mayor o igual al salario minimo legal (S/ " + salarioMinimo + ")",
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

        Contrato contrato =
                contratoRepository.findContratoActivoByEmpleado(idEmpleado);

        if (contrato == null) {
            throw new RuntimeException("El empleado no tiene contrato activo");
        }

        BigDecimal sueldoAnterior = contrato.getSueldo();
        BigDecimal sueldoNuevo = dto.getNuevoSueldo();

        if (sueldoAnterior != null && sueldoAnterior.compareTo(sueldoNuevo) == 0) {
            return new EmpleadoResponseDTO(
                    false,
                    "El nuevo sueldo no puede ser igual al sueldo actual",
                    empleado.getIdEmpleado(),
                    empleado.getNombres() + " " + empleado.getApellidos(),
                    null,
                    null,
                    contrato.getCargo(),
                    contrato.getNombreDepartamento(),
                    null,
                    empleado.getEstado()
            );
        }

        contrato.cambiarSueldo(sueldoNuevo);

        Contrato contratoActualizado =
                contratoRepository.save(contrato);

        UsuarioGestion usuario =
                usuarioRepository.findByEmpleadoId(idEmpleado);

        String motivo = "Sin motivo especificado";

        if (dto.getMotivo() != null && !dto.getMotivo().isBlank()) {
            motivo = dto.getMotivo();
        }

        String fechaCambio = dto.getFechaCambio() != null
                ? dto.getFechaCambio().toString()
                : "No especificada";

        String valorAnterior =
                "Sueldo anterior: " + sueldoAnterior
                        + " | Cargo: " + contratoActualizado.getCargo()
                        + " | Departamento: " + contratoActualizado.getNombreDepartamento();

        String valorNuevo =
                "Sueldo nuevo: " + sueldoNuevo
                        + " | Fecha cambio: " + fechaCambio
                        + " | Motivo: " + motivo;

        auditoriaEmpleadoService.registrar(
                empleado.getIdEmpleado(),
                "CAMBIO_SALARIAL",
                "CONTRATO",
                valorAnterior,
                valorNuevo
        );

        return new EmpleadoResponseDTO(
                true,
                "Cambio salarial registrado correctamente",
                empleado.getIdEmpleado(),
                empleado.getNombres() + " " + empleado.getApellidos(),
                usuario != null ? usuario.getNombreUsuario() : null,
                usuario != null ? usuario.getCorreoInst() : null,
                contratoActualizado.getCargo(),
                contratoActualizado.getNombreDepartamento(),
                usuario != null ? usuario.getNombreRol() : null,
                empleado.getEstado()
        );
    }
}