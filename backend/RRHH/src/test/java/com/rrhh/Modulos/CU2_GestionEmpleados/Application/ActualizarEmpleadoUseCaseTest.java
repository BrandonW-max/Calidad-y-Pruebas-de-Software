package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.ActualizarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.ActualizarEmpleadoUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ActualizarEmpleadoUseCase - departamento y cargo dependientes, rol recalculado")
class ActualizarEmpleadoUseCaseTest {

    private static final Long ID_EMPLEADO = 3L;
    private static final Integer ID_DPTO_MEDICINA = 5;
    private static final Integer ID_DPTO_RRHH = 1;

    @Mock IEmpleadoRepository empleadoRepository;
    @Mock IContratoRepository contratoRepository;
    @Mock IDepartamentoRepository departamentoRepository;
    @Mock IUsuarioGestionRepository usuarioRepository;
    @Mock IRolRepository rolRepository;
    @Mock RolAsignacionService rolAsignacionService;
    @Mock AuditoriaEmpleadoService auditoriaEmpleadoService;
    @Mock Empleado empleado;
    @Mock Contrato contrato;
    @Mock UsuarioGestion usuario;
    @Mock Rol rol;

    private ActualizarEmpleadoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ActualizarEmpleadoUseCase(empleadoRepository, contratoRepository, departamentoRepository,
                usuarioRepository, rolRepository, rolAsignacionService, auditoriaEmpleadoService);

        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(empleado.puedeSerEditado()).thenReturn(true);
        when(empleadoRepository.save(empleado)).thenReturn(empleado);
        when(contratoRepository.findContratoActivoByEmpleado(ID_EMPLEADO)).thenReturn(contrato);
        when(usuarioRepository.findByEmpleadoId(ID_EMPLEADO)).thenReturn(usuario);

        // Estado inicial: Médico General en Medicina
        when(contrato.getIdDpto()).thenReturn(ID_DPTO_MEDICINA);
        when(contrato.getNombreDepartamento()).thenReturn("Medicina");
        when(contrato.getCargo()).thenReturn("Médico General");

        Departamento rrhh = new Departamento();
        rrhh.setIdDpto(ID_DPTO_RRHH);
        rrhh.setNombre("Recursos Humanos");
        when(departamentoRepository.findById(ID_DPTO_RRHH)).thenReturn(rrhh);
    }

    @Test
    @DisplayName("Corregir departamento y cargo → el cargo es del nuevo departamento y el rol se recalcula")
    void cambioDeDepartamentoYCargo_RecalculaRol() {
        when(rolAsignacionService.determinarRol("Recursos Humanos", "Analista RRHH")).thenReturn("EMPLEADO_RRHH");
        when(rolRepository.findByNombreRol("EMPLEADO_RRHH")).thenReturn(rol);
        when(rol.getNombreRol()).thenReturn("EMPLEADO_RRHH");

        ActualizarEmpleadoRequestDTO dto = new ActualizarEmpleadoRequestDTO();
        dto.setIdDpto(ID_DPTO_RRHH);
        dto.setCargo("Analista RRHH");

        useCase.ejecutar(ID_EMPLEADO, dto);

        verify(contrato).cambiarDepartamento(ID_DPTO_RRHH, "Recursos Humanos");
        verify(contrato).cambiarCargo("Analista RRHH");
        verify(usuario).setNombreRol("EMPLEADO_RRHH");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("Cargo que no pertenece al departamento elegido → se rechaza sin guardar")
    void cargoDeOtroDepartamento_Rechaza() {
        ActualizarEmpleadoRequestDTO dto = new ActualizarEmpleadoRequestDTO();
        dto.setIdDpto(ID_DPTO_RRHH);
        dto.setCargo("Médico General");

        assertThrows(IllegalArgumentException.class, () -> useCase.ejecutar(ID_EMPLEADO, dto));

        verify(contrato, never()).cambiarCargo(anyString());
        verify(contratoRepository, never()).save(any());
        verify(usuario, never()).setNombreRol(anyString());
    }

    @Test
    @DisplayName("Editar solo el teléfono no revalida el cargo ni cambia el rol")
    void soloDatosPersonales_NoTocaCargoNiRol() {
        // Un cargo fuera del catálogo no debe bloquear la edición de datos personales
        when(contrato.getCargo()).thenReturn("Cargo heredado");

        ActualizarEmpleadoRequestDTO dto = new ActualizarEmpleadoRequestDTO();
        dto.setTelefono("999888777");
        dto.setIdDpto(ID_DPTO_MEDICINA);
        dto.setCargo("Cargo heredado");

        useCase.ejecutar(ID_EMPLEADO, dto);

        verify(contrato, never()).cambiarCargo(anyString());
        verify(contrato, never()).cambiarDepartamento(anyInt(), anyString());
        verify(usuario, never()).setNombreRol(anyString());
    }
}
