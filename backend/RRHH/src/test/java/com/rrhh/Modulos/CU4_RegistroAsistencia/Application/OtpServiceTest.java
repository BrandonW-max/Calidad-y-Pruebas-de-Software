package com.rrhh.Modulos.CU4_RegistroAsistencia.Application;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpServiceTest {

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService();
    }

    @Test
    @DisplayName("Generar OTP y verificarlo correctamente")
    void generarYVerificarOtp_Exitoso() {
        OtpService.OtpResult result = otpService.generarOtp("12345678");
        assertNotNull(result.sessionId());
        assertNotNull(result.codigo());
        assertEquals(6, result.codigo().length());

        OtpService.VerificacionResult ver = otpService.verificarOtp(result.sessionId(), result.codigo());
        assertEquals(OtpService.VerificacionResult.OK, ver);

        // Ya fue usado
        OtpService.VerificacionResult ver2 = otpService.verificarOtp(result.sessionId(), result.codigo());
        assertEquals(OtpService.VerificacionResult.EXPIRADO_O_INEXISTENTE, ver2);
    }

    @Test
    @DisplayName("Verificar OTP incorrecto agota intentos")
    void verificarOtp_CodigoInvalido() {
        OtpService.OtpResult result = otpService.generarOtp("12345678");

        for (int i = 0; i < 4; i++) {
            OtpService.VerificacionResult ver = otpService.verificarOtp(result.sessionId(), "999999");
            assertEquals(OtpService.VerificacionResult.INVALIDO, ver);
        }

        // Quinto intento fallido agota los intentos
        OtpService.VerificacionResult ultimo = otpService.verificarOtp(result.sessionId(), "999999");
        assertTrue(ultimo == OtpService.VerificacionResult.INVALIDO || ultimo == OtpService.VerificacionResult.SIN_INTENTOS);

        // Posterior intento inexistente
        OtpService.VerificacionResult posterior = otpService.verificarOtp(result.sessionId(), "999999");
        assertEquals(OtpService.VerificacionResult.EXPIRADO_O_INEXISTENTE, posterior);
    }

    @Test
    @DisplayName("Verificar sessionId inexistente")
    void verificarOtp_Inexistente() {
        OtpService.VerificacionResult ver = otpService.verificarOtp("sesion-falsa", "123456");
        assertEquals(OtpService.VerificacionResult.EXPIRADO_O_INEXISTENTE, ver);
    }

    @Test
    @DisplayName("Limpiar expirados ejecuta sin errores")
    void limpiarExpirados_Ejecuta() {
        otpService.generarOtp("12345678");
        assertDoesNotThrow(() -> otpService.limpiarExpirados());
    }
}

