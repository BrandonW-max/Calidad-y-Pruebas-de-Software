package com.rrhh.Modulos.CU2_GestionEmpleados;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.factory.IContratoFactory;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.*;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.*;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.*;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.HorarioService;
import com.rrhh.Shared.security.HashService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS – CU2: Gestión de Empleados (RegistrarEmpleadoUseCase)
 * Tipos: Unitarias, de Límite
 * Framework: JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CU2 - RegistrarEmpleadoUseCase: Registro de Empleados")
class RegistrarEmpleadoUseCaseTest {

    @Mock IEmpleadoRepository empleadoRepository;
    @Mock IContratoRepository contratoRepository;
    @Mock IDepartamentoRepository departamentoRepository;
    @Mock IRolRepository rolRepository;
    @Mock IUsuarioGestionRepository usuarioRepository;
    @Mock NombreUsuarioService nombreUsuarioService;
    @Mock RolAsignacionService rolAsignacionService;
    @Mock HashService hashService;
    @Mock AuditoriaEmpleadoService auditoriaEmpleadoService;
    @Mock IContratoFactory contratoFactory;
    @Mock HorarioService horarioService;

    RegistrarEmpleadoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarEmpleadoUseCase(
                empleadoRepository, contratoRepository, departamentoRepository,
                rolRepository, usuarioRepository, nombreUsuarioService,
                rolAsignacionService, hashService, auditoriaEmpleadoService,
                contratoFactory, horarioService
        );
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private RegistrarEmpleadoRequestDTO requestValido() {
        RegistrarEmpleadoRequestDTO dto = new RegistrarEmpleadoRequestDTO();
        dto.setNombres("Carlos");
        dto.setApellidos("Gomez");
        dto.setDocIdentidad("DNI");
        dto.setNumeroDi("12345678");
        dto.setFechaNac(LocalDate.of(1990, 1, 15));
        dto.setSexo("M");
        dto.setEstadoCivil("SOLTERO");
        dto.setDireccion("Av. Principal 123");
        dto.setCorreo("carlos@example.com");
        dto.setTelefono("999000111");
        dto.setIdDpto(1);
        dto.setCargo("Médico");
        dto.setSueldo(new BigDecimal("3000.00"));
        return dto;
    }

    private Empleado empleadoGuardado() {
        Empleado e = new Empleado();
        e.setIdEmpleado(10L);
        e.setNombres("Carlos");
        e.setApellidos("Gomez");
        e.setEstado("ACTIVO");
        return e;
    }

    private Departamento departamentoMedicina() {
        Departamento d = new Departamento();
        d.setIdDpto(1);
        d.setNombre("Medicina");
        return d;
    }

