package com.rrhh.AutenticacionYRol.Application.services;

import com.rrhh.AutenticacionYRol.Domain.entities.Usuario;
import com.rrhh.AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.AutenticacionYRol.Domain.repository.IUsuarioRepository;
import com.rrhh.AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.AutenticacionYRol.Application.dto.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // IMPORTANTE
import org.springframework.stereotype.Service;

@Service
public class AuthService implements IAuthService {

    @Autowired
    private IUsuarioRepository usuarioRepository; 
    
    @Autowired
    private IHistorialRepository historialRepository; 

    @Autowired
    private PasswordEncoder passwordEncoder; // Inyección del encoder para el profe

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto, String ip) {
        Usuario user = usuarioRepository.findByUsername(dto.getNombreUsuario());

        if (user == null) {
            return new AuthResponseDTO(false, "Credenciales inválidas");
        }

        if (!user.puedeIntentarLogin()) {
            return new AuthResponseDTO(false, "Cuenta bloqueada por 5 minutos");
        }

        // 🛡️ CORRECCIÓN: Usar matches() en lugar de equals()
        if (passwordEncoder.matches(dto.getPassword(), user.getContrasenia())) {
            user.resetearIntentos(); 
            usuarioRepository.save(user);

            Historial log = new Historial(user.getId(), "LOGIN_EXITOSO", ip);
            historialRepository.save(log);

            return new AuthResponseDTO(true, "2FA requerido", user.getNombreRol(), true);
        } else {
            user.registrarFallo();
            usuarioRepository.save(user);
            
            Historial logFallo = new Historial(user.getId(), "LOGIN_FALLIDO", ip);
            historialRepository.save(logFallo);
            
            String msg = user.isCuentaBloqueada() ? "Cuenta bloqueada" : "Contraseña incorrecta";
            return new AuthResponseDTO(false, msg);
        }
    }

    @Override
    public TwoFactorResponseDTO verify2FA(TwoFactorRequestDTO dto) {
        if (!dto.getCode().equals("1234")) { 
            return new TwoFactorResponseDTO(false, "Código incorrecto", null, null);
        }

        Usuario user = usuarioRepository.findByUsername(dto.getUsername());
        return new TwoFactorResponseDTO(true, "Acceso concedido", "TOKEN_JWT_GENERADO", user.getNombreRol());
    }
}
