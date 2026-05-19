package com.rrhh.AutenticacionYRol.Application.dto;

public class LoginRequestDTO {
    private String nombreUsuario;
    private String password;

    // Constructor vacío (necesario para Spring/JSON)
    public LoginRequestDTO() {
    }

    // Constructor con parámetros
    public LoginRequestDTO(String nombreUsuario, String password) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
    }

    // --- GETTERS Y SETTERS ---
    // Importante: El AuthService usa getNombreUsuario()
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
