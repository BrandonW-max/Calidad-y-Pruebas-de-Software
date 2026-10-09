package com.rrhh.Modulos.CU2_GestionEmpleados.Application;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.AuditoriaEmpleadoService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.CambioSalarialUseCase;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Contrato;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.Empleado;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IContratoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IEmpleadoRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.IUsuarioGestionRepository;
import com.rrhh.Shared.config.RRHHProperties;
import com.rrhh.Shared.exception.RecursoNoEncontradoException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CambioSalarialUseCase - Validaciones de cambio salarial (RF08)")
class CambioSalarialUseCaseTest {

    @Mock private IEmpleadoRepository empleadoRepository;
    @Mock private IContratoRepository contratoRepository;
    @Mock private IUsuarioGestionRepository usuarioRepository;
    @Mock private AuditoriaEmpleadoService auditoriaEmpleadoService;
    @Mock private RRHHProperties rrhhProperties;

    @InjectMocks
    private CambioSalarialUseCase useCase;

    private static final Long ID_EMPLEADO = 1L;
    private static final BigDecimal SUELDO_MINIMO = new BigDecimal("1025.00");
    private static final BigDecimal SUELDO_ACTUAL = new BigDecimal("2000.00");
    private static final BigDecimal SUELDO_NUEVO  = new BigDecimal("2500.00");

    private Empleado empleado;
    private Contrato contratoActivo;

    @BeforeEach
    void setUp() {
        empleado = new Empleado();
        empleado.setIdEmpleado(ID_EMPLEADO);
        empleado.setNombres("Juan");
        empleado.setApellidos("Perez");
        empleado.setEstado("ACTIVO");

        contratoActivo = new Contrato();
        contratoActivo.setSueldo(SUELDO_ACTUAL);
        contratoActivo.setCargo("Enfermero");
        contratoActivo.setNombreDepartamento("Emergencias");
        contratoActivo.setEstado("ACTIVO");
    }

    @Test
    @DisplayName("Test 16 — Empleado no existe → RecursoNoEncontradoException")
    void empleadoNoExiste_LanzaExcepcion() {
        when(empleadoRepository.findById(999L)).thenReturn(null);

        assertThrows(RecursoNoEncontradoException.class,
                () -> useCase.ejecutar(999L, new CambioSalarialRequestDTO()));
    }

    @Test
    @DisplayName("Test 17 — Empleado inactivo → no puede recibir cambio salarial")
    void empleadoInactivo_NoPuedeCambioSalarial() {
        empleado.setEstado("INACTIVO");
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);

        EmpleadoResponseDTO resultado = useCase.ejecutar(ID_EMPLEADO, new CambioSalarialRequestDTO());

        assertFalse(resultado.isSuccess());
        assertEquals("No se puede registrar cambio salarial a un empleado que no está activo",
                resultado.getMessage());
    }

    @Test
    @DisplayName("Test 18 — Sueldo menor al mínimo legal → rechazado")
    void sueldoMenorAlMinimo_Rechazado() {
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(rrhhProperties.getSalarioMinimo()).thenReturn(SUELDO_MINIMO);

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(new BigDecimal("900.00"));

        EmpleadoResponseDTO resultado = useCase.ejecutar(ID_EMPLEADO, dto);

        assertFalse(resultado.isSuccess());
        assertTrue(resultado.getMessage().contains("salario minimo legal"));
    }

    @Test
    @DisplayName("Test 19 — Sueldo igual al actual → rechazado")
    void sueldoIgualAlActual_Rechazado() {
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(rrhhProperties.getSalarioMinimo()).thenReturn(SUELDO_MINIMO);
        when(contratoRepository.findContratoActivoByEmpleado(ID_EMPLEADO)).thenReturn(contratoActivo);

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(SUELDO_ACTUAL);

        EmpleadoResponseDTO resultado = useCase.ejecutar(ID_EMPLEADO, dto);

        assertFalse(resultado.isSuccess());
        assertEquals("El nuevo sueldo no puede ser igual al sueldo actual", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 20 — Cambio salarial válido → registrado correctamente")
    void cambioValido_RegistradoCorrectamente() {
        when(empleadoRepository.findById(ID_EMPLEADO)).thenReturn(empleado);
        when(rrhhProperties.getSalarioMinimo()).thenReturn(SUELDO_MINIMO);
        when(contratoRepository.findContratoActivoByEmpleado(ID_EMPLEADO)).thenReturn(contratoActivo);
        when(contratoRepository.save(any())).thenReturn(contratoActivo);
        when(usuarioRepository.findByEmpleadoId(ID_EMPLEADO)).thenReturn(null);

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(SUELDO_NUEVO);

        EmpleadoResponseDTO resultado = useCase.ejecutar(ID_EMPLEADO, dto);

        assertTrue(resultado.isSuccess());
        assertEquals("Cambio salarial registrado correctamente", resultado.getMessage());
    }
}
