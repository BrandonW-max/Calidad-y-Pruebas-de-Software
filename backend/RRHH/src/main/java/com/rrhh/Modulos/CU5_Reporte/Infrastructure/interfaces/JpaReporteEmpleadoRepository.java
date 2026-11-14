package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaReporteEmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    @Query("SELECT e FROM EmpleadoModel e " +
           "LEFT JOIN ContratoModel c ON c.empleado = e AND c.estado = 'ACTIVO' " +
           "LEFT JOIN c.departamento d " +
           "WHERE (:estado IS NULL OR e.estado = :estado) AND " +
                       "(:area IS NULL OR d.nombre = :area) AND " +
           "(:cargo IS NULL OR c.cargo = :cargo) AND " +
           "(:tipoContrato IS NULL OR c.tipoContrato = :tipoContrato) AND " +
           "(:idEmpleado IS NULL OR e.idEmpleado = :idEmpleado) AND " +
           "e.creadoEl >= :fechaInicio AND e.creadoEl <= :fechaFin " +
           "ORDER BY e.idEmpleado")
    List<EmpleadoModel> buscarConFiltros(
        @Param("estado") String estado,
        @Param("area") String area,
        @Param("cargo") String cargo,
        @Param("tipoContrato") String tipoContrato,
        @Param("idEmpleado") Long idEmpleado,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin
    );
}