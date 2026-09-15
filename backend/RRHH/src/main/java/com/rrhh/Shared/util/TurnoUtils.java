package com.rrhh.Shared.util;

import java.time.Duration;
import java.time.LocalTime;

/**
 * Calcula minutos entre hora de entrada y salida tratando correctamente los
 * turnos que cruzan la medianoche (ej. 22:00 -> 06:00 del dia siguiente).
 * LocalTime no tiene fecha, asi que Duration.between da un valor negativo
 * en ese caso; aqui se corrige sumando un dia completo.
 */
public final class TurnoUtils {

    private static final long MINUTOS_POR_DIA = 24 * 60;

    private TurnoUtils() {}

    public static long minutosTrabajados(LocalTime horaEntrada, LocalTime horaSalida) {
        long minutos = Duration.between(horaEntrada, horaSalida).toMinutes();
        return minutos < 0 ? minutos + MINUTOS_POR_DIA : minutos;
    }
}
