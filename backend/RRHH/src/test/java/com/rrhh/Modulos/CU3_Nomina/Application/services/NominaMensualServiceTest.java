package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("NominaMensualService - cálculo de períodos anteriores sin nómina (RF47)")
class NominaMensualServiceTest {

    @Test
    @DisplayName("Calcula solo los períodos de los últimos meses que no tienen nómina")
    void calculaSoloPeriodosSinNomina() {
        INominaService nominaService = mock(INominaService.class);
        INominaRepository nominaRepository = mock(INominaRepository.class);
        YearMonth actual = YearMonth.now();
        String yaCalculado = actual.minusMonths(1).toString();

        when(nominaRepository.buscarPorPeriodo(anyString())).thenReturn(List.of());
        when(nominaRepository.buscarPorPeriodo(yaCalculado)).thenReturn(List.of(new Nomina()));

        NominaMensualService servicio = new NominaMensualService(nominaService, nominaRepository, 3);
        servicio.calcularPeriodosPendientes();

        ArgumentCaptor<CalcularNominaRequestDTO> captor = ArgumentCaptor.forClass(CalcularNominaRequestDTO.class);
        verify(nominaService, times(2)).calcularNomina(captor.capture(), anyLong());

        List<LocalDate> inicios = captor.getAllValues().stream().map(CalcularNominaRequestDTO::getFechaInicio).toList();
        assert inicios.contains(actual.minusMonths(2).atDay(1));
        assert inicios.contains(actual.minusMonths(3).atDay(1));
        assert !inicios.contains(actual.minusMonths(1).atDay(1));
    }

    @Test
    @DisplayName("Si un período falla, los demás igual se calculan y no se lanza la excepción")
    void unPeriodoFallido_NoDetieneElProceso() {
        INominaService nominaService = mock(INominaService.class);
        INominaRepository nominaRepository = mock(INominaRepository.class);
        when(nominaRepository.buscarPorPeriodo(anyString())).thenReturn(List.of());
        when(nominaService.calcularNomina(any(), anyLong()))
                .thenThrow(new IllegalStateException("No se encontraron empleados activos"))
                .thenReturn(List.of(new NominaResponseDTO()));

        NominaMensualService servicio = new NominaMensualService(nominaService, nominaRepository, 2);
        assertDoesNotThrow(servicio::calcularPeriodosPendientes);
        verify(nominaService, times(2)).calcularNomina(any(), anyLong());
    }

    @Test
    @DisplayName("Si todos los períodos ya tienen nómina, no calcula nada")
    void todosCalculados_noHaceNada() {
        INominaService nominaService = mock(INominaService.class);
        INominaRepository nominaRepository = mock(INominaRepository.class);
        when(nominaRepository.buscarPorPeriodo(anyString())).thenReturn(List.of(new Nomina()));

        new NominaMensualService(nominaService, nominaRepository, 3).calcularPeriodosPendientes();
        verify(nominaService, never()).calcularNomina(any(), anyLong());
    }
}
