package com.rrhh.Modulos.CU1_AutenticacionYRol.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.AuthResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.LoginRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.TwoFactorRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.TwoFactorResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.AuthService;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IUsuarioRepository;
import com.rrhh.Shared.security.HashService;
import com.rrhh.config.security.JwtConfig;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.TwoFactorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Autenticación y cambio de contraseña (Seguridad A07)")
class AuthServiceTest {

    @Mock private IUsuarioRepository usuarioRepository;
    @Mock private IHistorialRepository historialRepository;
    @Mock private HashService hashService;
    @Mock private JwtConfig jwtConfig;
    @Mock private TwoFactorService twoFactorService;

    @InjectMocks
    private AuthService authService;

    private static final String USERNAME = "juan.perez";
    private static final String DNI = "12345678";
    private static final String PASSWORD_VALIDA = "NuevaPass1!";

    private Usuario usuarioConDni;

    @BeforeEach
    void setUp() {
        usuarioConDni = new Usuario();
        usuarioConDni.setId(1L);
        usuarioConDni.setNombreUsuario(USERNAME);
        usuarioConDni.setNumeroDi(DNI);
        usuarioConDni.setContrasenia("hashed_" + DNI);
        usuarioConDni.setActivo(true);
        usuarioConDni.setNombreRol("EMPLEADO");
    }

    // ========================================================================
    // CASO 1: Un usuario normal con contraseña correcta puede autenticarse.
    // ========================================================================
    @Test
    @DisplayName("Caso 1: Usuario normal con contraseña correcta se autentica exitosamente")
    void usuarioNormal_ConPasswordCorrecta_AutenticaExitosamente() {
        LoginRequestDTO dto = new LoginRequestDTO(USERNAME, PASSWORD_VALIDA);

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(hashService.verificar(PASSWORD_VALIDA, "hashed_" + DNI)).thenReturn(true);
        when(jwtConfig.generarToken(eq(USERNAME), eq("EMPLEADO"), eq(1L), anyList(), any())).thenReturn("token.jwt.valido");

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertTrue(resultado.isSuccess());
        assertEquals("Acceso concedido.", resultado.getMessage());
        assertEquals("token.jwt.valido", resultado.getToken());
        assertEquals("EMPLEADO", resultado.getRol());
        assertEquals(0, usuarioConDni.getIntentosFallidos());
        verify(usuarioRepository).save(usuarioConDni);
        verify(historialRepository).save(argThat(h -> "LOGIN_EXITOSO".equals(h.getAccion())));
    }

    // ========================================================================
    // CASO 2: Un usuario normal con contraseña incorrecta no puede autenticarse.
    // ========================================================================
    @Test
    @DisplayName("Caso 2: Usuario normal con contraseña incorrecta es rechazado e incrementa intentos")
    void usuarioNormal_ConPasswordIncorrecta_NoAutentica() {
        LoginRequestDTO dto = new LoginRequestDTO(USERNAME, "PasswordErronea123!");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(hashService.verificar("PasswordErronea123!", "hashed_" + DNI)).thenReturn(false);

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertTrue(resultado.getMessage().contains("Credenciales invalidas"));
        assertNull(resultado.getToken());
        assertEquals(1, usuarioConDni.getIntentosFallidos());
        verify(jwtConfig, never()).generarToken(any(), any(), any(), any(), any());
        verify(usuarioRepository).save(usuarioConDni);
        verify(historialRepository).save(argThat(h -> "LOGIN_FALLIDO".equals(h.getAccion())));
    }

