package com.rrhh.AutenticacionYRol.Application.dto;

public class TwoFactorResponseDTO {
    private boolean success;
    private String message;
    private String token;
    private String rol;

    public TwoFactorResponseDTO(boolean success, String message, String token, String rol) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.rol = rol;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public String getRol() { return rol; }
}