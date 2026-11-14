package com.rrhh.Modulos.CU3_Nomina.Application.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class HistorialAjusteNominaConsultaService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(String periodo) {
        List<?> filas = em.createNativeQuery("""
            SELECT h.id_historial_ajuste, e.nombres, e.apellidos, h.periodo, h.version, h.sueldo_neto,
                   h.total_horas_extra, h.total_minutos_tardanza, h.reemplazado_por, h.reemplazado_el
            FROM historial_ajuste_nomina h
            JOIN empleado e ON e.id_empleado = h.id_empleado
            WHERE (:periodo = '' OR h.periodo = :periodo)
            ORDER BY h.reemplazado_el DESC NULLS LAST, h.id_historial_ajuste DESC
            """)
            .setParameter("periodo", periodo == null ? "" : periodo)
            .getResultList();

        List<Map<String, Object>> historial = new ArrayList<>();
        for (Object o : filas) {
            Object[] f = (Object[]) o;
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("id", ((Number) f[0]).intValue());
            fila.put("empleado", f[1] + " " + f[2]);
            fila.put("periodo", f[3]);
            fila.put("version", ((Number) f[4]).intValue());
            fila.put("sueldoNeto", f[5]);
            fila.put("totalHorasExtra", f[6]);
            fila.put("totalMinutosTardanza", f[7] == null ? null : ((Number) f[7]).intValue());
            fila.put("reemplazadoPor", f[8]);
            fila.put("reemplazadoEl", f[9] instanceof Timestamp t ? t.toLocalDateTime() : (LocalDateTime) f[9]);
            historial.add(fila);
        }
        return historial;
    }
}
