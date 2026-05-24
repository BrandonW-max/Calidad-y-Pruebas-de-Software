package com.rrhh.AutenticacionYRol.Application.services;

import com.rrhh.AutenticacionYRol.Application.dto.*;
import com.rrhh.AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.AutenticacionYRol.Domain.repository.IUsuarioRepository;
import com.rrhh.AutenticacionYRol.Infrastructure.config.JwtConfig;
import com.rrhh.AutenticacionYRol.Infrastructure.services.TwoFactorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private TwoFactorService twoFactorService;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IHistorialRepository historialRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto, String ip) {

        Usuario user =
                usuarioRepository.findByNombreUsuarioOrCorreoInst(
                        dto.getNombreUsuario(),
                        dto.getNombreUsuario()
                );

        if (user == null) {
            return new AuthResponseDTO(
                    false,
                    "Credenciales inválidas"
            );
        }

        // =========================
        // VALIDAR USUARIO ACTIVO
        // =========================
        if (!user.isActivo()) {
            return new AuthResponseDTO(
                    false,
                    "Usuario inactivo o desvinculado. No tiene acceso al sistema."
            );
        }

        if (!user.puedeIntentarLogin()) {
            return new AuthResponseDTO(
                    false,
                    "Cuenta bloqueada por 5 minutos"
            );
        }

        if (passwordEncoder.matches(dto.getPassword(), user.getContrasenia())) {

            user.resetearIntentos();
            usuarioRepository.save(user);

            Historial log =
                    new Historial(
                            user.getId(),
                            "LOGIN_EXITOSO",
                            ip
                    );

            historialRepository.save(log);

            return new AuthResponseDTO(
                    true,
                    "2FA requerido",
                    user.getNombreRol(),
                    true
            );

        } else {

            user.registrarFallo();
            usuarioRepository.save(user);

            Historial logFallo =
                    new Historial(
                            user.getId(),
                            "LOGIN_FALLIDO",
                            ip
                    );

            historialRepository.save(logFallo);

            String msg =
                    user.isCuentaBloqueada()
                            ? "Cuenta bloqueada"
                            : "Contraseña incorrecta";

            return new AuthResponseDTO(
                    false,
                    msg
            );
        }
    }

    @Override
    public TwoFactorResponseDTO verify2FA(TwoFactorRequestDTO dto) {

        Usuario user =
                usuarioRepository.findByNombreUsuarioOrCorreoInst(
                        dto.getUsername(),
                        dto.getUsername()
                );

        if (user == null) {
            return new TwoFactorResponseDTO(
                    false,
                    "Usuario no encontrado",
                    null,
                    null
            );
        }

        // =========================
        // VALIDAR USUARIO ACTIVO
        // =========================
        if (!user.isActivo()) {
            return new TwoFactorResponseDTO(
                    false,
                    "Usuario inactivo o desvinculado. No tiene acceso al sistema.",
                    null,
                    null
            );
        }

        if (user.getSecret2FA() == null || user.getSecret2FA().isBlank()) {
            return new TwoFactorResponseDTO(
                    false,
                    "El usuario no tiene 2FA activado",
                    null,
                    null
            );
        }

        boolean codigoValido =
                twoFactorService.verificarCodigo(
                        user.getSecret2FA(),
                        Integer.parseInt(dto.getCode())
                );

        if (!codigoValido) {
            return new TwoFactorResponseDTO(
                    false,
                    "Código incorrecto",
                    null,
                    null
            );
        }

        String token =
                jwtConfig.generarToken(
                        user.getNombreUsuario(),
                        user.getNombreRol(),
                        user.getId()
                );

        return new TwoFactorResponseDTO(
                true,
                "Acceso concedido",
                token,
                user.getNombreRol()
        );
    }

    @Override
    public byte[] enable2FA(String username) throws Exception {

        Usuario user =
                usuarioRepository.findByNombreUsuarioOrCorreoInst(
                        username,
                        username
                );

        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        // =========================
        // VALIDAR USUARIO ACTIVO
        // =========================
        if (!user.isActivo()) {
            throw new RuntimeException(
                    "Usuario inactivo o desvinculado. No puede activar 2FA."
            );
        }

        String secret =
                twoFactorService.generarSecret();

        user.setSecret2FA(secret);
        user.setDosFAActivado(true);

        usuarioRepository.save(user);

        String qrText =
                twoFactorService.generarQRUrl(
                        username,
                        secret
                );

        return twoFactorService.generarQRImagen(qrText);
    }
}