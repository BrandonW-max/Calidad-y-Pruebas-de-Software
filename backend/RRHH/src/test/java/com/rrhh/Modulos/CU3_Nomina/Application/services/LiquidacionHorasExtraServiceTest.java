package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LiquidacionHorasExtraServiceTest {

    private EntityManager em;
    private LiquidacionHorasExtraService service;

    @BeforeEach
    void setUp() {
        em = mock(EntityManager.class);
        service = new LiquidacionHorasExtraService();
        ReflectionTestUtils.setField(service, "em", em);
    }

    @Test
    @DisplayName("Calcular horas totales y horas de asistencia")
    void horasTotalesYAsistencia() {
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(120); // 120 minutos = 2.00 horas

        BigDecimal totales = service.horasTotales(1L, "2026-05");
        assertEquals(new BigDecimal("2.00"), totales);

        BigDecimal asistencia = service.horasAsistencia(1L, "2026-05");
        assertEquals(new BigDecimal("2.00"), asistencia);
    }

    @Test
    @DisplayName("Registrar ajuste manual de horas extra")
    void registrarAjuste_Exitoso() {
        Query insertQuery = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(insertQuery);
        when(insertQuery.setParameter(anyString(), any())).thenReturn(insertQuery);
        when(insertQuery.executeUpdate()).thenReturn(1);

        service.registrarAjuste(1L, "2026-05", new BigDecimal("1.5"), "Guardia especial", 99L);
        verify(insertQuery).executeUpdate();
    }

    @Test
    @DisplayName("Liquidar asistencia nueva de registros")
    void liquidarAsistencia_Exitoso() {
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(0); // 0 minutos previos
        when(query.getResultList()).thenReturn(List.of()); // sin liquidados previos
        when(query.executeUpdate()).thenReturn(1);

        RegistroAsistenciaModel reg = new RegistroAsistenciaModel();
        reg.setIdRegistro(101);
        reg.setFecha(LocalDate.of(2026, 5, 10));
        reg.setHoraEntrada(LocalTime.of(8, 0));
        reg.setHoraSalida(LocalTime.of(18, 0));
        reg.setMinutosExtra(60);

        service.liquidarAsistencia(1L, "2026-05", List.of(reg), 99L);
        verify(query, atLeastOnce()).executeUpdate();
    }

    @Test
    @DisplayName("Obtener detalle de liquidación")
    void detalle_Exitoso() {
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        Object[] fila = new Object[]{
                1, 101, "ASISTENCIA",
                LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 10),
                LocalTime.of(8, 0), LocalTime.of(18, 0),
                60, "Turno completo"
        };
        when(query.getResultList()).thenReturn(java.util.Collections.singletonList(fila));

        List<Map<String, Object>> detalle = service.detalle(1L, "2026-05");
        assertEquals(1, detalle.size());
        assertEquals("TURNO", detalle.get(0).get("tipo"));
        assertEquals(new BigDecimal("1.00"), detalle.get(0).get("horas"));
    }

    @Test
    @DisplayName("Calcular horas por turno")
    void horasPorTurno_Exitoso() {
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);

        Object[] fila = new Object[]{"R101", 120};
        when(query.getResultList()).thenReturn(java.util.Collections.singletonList(fila));

        List<BigDecimal> turnos = service.horasPorTurno(1L, "2026-05");
        assertEquals(1, turnos.size());
        assertEquals(new BigDecimal("2.00"), turnos.get(0));
    }
}

