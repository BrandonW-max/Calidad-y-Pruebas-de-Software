package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("PlanRecuperacionCalendario - días de recuperación excluyendo solicitudes aprobadas")
class PlanRecuperacionCalendarioTest {

    private static final Long ID_EMPLEADO = 1L;

    private static EntityManager emConSolicitudes(Object[]... filas) {
        EntityManager em = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(new ArrayList<Object>(List.of(filas)));
        return em;
    }

    private static Object[] solicitud(LocalDate inicio, LocalDate fin) {
        return new Object[]{Date.valueOf(inicio), Date.valueOf(fin)};
    }

    @Test
    @DisplayName("Sin solicitudes, el plazo de 5 días termina el 5.º día desde el inicio")
    void sinSolicitudes_cuentaCincoDiasCalendario() {
        EntityManager em = emConSolicitudes();
        LocalDate limite = PlanRecuperacionCalendario.calcularFechaLimite(em, ID_EMPLEADO, LocalDate.of(2026, 10, 28));
        assertEquals(LocalDate.of(2026, 11, 1), limite);
    }

    @Test
    @DisplayName("Una solicitud aprobada en medio del plazo lo extiende un día por cada día libre")
    void solicitudEnMedio_extiendeElPlazo() {
        EntityManager em = emConSolicitudes(
                solicitud(LocalDate.of(2026, 10, 29), LocalDate.of(2026, 10, 30)));
        LocalDate limite = PlanRecuperacionCalendario.calcularFechaLimite(em, ID_EMPLEADO, LocalDate.of(2026, 10, 28));
        assertEquals(LocalDate.of(2026, 11, 3), limite);
    }

    @Test
    @DisplayName("Si el primer día del plazo es libre, no cuenta y el plazo se extiende")
    void diaInicialLibre_noCuenta() {
        EntityManager em = emConSolicitudes(
                solicitud(LocalDate.of(2026, 10, 28), LocalDate.of(2026, 10, 28)));
        LocalDate limite = PlanRecuperacionCalendario.calcularFechaLimite(em, ID_EMPLEADO, LocalDate.of(2026, 10, 28));
        assertEquals(LocalDate.of(2026, 11, 2), limite);
    }

    @Test
    @DisplayName("Una solicitud que empieza antes del inicio solo cuenta desde el inicio del plazo")
    void solicitudQueEmpiezaAntes_soloCuentaDesdeInicio() {
        EntityManager em = emConSolicitudes(
                solicitud(LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 29)));
        LocalDate limite = PlanRecuperacionCalendario.calcularFechaLimite(em, ID_EMPLEADO, LocalDate.of(2026, 10, 28));
        assertEquals(LocalDate.of(2026, 11, 3), limite);
    }

    @Test
    @DisplayName("Sábados y domingos cuentan como días del plazo cuando no hay solicitud")
    void finesDeSemana_cuentanComoDias() {
        EntityManager em = emConSolicitudes();
        // 2026-10-31 es sábado y 2026-11-01 es domingo: ambos cuentan
        LocalDate limite = PlanRecuperacionCalendario.calcularFechaLimite(em, ID_EMPLEADO, LocalDate.of(2026, 10, 30));
        assertEquals(LocalDate.of(2026, 11, 3), limite);
    }
}
