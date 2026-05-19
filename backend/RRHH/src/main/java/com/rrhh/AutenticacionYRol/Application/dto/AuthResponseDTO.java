package com.rrhh.AutenticacionYRol.Application.dto;

public class AuthResponseDTO {
    private boolean success;
    private String message;
    private String rol;
    private boolean requires2FA;

  
    public AuthResponseDTO(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    
    public AuthResponseDTO(boolean success, String message, String rol, boolean requires2FA) {
        this.success = success;
        this.message = message;
        this.rol = rol;
        this.requires2FA = requires2FA;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isRequires2FA() {
        return requires2FA;
    }

    public void setRequires2FA(boolean requires2fa) {
        requires2FA = requires2fa;
    }

    
}
