package com.rrhh.Shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TurnoUtils - Cálculo de horas trabajadas (RF14)")
class TurnoUtilsTest {

    // -----------------------------------------------------------------------
    // Test 1 — RF14: Turno diurno estándar 8h
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 1 — Turno 08:00–17:00 → 540 minutos")
    void turnoNormal_8horas() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(8, 0), LocalTime.of(17, 0));
        assertEquals(540, minutos);
    }

    // -----------------------------------------------------------------------
    // Test 2 — RF14: Turno diurno con hora de almuerzo incluida
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 2 — Turno 08:00–17:00 incluye 9h → 540 min sin descuento")
    void turnoNormal_9horas() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(7, 0), LocalTime.of(16, 0));
        assertEquals(540, minutos);
    }

    // -----------------------------------------------------------------------
    // Test 3 — RF14: Turno nocturno 10 horas
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 3 — Turno 20:00–06:00 cruza medianoche → 600 minutos")
    void turnoNocturno_10horas() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(20, 0), LocalTime.of(6, 0));
        assertEquals(600, minutos);
    }

    // -----------------------------------------------------------------------
    // Test 4 — RF14: Turno nocturno 23:00–07:00
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 4 — Turno 23:00–07:00 cruza medianoche → 480 minutos")
    void turnoNocturno_medianoche() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(23, 0), LocalTime.of(7, 0));
        assertEquals(480, minutos);
    }

    // -----------------------------------------------------------------------
    // Test 5 — RF14: Misma hora entrada y salida → 0 minutos
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 5 — Entrada y salida a la misma hora → 0 minutos")
    void horaEntradaIgualSalida_cero() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(8, 0), LocalTime.of(8, 0));
        assertEquals(0, minutos);
    }

    // -----------------------------------------------------------------------
    // Test 6 — RF14: Turno nocturno 22:00–06:00
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 6 — Turno 22:00–06:00 cruza medianoche → 480 minutos")
    void turnoNocturno_22a06() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(22, 0), LocalTime.of(6, 0));
        assertEquals(480, minutos);
    }
}
