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
            "Solo se pueden justificar tardanzas de una hora o más e inasistencias.";
    private static final String MSG_YA_PENDIENTE =
            "Ya existe una justificación pendiente para este registro.";
    private static final String MSG_PLAZO_VENCIDO =
            "El plazo de justificacion vencio. Solo puedes justificar el mismo dia o el dia siguiente.";

    private EmpleadoModel empleado;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(justificacionService, "em", em);

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
    // Test 32 — RF18: Tardanza < 60 min → no justificable
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 32 — Tardanza de 30 min → no justificable")
    void tardanzaMenorA60Min_NoJustificable() {
        RegistroAsistenciaModel registro = registroConTardanza(30, LocalDate.now());
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
    @DisplayName("Test 34 — Ya existe justificación pendiente → rechazada")
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
}
