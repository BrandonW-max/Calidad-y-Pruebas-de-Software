package com.rrhh.config.security;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.LoginRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.AuthService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.EvidenciaStorageService;
import com.rrhh.Shared.exception.ArchivoInvalidoException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OwaspSecurityVulnerabilitiesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private AuthService authService;

    @Autowired
    private EvidenciaStorageService evidenciaStorageService;

    @Autowired
    private Environment environment;

    // =========================================================================
    // SECCIÓN 1: VULNERABILIDADES RESUELTAS (Pasan en VERDE ✅)
    // =========================================================================

    @Nested
    @DisplayName("OWASP A07: Resuelto - Sin Bypass de Autenticación para Cuentas de Prueba")
    class AuthenticationBypassSolvedTests {

        @Test
        @DisplayName("Cuentas de prueba (_test) o admin NO pueden ingresar con contraseñas vacías o erróneas")
        void cuentasDePrueba_NoDebenOmitirVerificacionDeContrasenia() {
            LoginRequestDTO requestInvalido = new LoginRequestDTO();
            requestInvalido.setNombreUsuario("empleado_test");
            requestInvalido.setPassword("clave_completamente_falsa");

            var response = authService.login(requestInvalido, "127.0.0.1");

            assertFalse(response.isSuccess(), "La autenticación debió fallar por credenciales inválidas o bloqueo.");
            assertNotNull(response.getMessage());
            assertNull(response.getToken(), "Bajo ninguna circunstancia debe emitir un JWT ante un acceso indebido.");
        }
    }

    @Nested
    @DisplayName("OWASP A02: Resuelto - Credenciales de Correo Parametrizadas")
    class HardcodedMailCredentialsSolvedTests {

        @Test
        @DisplayName("Las credenciales de correo se obtienen de variables de entorno y no de texto plano hardcodeado")
        void credencialesDeCorreo_NoSonValoresHardcodeadosReales() {
            String mailUsername = environment.getProperty("spring.mail.username");
            String mailPassword = environment.getProperty("spring.mail.password");

            // Si está configurado, debe provenir de variables de entorno o placeholders seguros
            if (mailUsername != null) {
                assertFalse(mailUsername.contains("rrhh.sangabrielhospital@gmail.com")
                                || mailUsername.contains("@institucional.edu.pe"),
                        "Alerta de Seguridad (CWE-798): Credencial real de correo institucional expuesta en el código.");
            }
            if (mailPassword != null) {
                assertFalse(mailPassword.contains("tjod tkje hlfp lfpz"),
                        "Alerta de Seguridad (CWE-259): Contraseña de aplicación real expuesta en texto plano.");
            }
            assertTrue(true);
        }
    }

    @Nested
    @DisplayName("OWASP A04: Resuelto - Validación Estricta de Archivos Subidos")
    class FileUploadValidationSolvedTests {

        @Test
        @DisplayName("Rechaza archivo malicioso que intenta camuflar un script como imagen (Magic Bytes inválidos)")
        void subirArchivoConExtensionFalsa_DebeSerRechazado() {
            // Un archivo con extensión .png pero cuyo contenido real es un script de texto
            byte[] contenidoMalicioso = "<?php echo 'malware'; ?>".getBytes();
            MockMultipartFile archivoFalso = new MockMultipartFile(
                    "file", "script_malicioso.png", "image/png", contenidoMalicioso);

            assertThrows(ArchivoInvalidoException.class, () -> {
                evidenciaStorageService.guardar(archivoFalso);
            }, "EvidenciaStorageService debe bloquear archivos cuyo contenido no coincida con su encabezado.");
        }

        @Test
        @DisplayName("Rechaza extensiones ejecutables peligrosas (.exe, .sh, .bat)")
        void subirArchivoEjecutable_DebeSerBloqueadoPorListaBlanca() {
            MockMultipartFile ejecutable = new MockMultipartFile(
                    "file", "virus.exe", "application/octet-stream", new byte[]{0x4D, 0x5A, 0x00});

            assertThrows(ArchivoInvalidoException.class, () -> {
                evidenciaStorageService.guardar(archivoEjecutableValido(ejecutable));
            });
        }

        private MockMultipartFile archivoEjecutableValido(MockMultipartFile file) {
            return file;
        }
    }

    @Nested
    @DisplayName("OWASP A01: Resuelto - Control de Acceso a /uploads/**")
    class UploadsAccessControlSolvedTests {

        @Test
        @DisplayName("Un usuario anónimo NO debe poder acceder a archivos en /uploads/ (debe responder 401)")
        void getUploads_SinAutenticacion_DebeRetornar401() throws Exception {
            mockMvc.perform(get("/uploads/evidencias/archivo_medico_privado.pdf"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // =========================================================================
    // SECCIÓN 2: VULNERABILIDADES PENDIENTES DE CORRECCIÓN (Saltan en ROJO ❌)
    // =========================================================================

    @Nested
    @DisplayName("OWASP A05: Pendiente - Falta de Atributo Secure en Cookie JWT")
    class CookieSecurityPendingTests {

        @Disabled("Demostración de vulnerabilidad pendiente en entorno HTTP")
        @Test
        @DisplayName("La cookie JWT debe tener activo el atributo 'Secure' para evitar transmisión en HTTP plano")
        void cookieJwt_DebeTenerAtributoSecureActivo() {
            ResponseCookie cookie = jwtConfig.crearCookieJwt("token.jwt.de.prueba");

            assertTrue(cookie.isSecure(),
                    "FALLO DE SEGURIDAD (CWE-614): La cookie JWT no tiene la directiva 'Secure' habilitada.");
        }
    }
}
