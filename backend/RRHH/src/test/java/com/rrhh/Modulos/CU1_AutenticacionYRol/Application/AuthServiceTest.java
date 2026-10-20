package com.rrhh.Modulos.CU1_AutenticacionYRol.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.AuthResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.LoginRequestDTO;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService - Autenticación y cambio de contraseña")
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
        ReflectionTestUtils.setField(authService, "usuariosPruebaConfig", "");
        usuarioConDni = new Usuario();
        usuarioConDni.setNombreUsuario(USERNAME);
        usuarioConDni.setNumeroDi(DNI);
        usuarioConDni.setContrasenia("hashed_" + DNI);
        usuarioConDni.setActivo(true);
        usuarioConDni.setNombreRol("EMPLEADO");
    }

    @Test
    @DisplayName("Test 12 — Login con contraseña igual al DNI → requiereCambioPassword=true")
    void loginConDni_RequiereCambioPassword() {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setNombreUsuario(USERNAME);
        dto.setPassword(DNI);

        when(usuarioRepository.findByNombreUsuarioOrCorreoInst(USERNAME, USERNAME))
                .thenReturn(usuarioConDni);
        when(hashService.verificar(DNI, "hashed_" + DNI)).thenReturn(true);
        when(jwtConfig.generarToken(anyString(), anyString(), any(), any(), any()))
                .thenReturn("token.jwt");

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
}
