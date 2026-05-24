package com.rrhh.Reporte.Domain.repository;

import com.rrhh.Reporte.Domain.entities.ReporteGenerado;

import java.util.List;

public interface IReporteGeneradoRepository {

    ReporteGenerado guardar(ReporteGenerado reporte);

    List<ReporteGenerado> buscarTodos();

    List<ReporteGenerado> buscarPorTipo(String tipoReporte);
}