package com.rrhh.GestionEmpleados.Application.dto;

import java.time.LocalDate;

public class ActualizarEmpleadoRequestDTO {

    private LocalDate fechaNac;
    private String sexo;
    private String estadoCivil;
    private String direccion;
    private String correo;
    private String telefono;

    public ActualizarEmpleadoRequestDTO() {
    }

    public ActualizarEmpleadoRequestDTO(
            LocalDate fechaNac,
            String sexo,
            String estadoCivil,
            String direccion,
            String correo,
            String telefono
    ) {
        this.fechaNac = fechaNac;
        this.sexo = sexo;
        this.estadoCivil = estadoCivil;
        this.direccion = direccion;
        this.correo = correo;
        this.telefono = telefono;
    }

    public LocalDate getFechaNac() {
        return fechaNac;
    }

    public void setFechaNac(LocalDate fechaNac) {
        this.fechaNac = fechaNac;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
}