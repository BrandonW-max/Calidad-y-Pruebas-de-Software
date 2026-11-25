package com.rrhh.Modulos.CU1_AutenticacionYRol.Domain;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Usuario - Seguridad de acceso")
class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
    }

    // -----------------------------------------------------------------------
    // RF oculto: bloqueo por intentos fallidos de login
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("1 fallo de login no bloquea la cuenta")
    void unFallo_NoBloquea() {
        usuario.registrarFallo();

        assertFalse(usuario.isCuentaBloqueada());
    }

    @Test
    @DisplayName("2 fallos de login no bloquean la cuenta")
    void dosFallos_NoBloquean() {
        usuario.registrarFallo();
        usuario.registrarFallo();

        assertFalse(usuario.isCuentaBloqueada());
    }

    @Test
    @DisplayName("3 fallos de login bloquean la cuenta")
    void tresFallos_BloqueanLaCuenta() {
        usuario.registrarFallo();
        usuario.registrarFallo();
        usuario.registrarFallo();

        assertTrue(usuario.isCuentaBloqueada());
    }

    @Test
    @DisplayName("Cuenta bloqueada no puede intentar login")
    void cuentaBloqueada_NoPuedeIntentarLogin() {
        usuario.registrarFallo();
        usuario.registrarFallo();
        usuario.registrarFallo();

        assertFalse(usuario.puedeIntentarLogin());
    }

    // -----------------------------------------------------------------------
    // RF oculto: bloqueo por intentos fallidos de 2FA
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("3 fallos de 2FA bloquean temporalmente")
    void tresFallos2FA_BloqueanTemporalmente() {
        usuario.registrarFallo2FA();
        usuario.registrarFallo2FA();
        usuario.registrarFallo2FA();

        assertFalse(usuario.puedeIntentar2FA());
    }
}
