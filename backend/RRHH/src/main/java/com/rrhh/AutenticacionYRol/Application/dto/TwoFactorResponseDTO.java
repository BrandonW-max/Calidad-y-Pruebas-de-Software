
package com.rrhh.AutenticacionYRol.Application.dto;

public class TwoFactorResponseDTO {

    private boolean success;
    private String message;
    private String token;
    private String rol;

    public TwoFactorResponseDTO() {
    }

    public TwoFactorResponseDTO(
            boolean success,
            String message,
            String token,
            String rol
    ) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.rol = rol;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}