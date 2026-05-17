package com.rrhh.AutenticacionYRol.domain.dtos;

public class AuthResponseDTO {

    private boolean success;
    private String token;
    private String rol;
    private String message;
    private boolean require2FA;

    public AuthResponseDTO() {}

    public AuthResponseDTO(boolean success, String token, String rol, String message, boolean require2FA) {
        this.success = success;
        this.token = token;
        this.rol = rol;
        this.message = message;
        this.require2FA = require2FA;
    }

    public boolean isSuccess() { return success; }
    public String getToken() { return token; }
    public String getRol() { return rol; }
    public String getMessage() { return message; }
    public boolean isRequire2FA() { return require2FA; }
}