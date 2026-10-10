package com.rrhh.Modulos.CU2_GestionEmpleados;

import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.CambioSalarialRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.DesactivarEmpleadoRequestDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.dto.EmpleadoResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Application.services.*;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.entities.*;
import com.rrhh.Modulos.CU2_GestionEmpleados.Domain.repository.*;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Shared.config.RRHHProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS Y DE LÍMITE – CU2: Gestión de Empleados
 * Cubre: DesactivarEmpleadoUseCase y CambioSalarialUseCase
 * Framework: JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CU2 - Gestión de Empleados: Desactivar y Cambio Salarial")
class EmpleadoGestionTest {

    // -------- Desactivar --------
    @Mock IEmpleadoRepository empleadoRepository;
    @Mock IContratoRepository contratoRepository;
    @Mock IUsuarioGestionRepository usuarioRepository;
    @Mock AuditoriaEmpleadoService auditoriaEmpleadoService;
    @Mock JpaUsuarioRepository jpaUsuarioRepository;

    DesactivarEmpleadoUseCase desactivarUseCase;
    CambioSalarialUseCase cambioSalarialUseCase;

    private RRHHProperties props;

    @BeforeEach
    void setUp() {
        desactivarUseCase = new DesactivarEmpleadoUseCase(
                empleadoRepository, contratoRepository, usuarioRepository,
                auditoriaEmpleadoService, jpaUsuarioRepository);

        props = new RRHHProperties();
        props.setSalarioMinimo(new BigDecimal("1025.00"));
        cambioSalarialUseCase = new CambioSalarialUseCase(
                empleadoRepository, contratoRepository, usuarioRepository,
                auditoriaEmpleadoService, props);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Empleado empleadoActivo(Long id) {
        Empleado e = new Empleado();
        e.setIdEmpleado(id);
        e.setNombres("Maria");
        e.setApellidos("Lopez");
        e.setEstado("ACTIVO");
        return e;
    }

    private Empleado empleadoDesvinculado(Long id) {
        Empleado e = new Empleado();
        e.setIdEmpleado(id);
        e.setNombres("Pedro");
        e.setApellidos("Ruiz");
        e.setEstado("DESVINCULADO");
        return e;
    }

    private Contrato contratoActivo() {
        Contrato c = new Contrato();
        c.setIdContrato(1L);
        c.setCargo("Enfermero");
        c.setNombreDepartamento("Enfermería");
        c.setSueldo(new BigDecimal("1800.00"));
        c.setEstado("ACTIVO");
        return c;
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS – Desactivar Empleado
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU2-U05: Desactivar empleado activo → estado DESVINCULADO")
    void desactivar_empleadoActivo_exitoso() {
        Empleado emp = empleadoActivo(1L);
        when(empleadoRepository.findById(1L)).thenReturn(emp);
        when(empleadoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(usuarioRepository.findByEmpleadoId(1L)).thenReturn(null);
        when(contratoRepository.findContratoActivoByEmpleado(1L)).thenReturn(null);

        DesactivarEmpleadoRequestDTO dto = new DesactivarEmpleadoRequestDTO();
        dto.setMotivo("Renuncia voluntaria");

        EmpleadoResponseDTO resultado = desactivarUseCase.ejecutar(1L, dto);

        assertThat(resultado.isSuccess()).isTrue();
        assertThat(emp.getEstado()).isEqualTo("DESVINCULADO");
    }

    @Test
    @DisplayName("TC-CU2-U06: Desactivar empleado ya desvinculado → retorna false")
    void desactivar_empleadoYaDesvinculado_retornaFallo() {
        when(empleadoRepository.findById(2L)).thenReturn(empleadoDesvinculado(2L));

        EmpleadoResponseDTO resultado = desactivarUseCase.ejecutar(2L, null);

        assertThat(resultado.isSuccess()).isFalse();
        assertThat(resultado.getMessage()).contains("ya se encuentra desvinculado");
    }

    @Test
    @DisplayName("TC-CU2-U07: Desactivar empleado inexistente → lanza excepción")
    void desactivar_empleadoNoExiste_lanzaExcepcion() {
        when(empleadoRepository.findById(99L)).thenReturn(null);

        assertThatThrownBy(() -> desactivarUseCase.ejecutar(99L, null))
                .isInstanceOf(com.rrhh.Shared.exception.RecursoNoEncontradoException.class);
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS – Cambio Salarial
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU2-U08: Cambio salarial exitoso para empleado activo")
    void cambioSalarial_empleadoActivo_exitoso() {
        Empleado emp = empleadoActivo(1L);
        Contrato contrato = contratoActivo();
        when(empleadoRepository.findById(1L)).thenReturn(emp);
        when(contratoRepository.findContratoActivoByEmpleado(1L)).thenReturn(contrato);
        when(contratoRepository.save(any())).thenReturn(contrato);
        when(usuarioRepository.findByEmpleadoId(1L)).thenReturn(null);

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(new BigDecimal("2500.00"));
        dto.setMotivo("Ascenso merecido");

        EmpleadoResponseDTO resultado = cambioSalarialUseCase.ejecutar(1L, dto);

        assertThat(resultado.isSuccess()).isTrue();
        assertThat(contrato.getSueldo()).isEqualByComparingTo(new BigDecimal("2500.00"));
    }

    @Test
    @DisplayName("TC-CU2-U09: Cambio salarial con empleado desvinculado → retorna fallo")
    void cambioSalarial_empleadoDesvinculado_retornaFallo() {
        when(empleadoRepository.findById(2L)).thenReturn(empleadoDesvinculado(2L));

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(new BigDecimal("2000.00"));

        EmpleadoResponseDTO resultado = cambioSalarialUseCase.ejecutar(2L, dto);

        assertThat(resultado.isSuccess()).isFalse();
        assertThat(resultado.getMessage()).contains("no está activo");
    }

    @Test
    @DisplayName("TC-CU2-U10: Cambio salarial sin nuevo sueldo → retorna fallo")
    void cambioSalarial_sinSueldo_retornaFallo() {
        when(empleadoRepository.findById(1L)).thenReturn(empleadoActivo(1L));

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(null);

        EmpleadoResponseDTO resultado = cambioSalarialUseCase.ejecutar(1L, dto);

        assertThat(resultado.isSuccess()).isFalse();
        assertThat(resultado.getMessage()).contains("nuevo sueldo");
    }

    // ---------------------------------------------------------------
    // PRUEBAS DE LÍMITE – Cambio Salarial
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU2-L04: LÍMITE – sueldo por debajo del mínimo (1024.99) → retorna fallo")
    void cambioSalarial_sueldoDebajoDel_minimo_retornaFallo() {
        when(empleadoRepository.findById(1L)).thenReturn(empleadoActivo(1L));

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(new BigDecimal("1024.99"));

        EmpleadoResponseDTO resultado = cambioSalarialUseCase.ejecutar(1L, dto);

        assertThat(resultado.isSuccess()).isFalse();
        assertThat(resultado.getMessage()).contains("salario minimo");
    }

    @Test
    @DisplayName("TC-CU2-L05: LÍMITE – sueldo igual al actual → retorna fallo")
    void cambioSalarial_sueldoIgualAlActual_retornaFallo() {
        Empleado emp = empleadoActivo(1L);
        Contrato contrato = contratoActivo(); // sueldo 1800.00
        when(empleadoRepository.findById(1L)).thenReturn(emp);
        when(contratoRepository.findContratoActivoByEmpleado(1L)).thenReturn(contrato);

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(new BigDecimal("1800.00")); // igual al actual

        EmpleadoResponseDTO resultado = cambioSalarialUseCase.ejecutar(1L, dto);

        assertThat(resultado.isSuccess()).isFalse();
        assertThat(resultado.getMessage()).contains("igual al sueldo actual");
    }

    @Test
    @DisplayName("TC-CU2-L06: LÍMITE – empleado sin contrato activo → lanza RuntimeException")
    void cambioSalarial_sinContrato_lanzaExcepcion() {
        when(empleadoRepository.findById(1L)).thenReturn(empleadoActivo(1L));
        when(contratoRepository.findContratoActivoByEmpleado(1L)).thenReturn(null);

        CambioSalarialRequestDTO dto = new CambioSalarialRequestDTO();
        dto.setNuevoSueldo(new BigDecimal("2000.00"));

        assertThatThrownBy(() -> cambioSalarialUseCase.ejecutar(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("no tiene contrato activo");
    }
}
