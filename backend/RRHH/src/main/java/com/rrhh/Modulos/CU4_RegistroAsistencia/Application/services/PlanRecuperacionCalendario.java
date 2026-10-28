package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class PlanRecuperacionCalendario {

    static final int DIAS_RECUPERACION = 5;
    static final int BLOQUE_INASISTENCIA_MINUTOS = 120;
    private static final int HORIZONTE_DIAS = 60;

    private PlanRecuperacionCalendario() {}

    static LocalDate calcularFechaLimite(EntityManager em, Long idEmpleado, LocalDate inicio) {
        Set<LocalDate> diasLibres = diasDeSolicitudesAprobadas(em, idEmpleado, inicio, inicio.plusDays(HORIZONTE_DIAS));
        LocalDate dia = inicio;
        int contados = 0;
        while (true) {
            if (!diasLibres.contains(dia)) {
                contados++;
                if (contados == DIAS_RECUPERACION) {
                    return dia;
                }
            }
            dia = dia.plusDays(1);
        }
    }

    static int minutosRecuperables(String tipo, int minutosDespuesDeSalida) {
        if ("INASISTENCIA".equals(tipo)) {
            return (minutosDespuesDeSalida / BLOQUE_INASISTENCIA_MINUTOS) * BLOQUE_INASISTENCIA_MINUTOS;
        }
        return minutosDespuesDeSalida;
    }

    static LocalTime aHora(Object valor) {
        if (valor instanceof java.sql.Time hora) {
            return hora.toLocalTime();
        }
        return (LocalTime) valor;
    }

    static LocalDate aFecha(Object valor) {
        if (valor instanceof java.sql.Date fecha) {
            return fecha.toLocalDate();
        }
        return (LocalDate) valor;
    }

    private static Set<LocalDate> diasDeSolicitudesAprobadas(EntityManager em, Long idEmpleado,
                                                             LocalDate desde, LocalDate hasta) {
        List<?> filas = em.createNativeQuery("SELECT fecha_inicio, fecha_fin FROM solicitud "
                + "WHERE id_empleado = :idEmpleado AND estado = 'APROBADA' "
                + "AND fecha_fin >= :desde AND fecha_inicio <= :hasta")
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("desde", desde)
            .setParameter("hasta", hasta)
            .getResultList();

        Set<LocalDate> dias = new HashSet<>();
        for (Object o : filas) {
            Object[] fila = (Object[]) o;
            LocalDate ini = aFecha(fila[0]);
            LocalDate fin = aFecha(fila[1]);
            for (LocalDate d = ini.isBefore(desde) ? desde : ini;
                 !d.isAfter(fin) && !d.isAfter(hasta);
                 d = d.plusDays(1)) {
                dias.add(d);
            }
        }
        return dias;
    }
}
