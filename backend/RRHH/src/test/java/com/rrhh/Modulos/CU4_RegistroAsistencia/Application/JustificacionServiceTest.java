package com.rrhh.Modulos.CU4_RegistroAsistencia.Application;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.JustificacionService;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("JustificacionService - Justificación de tardanzas (RF18)")
class JustificacionServiceTest {

    @Mock private EntityManager em;

    @InjectMocks
    private JustificacionService justificacionService;

    private static final Integer ID_REGISTRO = 1;
    private static final Long    ID_EMPLEADO  = 1L;

    private static final String MSG_TARDANZA_MINIMA =
            "Solo se pueden justificar tardanzas e inasistencias.";
    private static final String MSG_YA_PENDIENTE =
            "Esta marcación ya tiene una justificación pendiente o aprobada.";
    private static final String MSG_PLAZO_VENCIDO =
            "El plazo de justificacion vencio. Solo puedes justificar el mismo dia o el dia siguiente.";

    private EmpleadoModel empleado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(justificacionService, "em", em);
        jakarta.persistence.Query nativeQuery = org.mockito.Mockito.mock(jakarta.persistence.Query.class);
        org.mockito.Mockito.lenient().when(em.createNativeQuery(org.mockito.ArgumentMatchers.anyString())).thenReturn(nativeQuery);
        org.mockito.Mockito.lenient().when(nativeQuery.setParameter(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(nativeQuery);
        org.mockito.Mockito.lenient().when(nativeQuery.getResultList()).thenReturn(java.util.List.of());
        org.mockito.Mockito.lenient().when(nativeQuery.executeUpdate()).thenReturn(1);

        empleado = new EmpleadoModel();
        empleado.setIdEmpleado(ID_EMPLEADO);
    }

    private RegistroAsistenciaModel registroConTardanza(int minutos, LocalDate fecha) {
        RegistroAsistenciaModel r = new RegistroAsistenciaModel();
        r.setEmpleado(empleado);
        r.setFecha(fecha);
        r.setMinutosTardanza(minutos);
        r.setEstado(minutos > 0 ? "TARDANZA" : "PUNTUAL");
        return r;
    }

    private void mockConteoJustificaciones(long cantidad) {
        TypedQuery<Long> countQuery = mock(TypedQuery.class);
        when(em.createQuery(contains("COUNT"), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(cantidad);
    }

    // -----------------------------------------------------------------------
    // Test 32 — RF18: Sin tardanza registrada → no justificable
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 32 — Sin tardanza registrada → no justificable")
    void sinTardanza_NoJustificable() {
        RegistroAsistenciaModel registro = registroConTardanza(0, LocalDate.now());
        when(em.find(RegistroAsistenciaModel.class, ID_REGISTRO)).thenReturn(registro);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> justificacionService.registrar(ID_REGISTRO, ID_EMPLEADO, "motivo", null));

        assertEquals(MSG_TARDANZA_MINIMA, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 33 — RF18: Tardanza ≥ 60 min → justificable
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 33 — Tardanza de 90 min → justificación registrada")
    void tardanzaMayorA60Min_Justificable() {
        RegistroAsistenciaModel registro = registroConTardanza(90, LocalDate.now());
        when(em.find(RegistroAsistenciaModel.class, ID_REGISTRO)).thenReturn(registro);
        mockConteoJustificaciones(0L);
        when(em.merge(any(RegistroAsistenciaModel.class))).thenReturn(registro);

        JustificacionTardanzaModel resultado =
                justificacionService.registrar(ID_REGISTRO, ID_EMPLEADO, "Fui al médico", null);

        assertNotNull(resultado);
        assertEquals("PENDIENTE", resultado.getEstado());
    }

    // -----------------------------------------------------------------------
    // Test 34 — RF18: Ya existe justificación pendiente → rechazada
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 34 — Ya existe justificación pendiente o aprobada → rechazada")
    void yaExisteJustificacionPendiente_Rechazada() {
        RegistroAsistenciaModel registro = registroConTardanza(90, LocalDate.now());
        when(em.find(RegistroAsistenciaModel.class, ID_REGISTRO)).thenReturn(registro);
        mockConteoJustificaciones(1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> justificacionService.registrar(ID_REGISTRO, ID_EMPLEADO, "motivo", null));

        assertEquals(MSG_YA_PENDIENTE, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 35 — RF18: Plazo vencido → rechazado
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 35 — Plazo vencido (hace 2 días) → rechazado")
    void plazoVencido_Rechazado() {
        RegistroAsistenciaModel registro = registroConTardanza(90, LocalDate.now().minusDays(2));
        when(em.find(RegistroAsistenciaModel.class, ID_REGISTRO)).thenReturn(registro);
        mockConteoJustificaciones(0L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> justificacionService.registrar(ID_REGISTRO, ID_EMPLEADO, "motivo", null));

        assertEquals(MSG_PLAZO_VENCIDO, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Motivo de la justificación: máximo 200 caracteres
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Motivo de 201 caracteres en la justificación → rechazado")
    void motivoMayorA200_Rechazado() {
        RegistroAsistenciaModel registro = registroConTardanza(90, LocalDate.now());
        when(em.find(RegistroAsistenciaModel.class, ID_REGISTRO)).thenReturn(registro);
        mockConteoJustificaciones(0L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> justificacionService.registrar(ID_REGISTRO, ID_EMPLEADO, "x".repeat(201), null));

        assertEquals("El motivo no puede superar los 200 caracteres.", ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Aprobar fuera de plazo: queda RECHAZADA y la tardanza no se anula
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Aprobar una justificación fuera de plazo → queda RECHAZADA y conserva la tardanza")
    void aprobarFueraDePlazo_QuedaRechazadaYConservaTardanza() {
        JustificacionTardanzaModel justificacion = new JustificacionTardanzaModel();
        justificacion.setIdJustificacion(5);
        justificacion.setIdRegistroAsistencia(ID_REGISTRO);
        justificacion.setIdEmpleado(ID_EMPLEADO);
        justificacion.setEstado("PENDIENTE");

        RegistroAsistenciaModel registro = registroConTardanza(90, LocalDate.now().minusDays(2));

        when(em.find(JustificacionTardanzaModel.class, 5L)).thenReturn(justificacion);
        when(em.find(RegistroAsistenciaModel.class, ID_REGISTRO)).thenReturn(registro);
        when(em.merge(any())).thenAnswer(inv -> inv.getArgument(0));

        JustificacionTardanzaModel resultado = justificacionService.revisar(5L, "APROBADA", 99L, "");

        assertEquals("RECHAZADA", resultado.getEstado());
        assertEquals(90, registro.getMinutosTardanza());
        assertTrue(resultado.getComentarioRevision().contains("Plazo de justificacion vencido"));
    }

    @Test
    @DisplayName("Listar justificaciones pendientes, por estado y por empleado")
    void listarJustificaciones_Exitoso() {
        TypedQuery<JustificacionTardanzaModel> query = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(JustificacionTardanzaModel.class))).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        JustificacionTardanzaModel j = new JustificacionTardanzaModel();
        j.setIdJustificacion(1);
        j.setIdRegistroAsistencia(ID_REGISTRO);
        j.setIdEmpleado(ID_EMPLEADO);
        j.setEstado("PENDIENTE");

        RegistroAsistenciaModel r = registroConTardanza(30, LocalDate.now());
        r.setHoraEntrada(java.time.LocalTime.of(8, 30));
        when(em.find(RegistroAsistenciaModel.class, ID_REGISTRO)).thenReturn(r);

        TypedQuery<com.rrhh.Shared.persistence.AsignacionHorarioModel> asigQuery = mock(TypedQuery.class);
        when(em.createQuery(contains("AsignacionHorarioModel"), eq(com.rrhh.Shared.persistence.AsignacionHorarioModel.class)))
                .thenReturn(asigQuery);
        when(asigQuery.setParameter(anyString(), any())).thenReturn(asigQuery);
        when(asigQuery.setMaxResults(anyInt())).thenReturn(asigQuery);
        when(asigQuery.getResultList()).thenReturn(java.util.List.of());

        when(query.getResultList()).thenReturn(java.util.List.of(j));

        java.util.List<JustificacionTardanzaModel> pendientes = justificacionService.listarPendientes();
        assertEquals(1, pendientes.size());

        java.util.List<JustificacionTardanzaModel> aprobadas = justificacionService.listarPorEstado("APROBADA");
        assertEquals(1, aprobadas.size());

        java.util.List<JustificacionTardanzaModel> porEmpleado = justificacionService.listarPorEmpleado(ID_EMPLEADO);
        assertEquals(1, porEmpleado.size());
    }
}
