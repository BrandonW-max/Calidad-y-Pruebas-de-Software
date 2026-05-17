package com.rrhh.AutenticacionYRol.presentation.controllers;

import com.rrhh.AutenticacionYRol.business.services.AuthService;
import com.rrhh.AutenticacionYRol.domain.dtos.AuthResponseDTO;
import com.rrhh.AutenticacionYRol.domain.dtos.LoginRequestDTO;
import com.rrhh.AutenticacionYRol.domain.dtos.TwoFactorRequestDTO;
import com.rrhh.AutenticacionYRol.domain.dtos.TwoFactorResponseDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/verify-2fa")
    public TwoFactorResponseDTO verify(@RequestBody TwoFactorRequestDTO request) {
        return authService.verify2FA(request);
    }
}