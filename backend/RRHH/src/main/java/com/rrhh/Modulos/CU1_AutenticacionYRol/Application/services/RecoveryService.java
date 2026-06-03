package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.ResetPasswordRequestDTO;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaPasswordResetTokenRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;

import com.rrhh.Shared.persistence.PasswordResetTokenModel;
import com.rrhh.Shared.persistence.UsuarioModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RecoveryService {

    @Autowired
    private JpaUsuarioRepository usuarioRepository;

    @Autowired
    private JpaPasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public RecoveryResponseDTO solicitarRecuperacion(
            RecoveryRequestDTO dto
    ) {

        if (dto == null || dto.getEmail() == null || dto.getEmail().isBlank()) {
            return new RecoveryResponseDTO(
                    false,
                    "Debe ingresar un correo válido"
            );
        }

        UsuarioModel usuario =
                usuarioRepository
                        .findByCorreoPersonalOInstitucional(dto.getEmail())
                        .orElse(null);

        /*
         * Por seguridad, no revelamos si el correo existe o no.
         * Así evitamos enumeración de usuarios.
         */
        if (usuario == null) {
            return new RecoveryResponseDTO(
                    true,
                    "Si el correo existe, se enviará un enlace de recuperación."
            );
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            return new RecoveryResponseDTO(
                    false,
                    "La cuenta está inactiva. Contacte con RRHH."
            );
        }

        String token = UUID.randomUUID().toString();

        PasswordResetTokenModel resetToken =
                new PasswordResetTokenModel();

        resetToken.setToken(token);
        resetToken.setUsuario(usuario);
        resetToken.setExpiracion(LocalDateTime.now().plusMinutes(15));
        resetToken.setUsado(false);

        tokenRepository.save(resetToken);

        String link =
                "http://localhost:4200/reset-password?token=" + token;

        /*
         * El correo destino será el correo que ingresó.
         * Si quieres forzar que siempre sea el correo personal:
         * usuario.getEmpleado().getCorreo()
         */
        emailService.enviarCorreoRecuperacion(
                dto.getEmail(),
                link
        );

        return new RecoveryResponseDTO(
                true,
                "Si el correo existe, se enviará un enlace de recuperación."
        );
    }

    @Transactional
    public RecoveryResponseDTO cambiarContrasenia(
            ResetPasswordRequestDTO dto
    ) {

        if (dto == null || dto.getToken() == null || dto.getToken().isBlank()) {
            return new RecoveryResponseDTO(
                    false,
                    "Token inválido"
            );
        }

        if (dto.getNuevaContrasenia() == null || dto.getNuevaContrasenia().isBlank()) {
            return new RecoveryResponseDTO(
                    false,
                    "Debe ingresar una nueva contraseña"
            );
        }

        if (!dto.getNuevaContrasenia().equals(dto.getConfirmarContrasenia())) {
            return new RecoveryResponseDTO(
                    false,
                    "Las contraseñas no coinciden"
            );
        }

        PasswordResetTokenModel resetToken =
                tokenRepository
                        .findByToken(dto.getToken())
                        .orElse(null);

        if (resetToken == null) {
            return new RecoveryResponseDTO(
                    false,
                    "Token inválido"
            );
        }

        if (Boolean.TRUE.equals(resetToken.getUsado())) {
            return new RecoveryResponseDTO(
                    false,
                    "El token ya fue utilizado"
            );
        }

        if (resetToken.getExpiracion().isBefore(LocalDateTime.now())) {
            return new RecoveryResponseDTO(
                    false,
                    "El token ha expirado"
            );
        }

        UsuarioModel usuario = resetToken.getUsuario();

        usuario.setContrasenia(
                passwordEncoder.encode(dto.getNuevaContrasenia())
        );

        usuario.setIntentosFallidos(0);
        usuario.setCuentaBloqueada(false);
        usuario.setFechaBloqueo(null);

        usuarioRepository.save(usuario);

        resetToken.setUsado(true);
        tokenRepository.save(resetToken);

        return new RecoveryResponseDTO(
                true,
                "Contraseña actualizada correctamente"
        );
    }
}