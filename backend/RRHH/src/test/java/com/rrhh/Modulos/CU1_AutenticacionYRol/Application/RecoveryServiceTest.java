package com.rrhh.Modulos.CU1_AutenticacionYRol.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.ResetPasswordRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RecoveryService;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaPasswordResetTokenRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.PasswordResetTokenModel;
import com.rrhh.Shared.persistence.UsuarioModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RecoveryServiceTest {

    private JpaUsuarioRepository usuarioRepository;
    private JpaPasswordResetTokenRepository tokenRepository;
    private EmailService emailService;
    private PasswordEncoder passwordEncoder;
    private RecoveryService recoveryService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(JpaUsuarioRepository.class);
        tokenRepository = mock(JpaPasswordResetTokenRepository.class);
        emailService = mock(EmailService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        recoveryService = new RecoveryService(usuarioRepository, tokenRepository, emailService, passwordEncoder);
        ReflectionTestUtils.setField(recoveryService, "frontendUrl", "http://localhost:5173");
    }

    @Test
    @DisplayName("Solicitar recuperación con usuario válido envía correo")
    void solicitarRecuperacion_Exitoso() {
        UsuarioModel user = new UsuarioModel();
        user.setIdUsuario(1L);
        user.setActivo(true);
        user.setCorreoInst("test@hospital.com");

        EmpleadoModel emp = new EmpleadoModel();
        emp.setCorreo("personal@gmail.com");
        user.setEmpleado(emp);

        when(usuarioRepository.findByIdentifier("testuser")).thenReturn(Optional.of(user));

        RecoveryRequestDTO dto = new RecoveryRequestDTO();
        dto.setIdentifier("testuser");

        RecoveryResponseDTO res = recoveryService.solicitarRecuperacion(dto);
        assertTrue(res.isSuccess());
        verify(tokenRepository).save(any(PasswordResetTokenModel.class));
        verify(emailService).enviarCorreoRecuperacion(eq("personal@gmail.com"), anyString());
    }

    @Test
    @DisplayName("Solicitar recuperación con usuario inexistente responde genérico anti-enumeración")
    void solicitarRecuperacion_UsuarioInexistente() {
        when(usuarioRepository.findByIdentifier("noexiste")).thenReturn(Optional.empty());

        RecoveryRequestDTO dto = new RecoveryRequestDTO();
        dto.setIdentifier("noexiste");

        RecoveryResponseDTO res = recoveryService.solicitarRecuperacion(dto);
        assertTrue(res.isSuccess());
        verify(tokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cambiar contraseña con token válido y vigente")
    void cambiarContrasenia_Exitoso() {
        UsuarioModel user = new UsuarioModel();
        user.setIdUsuario(1L);

        PasswordResetTokenModel token = new PasswordResetTokenModel();
        token.setToken("token-valido-123");
        token.setUsuario(user);
        token.setExpiracion(LocalDateTime.now().plusMinutes(10));
        token.setUsado(false);

        when(tokenRepository.findByToken("token-valido-123")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("NuevaClave123.")).thenReturn("encodedHash");

        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();
        dto.setToken("token-valido-123");
        dto.setNuevaContrasenia("NuevaClave123.");
        dto.setConfirmarContrasenia("NuevaClave123.");

        RecoveryResponseDTO res = recoveryService.cambiarContrasenia(dto);
        assertTrue(res.isSuccess());
        assertTrue(token.getUsado());
        verify(usuarioRepository).save(user);
    }

    @Test
    @DisplayName("Cambiar contraseña con contraseñas que no coinciden")
    void cambiarContrasenia_ContraseniasNoCoinciden() {
        ResetPasswordRequestDTO dto = new ResetPasswordRequestDTO();
        dto.setToken("token-123");
        dto.setNuevaContrasenia("Clave1.");
        dto.setConfirmarContrasenia("Clave2.");

        RecoveryResponseDTO res = recoveryService.cambiarContrasenia(dto);
        assertFalse(res.isSuccess());
        assertEquals("Las contraseñas no coinciden", res.getMessage());
    }
}

