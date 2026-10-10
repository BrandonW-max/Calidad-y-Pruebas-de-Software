package com.rrhh.Modulos.CU4_RegistroAsistencia;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.AsistenciaService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS Y DE LÍMITE – CU4: Registro de Asistencia (AsistenciaService)
 * Framework: JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CU4 - AsistenciaService: Registro de Asistencia")
class AsistenciaServiceTest {

    @Mock JpaRegistroAsistenciaNominaRepository registroRepo;
    @Mock JpaEmpleadoRepository empleadoRepo;
    @Mock IHistorialRepository historialRepository;
    @Mock EntityManager em;

    AsistenciaService service;

    private static final Integer EMP_ID = 1;
    private static final Long EMP_LONG_ID = 1L;

    @BeforeEach
    void setUp() {
        service = new AsistenciaService(registroRepo, empleadoRepo, historialRepository);
        // Inyectar EntityManager via ReflectionTestUtils (campo @PersistenceContext)
        ReflectionTestUtils.setField(service, "em", em);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private EmpleadoModel empleadoActivo() {
        EmpleadoModel e = new EmpleadoModel();
        e.setIdEmpleado(EMP_LONG_ID);
        e.setNombres("Ana");
        e.setApellidos("Torres");
        e.setEstado("ACTIVO");
        e.setHuellaTemplate("HUELLA_BASE64");
        return e;
    }

    private EmpleadoModel empleadoInactivo() {
        EmpleadoModel e = new EmpleadoModel();
        e.setIdEmpleado(EMP_LONG_ID);
        e.setNombres("Luis");
        e.setApellidos("Vargas");
        e.setEstado("DESVINCULADO");
        return e;
    }

    private RegistroAsistenciaModel registroConEntrada() {
        RegistroAsistenciaModel r = new RegistroAsistenciaModel();
        r.setEmpleado(empleadoActivo());
        r.setFecha(LocalDate.now());
        r.setHoraEntrada(LocalTime.of(8, 0));
        r.setHoraSalida(null);
        r.setTipoRegistro("ORIGINAL");
        return r;
    }

    private MarcarAsistenciaRequestDTO requestConHuella(Integer idEmpleado, String huella) {
        MarcarAsistenciaRequestDTO req = new MarcarAsistenciaRequestDTO();
        req.setIdEmpleado(idEmpleado);
        req.setHuellaTemplate(huella);
        return req;
    }

    @SuppressWarnings("unchecked")
    private void mockearAsignacionHorario() {
        TypedQuery<Object> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), any())).thenReturn((TypedQuery) mockQuery);
        when(mockQuery.setParameter(anyString(), any())).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(List.of());
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU4-U01: Marcar entrada exitosa – primer registro del día")
    void marcarAsistencia_primeraEntrada_exitosa() {
        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.of(empleadoActivo()));
        when(registroRepo.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(EMP_ID), any(LocalDate.class), eq("ORIGINAL"))).thenReturn(Optional.empty());
        mockearAsignacionHorario();
        RegistroAsistenciaModel saved = new RegistroAsistenciaModel();
        saved.setFecha(LocalDate.now());
        saved.setHoraEntrada(LocalTime.now());
        saved.setEstado("PUNTUAL");
        when(registroRepo.save(any())).thenReturn(saved);

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, "HUELLA_BASE64"), "127.0.0.1");

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getTipoRegistro()).isEqualTo("ENTRADA");
    }

    @Test
    @DisplayName("TC-CU4-U02: Marcar salida exitosa – cuando ya existe entrada")
    void marcarAsistencia_salida_exitosa() {
        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.of(empleadoActivo()));
        when(registroRepo.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(EMP_ID), any(LocalDate.class), eq("ORIGINAL")))
                .thenReturn(Optional.of(registroConEntrada()));
        mockearAsignacionHorario();
        RegistroAsistenciaModel saved = new RegistroAsistenciaModel();
        saved.setFecha(LocalDate.now());
        saved.setHoraEntrada(LocalTime.of(8, 0));
        saved.setHoraSalida(LocalTime.of(17, 0));
        saved.setHorasTrabajadas(BigDecimal.valueOf(9));
        when(registroRepo.save(any())).thenReturn(saved);

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, "HUELLA_BASE64"), "127.0.0.1");

        assertThat(resp.isSuccess()).isTrue();
        assertThat(resp.getTipoRegistro()).isEqualTo("SALIDA");
    }

    @Test
    @DisplayName("TC-CU4-U03: Empleado no encontrado → retorna fallo")
    void marcarAsistencia_empleadoNoEncontrado_retornaFallo() {
        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.empty());

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, "HUELLA"), "127.0.0.1");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("Empleado no encontrado");
    }

    @Test
    @DisplayName("TC-CU4-U04: Empleado inactivo → retorna fallo")
    void marcarAsistencia_empleadoInactivo_retornaFallo() {
        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.of(empleadoInactivo()));

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, "HUELLA"), "127.0.0.1");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("inactivo");
    }

    @Test
    @DisplayName("TC-CU4-U05: Huella incorrecta → retorna fallo")
    void marcarAsistencia_huellaNoCoincide_retornaFallo() {
        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.of(empleadoActivo()));

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, "HUELLA_INCORRECTA"), "127.0.0.1");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("huella no coincide");
    }

    @Test
    @DisplayName("TC-CU4-U06: Registro duplicado (ya tiene entrada y salida) → retorna fallo")
    void marcarAsistencia_registroDuplicado_retornaFallo() {
        RegistroAsistenciaModel completo = new RegistroAsistenciaModel();
        completo.setHoraEntrada(LocalTime.of(8, 0));
        completo.setHoraSalida(LocalTime.of(17, 0));
        completo.setTipoRegistro("ORIGINAL");
        completo.setEmpleado(empleadoActivo());
        completo.setFecha(LocalDate.now());

        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.of(empleadoActivo()));
        when(registroRepo.findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                eq(EMP_ID), any(LocalDate.class), eq("ORIGINAL")))
                .thenReturn(Optional.of(completo));

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, "HUELLA_BASE64"), "127.0.0.1");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("duplicado");
    }

    // ---------------------------------------------------------------
    // PRUEBAS DE LÍMITE
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU4-L01: LÍMITE – ID de empleado null → retorna fallo")
    void marcarAsistencia_idNull_retornaFallo() {
        MarcarAsistenciaRequestDTO req = new MarcarAsistenciaRequestDTO();
        req.setIdEmpleado(null);

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(req, "127.0.0.1");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("ID del empleado");
    }

    @Test
    @DisplayName("TC-CU4-L02: LÍMITE – huella template null → retorna fallo")
    void marcarAsistencia_huellaNull_retornaFallo() {
        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.of(empleadoActivo()));

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, null), "127.0.0.1");

        assertThat(resp.isSuccess()).isFalse();
        assertThat(resp.getMessage()).contains("huella");
    }

    @Test
    @DisplayName("TC-CU4-L03: LÍMITE – huella template vacía → retorna fallo")
    void marcarAsistencia_huellaVacia_retornaFallo() {
        when(empleadoRepo.findById(EMP_LONG_ID)).thenReturn(Optional.of(empleadoActivo()));

        MarcarAsistenciaResponseDTO resp = service.marcarAsistencia(
                requestConHuella(EMP_ID, ""), "127.0.0.1");

        assertThat(resp.isSuccess()).isFalse();
    }
}
