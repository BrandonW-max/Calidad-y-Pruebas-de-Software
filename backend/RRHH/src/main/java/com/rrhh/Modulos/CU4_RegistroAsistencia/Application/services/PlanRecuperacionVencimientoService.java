package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class PlanRecuperacionVencimientoService {

    private static final Logger log = LoggerFactory.getLogger(PlanRecuperacionVencimientoService.class);
    private static final int UMBRAL_DESCUENTO_MINUTOS = 60;

    @PersistenceContext
    private EntityManager em;

    @Scheduled(cron = "${plan.recuperacion.cron:0 5 0 * * *}", zone = "${sistema.zona-horaria:America/Lima}")
    @Transactional
    public void actualizarPlanesPendientes() {
        LocalDate hoy = LocalDate.now();
        List<?> pendientes = em.createNativeQuery(
                "SELECT id_plan, id_empleado, fecha_inicio FROM plan_recuperacion_tardanza WHERE estado = 'PENDIENTE' FOR UPDATE")
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
    // El sueldo sale del contrato activo o, si no hay, del más reciente, para que el plan no quede abierto
    private void cerrarPlan(int idPlan, LocalDate limite, LocalDate hoy) {
        int cerrados = em.createNativeQuery("""
            UPDATE plan_recuperacion_tardanza p
            SET fecha_limite = :limite,
                estado = CASE WHEN p.minutos_pendientes >= :umbral THEN 'DESCUENTO' ELSE 'PERDONADO' END,
                periodo_descuento = CASE WHEN p.minutos_pendientes >= :umbral THEN :periodoSiguiente ELSE NULL END,
                monto_descuento = CASE WHEN p.minutos_pendientes >= :umbral
                                       THEN ROUND(p.minutos_pendientes * c.sueldo / 14400.0, 2) ELSE 0 END
            FROM (SELECT DISTINCT ON (id_empleado) id_empleado, sueldo
                  FROM contrato
                  ORDER BY id_empleado, (estado = 'ACTIVO') DESC, fecha_inicio DESC) c
            WHERE c.id_empleado = p.id_empleado AND p.id_plan = :idPlan
            """)
            .setParameter("limite", limite)
            .setParameter("umbral", UMBRAL_DESCUENTO_MINUTOS)
            .setParameter("periodoSiguiente", YearMonth.from(hoy).plusMonths(1).toString())
            .setParameter("idPlan", idPlan)
            .executeUpdate();
        if (cerrados == 0) {
            log.warn("Plan de recuperación {} vencido sin contrato: queda PENDIENTE para revisión de RRHH", idPlan);
        }
    }
}
