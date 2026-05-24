package com.rrhh.Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.NominaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface JpaReporteNominaRepository extends JpaRepository<NominaModel, Integer> {

    @Query("SELECT n FROM NominaModel n WHERE " +
           "(:idEmpleado IS NULL OR n.empleado.idEmpleado = :idEmpleado) AND " +
           "(:fechaInicio IS NULL OR n.fechaInicio >= :fechaInicio) AND " +
           "(:fechaFin IS NULL OR n.fechaFin <= :fechaFin) AND " +
           "(:salarioMin IS NULL OR n.sueldoNeto >= :salarioMin) AND " +
           "(:salarioMax IS NULL OR n.sueldoNeto <= :salarioMax)")
    List<NominaModel> buscarConFiltros(
        @Param("idEmpleado") Long idEmpleado,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin,
        @Param("salarioMin") BigDecimal salarioMin,
        @Param("salarioMax") BigDecimal salarioMax
    );
}