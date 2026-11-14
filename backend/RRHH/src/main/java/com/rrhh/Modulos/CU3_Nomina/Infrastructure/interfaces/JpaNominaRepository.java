package com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces;
 
import com.rrhh.Shared.persistence.NominaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaNominaRepository extends JpaRepository<NominaModel, Integer> {

    List<NominaModel> findByEmpleadoIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);

    List<NominaModel> findByPeriodo(String periodo);

    List<NominaModel> findByEmpleadoIdEmpleado(Long idEmpleado);

    boolean existsByEmpleadoIdEmpleadoAndPeriodo(Long idEmpleado, String periodo);

    @Modifying
    @Query("DELETE FROM DetalleNominaModel d WHERE d.nomina.periodo = :periodo")
    void deleteDetallesByPeriodo(@Param("periodo") String periodo);

    @Modifying
    @Query("DELETE FROM NominaModel n WHERE n.periodo = :periodo")
    void deleteByPeriodo(@Param("periodo") String periodo);

    @Modifying
    @Query("DELETE FROM DetalleNominaModel d WHERE d.nomina.idNomina IN " +
           "(SELECT n.idNomina FROM NominaModel n WHERE n.periodo = :periodo AND n.empleado.idEmpleado IN :ids)")
    void deleteDetallesByPeriodoYEmpleados(@Param("periodo") String periodo, @Param("ids") List<Long> ids);

    @Modifying
    @Query("DELETE FROM NominaModel n WHERE n.periodo = :periodo AND n.empleado.idEmpleado IN :ids")
    void deleteByPeriodoYEmpleados(@Param("periodo") String periodo, @Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "INSERT INTO nomina_version_anterior (id_empleado, periodo, version, sueldo_base, total_horas_extra, "
            + "total_minutos_tardanza, descuento_tardanzas, descuento_ley, sueldo_bruto, sueldo_neto, tipo_pension_aplicada, reemplazada_el) "
            + "SELECT n.id_empleado, n.periodo, n.version, n.sueldo_base, n.total_horas_extra, n.total_minutos_tardanza, "
            + "n.descuento_tardanzas, n.descuento_ley, n.sueldo_bruto, n.sueldo_neto, n.tipo_pension_aplicada, now() "
            + "FROM nomina n WHERE n.periodo = :periodo "
            + "ON CONFLICT (id_empleado, periodo) DO UPDATE SET version = EXCLUDED.version, sueldo_base = EXCLUDED.sueldo_base, "
            + "total_horas_extra = EXCLUDED.total_horas_extra, total_minutos_tardanza = EXCLUDED.total_minutos_tardanza, "
            + "descuento_tardanzas = EXCLUDED.descuento_tardanzas, descuento_ley = EXCLUDED.descuento_ley, "
            + "sueldo_bruto = EXCLUDED.sueldo_bruto, sueldo_neto = EXCLUDED.sueldo_neto, "
            + "tipo_pension_aplicada = EXCLUDED.tipo_pension_aplicada, reemplazada_el = EXCLUDED.reemplazada_el",
            nativeQuery = true)
    void guardarVersionAnterior(@Param("periodo") String periodo);

    @Modifying
    @Query(value = "INSERT INTO nomina_version_anterior (id_empleado, periodo, version, sueldo_base, total_horas_extra, "
            + "total_minutos_tardanza, descuento_tardanzas, descuento_ley, sueldo_bruto, sueldo_neto, tipo_pension_aplicada, reemplazada_el) "
            + "SELECT n.id_empleado, n.periodo, n.version, n.sueldo_base, n.total_horas_extra, n.total_minutos_tardanza, "
            + "n.descuento_tardanzas, n.descuento_ley, n.sueldo_bruto, n.sueldo_neto, n.tipo_pension_aplicada, now() "
            + "FROM nomina n WHERE n.periodo = :periodo AND n.id_empleado IN :ids "
            + "ON CONFLICT (id_empleado, periodo) DO UPDATE SET version = EXCLUDED.version, sueldo_base = EXCLUDED.sueldo_base, "
            + "total_horas_extra = EXCLUDED.total_horas_extra, total_minutos_tardanza = EXCLUDED.total_minutos_tardanza, "
            + "descuento_tardanzas = EXCLUDED.descuento_tardanzas, descuento_ley = EXCLUDED.descuento_ley, "
            + "sueldo_bruto = EXCLUDED.sueldo_bruto, sueldo_neto = EXCLUDED.sueldo_neto, "
            + "tipo_pension_aplicada = EXCLUDED.tipo_pension_aplicada, reemplazada_el = EXCLUDED.reemplazada_el",
            nativeQuery = true)
    void guardarVersionAnteriorDeEmpleados(@Param("periodo") String periodo, @Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "INSERT INTO historial_ajuste_nomina (id_empleado, periodo, version, sueldo_base, total_horas_trabajadas, "
            + "total_horas_extra, total_minutos_tardanza, bonif_familiar, bonif_turno_nocturno, bonif_guardia, bonif_riesgo, "
            + "descuento_tardanzas, descuento_ley, sueldo_bruto, sueldo_neto, tipo_pension_aplicada, reemplazado_por, reemplazado_el) "
            + "SELECT n.id_empleado, n.periodo, h.version, h.sueldo_base, h.total_horas_trabajadas, h.total_horas_extra, "
            + "h.total_minutos_tardanza, h.bonif_familiar, h.bonif_turno_nocturno, h.bonif_guardia, h.bonif_riesgo, "
            + "h.descuento_tardanzas, h.descuento_ley, h.sueldo_bruto, h.sueldo_neto, h.tipo_pension_aplicada, "
            + "h.reemplazado_por, h.reemplazado_el "
            + "FROM historial_version_nomina h JOIN nomina n ON n.id_nomina = h.id_nomina "
            + "WHERE n.periodo = :periodo "
            + "ON CONFLICT (id_empleado, periodo, version, reemplazado_el) DO NOTHING",
            nativeQuery = true)
    void copiarHistorialAjustes(@Param("periodo") String periodo);

    @Modifying
    @Query(value = "INSERT INTO historial_ajuste_nomina (id_empleado, periodo, version, sueldo_base, total_horas_trabajadas, "
            + "total_horas_extra, total_minutos_tardanza, bonif_familiar, bonif_turno_nocturno, bonif_guardia, bonif_riesgo, "
            + "descuento_tardanzas, descuento_ley, sueldo_bruto, sueldo_neto, tipo_pension_aplicada, reemplazado_por, reemplazado_el) "
            + "SELECT n.id_empleado, n.periodo, h.version, h.sueldo_base, h.total_horas_trabajadas, h.total_horas_extra, "
            + "h.total_minutos_tardanza, h.bonif_familiar, h.bonif_turno_nocturno, h.bonif_guardia, h.bonif_riesgo, "
            + "h.descuento_tardanzas, h.descuento_ley, h.sueldo_bruto, h.sueldo_neto, h.tipo_pension_aplicada, "
            + "h.reemplazado_por, h.reemplazado_el "
            + "FROM historial_version_nomina h JOIN nomina n ON n.id_nomina = h.id_nomina "
            + "WHERE n.periodo = :periodo AND n.id_empleado IN :ids "
            + "ON CONFLICT (id_empleado, periodo, version, reemplazado_el) DO NOTHING",
            nativeQuery = true)
    void copiarHistorialAjustesDeEmpleados(@Param("periodo") String periodo, @Param("ids") List<Long> ids);

    @Query(value = "SELECT COALESCE(SUM(p.monto_descuento), 0) FROM plan_recuperacion_tardanza p "
            + "WHERE p.id_empleado = :idEmpleado AND p.estado = 'DESCUENTO' AND p.periodo_descuento = :periodo",
            nativeQuery = true)
    java.math.BigDecimal sumarDescuentosPlanes(@Param("idEmpleado") Long idEmpleado, @Param("periodo") String periodo);
}
 