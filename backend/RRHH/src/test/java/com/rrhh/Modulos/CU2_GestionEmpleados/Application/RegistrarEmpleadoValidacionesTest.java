package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.RegistrarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.factory.IContratoFactory;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.HorarioService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.NombreUsuarioService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.RegistrarEmpleadoUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IDepartamentoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IRolRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.config.RRHHProperties;
import com.rrhh.Shared.security.HashService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Registro de empleado - validaciones de documento y sueldo (PI2-02, PI2-03, PI2-04)")
class RegistrarEmpleadoValidacionesTest {

    @Mock private IEmpleadoRepository empleadoRepository;
    @Mock private IContratoRepository contratoRepository;
    @Mock private IDepartamentoRepository departamentoRepository;
    @Mock private IRolRepository rolRepository;
    @Mock private IUsuarioGestionRepository usuarioRepository;
    @Mock private NombreUsuarioService nombreUsuarioService;
    @Mock private RolAsignacionService rolAsignacionService;
    @Mock private HashService hashService;
    @Mock private AuditoriaEmpleadoService auditoriaEmpleadoService;
    @Mock private IContratoFactory contratoFactory;
    @Mock private HorarioService horarioService;
    @Mock private RRHHProperties rrhhProperties;

    @InjectMocks
    private RegistrarEmpleadoUseCase useCase;

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private RegistrarEmpleadoRequestDTO empleadoBase(String numeroDi, BigDecimal sueldo) {
        RegistrarEmpleadoRequestDTO dto = new RegistrarEmpleadoRequestDTO();
        dto.setNombres("Juan Carlos");
        dto.setApellidos("Perez Lopez");
        dto.setDocIdentidad("DNI");
        dto.setNumeroDi(numeroDi);
        dto.setFechaNac(LocalDate.of(1990, 5, 15));
        dto.setSexo("Femenino");
        dto.setDireccion("Av. Test 123");
        dto.setCargo("Médico de Emergencia");
        dto.setSueldo(sueldo);
        dto.setIdDpto(3);
        return dto;
    }

    @Test
    @DisplayName("PI2-03: documento de 7 dígitos → 'El numero de documento debe tener entre 8 y 12 digitos'")
    void documentoDeSieteDigitos_ViolaValidacion() {
        Set<ConstraintViolation<RegistrarEmpleadoRequestDTO>> violaciones =
                validator.validate(empleadoBase("1000000", new BigDecimal("2500.50")));

        assertTrue(violaciones.stream().anyMatch(v ->
                v.getMessage().equals("El numero de documento debe tener entre 8 y 12 digitos")));
    }

    @Test
    @DisplayName("PI2-02: documento de 8 dígitos y nombre de 1 carácter pasan la validación")
    void documentoDeOchoDigitos_NoViolaValidacion() {
        RegistrarEmpleadoRequestDTO dto = empleadoBase("10000000", new BigDecimal("1130"));
        dto.setNombres("J");

        Set<ConstraintViolation<RegistrarEmpleadoRequestDTO>> violaciones = validator.validate(dto);

        assertTrue(violaciones.isEmpty());
    }

    @Test
    @DisplayName("PI2-04: sueldo 1129.99 → 'Error: el sueldo debe ser mayor o igual a 1130' y no guarda")
    void sueldoBajoElMinimo_RechazaYNoGuarda() {
        when(empleadoRepository.existsByNumeroDi("70123456")).thenReturn(false);
        when(rrhhProperties.getSalarioMinimo()).thenReturn(new BigDecimal("1130.00"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> useCase.ejecutar(empleadoBase("70123456", new BigDecimal("1129.99"))));

        assertEquals("Error: el sueldo debe ser mayor o igual a 1130", ex.getMessage());
        verify(empleadoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registrar empleado exitosamente en happy path")
    void registrarEmpleado_HappyPath_Exitoso() {
        RegistrarEmpleadoRequestDTO dto = empleadoBase("70123456", new BigDecimal("2500.00"));
        dto.setCorreo("test@gmail.com");

        when(empleadoRepository.existsByNumeroDi("70123456")).thenReturn(false);
        when(rrhhProperties.getSalarioMinimo()).thenReturn(new BigDecimal("1130.00"));

        com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento dpto =
                new com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Departamento();
        dpto.setIdDpto(3);
        dpto.setNombre("Emergencias");
        when(departamentoRepository.findById(3)).thenReturn(dpto);

        com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado empGuardado =
                new com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado();
        empGuardado.setIdEmpleado(100L);
        empGuardado.setNombres(dto.getNombres());
        empGuardado.setApellidos(dto.getApellidos());
        empGuardado.setNumeroDi(dto.getNumeroDi());
        when(empleadoRepository.save(any())).thenReturn(empGuardado);

        com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato contrato =
                new com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato();
        when(contratoFactory.crearIndefinido(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(contrato);

        when(rolAsignacionService.determinarRol(any(), any())).thenReturn("MEDICO");
        com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol rol =
                new com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Rol();
        rol.setIdRol(2);
        rol.setNombreRol("MEDICO");
        when(rolRepository.findByNombreRol("MEDICO")).thenReturn(rol);

        when(nombreUsuarioService.generarNombreUsuarioUnico(any(), any())).thenReturn("juan.perez");
        when(nombreUsuarioService.generarCorreoInstitucional(any())).thenReturn("juan.perez@hospital.com");
        when(hashService.encriptar(any())).thenReturn("hashed123");

        com.rrhh.Shared.persistence.HorarioModel h = new com.rrhh.Shared.persistence.HorarioModel();
        h.setIdHorario(1);
        h.setNombreTurno("Turno Mañana");
        when(horarioService.listar()).thenReturn(java.util.List.of(h));

        com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO res =
                useCase.ejecutar(dto);

        assertNotNull(res);
        assertEquals(100L, res.getIdEmpleado());
        assertEquals("juan.perez", res.getNombreUsuario());
        verify(empleadoRepository).save(any());
        verify(contratoRepository).save(any());
        verify(usuarioRepository).save(any());
    }
}
