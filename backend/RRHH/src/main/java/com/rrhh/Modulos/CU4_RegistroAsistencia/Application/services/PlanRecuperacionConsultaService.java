package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlanRecuperacionConsultaService {

    @PersistenceContext
    private EntityManager em;

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listar(String estado) {
        List<?> filas = em.createNativeQuery("""
            SELECT p.id_plan, e.nombres, e.apellidos, p.minutos_iniciales, p.minutos_pendientes, p.tipo,
                   p.fecha_inicio, p.fecha_limite, p.estado, p.periodo_descuento, p.monto_descuento
            FROM plan_recuperacion_tardanza p
            JOIN empleado e ON e.id_empleado = p.id_empleado
            WHERE (:estado = '' OR p.estado = :estado)
            ORDER BY p.estado, p.fecha_limite, p.id_plan
            """)
            .setParameter("estado", estado == null ? "" : estado)
            .getResultList();

        List<Map<String, Object>> planes = new ArrayList<>();
        for (Object o : filas) {
            Object[] f = (Object[]) o;
            Map<String, Object> plan = new LinkedHashMap<>();
            plan.put("id", ((Number) f[0]).intValue());
            plan.put("empleado", f[1] + " " + f[2]);
            plan.put("minutosIniciales", ((Number) f[3]).intValue());
            plan.put("minutosPendientes", ((Number) f[4]).intValue());
            plan.put("tipo", f[5]);
            plan.put("fechaInicio", PlanRecuperacionCalendario.aFecha(f[6]));
            plan.put("fechaLimite", PlanRecuperacionCalendario.aFecha(f[7]));
            plan.put("estado", f[8]);
            plan.put("periodoDescuento", f[9]);
            plan.put("montoDescuento", f[10]);
            planes.add(plan);
        }
        return planes;
    }
}
