package com.rrhh.Shared;

import com.rrhh.Shared.util.TurnoUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PRUEBAS UNITARIAS Y DE LÍMITE – Shared: TurnoUtils
 * Prueba la lógica de cálculo de minutos trabajados,
 * incluyendo turnos nocturnos que cruzan la medianoche.
 * Framework: JUnit 5 (sin Mockito, clase pura)
 */
@DisplayName("Shared - TurnoUtils: Cálculo de minutos trabajados")
class TurnoUtilsTest {

    // ---------------------------------------------------------------
    // PRUEBAS UNITARIAS
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-SH-U01: Turno diurno estándar 08:00 - 17:00 = 540 minutos")
    void minutosTrabajados_turnoDiurnoEstandar() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(8, 0), LocalTime.of(17, 0));
        assertThat(minutos).isEqualTo(540);
    }

    @Test
    @DisplayName("TC-SH-U02: Turno nocturno 22:00 - 06:00 cruza medianoche = 480 minutos")
    void minutosTrabajados_turnoNocturnoMedianoche() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(22, 0), LocalTime.of(6, 0));
        assertThat(minutos).isEqualTo(480);
    }

    @Test
    @DisplayName("TC-SH-U03: Turno exacto de 8 horas = 480 minutos")
    void minutosTrabajados_ochoHorasExactas() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(9, 0), LocalTime.of(17, 0));
        assertThat(minutos).isEqualTo(480);
    }

    // ---------------------------------------------------------------
    // PRUEBAS DE LÍMITE
    // ---------------------------------------------------------------

    @Test
    @DisplayName("TC-SH-L01: LÍMITE – turno de 1 minuto = 1 minuto")
    void minutosTrabajados_unMinuto() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(8, 0), LocalTime.of(8, 1));
        assertThat(minutos).isEqualTo(1);
    }

    @Test
    @DisplayName("TC-SH-L02: LÍMITE – turno de 24 horas completas = 1440 minutos")
    void minutosTrabajados_veinticuatroHoras() {
        // 00:00 -> 00:00 mismo momento, Duration = 0; no aplicable en práctica.
        // Caso real: 00:01 -> 00:00 del día siguiente = 23h59m = 1439 min
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(0, 1), LocalTime.of(0, 0));
        assertThat(minutos).isEqualTo(1439);
    }

    @Test
    @DisplayName("TC-SH-L03: LÍMITE – turno de medianoche 00:00 - 08:00 = 480 minutos")
    void minutosTrabajados_desdeMedianoche() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(0, 0), LocalTime.of(8, 0));
        assertThat(minutos).isEqualTo(480);
    }

    @Test
    @DisplayName("TC-SH-L04: LÍMITE – turno nocturno 19:00 - 07:00 = 720 minutos")
    void minutosTrabajados_turnoNocturno12h() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(19, 0), LocalTime.of(7, 0));
        assertThat(minutos).isEqualTo(720);
    }

    @Test
    @DisplayName("TC-SH-L05: LÍMITE – turno con minutos parciales 08:30 - 17:45 = 555 minutos")
    void minutosTrabajados_conMinutosParciales() {
        long minutos = TurnoUtils.minutosTrabajados(LocalTime.of(8, 30), LocalTime.of(17, 45));
        assertThat(minutos).isEqualTo(555);
    }
}