    // ========================================================================
    // CASO 3: Un usuario cuyo nombre antes figuraba en la lista de prueba no puede autenticarse con contraseña incorrecta.
    // ========================================================================
    @Test
    @DisplayName("Caso 3: Usuario antes en lista de prueba (admin.rrhh) con contraseña incorrecta es rechazado")
    void usuarioExPrueba_ConPasswordIncorrecta_NoAutentica() {
        Usuario usuarioAdmin = new Usuario();
        usuarioAdmin.setId(10L);
        usuarioAdmin.setNombreUsuario("admin.rrhh");
        usuarioAdmin.setContrasenia("hashed_admin_real");
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setNombreRol("RRHH");

        LoginRequestDTO dto = new LoginRequestDTO("admin.rrhh", "CualquierPasswordInvalida");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst("admin.rrhh", "admin.rrhh")).thenReturn(usuarioAdmin);
        when(hashService.verificar("CualquierPasswordInvalida", "hashed_admin_real")).thenReturn(false);

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertTrue(resultado.getMessage().contains("Credenciales invalidas"));
        assertNull(resultado.getToken());
        assertEquals(1, usuarioAdmin.getIntentosFallidos());
        verify(jwtConfig, never()).generarToken(any(), any(), any(), any(), any());
    }

    // ========================================================================
    // CASO 4: Los usuarios admin.rrhh, gerencia_test y empleado_test deben superar la verificación de contraseña.
    // ========================================================================
    @ParameterizedTest
    @ValueSource(strings = {"admin.rrhh", "gerencia_test", "empleado_test"})
    @DisplayName("Caso 4: Cuentas de prueba requieren obligatoriamente superar verificación de contraseña")
    void cuentasDePrueba_DebenSuperarVerificacionPassword(String nombreUsuarioPrueba) {
        Usuario usuarioPrueba = new Usuario();
        usuarioPrueba.setId(99L);
        usuarioPrueba.setNombreUsuario(nombreUsuarioPrueba);
        usuarioPrueba.setContrasenia("hash_seguro_" + nombreUsuarioPrueba);
        usuarioPrueba.setActivo(true);
        usuarioPrueba.setNombreRol("RRHH");

        // 4a: Con contraseña errónea -> RECHAZADO
        LoginRequestDTO dtoInvalido = new LoginRequestDTO(nombreUsuarioPrueba, "WrongPassword!");
        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(nombreUsuarioPrueba, nombreUsuarioPrueba)).thenReturn(usuarioPrueba);
        when(hashService.verificar("WrongPassword!", "hash_seguro_" + nombreUsuarioPrueba)).thenReturn(false);

        AuthResponseDTO resInvalido = authService.login(dtoInvalido, "127.0.0.1");
        assertFalse(resInvalido.isSuccess());
        assertNull(resInvalido.getToken());

        // 4b: Con contraseña correcta -> EXITOSO
        LoginRequestDTO dtoValido = new LoginRequestDTO(nombreUsuarioPrueba, "PasswordCorrecta1!");
        when(hashService.verificar("PasswordCorrecta1!", "hash_seguro_" + nombreUsuarioPrueba)).thenReturn(true);
        when(jwtConfig.generarToken(eq(nombreUsuarioPrueba), any(), any(), anyList(), any())).thenReturn("jwt.token." + nombreUsuarioPrueba);

        AuthResponseDTO resValido = authService.login(dtoValido, "127.0.0.1");
        assertTrue(resValido.isSuccess());
        assertNotNull(resValido.getToken());
    }

    // ========================================================================
    // CASO 5: Una contraseña vacía o nula nunca produce autenticación exitosa.
    // ========================================================================
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Caso 5: Contraseña nula, vacía o en blanco nunca produce autenticación exitosa")
    void passwordVaciaONula_NuncaProduceAutenticacionExitosa(String passwordVaciaONula) {
        LoginRequestDTO dto = new LoginRequestDTO(USERNAME, passwordVaciaONula);
        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertNull(resultado.getToken());
        verify(jwtConfig, never()).generarToken(any(), any(), any(), any(), any());
        verify(hashService, never()).verificar(any(), any());
    }

    // ========================================================================
    // CASO 6: Una cuenta bloqueada o deshabilitada no puede autenticarse.
    // ========================================================================
    @Test
    @DisplayName("Caso 6a: Cuenta inactiva/deshabilitada no puede autenticarse")
    void cuentaInactiva_NoPuedeAutenticarse() {
        usuarioConDni.setActivo(false);
        LoginRequestDTO dto = new LoginRequestDTO(USERNAME, PASSWORD_VALIDA);
        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Usuario inactivo o desvinculado. No tiene acceso al sistema.", resultado.getMessage());
        assertNull(resultado.getToken());
        verify(hashService, never()).verificar(any(), any());
        verify(jwtConfig, never()).generarToken(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Caso 6b: Cuenta bloqueada por intentos no puede autenticarse")
    void cuentaBloqueada_NoPuedeAutenticarse() {
        usuarioConDni.setCuentaBloqueada(true);
        LoginRequestDTO dto = new LoginRequestDTO(USERNAME, PASSWORD_VALIDA);
        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Cuenta bloqueada - acceso denegado", resultado.getMessage());
        assertNull(resultado.getToken());
        verify(hashService, never()).verificar(any(), any());
        verify(jwtConfig, never()).generarToken(any(), any(), any(), any(), any());
    }

    // ========================================================================
    // CASO 7: Una autenticación fallida no emite un JWT ni restablece indebidamente los intentos fallidos.
    // ========================================================================
    @Test
    @DisplayName("Caso 7: Autenticación fallida no emite JWT, incrementa intentos y bloquea al 3er fallo")
    void autenticacionFallida_NoEmiteJwt_NiReseteaIntentos() {
        usuarioConDni.setIntentosFallidos(2);
        LoginRequestDTO dto = new LoginRequestDTO(USERNAME, "ClaveIncorrecta");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(hashService.verificar("ClaveIncorrecta", "hashed_" + DNI)).thenReturn(false);

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertFalse(resultado.isSuccess());
        assertEquals("Cuenta bloqueada - acceso denegado", resultado.getMessage());
        assertNull(resultado.getToken());
        assertEquals(3, usuarioConDni.getIntentosFallidos());
        assertTrue(usuarioConDni.isCuentaBloqueada());
        verify(jwtConfig, never()).generarToken(any(), any(), any(), any(), any());
    }

    // ========================================================================
    // CASO 8: Una autenticación correcta mantiene el funcionamiento esperado de roles, permisos y emisión de JWT.
    // ========================================================================
    @Test
    @DisplayName("Caso 8: Autenticación correcta emite JWT con rol y permisos esperados")
    void autenticacionCorrecta_EmiteJwtConRolYPermisosEsperados() {
        usuarioConDni.setId(55L);
        usuarioConDni.setNombreRol("EMPLEADO");
        LoginRequestDTO dto = new LoginRequestDTO(USERNAME, PASSWORD_VALIDA);

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(hashService.verificar(PASSWORD_VALIDA, "hashed_" + DNI)).thenReturn(true);
        when(jwtConfig.generarToken(eq(USERNAME), eq("EMPLEADO"), eq(55L), anyList(), any())).thenReturn("jwt.token.esperado");

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertTrue(resultado.isSuccess());
        assertEquals("jwt.token.esperado", resultado.getToken());
        assertEquals("EMPLEADO", resultado.getRol());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> permisosCaptor = ArgumentCaptor.forClass(List.class);
        verify(jwtConfig).generarToken(eq(USERNAME), eq("EMPLEADO"), eq(55L), permisosCaptor.capture(), any());

        List<String> permisos = permisosCaptor.getValue();
        assertTrue(permisos.contains("VER_ASISTENCIA"));
        assertTrue(permisos.contains("REGISTRAR_SOLICITUD"));
        assertTrue(permisos.contains("VER_NOMINA_PROPIA"));
    }

    // ========================================================================
    // PRUEBAS EXISTENTES DEL SISTEMA
    // ========================================================================
    @Test
    @DisplayName("Test 12 — Login con contraseña igual al DNI → requiereCambioPassword=true")
    void loginConDni_RequiereCambioPassword() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setNombreUsuario(USERNAME);
        dto.setPassword(DNI);

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(hashService.verificar(DNI, "hashed_" + DNI)).thenReturn(true);
        when(jwtConfig.generarToken(anyString(), anyString(), any(), any(), any())).thenReturn("token.jwt");

        AuthResponseDTO resultado = authService.login(dto, "127.0.0.1");

        assertTrue(resultado.isRequiereCambioPassword());
    }

    @Test
    @DisplayName("Test 13 — setPassword con contraseña de 7 caracteres → rechazada")
    void setPassword_MenosDeOchoCaracteres_Rechazada() {
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(usuarioConDni);

        AuthResponseDTO resultado = authService.setPassword("abc1234", "abc1234", USERNAME);

        assertFalse(resultado.isSuccess());
        assertEquals("La contraseña debe tener al menos 8 caracteres", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 14 — setPassword con confirmación diferente → rechazada")
    void setPassword_NoCoinciden_Rechazada() {
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(usuarioConDni);

        AuthResponseDTO resultado = authService.setPassword(PASSWORD_VALIDA, "OtraPass1!", USERNAME);

        assertFalse(resultado.isSuccess());
        assertEquals("Las contraseñas no coinciden", resultado.getMessage());
    }

    @Test
    @DisplayName("Test 15 — setPassword válida → contraseña actualizada correctamente")
    void setPassword_Valida_Guardada() {
        when(usuarioRepository.findByUsername(USERNAME)).thenReturn(usuarioConDni);
        when(hashService.encriptar(PASSWORD_VALIDA)).thenReturn("hashed_nueva");

        AuthResponseDTO resultado = authService.setPassword(PASSWORD_VALIDA, PASSWORD_VALIDA, USERNAME);

        assertTrue(resultado.isSuccess());
        assertEquals("Contrasenia establecida correctamente", resultado.getMessage());
        verify(usuarioRepository).save(usuarioConDni);
    }

    // ========================================================================
    // 2FA: código incorrecto, bloqueo temporal y precondición (PI1-08, PI1-09, RNF2)
    // ========================================================================
    @Test
    @DisplayName("2FA: código incorrecto responde 'Codigo 2FA invalido' y registra el fallo")
    void verify2FA_CodigoIncorrecto_RespondeInvalidoYRegistraFallo() {
        usuarioConDni.setSecret2FA("SECRETO");
        TwoFactorRequestDTO dto = new TwoFactorRequestDTO();
        dto.setUsername(USERNAME);
        dto.setCode("12345");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(twoFactorService.verificarCodigo("SECRETO", 12345)).thenReturn(false);

        TwoFactorResponseDTO resultado = authService.verify2FA(dto);

        assertFalse(resultado.isSuccess());
        assertEquals("Codigo 2FA invalido", resultado.getMessage());
        assertEquals(1, usuarioConDni.getIntentosFalla2fa());
        assertNull(usuarioConDni.getBloqueo2faHasta());
        verify(usuarioRepository).save(usuarioConDni);
    }

    @Test
    @DisplayName("2FA: el tercer código incorrecto bloquea la verificación por 5 minutos")
    void verify2FA_TercerFallo_BloqueaVerificacionPor5Minutos() {
        usuarioConDni.setSecret2FA("SECRETO");
        usuarioConDni.setIntentosFalla2fa(2);
        TwoFactorRequestDTO dto = new TwoFactorRequestDTO();
        dto.setUsername(USERNAME);
        dto.setCode("1234567");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(twoFactorService.verificarCodigo(anyString(), anyInt())).thenReturn(false);

        TwoFactorResponseDTO resultado = authService.verify2FA(dto);

        assertFalse(resultado.isSuccess());
        assertEquals("Cuenta bloqueada temporalmente por 5 minutos", resultado.getMessage());
        assertEquals(3, usuarioConDni.getIntentosFalla2fa());
        assertNotNull(usuarioConDni.getBloqueo2faHasta());
        assertTrue(usuarioConDni.getBloqueo2faHasta().isAfter(LocalDateTime.now().plusMinutes(4)));
    }

    @Test
    @DisplayName("2FA: mientras dure el bloqueo no se valida el código")
    void verify2FA_Bloqueado_NoValidaElCodigo() {
        usuarioConDni.setSecret2FA("SECRETO");
        usuarioConDni.setIntentosFalla2fa(3);
        usuarioConDni.setBloqueo2faHasta(LocalDateTime.now().plusMinutes(3));
        TwoFactorRequestDTO dto = new TwoFactorRequestDTO();
        dto.setUsername(USERNAME);
        dto.setCode("12345");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);

        TwoFactorResponseDTO resultado = authService.verify2FA(dto);

        assertFalse(resultado.isSuccess());
        assertEquals("Acceso bloqueado temporalmente (5 minutos)", resultado.getMessage());
        verify(twoFactorService, never()).verificarCodigo(anyString(), anyInt());
    }

    @Test
    @DisplayName("2FA: vencido el bloqueo de 5 minutos se vuelve a permitir el intento")
    void verify2FA_BloqueoVencido_PermiteIntentarDeNuevo() {
        usuarioConDni.setSecret2FA("SECRETO");
        usuarioConDni.setIntentosFalla2fa(3);
        usuarioConDni.setBloqueo2faHasta(LocalDateTime.now().minusMinutes(1));
        TwoFactorRequestDTO dto = new TwoFactorRequestDTO();
        dto.setUsername(USERNAME);
        dto.setCode("12345");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);
        when(twoFactorService.verificarCodigo("SECRETO", 12345)).thenReturn(false);

        TwoFactorResponseDTO resultado = authService.verify2FA(dto);

        assertEquals("Codigo 2FA invalido", resultado.getMessage());
        assertEquals(1, usuarioConDni.getIntentosFalla2fa());
        assertNull(usuarioConDni.getBloqueo2faHasta());
        verify(twoFactorService).verificarCodigo("SECRETO", 12345);
    }

    @Test
    @DisplayName("2FA: usuario sin 2FA activado no puede verificar un código")
    void verify2FA_SinSecreto_RespondeQueNoTieneDosFA() {
        TwoFactorRequestDTO dto = new TwoFactorRequestDTO();
        dto.setUsername(USERNAME);
        dto.setCode("12345");

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME)).thenReturn(usuarioConDni);

        TwoFactorResponseDTO resultado = authService.verify2FA(dto);

        assertFalse(resultado.isSuccess());
        assertEquals("El usuario no tiene 2FA activado", resultado.getMessage());
        verify(twoFactorService, never()).verificarCodigo(anyString(), anyInt());
    }
}
