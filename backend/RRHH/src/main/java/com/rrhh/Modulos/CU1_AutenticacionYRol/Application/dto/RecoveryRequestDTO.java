package com.rrhh.Modulos.CU1_AutenticacionYRol.Application.dto;

public class RecoveryRequestDTO {

    private String email;

    public RecoveryRequestDTO() {
    }

    public RecoveryRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}