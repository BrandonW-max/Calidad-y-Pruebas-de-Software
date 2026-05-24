package com.rrhh.Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.EmpleadoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaReporteEmpleadoRepository extends JpaRepository<EmpleadoModel, Long> {

    @Query("SELECT e FROM EmpleadoModel e WHERE " +
           "(:estado IS NULL OR e.estado = :estado) AND " +
           "(:cargo IS NULL OR EXISTS (SELECT c FROM ContratoModel c WHERE c.empleado = e AND c.cargo = :cargo AND c.estado = 'ACTIVO')) AND " +
           "(:idEmpleado IS NULL OR e.idEmpleado = :idEmpleado)")
    List<EmpleadoModel> buscarConFiltros(
        @Param("estado") String estado,
        @Param("cargo") String cargo,
        @Param("idEmpleado") Long idEmpleado
    );
}