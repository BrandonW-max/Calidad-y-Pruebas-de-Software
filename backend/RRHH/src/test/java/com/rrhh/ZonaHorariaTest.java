package com.rrhh;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Zona horaria del sistema: la del hospital, no la de la máquina")
class ZonaHorariaTest {

    private TimeZone zonaOriginal;

    @BeforeEach
    void guardarZona() {
        zonaOriginal = TimeZone.getDefault();
        // Simula un servidor instalado en otra zona
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"));
    }

    @AfterEach
    void restaurarZona() {
        TimeZone.setDefault(zonaOriginal);
    }

    @Test
    @DisplayName("Al configurar la zona de Lima, 'hoy' (LocalDate.now) usa Lima aunque la máquina esté en Tokio")
    void configurarZona_LimaAunqueMaquinaEnTokio() {
        SistemaRrhhApplication.configurarZonaHoraria("America/Lima");

        assertEquals("America/Lima", ZoneId.systemDefault().getId());
    }
}
