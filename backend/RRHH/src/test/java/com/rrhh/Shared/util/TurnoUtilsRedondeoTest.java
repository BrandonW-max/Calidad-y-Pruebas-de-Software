package com.rrhh.Shared.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TurnoUtils.redondearHorasExtra - desde 10 minutos cuenta una hora completa")
class TurnoUtilsRedondeoTest {

    @Test
    @DisplayName("Menos de 10 minutos no cuenta")
    void menosDeDiezMinutos_noCuenta() {
        assertEquals(0, TurnoUtils.redondearHorasExtra(0));
        assertEquals(0, TurnoUtils.redondearHorasExtra(9));
    }

    @Test
    @DisplayName("Desde 10 minutos hasta 60 cuenta una hora")
    void desdeDiezHastaSesenta_cuentaUnaHora() {
        assertEquals(60, TurnoUtils.redondearHorasExtra(10));
        assertEquals(60, TurnoUtils.redondearHorasExtra(30));
        assertEquals(60, TurnoUtils.redondearHorasExtra(60));
    }

    @Test
    @DisplayName("Cada hora adicional se redondea hacia arriba")
    void horasAdicionales_seRedondeanHaciaArriba() {
        assertEquals(120, TurnoUtils.redondearHorasExtra(61));
        assertEquals(120, TurnoUtils.redondearHorasExtra(120));
        assertEquals(180, TurnoUtils.redondearHorasExtra(121));
    }
}
