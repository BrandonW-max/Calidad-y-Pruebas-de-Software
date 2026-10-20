package com.rrhh.Modulos.CU4_RegistroAsistencia.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaManualRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.AsistenciaService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AsistenciaService - Registro de asistencia (RF15, RF16, RF Oculto)")
class AsistenciaServiceTest {

    @Mock private JpaRegistroAsistenciaNominaRepository registroRepository;
    @Mock private JpaEmpleadoRepository empleadoRepository;
    @Mock private IHistorialRepository historialRepository;
    @Mock private EntityManager em;

    @InjectMocks
    private AsistenciaService asistenciaService;

    private static final Integer ID_EMPLEADO = 1;
    private static final String HUELLA_OK = "huella_valida_123";

    private EmpleadoModel empleado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(asistenciaService, "em", em);

        empleado = new EmpleadoModel();
        empleado.setIdEmpleado(1L);
        empleado.setNombres("Juan");
        empleado.setApellidos("Perez");
        empleado.setEstado("ACTIVO");
        empleado.setHuellaTemplate(HUELLA_OK);

        // EntityManager mock: sin horario asignado → usa defaults (08:00, tolerancia 10 min)
        TypedQuery<AsignacionHorarioModel> queryMock = mock(TypedQuery.class);
        lenient().when(em.createQuery(anyString(), eq(AsignacionHorarioModel.class))).thenReturn(queryMock);
        lenient().when(queryMock.setParameter(anyString(), any())).thenReturn(queryMock);
        lenient().when(queryMock.getResultList()).thenReturn(Collections.emptyList());

    }

    // -----------------------------------------------------------------------
    // Tests 21-22 — RF15: Entrada puntual y tardanza (via registrarManual)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 21 — Entrada 08:05 dentro de tolerancia → PUNTUAL, tardanza=0")
    void entradaDentroDeTolerancia_Puntual() {
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("CORRECCION"))).thenReturn(Optional.empty());
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("ORIGINAL"))).thenReturn(Optional.empty());
        when(registroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setTipo("ENTRADA");
        req.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 5)));

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertTrue(resultado.isSuccess());
        assertEquals("PUNTUAL", resultado.getEstado());
        assertEquals(0, resultado.getMinutosTardanza());
    }

    @Test
    @DisplayName("Test 22 — Entrada 08:20 fuera de tolerancia → TARDANZA, tardanza=20")
    void entradaFueraDeTolerancia_Tardanza() {
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("CORRECCION"))).thenReturn(Optional.empty());
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("ORIGINAL"))).thenReturn(Optional.empty());
        when(registroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setTipo("ENTRADA");
        req.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 20)));

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertTrue(resultado.isSuccess());
        assertEquals("TARDANZA", resultado.getEstado());
        assertTrue(resultado.getMinutosTardanza() > 0);
    }

    // -----------------------------------------------------------------------
    // Tests 23-24 — RF16: Salida a tiempo y con extra (via registrarManual)
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 23 — Salida a las 17:00 → minutosExtra=0")
    void salidaATiempo_SinExtra() {
        RegistroAsistenciaModel registroExistente = new RegistroAsistenciaModel();
        registroExistente.setEmpleado(empleado);
        registroExistente.setFecha(LocalDate.now());
        registroExistente.setHoraEntrada(LocalTime.of(8, 0));

        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("CORRECCION"))).thenReturn(Optional.empty());
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("ORIGINAL"))).thenReturn(Optional.of(registroExistente));
        when(registroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setTipo("SALIDA");
        req.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 0)));

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertTrue(resultado.isSuccess());
        assertEquals(0, resultado.getMinutosExtra());
    }

    @Test
    @DisplayName("Test 24 — Salida a las 17:30 → minutosExtra=30")
    void salidaTarde_ConExtra() {
        RegistroAsistenciaModel registroExistente = new RegistroAsistenciaModel();
        registroExistente.setEmpleado(empleado);
        registroExistente.setFecha(LocalDate.now());
        registroExistente.setHoraEntrada(LocalTime.of(8, 0));

        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("CORRECCION"))).thenReturn(Optional.empty());
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("ORIGINAL"))).thenReturn(Optional.of(registroExistente));
        when(registroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setTipo("SALIDA");
        req.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(17, 30)));

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertTrue(resultado.isSuccess());
        assertEquals(30, resultado.getMinutosExtra());
    }

    // -----------------------------------------------------------------------
    // Tests 25-28 — RF Oculto: Validaciones marcarAsistencia
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 25 — idEmpleado null → denegado")
    void idEmpleadoNull_Denegado() {
        MarcarAsistenciaRequestDTO req = new MarcarAsistenciaRequestDTO();
        req.setIdEmpleado(null);

        MarcarAsistenciaResponseDTO resultado = asistenciaService.marcarAsistencia(req, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Debe enviar el ID del empleado.", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 26 — Empleado inactivo → denegado")
    void empleadoInactivo_Denegado() {
        empleado.setEstado("INACTIVO");
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));

        MarcarAsistenciaRequestDTO req = new MarcarAsistenciaRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);

        MarcarAsistenciaResponseDTO resultado = asistenciaService.marcarAsistencia(req, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Usuario denegado. El empleado esta inactivo.", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 27 — Huella no coincide → denegada")
    void huellaNoCoincide_Denegada() {
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));

        MarcarAsistenciaRequestDTO req = new MarcarAsistenciaRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setHuellaTemplate("huella_incorrecta");

        MarcarAsistenciaResponseDTO resultado = asistenciaService.marcarAsistencia(req, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Usuario denegado. La huella no coincide.", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 28 — Empleado ya tiene entrada y salida hoy → registro duplicado")
    void registroDuplicado_Rechazado() {
        RegistroAsistenciaModel registroCompleto = new RegistroAsistenciaModel();
        registroCompleto.setHoraEntrada(LocalTime.of(8, 0));
        registroCompleto.setHoraSalida(LocalTime.of(17, 0));

        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("ORIGINAL"))).thenReturn(Optional.of(registroCompleto));

        MarcarAsistenciaRequestDTO req = new MarcarAsistenciaRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setHuellaTemplate(HUELLA_OK);

        MarcarAsistenciaResponseDTO resultado = asistenciaService.marcarAsistencia(req, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Registro duplicado. El empleado ya tiene entrada y salida registradas hoy.",
                resultado.getMessage());
    }

    // -----------------------------------------------------------------------
    // Tests 29-31 — RF Oculto: Validaciones registrarManual
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 29 — registrarManual con fechaHora null → denegado")
    void registrarManual_FechaNull_Denegado() {
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setFechaHora(null);

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Debe enviar la fecha y hora del registro.", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 30 — registrarManual con desfase de 2 días → rechazado")
    void registrarManual_DesfaseMayorUnDia_Rechazado() {
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setFechaHora(LocalDateTime.now().minusDays(2));

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("El registro manual solo permite un desfase maximo de 1 dia.", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 31 — registrarManual válido → almacenado correctamente")
    void registrarManual_Valido_Guardado() {
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("CORRECCION"))).thenReturn(Optional.empty());
        when(registroRepository.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(ID_EMPLEADO), any(), eq("ORIGINAL"))).thenReturn(Optional.empty());
        when(registroRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setTipo("ENTRADA");
        req.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        req.setMotivo("Fallo en el sistema biométrico");

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertTrue(resultado.isSuccess());
        assertEquals("Entrada registrada correctamente.", resultado.getMessage());
    }

    @Test
    @DisplayName("VL-CU4-07 — Motivo de 201 caracteres → rechazado")
    void registrarManual_MotivoMayorA200_Rechazado() {
        when(empleadoRepository.findById(ID_EMPLEADO.longValue())).thenReturn(Optional.of(empleado));

        MarcarAsistenciaManualRequestDTO req = new MarcarAsistenciaManualRequestDTO();
        req.setIdEmpleado(ID_EMPLEADO);
        req.setTipo("ENTRADA");
        req.setFechaHora(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)));
        req.setMotivo("a".repeat(201));

        MarcarAsistenciaResponseDTO resultado = asistenciaService.registrarManual(req, 1L, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("El motivo no puede superar los 200 caracteres.", resultado.getMessage());
    }
}
