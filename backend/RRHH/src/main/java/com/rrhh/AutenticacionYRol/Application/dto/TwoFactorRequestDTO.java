package com.rrhh.AutenticacionYRol.Application.dto;

public class TwoFactorRequestDTO {
    private String username;
    private String code;

    public String getUsername() { return username; }
    public String getCode() { return code; }

    public void setUsername(String username) { this.username = username; }
    public void setCode(String code) { this.code = code; }
}