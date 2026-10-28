package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("PlanRecuperacionCalendario.minutosRecuperables - tardanza en minutos, inasistencia en bloques de 2 horas")
class PlanRecuperacionBloquesTest {

    @Test
    @DisplayName("Tardanza: todos los minutos de salida extra cuentan")
    void tardanza_cuentaMinutosCompletos() {
        assertEquals(100, PlanRecuperacionCalendario.minutosRecuperables("TARDANZA", 100));
    }

    @Test
    @DisplayName("Inasistencia: solo cuentan bloques completos de 120 minutos")
    void inasistencia_soloBloquesCompletos() {
        assertEquals(0, PlanRecuperacionCalendario.minutosRecuperables("INASISTENCIA", 100));
        assertEquals(120, PlanRecuperacionCalendario.minutosRecuperables("INASISTENCIA", 120));
        assertEquals(240, PlanRecuperacionCalendario.minutosRecuperables("INASISTENCIA", 250));
        assertEquals(480, PlanRecuperacionCalendario.minutosRecuperables("INASISTENCIA", 480));
    }
}
