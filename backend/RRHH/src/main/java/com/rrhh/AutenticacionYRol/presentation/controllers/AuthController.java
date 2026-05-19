package com.rrhh.AutenticacionYRol.Presentation.controllers;

import com.rrhh.AutenticacionYRol.Application.dto.*;
import com.rrhh.AutenticacionYRol.Application.services.IAuthService; 
import com.rrhh.AutenticacionYRol.Presentation.routes.ApiRoutes;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiRoutes.AUTH_BASE) 
@CrossOrigin("*") 
public class AuthController {

    @Autowired
    private IAuthService authService; 

    
    
    @PostMapping(ApiRoutes.LOGIN)
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        
        String clientIp = httpRequest.getRemoteAddr(); 
        
        AuthResponseDTO response = authService.login(request, clientIp);
        
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    
    @PostMapping(ApiRoutes.VERIFY_2FA)
    public ResponseEntity<TwoFactorResponseDTO> verify(@RequestBody TwoFactorRequestDTO request) {
        TwoFactorResponseDTO response = authService.verify2FA(request);
        
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiRoutes.RECOVERY)
    public ResponseEntity<AuthResponseDTO> recovery(@RequestBody String email) {
       
        return ResponseEntity.ok(new AuthResponseDTO(true, "Si el correo está registrado, recibirá instrucciones en breve."));
    }
}
