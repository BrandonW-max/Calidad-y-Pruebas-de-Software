package com.rrhh.AutenticacionYRol.business.services;

import com.rrhh.AutenticacionYRol.data.entity.Usuario;
import com.rrhh.AutenticacionYRol.data.repository.UsuarioRepository;
import com.rrhh.AutenticacionYRol.domain.dtos.AuthResponseDTO;
import com.rrhh.AutenticacionYRol.domain.dtos.LoginRequestDTO;
import com.rrhh.AutenticacionYRol.domain.dtos.TwoFactorRequestDTO;
import com.rrhh.AutenticacionYRol.domain.dtos.TwoFactorResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // LOGIN
    public AuthResponseDTO login(LoginRequestDTO dto) {

        Usuario user = usuarioRepository.findByUsername(dto.getUsername());

        if (user == null) {
            return new AuthResponseDTO(false, null, null, "Usuario no existe", false);
        }

        if (!user.isActivo()) {
            return new AuthResponseDTO(false, null, null, "Usuario desactivado", false);
        }

        if (!user.getPassword().equals(dto.getPassword())) {
            return new AuthResponseDTO(false, null, null, "Contraseña incorrecta", false);
        }

        return new AuthResponseDTO(
                true,
                null,
                user.getRol(),
                "2FA requerido",
                true
        );
    }


    public TwoFactorResponseDTO verify2FA(TwoFactorRequestDTO dto) {

        if (!dto.getCode().equals("1234")) {
            return new TwoFactorResponseDTO(false, "Código incorrecto", null, null);
        }

        Usuario user = usuarioRepository.findByUsername(dto.getUsername());

        return new TwoFactorResponseDTO(
                true,
                "Acceso concedido",
                "FAKE_TOKEN",
                user.getRol()
        );
    }
}