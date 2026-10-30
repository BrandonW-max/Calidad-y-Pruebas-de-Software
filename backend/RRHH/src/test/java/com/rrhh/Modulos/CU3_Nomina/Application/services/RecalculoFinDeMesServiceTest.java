package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@DisplayName("RecalculoFinDeMesService - recálculo automático al cierre de cada mes")
class RecalculoFinDeMesServiceTest {

    private RecalculoFinDeMesService servicio(INominaService nominaService) {
        return new RecalculoFinDeMesService(nominaService);
    }

    @Test
    @DisplayName("No corre si hoy no es el último día del mes")
    void noEsUltimoDia_noHaceNada() {
        INominaService nominaService = mock(INominaService.class);
        servicio(nominaService).recalcularMesSiNoSeHizo(LocalDate.of(2026, 10, 15));
        verify(nominaService, never()).calcularNomina(any(), anyLong());
    }

    @Test
    @DisplayName("Último día del mes: recalcula el mes completo")
    void ultimoDia_recalculaElMes() {
        INominaService nominaService = mock(INominaService.class);
        servicio(nominaService).recalcularMesSiNoSeHizo(LocalDate.of(2026, 10, 31));

        verify(nominaService).calcularNomina(
                org.mockito.ArgumentMatchers.argThat(r -> r instanceof CalcularNominaRequestDTO dto
                        && dto.getFechaInicio().equals(LocalDate.of(2026, 10, 1))
                        && dto.getFechaFin().equals(LocalDate.of(2026, 10, 31))
                        && dto.getIdEmpleado() == null && dto.getIdDepartamento() == null),
                anyLong());
    }

    @Test
    @DisplayName("Último día aunque ya hubo cálculos en el mes: recalcula para liquidar los últimos días")
    void ultimoDiaConCalculosPrevios_recalculaIgual() {
        INominaService nominaService = mock(INominaService.class);
        servicio(nominaService).recalcularMesSiNoSeHizo(LocalDate.of(2026, 10, 31));
        verify(nominaService).calcularNomina(any(), anyLong());
    }
}
