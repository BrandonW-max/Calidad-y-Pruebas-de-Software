package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PlanRecuperacionVencimientoService {

    private static final int UMBRAL_DESCUENTO_MINUTOS = 60;

    @PersistenceContext
    private EntityManager em;

    @Scheduled(cron = "${plan.recuperacion.cron:0 5 0 * * *}", zone = "${sistema.zona-horaria:America/Lima}")
    @Transactional
    public void actualizarPlanesPendientes() {
        LocalDate hoy = LocalDate.now();
        List<?> pendientes = em.createNativeQuery(
                "SELECT id_plan, id_empleado, fecha_inicio FROM plan_recuperacion_tardanza WHERE estado = 'PENDIENTE'")
            .getResultList();

        for (Object o : pendientes) {
            Object[] plan = (Object[]) o;
            int idPlan = ((Number) plan[0]).intValue();
            Long idEmpleado = ((Number) plan[1]).longValue();
            LocalDate inicio = PlanRecuperacionCalendario.aFecha(plan[2]);

            // Recalcula el límite cada noche: una solicitud aprobada después puede agregar días libres
            LocalDate limite = PlanRecuperacionCalendario.calcularFechaLimite(em, idEmpleado, inicio);
            if (limite.isBefore(hoy)) {
                cerrarPlan(idPlan, limite, hoy);
            } else {
                em.createNativeQuery("UPDATE plan_recuperacion_tardanza SET fecha_limite = :limite WHERE id_plan = :idPlan")
                    .setParameter("limite", limite)
                    .setParameter("idPlan", idPlan)
                    .executeUpdate();
            }
        }
    }

    // Menos de 1 hora pendiente se perdona; 1 hora o más se descuenta en la nómina del mes siguiente
    private void cerrarPlan(int idPlan, LocalDate limite, LocalDate hoy) {
        em.createNativeQuery("""
            UPDATE plan_recuperacion_tardanza p
            SET fecha_limite = :limite,
                estado = CASE WHEN p.minutos_pendientes >= :umbral THEN 'DESCUENTO' ELSE 'PERDONADO' END,
                periodo_descuento = CASE WHEN p.minutos_pendientes >= :umbral THEN :periodoSiguiente ELSE NULL END,
                monto_descuento = CASE WHEN p.minutos_pendientes >= :umbral
                                       THEN ROUND(p.minutos_pendientes * c.sueldo / 14400.0, 2) ELSE 0 END
            FROM contrato c
            WHERE c.id_empleado = p.id_empleado AND c.estado = 'ACTIVO' AND p.id_plan = :idPlan
            """)
            .setParameter("limite", limite)
            .setParameter("umbral", UMBRAL_DESCUENTO_MINUTOS)
            .setParameter("periodoSiguiente", YearMonth.from(hoy).plusMonths(1).toString())
            .setParameter("idPlan", idPlan)
            .executeUpdate();
    }
}
