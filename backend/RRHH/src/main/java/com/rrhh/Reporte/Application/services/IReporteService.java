package com.rrhh.Reporte.Application.services;

import com.rrhh.Reporte.Application.dto.*;

public interface IReporteService {

    ReporteResponseDTO generarReporteEmpleados(FiltroEmpleadoDTO filtro, Long idUsuario);

    ReporteResponseDTO generarReporteAsistencia(FiltroAsistenciaDTO filtro, Long idUsuario);

    ReporteResponseDTO generarReporteNomina(FiltroNominaDTO filtro, Long idUsuario);

    ReporteResponseDTO generarReporteSolicitudes(FiltroSolicitudDTO filtro, Long idUsuario);
}