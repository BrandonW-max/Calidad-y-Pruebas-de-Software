package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarAscensoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.RegistrarAscensoUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.config.RRHHProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RegistrarAscensoUseCase - cargo del mismo departamento y sueldo opcional")
class RegistrarAscensoUseCaseTest {

    private static final Long ID_EMPLEADO = 3L;
    private static final String DEPARTAMENTO = "Medicina";
    private static final String CARGO_ACTUAL = "Médico General";
    private static final String CARGO_VALIDO = "Médico Especialista";
    private static final BigDecimal SUELDO_ACTUAL = new BigDecimal("2000.00");
    private static final BigDecimal SUELDO_MINIMO = new BigDecimal("1130.00");

    @Mock IEmpleadoRepository empleadoRepository;
    @Mock IContratoRepository contratoRepository;
    @Mock IUsuarioGestionRepository usuarioRepository;
    @Mock IRolRepository rolRepository;
    @Mock RolAsignacionService rolAsignacionService;
    @Mock AuditoriaEmpleadoService auditoriaEmpleadoService;
    @Mock RRHHProperties rrhhProperties;
    @Mock Empleado empleado;
    @Mock Contrato contrato;
    @Mock com.rrhh.Shared.service.CambioSueldoService cambioSueldoService;

    private RegistrarAscensoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarAscensoUseCase(
                empleadoRepository, contratoRepository, usuarioRepository,
                rolRepository, rolAsignacionService, auditoriaEmpleadoService, rrhhProperties, cambioSueldoService);

        when(rrhhProperties.getSalarioMinimo()).thenReturn(SUELDO_MINIMO);
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(empleado.puedeRecibirAscenso()).thenReturn(true);
        when(contratoRepository.findContratoActivoByEmpleado(ID_EMPLEADO)).thenReturn(contrato);
        when(contrato.getCargo()).thenReturn(CARGO_ACTUAL);
        when(contrato.getNombreDepartamento()).thenReturn(DEPARTAMENTO);
        when(contrato.getSueldo()).thenReturn(SUELDO_ACTUAL);
        when(contratoRepository.save(contrato)).thenReturn(contrato);
    }

    private RegistrarAscensoRequestDTO dto(String cargo) {
        RegistrarAscensoRequestDTO dto = new RegistrarAscensoRequestDTO();
        dto.setNuevoCargo(cargo);
        dto.setFechaAscenso(LocalDate.of(2026, 10, 27));
        return dto;
    }

    @Test
    @DisplayName("Ascenso a un cargo del mismo departamento con nuevo sueldo → cambia cargo y sueldo")
    void ascenso_CargoDelMismoDepartamento_CambiaCargoYSueldo() {
        RegistrarAscensoRequestDTO dto = dto(CARGO_VALIDO);
        dto.setNuevoSueldo(new BigDecimal("2500.00"));

        useCase.ejecutar(ID_EMPLEADO, dto);

        verify(contrato).cambiarCargo(CARGO_VALIDO);
        verify(contrato).cambiarSueldo(new BigDecimal("2500.00"));
    }

    @Test
    @DisplayName("Ascenso sin nuevo sueldo → el sueldo del contrato no cambia")
    void ascenso_SinNuevoSueldo_MantieneSueldo() {
        useCase.ejecutar(ID_EMPLEADO, dto(CARGO_VALIDO));

        verify(contrato, never()).cambiarSueldo(any());
    }

    @Test
    @DisplayName("Ascenso con sueldo menor al mínimo legal → se rechaza sin guardar")
    void ascenso_SueldoMenorAlMinimo_Rechaza() {
        RegistrarAscensoRequestDTO dto = dto(CARGO_VALIDO);
        dto.setNuevoSueldo(new BigDecimal("500.00"));

        assertThrows(IllegalArgumentException.class, () -> useCase.ejecutar(ID_EMPLEADO, dto));

        verify(contrato, never()).cambiarSueldo(any());
        verify(contratoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ascenso a un cargo de otro departamento → se rechaza sin guardar")
    void ascenso_CargoDeOtroDepartamento_Rechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.ejecutar(ID_EMPLEADO, dto("Analista RRHH")));

        verify(contrato, never()).cambiarCargo(anyString());
        verify(contratoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Empleado sin departamento asignado → no puede recibir ascenso")
    void ascenso_EmpleadoSinDepartamento_Rechaza() {
        when(contrato.getNombreDepartamento()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> useCase.ejecutar(ID_EMPLEADO, dto(CARGO_VALIDO)));

        verify(contratoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ascenso al mismo cargo que ya tiene → se rechaza")
    void ascenso_MismoCargoActual_Rechaza() {
        assertThrows(IllegalArgumentException.class,
                () -> useCase.ejecutar(ID_EMPLEADO, dto(CARGO_ACTUAL)));

        verify(contratoRepository, never()).save(any());
    }
}
