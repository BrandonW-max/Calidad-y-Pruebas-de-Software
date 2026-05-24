package com.rrhh.AutenticacionYRol.Presentation.controllers;

import com.rrhh.AutenticacionYRol.Application.dto.AuthResponseDTO;
import com.rrhh.AutenticacionYRol.Application.dto.LoginRequestDTO;
import com.rrhh.AutenticacionYRol.Application.dto.RecoveryRequestDTO;
import com.rrhh.AutenticacionYRol.Application.dto.RecoveryResponseDTO;
import com.rrhh.AutenticacionYRol.Application.dto.ResetPasswordRequestDTO;
import com.rrhh.AutenticacionYRol.Application.dto.TwoFactorRequestDTO;
import com.rrhh.AutenticacionYRol.Application.dto.TwoFactorResponseDTO;

import com.rrhh.AutenticacionYRol.Application.services.IAuthService;
import com.rrhh.AutenticacionYRol.Application.services.RecoveryService;
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

    @Autowired
    private RecoveryService recoveryService;

    @PostMapping(ApiRoutes.LOGIN)
    public ResponseEntity<AuthResponseDTO> login(
            @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest
    ) {

        String clientIp = httpRequest.getRemoteAddr();

        AuthResponseDTO response =
                authService.login(request, clientIp);

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiRoutes.VERIFY_2FA)
    public ResponseEntity<TwoFactorResponseDTO> verify(
            @RequestBody TwoFactorRequestDTO request
    ) {

        TwoFactorResponseDTO response =
                authService.verify2FA(request);

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiRoutes.RECOVERY)
    public ResponseEntity<RecoveryResponseDTO> recovery(
            @RequestBody RecoveryRequestDTO request
    ) {

        RecoveryResponseDTO response =
                recoveryService.solicitarRecuperacion(request);

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping(ApiRoutes.RESET_PASSWORD)
    public ResponseEntity<RecoveryResponseDTO> resetPassword(
            @RequestBody ResetPasswordRequestDTO request
    ) {

        RecoveryResponseDTO response =
                recoveryService.cambiarContrasenia(request);

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping(ApiRoutes.ENABLE_2FA)
    public ResponseEntity<byte[]> enable2FA(
            @PathVariable String username
    ) throws Exception {

        byte[] qrImage =
                authService.enable2FA(username);

        return ResponseEntity.ok()
                .contentType(
                        org.springframework.http.MediaType.IMAGE_PNG
                )
                .header(
                        "Content-Disposition",
                        "inline; filename=qr.png"
                )
                .header(
                        "Cache-Control",
                        "no-cache"
                )
                .header(
                        "Pragma",
                        "no-cache"
                )
                .header(
                        "Expires",
                        "0"
                )
                .body(qrImage);
    }
}