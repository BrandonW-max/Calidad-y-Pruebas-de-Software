package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

public class LoginRequestDTO {
    private String nombreUsuario;
    private String password;

    public LoginRequestDTO() {
    }

    // Constructor con parámetros
    public LoginRequestDTO(String nombreUsuario, String password) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
