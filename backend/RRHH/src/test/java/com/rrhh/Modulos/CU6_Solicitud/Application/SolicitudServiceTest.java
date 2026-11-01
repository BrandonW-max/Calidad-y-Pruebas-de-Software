package com.rrhh.Modulos.CU6_Solicitud.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.SolicitudResponseDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.factory.ISolicitudFactory;
import com.rrhh.Modulos.CU6_Solicitud.Application.services.SolicitudService;
import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;
import com.rrhh.Modulos.CU6_Solicitud.Domain.repository.ISolicitudRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.UsuarioModel;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("SolicitudService - Gestión de solicitudes y permisos (RF25, RF26, RF28, RF36)")
class SolicitudServiceTest {

    @Mock private ISolicitudRepository solicitudRepository;
    @Mock private ISolicitudFactory solicitudFactory;
    @Mock private JpaEmpleadoRepository jpaEmpleadoRepository;
    @Mock private JpaUsuarioRepository jpaUsuarioRepository;
    @Mock private EntityManager em;

    @InjectMocks
    private SolicitudService solicitudService;

    private static final Long    ID_USUARIO   = 1L;
    private static final Long    ID_GERENCIA  = 2L;
    private static final Long    ID_RRHH      = 3L;
    private static final Integer ID_SOLICITUD = 10;

    private static final String MSG_FECHA_INVALIDA   = "La fecha de inicio no puede ser mayor que la fecha fin.";
    private static final String MSG_INACTIVO         = "El empleado no esta activo en el sistema.";
    private static final String MSG_SIN_SALDO        = "No cuenta con dias disponibles de vacaciones.";
    private static final String MSG_ESTADO_INVALIDO  = "La solicitud no esta en estado valido para revision.";
    private static final String MSG_SIN_OBSERVACION  = "Debe ingresar una observacion para devolver la solicitud.";
    private static final String MSG_NO_PROCESADO     = "La solicitud no esta en estado PROCESADO para decision de Gerencia.";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(solicitudService, "em", em);

