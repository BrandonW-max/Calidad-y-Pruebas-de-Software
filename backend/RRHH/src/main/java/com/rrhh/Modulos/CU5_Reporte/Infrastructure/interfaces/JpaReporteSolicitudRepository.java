package com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces;

import com.rrhh.Shared.persistence.SolicitudModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JpaReporteSolicitudRepository extends JpaRepository<SolicitudModel, Integer> {

    @Query("SELECT s FROM SolicitudModel s " +
           "JOIN ContratoModel c ON c.empleado = s.empleado AND c.estado = 'ACTIVO' " +
           "WHERE (:idEmpleado = 0 OR s.empleado.idEmpleado = :idEmpleado) AND " +
                       "(:area = '' OR c.departamento.nombre = :area) AND " +
           "(:tipoSolicitud = '' OR s.tipoSolicitud = :tipoSolicitud) AND " +
           "(:estado = '' OR s.estado = :estado) AND " +
           "(:cargo = '' OR c.cargo = :cargo) AND " +
           "s.fechaInicio >= :fechaInicio AND " +
           "s.fechaFin <= :fechaFin")
    List<SolicitudModel> buscarConFiltros(
        @Param("idEmpleado") Long idEmpleado,
        @Param("area") String area,
        @Param("tipoSolicitud") String tipoSolicitud,
        @Param("estado") String estado,
        @Param("cargo") String cargo,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
}