package com.rrhh.Modulos.CU1_AutenticacionYRol.Presentation.controllers;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.AuthResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.ChangePasswordRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.LoginRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.RecoveryResponseDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.ResetPasswordRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.TwoFactorRequestDTO;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto.TwoFactorResponseDTO;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.IAuthService;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RecoveryService;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Presentation.routes.ApiRoutes;

import com.rrhh.config.security.AuthenticatedUser;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping(ApiRoutes.AUTH_BASE)
public class AuthController {

    @Autowired
    private IAuthService authService;

    @Autowired
    private RecoveryService recoveryService;

    @PostMapping(ApiRoutes.LOGIN)
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO request,
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
            @Valid @RequestBody TwoFactorRequestDTO request
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
            @Valid @RequestBody RecoveryRequestDTO request
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
            @Valid @RequestBody ResetPasswordRequestDTO request
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

        AuthenticatedUser authUser = (AuthenticatedUser) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        if (!authUser.getUsername().equals(username)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo puedes activar 2FA en tu propia cuenta"
            );
        }

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

    @PostMapping(ApiRoutes.CHANGE_PASSWORD)
    public ResponseEntity<AuthResponseDTO> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request
    ) {

        AuthenticatedUser authUser = (AuthenticatedUser) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();

        AuthResponseDTO response =
                authService.changePassword(request, authUser.getUsername());

        if (!response.isSuccess()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(response);
        }

        return ResponseEntity.ok(response);
    }
}