        // Mock feriados query: sin feriados para no bloquear días laborables
        TypedQuery<LocalDate> feriadoQuery = mock(TypedQuery.class);
        when(em.createQuery(contains("FeriadoModel"), eq(LocalDate.class))).thenReturn(feriadoQuery);
        when(feriadoQuery.getResultList()).thenReturn(Collections.emptyList());
    }

    // -----------------------------------------------------------------------
    // Test 43 — RF25: fechaInicio > fechaFin → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 43 — fechaInicio posterior a fechaFin → IllegalArgumentException")
    void registrar_FechaInicioMayorFechaFin_LanzaExcepcion() {
        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("PERMISO_PERSONAL");
        req.setFechaInicio(LocalDate.of(2026, 3, 10));
        req.setFechaFin(LocalDate.of(2026, 3, 5));
        req.setMotivo("Motivo");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> solicitudService.registrarSolicitud(req, ID_USUARIO));

        assertEquals(MSG_FECHA_INVALIDA, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 44 — RF25 / RNF77: fechas con fin de semana → permitidas
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 44 — Fechas con sábado y domingo → permitidas (RNF77)")
    void registrar_FechasConFinDeSemana_Permitido() {
        when(jpaUsuarioRepository.findById(ID_USUARIO))
                .thenReturn(Optional.of(usuarioConEmpleado("ACTIVO")));
        Solicitud creada = new Solicitud();
        creada.setEstado("PENDIENTE");
        creada.setIdEmpleado(1L);
        when(solicitudFactory.crearSolicitudPermiso(anyString(), anyString())).thenReturn(creada);
        when(solicitudRepository.guardar(any())).thenReturn(creada);

        // 2026-03-14 es sábado
        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("PERMISO_PERSONAL");
        req.setFechaInicio(LocalDate.of(2026, 3, 12));
        req.setFechaFin(LocalDate.of(2026, 3, 14));
        req.setMotivo("Motivo");

        SolicitudResponseDTO resultado = solicitudService.registrarSolicitud(req, ID_USUARIO);

        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(3, resultado.getDiasSolicitados());
    }

    // -----------------------------------------------------------------------
    // Test 45 — RF25: empleado inactivo → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 45 — Empleado inactivo → IllegalStateException")
    void registrar_EmpleadoInactivo_LanzaExcepcion() {
        when(jpaUsuarioRepository.findById(ID_USUARIO))
                .thenReturn(Optional.of(usuarioConEmpleado("INACTIVO")));

        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("PERMISO_PERSONAL");
        req.setFechaInicio(LocalDate.of(2026, 3, 10));
        req.setFechaFin(LocalDate.of(2026, 3, 11));
        req.setMotivo("Motivo");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> solicitudService.registrarSolicitud(req, ID_USUARIO));

        assertEquals(MSG_INACTIVO, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 45 — RF25: VACACIONES sin saldo → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 46 — Vacaciones sin saldo disponible → IllegalStateException")
    void registrar_VacacionesSinSaldo_LanzaExcepcion() {
        EmpleadoModel empleado = empleadoActivo();
        empleado.setSaldoVacaciones(0);
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmpleado(empleado);
        when(jpaUsuarioRepository.findById(ID_USUARIO)).thenReturn(Optional.of(usuario));

        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("VACACIONES");
        req.setFechaInicio(LocalDate.of(2026, 3, 10));
        req.setFechaFin(LocalDate.of(2026, 3, 11));
        req.setMotivo("Vacaciones");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> solicitudService.registrarSolicitud(req, ID_USUARIO));

        assertTrue(ex.getMessage().contains("No cuenta con dias disponibles de vacaciones"));
    }

    // -----------------------------------------------------------------------
    // Test 46 — RF25: solicitud válida → estado = PENDIENTE
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 47 — Solicitud de permiso válida → estado=PENDIENTE")
    void registrar_Valido_EstadoPendiente() {
        when(jpaUsuarioRepository.findById(ID_USUARIO))
                .thenReturn(Optional.of(usuarioConEmpleado("ACTIVO")));

        Solicitud solicitudCreada = new Solicitud();
        solicitudCreada.setEstado("PENDIENTE");
        solicitudCreada.setIdEmpleado(1L);
        solicitudCreada.setTipoSolicitud("PERMISO_PERSONAL");
        when(solicitudFactory.crearSolicitudPermiso(anyString(), anyString())).thenReturn(solicitudCreada);
        when(solicitudRepository.guardar(any())).thenReturn(solicitudCreada);

        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("PERMISO_PERSONAL");
        req.setFechaInicio(LocalDate.of(2026, 3, 10));
        req.setFechaFin(LocalDate.of(2026, 3, 11));
        req.setMotivo("Cita médica");

        SolicitudResponseDTO resultado = solicitudService.registrarSolicitud(req, ID_USUARIO);

        assertEquals("PENDIENTE", resultado.getEstado());
    }

    // -----------------------------------------------------------------------
    // Test 47 — RF26: revisión en estado inválido → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 48 — Revisión RRHH de solicitud no pendiente → IllegalStateException")
    void revisar_EstadoInvalido_LanzaExcepcion() {
        when(solicitudRepository.buscarPorId(ID_SOLICITUD))
                .thenReturn(Optional.of(solicitudConEstado("APROBADA")));

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(ID_SOLICITUD);
        req.setAprobado(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> solicitudService.revisarSolicitudRrhh(req, ID_RRHH));

        assertEquals(MSG_ESTADO_INVALIDO, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 48 — RF26: rechazo sin observación → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 49 — Rechazo RRHH sin observación → IllegalArgumentException")
    void revisar_RechazoSinObservacion_LanzaExcepcion() {
        when(solicitudRepository.buscarPorId(ID_SOLICITUD))
                .thenReturn(Optional.of(solicitudConEstado("PENDIENTE")));

        RevisionRrhhRequestDTO req = new RevisionRrhhRequestDTO();
        req.setIdSolicitud(ID_SOLICITUD);
        req.setAprobado(false);
        req.setObservacionRrhh(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> solicitudService.revisarSolicitudRrhh(req, ID_RRHH));

        assertEquals(MSG_SIN_OBSERVACION, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 50 — RF28: gerencia rechaza sin motivo → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 50 — Gerencia rechaza sin motivo → IllegalArgumentException")
    void gerencia_RechazoSinMotivo_LanzaExcepcion() {
        Solicitud solicitud = solicitudConEstado("PROCESADO");
        solicitud.setIdEmpleado(1L);
        when(solicitudRepository.buscarPorId(ID_SOLICITUD)).thenReturn(Optional.of(solicitud));

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(ID_SOLICITUD);
        req.setAprobado(false);
        req.setRespuesta(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> solicitudService.decidirSolicitudGerencia(req, ID_GERENCIA));

        assertEquals("Ingrese el motivo del rechazo.", ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 51 — RF28: decisión gerencia con estado no PROCESADO → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 51 — Decisión gerencia con solicitud no PROCESADA → IllegalStateException")
    void gerencia_NoEnProcesado_LanzaExcepcion() {
        when(solicitudRepository.buscarPorId(ID_SOLICITUD))
                .thenReturn(Optional.of(solicitudConEstado("PENDIENTE")));

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(ID_SOLICITUD);
        req.setAprobado(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> solicitudService.decidirSolicitudGerencia(req, ID_GERENCIA));

        assertEquals(MSG_NO_PROCESADO, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 50 — RF28: aprobación gerencia válida → estado = APROBADA
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 52 — Aprobación gerencia válida → estado=APROBADA")
    void gerencia_Aprobada_EstadoAPROBADA() {
        Solicitud solicitud = solicitudConEstado("PROCESADO");
        solicitud.setIdEmpleado(1L);
        solicitud.setTipoSolicitud("PERMISO_PERSONAL");
        when(solicitudRepository.buscarPorId(ID_SOLICITUD)).thenReturn(Optional.of(solicitud));

        Solicitud aprobada = solicitudConEstado("APROBADA");
        aprobada.setIdEmpleado(1L);
        when(solicitudRepository.guardar(any())).thenReturn(aprobada);
        when(jpaEmpleadoRepository.findById(1L)).thenReturn(Optional.of(empleadoActivo()));

        DecisionGerenciaRequestDTO req = new DecisionGerenciaRequestDTO();
        req.setIdSolicitud(ID_SOLICITUD);
        req.setAprobado(true);
        req.setRespuesta("Aprobado por gerencia");

        SolicitudResponseDTO resultado = solicitudService.decidirSolicitudGerencia(req, ID_GERENCIA);

        assertEquals("APROBADA", resultado.getEstado());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Solicitud solicitudConEstado(String estado) {
        Solicitud s = new Solicitud();
        s.setIdSolicitud(ID_SOLICITUD);
        s.setEstado(estado);
        return s;
    }

    private EmpleadoModel empleadoActivo() {
        EmpleadoModel emp = new EmpleadoModel();
        emp.setIdEmpleado(1L);
        emp.setNombres("Juan");
        emp.setApellidos("Perez");
        emp.setEstado("ACTIVO");
        return emp;
    }

    private UsuarioModel usuarioConEmpleado(String estadoEmpleado) {
        EmpleadoModel emp = empleadoActivo();
        emp.setEstado(estadoEmpleado);
        UsuarioModel usuario = new UsuarioModel();
        usuario.setEmpleado(emp);
        return usuario;
    }

    @Test
    @DisplayName("registrar con fin de semana intermedio (vie-lun) → permitido; cuenta todos los días (4)")
    void registrar_FinDeSemanaIntermedio_Permitido() {
        when(jpaUsuarioRepository.findById(ID_USUARIO))
                .thenReturn(Optional.of(usuarioConEmpleado("ACTIVO")));
        Solicitud creada = new Solicitud();
        creada.setEstado("PENDIENTE");
        creada.setIdEmpleado(1L);
        when(solicitudFactory.crearSolicitudPermiso(anyString(), anyString())).thenReturn(creada);
        when(solicitudRepository.guardar(any())).thenReturn(creada);

        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("VACACIONES");
        req.setFechaInicio(LocalDate.of(2026, 3, 6));   // viernes
        req.setFechaFin(LocalDate.of(2026, 3, 9));      // lunes
        req.setMotivo("Vacaciones");

        SolicitudResponseDTO resultado = solicitudService.registrarSolicitud(req, ID_USUARIO);

        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(4, resultado.getDiasSolicitados());
    }

    @Test
    @DisplayName("validarPropiedad: el dueño pasa; otro empleado → AccessDeniedException")
    void validarPropiedad_DuenoYAjeno() {
        when(jpaUsuarioRepository.findById(ID_USUARIO))
                .thenReturn(Optional.of(usuarioConEmpleado("ACTIVO")));

        assertDoesNotThrow(() -> solicitudService.validarPropiedad(ID_USUARIO, 1L));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> solicitudService.validarPropiedad(ID_USUARIO, 2L));
    }

    private static Solicitud solicitudExistente(String estado, LocalDate inicio, LocalDate fin) {
        Solicitud s = new Solicitud();
        s.setEstado(estado);
        s.setFechaInicio(inicio);
        s.setFechaFin(fin);
        s.setIdEmpleado(1L);
        return s;
    }

    @Test
    @DisplayName("registrar con fechas que se cruzan con una solicitud PENDIENTE → IllegalStateException")
    void registrar_SolapaConSolicitudActiva_LanzaExcepcion() {
        when(jpaUsuarioRepository.findById(ID_USUARIO))
                .thenReturn(Optional.of(usuarioConEmpleado("ACTIVO")));
        when(solicitudRepository.buscarPorEmpleado(1L))
                .thenReturn(List.of(solicitudExistente("PENDIENTE",
                        LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 11))));

        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("PERMISO_PERSONAL");
        req.setFechaInicio(LocalDate.of(2026, 3, 10));
        req.setFechaFin(LocalDate.of(2026, 3, 12));
        req.setMotivo("Cita médica");

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> solicitudService.registrarSolicitud(req, ID_USUARIO));

        assertTrue(ex.getMessage().contains("se cruza"));
        verify(solicitudRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("registrar con fechas que se cruzan con una solicitud RECHAZADA → permitido")
    void registrar_SolicitudRechazadaNoBloquea() {
        when(jpaUsuarioRepository.findById(ID_USUARIO))
                .thenReturn(Optional.of(usuarioConEmpleado("ACTIVO")));
        when(solicitudRepository.buscarPorEmpleado(1L))
                .thenReturn(List.of(solicitudExistente("RECHAZADA",
                        LocalDate.of(2026, 3, 9), LocalDate.of(2026, 3, 11))));
        Solicitud creada = new Solicitud();
        creada.setEstado("PENDIENTE");
        creada.setIdEmpleado(1L);
        when(solicitudFactory.crearSolicitudPermiso(anyString(), anyString())).thenReturn(creada);
        when(solicitudRepository.guardar(any())).thenReturn(creada);

        RegistrarSolicitudRequestDTO req = new RegistrarSolicitudRequestDTO();
        req.setTipoSolicitud("PERMISO_PERSONAL");
        req.setFechaInicio(LocalDate.of(2026, 3, 10));
        req.setFechaFin(LocalDate.of(2026, 3, 12));
        req.setMotivo("Cita médica");

        assertEquals("PENDIENTE", solicitudService.registrarSolicitud(req, ID_USUARIO).getEstado());
    }

}
