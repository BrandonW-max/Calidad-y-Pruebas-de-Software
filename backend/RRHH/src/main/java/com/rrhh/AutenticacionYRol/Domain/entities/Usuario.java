
package com.rrhh.AutenticacionYRol.Domain.entities;

import java.time.LocalDateTime;

public class Usuario {
    private Long IdUsuario;
    private String nombreUsuario;
    private String contrasenia;
    private int intentosFallidos;
    private boolean cuentaBloqueada;
    private LocalDateTime fechaBloqueo; 
    private boolean dosFAActivado;
    private String secret2FA;
    private String nombreRol; 

    

    public boolean puedeIntentarLogin() {
        if (!cuentaBloqueada) return true;

       
        if (fechaBloqueo != null && fechaBloqueo.plusMinutes(5).isBefore(LocalDateTime.now())) {
            resetearIntentos(); 
            return true;
        }
        return false;
    }

    public void registrarFallo() {
        this.intentosFallidos++;
        if (this.intentosFallidos >= 5) {
            this.cuentaBloqueada = true;
            this.fechaBloqueo = LocalDateTime.now(); 
        }
    }

    public void resetearIntentos() {
        this.intentosFallidos = 0;
        this.cuentaBloqueada = false;
        this.fechaBloqueo = null;
    }

    public Long getId() {
        return IdUsuario;
    }

    public void setId(Long id) {
        this.IdUsuario = id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public boolean isCuentaBloqueada() {
        return cuentaBloqueada;
    }

    public void setCuentaBloqueada(boolean cuentaBloqueada) {
        this.cuentaBloqueada = cuentaBloqueada;
    }

    public LocalDateTime getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(LocalDateTime fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }

    public boolean isDosFAActivado() {
        return dosFAActivado;
    }

    public void setDosFAActivado(boolean dosFAActivado) {
        this.dosFAActivado = dosFAActivado;
    }

    public String getSecret2FA() {
        return secret2FA;
    }

    public void setSecret2FA(String secret2fa) {
        secret2FA = secret2fa;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    
}
