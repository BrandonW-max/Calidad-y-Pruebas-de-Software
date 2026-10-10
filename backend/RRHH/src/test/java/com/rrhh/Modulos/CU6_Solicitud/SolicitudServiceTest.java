package com.rrhh.Modulos.CU6_Solicitud;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.SolicitudResponseDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.factory.ISolicitudFactory;
import com.rrhh.Modulos.CU6_Solicitud.Application.factory.SolicitudFactory;
import com.rrhh.Modulos.CU6_Solicitud.Application.services.SolicitudService;
import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;
import com.rrhh.Modulos.CU6_Solicitud.Domain.repository.ISolicitudRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PRUEBAS UNITARIAS Y DE LÍMITE – CU6: Gestión de Solicitudes (SolicitudService)
 * Framework: JUnit 5 + Mockito
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CU6 - SolicitudService: Flujo de Solicitudes de Vacaciones/Permisos")
class SolicitudServiceTest {

    @Mock ISolicitudRepository solicitudRepository;
    @Mock JpaEmpleadoRepository jpaEmpleadoRepository;
    @Mock JpaUsuarioRepository jpaUsuarioRepository;
    @Mock EntityManager em;

    SolicitudService service;

    private static final Long USUARIO_ID   = 5L;
    private static final Long EMPLEADO_ID  = 10L;
    private static final Long GERENCIA_ID  = 20L;

    @BeforeEach
    void setUp() {
        ISolicitudFactory factory = new SolicitudFactory();
        service = new SolicitudService(solicitudRepository, factory, jpaEmpleadoRepository, jpaUsuarioRepository);
        ReflectionTestUtils.setField(service, "em", em);
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void mockFeriados() {
        TypedQuery<LocalDate> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(LocalDate.class))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(List.of());
    }

    private com.rrhh.Shared.persistence.UsuarioModel usuarioConEmpleado() {
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(EMPLEADO_ID);
        emp.setNombres("Rosa");
        emp.setApellidos("Medina");
        emp.setEstado("ACTIVO");
        emp.setSaldoVacaciones(30);
        com.rrhh.Shared.persistence.UsuarioModel u = new com.rrhh.Shared.persistence.UsuarioModel();
        u.setEmpleado(emp);
        return u;
    }

    private Solicitud solicitudPendiente(Integer id) {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(id);
        s.setTipoSolicitud("VACACIONES");
        s.setFechaInicio(LocalDate.of(2025, 6, 2));
        s.setFechaFin(LocalDate.of(2025, 6, 6));
        s.setDiasSolicitados(5);
        s.setEstado("PENDIENTE");
        s.setIdEmpleado(EMPLEADO_ID);
        return s;
    }