    private Rol rolMedico() {
        Rol r = new Rol();
        r.setIdRol(2);
        r.setNombreRol("MEDICO");
        return r;
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU2-U01: Registro exitoso de un empleado nuevo")
    void registrar_empleadoNuevo_exitoso() {
        when(empleadoRepository.existsByNumeroDi("12345678")).thenReturn(false);
        when(departamentoRepository.findById(1)).thenReturn(departamentoMedicina());
        when(empleadoRepository.save(any(Empleado.class))).thenReturn(empleadoGuardado());
        Contrato contrato = new Contrato();
        contrato.setCargo("Médico");
        contrato.setNombreDepartamento("Medicina");
        when(contratoFactory.crearIndefinido(anyLong(), anyInt(), anyString(), anyString(),
                any(), any(), any())).thenReturn(contrato);
        when(rolAsignacionService.determinarRol(anyString(), anyString())).thenReturn("MEDICO");
        when(rolRepository.findByNombreRol("MEDICO")).thenReturn(rolMedico());
        when(nombreUsuarioService.generarNombreUsuarioUnico(anyString(), anyString())).thenReturn("carlos.gomez");
        when(nombreUsuarioService.generarCorreoInstitucional(anyString())).thenReturn("carlos.gomez@hospitalsangabriel.com");
        when(hashService.encriptar(anyString())).thenReturn("hashedPassword");
        when(horarioService.listar()).thenReturn(List.of());

        EmpleadoResponseDTO resultado = useCase.ejecutar(requestValido());

        assertThat(resultado.isSuccess()).isTrue();
        assertThat(resultado.getMessage()).contains("correctamente");
        assertThat(resultado.getNombreUsuario()).isEqualTo("carlos.gomez");
        verify(empleadoRepository).save(any(Empleado.class));
        verify(usuarioRepository).save(any(UsuarioGestion.class));
    }

    @Test
    @DisplayName("TC-CU2-U02: Registro con DNI duplicado → retorna respuesta de fallo")
    void registrar_dniDuplicado_retornaFallo() {
        when(empleadoRepository.existsByNumeroDi("12345678")).thenReturn(true);

        EmpleadoResponseDTO resultado = useCase.ejecutar(requestValido());

        assertThat(resultado.isSuccess()).isFalse();
        assertThat(resultado.getMessage()).contains("Ya existe un empleado");
        verify(empleadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("TC-CU2-U03: Departamento no existe → lanza RecursoNoEncontradoException")
    void registrar_departamentoNoExiste_lanzaExcepcion() {
        when(empleadoRepository.existsByNumeroDi("12345678")).thenReturn(false);
        when(departamentoRepository.findById(1)).thenReturn(null);

        assertThatThrownBy(() -> useCase.ejecutar(requestValido()))
                .isInstanceOf(com.rrhh.Shared.exception.RecursoNoEncontradoException.class)
                .hasMessageContaining("Departamento no encontrado");
    }

    @Test
    @DisplayName("TC-CU2-U04: Rol no encontrado → lanza RecursoNoEncontradoException")
    void registrar_rolNoEncontrado_lanzaExcepcion() {
        when(empleadoRepository.existsByNumeroDi("12345678")).thenReturn(false);
        when(departamentoRepository.findById(1)).thenReturn(departamentoMedicina());
        when(empleadoRepository.save(any())).thenReturn(empleadoGuardado());
        Contrato contrato = new Contrato();
        when(contratoFactory.crearIndefinido(anyLong(), anyInt(), anyString(), anyString(),
                any(), any(), any())).thenReturn(contrato);
        when(rolAsignacionService.determinarRol(anyString(), anyString())).thenReturn("ROL_INEXISTENTE");
        when(rolRepository.findByNombreRol("ROL_INEXISTENTE")).thenReturn(null);

        assertThatThrownBy(() -> useCase.ejecutar(requestValido()))
                .isInstanceOf(com.rrhh.Shared.exception.RecursoNoEncontradoException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    // ---------------------------------------------------------------
    // PRUEBAS DE LÍMITE
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU2-L01: LÍMITE – sueldo mínimo exacto (1025.00) → registro válido")
    void registrar_sueldoMinimoExacto_valido() {
        RegistrarEmpleadoRequestDTO dto = requestValido();
        dto.setSueldo(new BigDecimal("1025.00"));

        when(empleadoRepository.existsByNumeroDi("12345678")).thenReturn(false);
        when(departamentoRepository.findById(1)).thenReturn(departamentoMedicina());
        when(empleadoRepository.save(any())).thenReturn(empleadoGuardado());
        Contrato contrato = new Contrato();
        contrato.setSueldo(new BigDecimal("1025.00"));
        contrato.setCargo("Médico");
        when(contratoFactory.crearIndefinido(anyLong(), anyInt(), anyString(), anyString(),
                any(), any(), any())).thenReturn(contrato);
        when(rolAsignacionService.determinarRol(anyString(), anyString())).thenReturn("MEDICO");
        when(rolRepository.findByNombreRol("MEDICO")).thenReturn(rolMedico());
        when(nombreUsuarioService.generarNombreUsuarioUnico(anyString(), anyString())).thenReturn("carlos.gomez");
        when(nombreUsuarioService.generarCorreoInstitucional(anyString())).thenReturn("carlos.gomez@hospitalsangabriel.com");
        when(hashService.encriptar(anyString())).thenReturn("hash");
        when(horarioService.listar()).thenReturn(List.of());

        EmpleadoResponseDTO resultado = useCase.ejecutar(dto);
        assertThat(resultado.isSuccess()).isTrue();
    }

    @Test
    @DisplayName("TC-CU2-L02: LÍMITE – contrato tipo TEMPORAL (no null, no blank)")
    void registrar_tipoContratoTemporal_valido() {
        RegistrarEmpleadoRequestDTO dto = requestValido();
        dto.setTipoContrato("TEMPORAL");

        when(empleadoRepository.existsByNumeroDi("12345678")).thenReturn(false);
        when(departamentoRepository.findById(1)).thenReturn(departamentoMedicina());
        when(empleadoRepository.save(any())).thenReturn(empleadoGuardado());
        Contrato contrato = new Contrato();
        when(contratoFactory.crearIndefinido(anyLong(), anyInt(), anyString(), anyString(),
                any(), any(), any())).thenReturn(contrato);
        when(rolAsignacionService.determinarRol(anyString(), anyString())).thenReturn("MEDICO");
        when(rolRepository.findByNombreRol("MEDICO")).thenReturn(rolMedico());
        when(nombreUsuarioService.generarNombreUsuarioUnico(anyString(), anyString())).thenReturn("c.gomez");
        when(nombreUsuarioService.generarCorreoInstitucional(anyString())).thenReturn("c.gomez@hospitalsangabriel.com");
        when(hashService.encriptar(anyString())).thenReturn("hash");
        when(horarioService.listar()).thenReturn(List.of());

        EmpleadoResponseDTO resultado = useCase.ejecutar(dto);
        assertThat(resultado.isSuccess()).isTrue();
        // El tipo de contrato TEMPORAL debe ser asignado (no reemplazado por INDEFINIDO)
        assertThat(contrato.getTipoContrato()).isEqualTo("TEMPORAL");
    }

    @Test
    @DisplayName("TC-CU2-L03: LÍMITE – contrato tipo null → usa INDEFINIDO como fallback")
    void registrar_tipoContratoNull_usaIndefinido() {
        RegistrarEmpleadoRequestDTO dto = requestValido();
        dto.setTipoContrato(null);

        when(empleadoRepository.existsByNumeroDi("12345678")).thenReturn(false);
        when(departamentoRepository.findById(1)).thenReturn(departamentoMedicina());
        when(empleadoRepository.save(any())).thenReturn(empleadoGuardado());
        Contrato contrato = new Contrato();
        when(contratoFactory.crearIndefinido(anyLong(), anyInt(), anyString(), anyString(),
                any(), any(), any())).thenReturn(contrato);
        when(rolAsignacionService.determinarRol(anyString(), anyString())).thenReturn("MEDICO");
        when(rolRepository.findByNombreRol("MEDICO")).thenReturn(rolMedico());
        when(nombreUsuarioService.generarNombreUsuarioUnico(anyString(), anyString())).thenReturn("c.gomez");
        when(nombreUsuarioService.generarCorreoInstitucional(anyString())).thenReturn("c.gomez@hospitalsangabriel.com");
        when(hashService.encriptar(anyString())).thenReturn("hash");
        when(horarioService.listar()).thenReturn(List.of());

        useCase.ejecutar(dto);
        assertThat(contrato.getTipoContrato()).isEqualTo("INDEFINIDO");
    }
}
