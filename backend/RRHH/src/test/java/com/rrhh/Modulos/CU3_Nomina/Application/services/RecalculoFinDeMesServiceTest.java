package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("RecalculoFinDeMesService - recálculo automático si no hubo cálculos en el mes")
class RecalculoFinDeMesServiceTest {

    private RecalculoFinDeMesService servicio(INominaService nominaService, long cantidadCalculos) {
        EntityManager em = mock(EntityManager.class);
        Query query = mock(Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(cantidadCalculos);
        RecalculoFinDeMesService servicio = new RecalculoFinDeMesService(nominaService);
        ReflectionTestUtils.setField(servicio, "em", em);
        return servicio;
    }

    @Test
    @DisplayName("No corre si hoy no es el último día del mes")
    void noEsUltimoDia_noHaceNada() {
        INominaService nominaService = mock(INominaService.class);
        servicio(nominaService, 0).recalcularMesSiNoSeHizo(LocalDate.of(2026, 10, 15));
        verify(nominaService, never()).calcularNomina(any(), anyLong());
    }

    @Test
    @DisplayName("Último día sin cálculos en el mes: recalcula el mes completo")
    void ultimoDiaSinCalculos_recalculaElMes() {
        INominaService nominaService = mock(INominaService.class);
        servicio(nominaService, 0).recalcularMesSiNoSeHizo(LocalDate.of(2026, 10, 31));

        verify(nominaService).calcularNomina(
                org.mockito.ArgumentMatchers.argThat(r -> r instanceof CalcularNominaRequestDTO dto
                        && dto.getFechaInicio().equals(LocalDate.of(2026, 10, 1))
                        && dto.getFechaFin().equals(LocalDate.of(2026, 10, 31))
                        && dto.getIdEmpleado() == null && dto.getIdDepartamento() == null),
                anyLong());
    }

    @Test
    @DisplayName("Último día con cálculos previos en el mes: no recalcula")
    void ultimoDiaConCalculos_noHaceNada() {
        INominaService nominaService = mock(INominaService.class);
        servicio(nominaService, 3).recalcularMesSiNoSeHizo(LocalDate.of(2026, 10, 31));
        verify(nominaService, never()).calcularNomina(any(), anyLong());
    }
}