    private Solicitud solicitudProcesada(Integer id) {
        Solicitud s = solicitudPendiente(id);
        s.setEstado("PROCESADO");
        return s;
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS – revisarSolicitudRrhh
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU6-U01: RRHH aprueba solicitud PENDIENTE → estado PROCESADO")
    void revisar_aprobacion_cambiaAProcesado() {
        Solicitud sol = solicitudPendiente(1);
        when(solicitudRepository.buscarPorId(1)).thenReturn(Optional.of(sol));
        when(solicitudRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jpaEmpleadoRepository.findById(EMPLEADO_ID)).thenReturn(Optional.empty());

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(1);
        req.setAprobado(true);
        req.setObservacionRrhh("Todo en regla");

        SolicitudResponseDTO resp = service.revisarSolicitudRrhh(req, USUARIO_ID);

        assertThat(resp.getEstado()).isEqualTo("PROCESADO");
    }

    @Test
    @DisplayName("TC-CU6-U02: RRHH rechaza solicitud sin observación → lanza IllegalArgumentException")
    void revisar_rechazoSinObservacion_lanzaExcepcion() {
        Solicitud sol = solicitudPendiente(1);
        when(solicitudRepository.buscarPorId(1)).thenReturn(Optional.of(sol));

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(1);
        req.setAprobado(false);
        req.setObservacionRrhh(null);

        assertThatThrownBy(() -> service.revisarSolicitudRrhh(req, USUARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("observacion");
    }

    @Test
    @DisplayName("TC-CU6-U03: RRHH rechaza solicitud con observación → estado OBSERVADO")
    void revisar_rechazo_cambiaAObservado() {
        Solicitud sol = solicitudPendiente(1);
        when(solicitudRepository.buscarPorId(1)).thenReturn(Optional.of(sol));
        when(solicitudRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jpaEmpleadoRepository.findById(EMPLEADO_ID)).thenReturn(Optional.empty());

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(1);
        req.setAprobado(false);
        req.setObservacionRrhh("Documentación incompleta");

        SolicitudResponseDTO resp = service.revisarSolicitudRrhh(req, USUARIO_ID);

        assertThat(resp.getEstado()).isEqualTo("OBSERVADO");
    }

    @Test
    @DisplayName("TC-CU6-U04: Revisión de solicitud no PENDIENTE → lanza IllegalStateException")
    void revisar_solicitudNoEnEstadoValido_lanzaExcepcion() {
        Solicitud sol = solicitudPendiente(1);
        sol.setEstado("APROBADA"); // estado no revisable
        when(solicitudRepository.buscarPorId(1)).thenReturn(Optional.of(sol));

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(1);
        req.setAprobado(true);

        assertThatThrownBy(() -> service.revisarSolicitudRrhh(req, USUARIO_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estado valido para revision");
    }

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS – decidirSolicitudGerencia
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU6-U05: Gerencia aprueba solicitud PROCESADA → estado APROBADA")
    void decidir_aprobacion_cambiaAAprobada() {
        Solicitud sol = solicitudProcesada(2);
        when(solicitudRepository.buscarPorId(2)).thenReturn(Optional.of(sol));
        when(solicitudRepository.guardar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(jpaEmpleadoRepository.findById(EMPLEADO_ID)).thenReturn(Optional.empty());

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(2);
        req.setAprobado(true);
        req.setRespuesta("Aprobado");

        SolicitudResponseDTO resp = service.decidirSolicitudGerencia(req, GERENCIA_ID);

        assertThat(resp.getEstado()).isEqualTo("APROBADA");
    }

    @Test
    @DisplayName("TC-CU6-U06: Gerencia rechaza solicitud sin motivo → lanza IllegalArgumentException")
    void decidir_rechazoSinMotivo_lanzaExcepcion() {
        Solicitud sol = solicitudProcesada(2);
        when(solicitudRepository.buscarPorId(2)).thenReturn(Optional.of(sol));

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(2);
        req.setAprobado(false);
        req.setRespuesta(null);

        assertThatThrownBy(() -> service.decidirSolicitudGerencia(req, GERENCIA_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motivo del rechazo");
    }

    @Test
    @DisplayName("TC-CU6-U07: Decisión de gerencia en solicitud no PROCESADA → lanza IllegalStateException")
    void decidir_solicitudNoProcesada_lanzaExcepcion() {
        Solicitud sol = solicitudPendiente(3); // Estado PENDIENTE, no PROCESADO
        when(solicitudRepository.buscarPorId(3)).thenReturn(Optional.of(sol));

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(3);
        req.setAprobado(true);

        assertThatThrownBy(() -> service.decidirSolicitudGerencia(req, GERENCIA_ID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estado PROCESADO");
    }

    // ---------------------------------------------------------------
    // PRUEBAS DE LÍMITE
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-CU6-L01: LÍMITE – solicitud no encontrada en RRHH → lanza RecursoNoEncontradoException")
    void revisar_solicitudNoEncontrada_lanzaExcepcion() {
        when(solicitudRepository.buscarPorId(999)).thenReturn(Optional.empty());

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(999);
        req.setAprobado(true);

        assertThatThrownBy(() -> service.revisarSolicitudRrhh(req, USUARIO_ID))
                .isInstanceOf(com.rrhh.Shared.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("TC-CU6-L02: LÍMITE – solicitud no encontrada en Gerencia → lanza RecursoNoEncontradoException")
    void decidir_solicitudNoEncontrada_lanzaExcepcion() {
        when(solicitudRepository.buscarPorId(999)).thenReturn(Optional.empty());

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(999);
        req.setAprobado(true);

        assertThatThrownBy(() -> service.decidirSolicitudGerencia(req, GERENCIA_ID))
                .isInstanceOf(com.rrhh.Shared.exception.RecursoNoEncontradoException.class);
    }

    @Test
    @DisplayName("TC-CU6-L03: LÍMITE – rechazo con observación en blanco → lanza excepción")
    void revisar_rechazoObservacionBlanca_lanzaExcepcion() {
        Solicitud sol = solicitudPendiente(1);
        when(solicitudRepository.buscarPorId(1)).thenReturn(Optional.of(sol));

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(1);
        req.setAprobado(false);
        req.setObservacionRrhh("   "); // blank

        assertThatThrownBy(() -> service.revisarSolicitudRrhh(req, USUARIO_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("observacion");
    }

    @Test
    @DisplayName("TC-CU6-L04: LÍMITE – gerencia rechaza con respuesta en blanco → lanza excepción")
    void decidir_rechazoRespuestaBlanca_lanzaExcepcion() {
        Solicitud sol = solicitudProcesada(2);
        when(solicitudRepository.buscarPorId(2)).thenReturn(Optional.of(sol));

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(2);
        req.setAprobado(false);
        req.setRespuesta("   "); // blank

        assertThatThrownBy(() -> service.decidirSolicitudGerencia(req, GERENCIA_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motivo del rechazo");
    }
}
