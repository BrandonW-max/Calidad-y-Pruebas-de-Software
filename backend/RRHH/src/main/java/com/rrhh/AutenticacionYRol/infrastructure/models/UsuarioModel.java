package com.rrhh.AutenticacionYRol.Infrastructure.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario") 
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario") 
    private Long idUsuario;

    @Column(name = "nombre_usuario") 
    private String nombreUsuario;

    @Column(name = "contrasenia")
    private String contrasenia;

    @Column(name = "intentos_fallidos")
    private int intentosFallidos;

    @Column(name = "cuenta_bloqueada")
    private boolean cuentaBloqueada;

    @Column(name = "fecha_bloqueo")
    private LocalDateTime fechaBloqueo;

    @Column(name = "dosfa_activado")
    private boolean dosfaActivado;

    @Column(name = "secret_2fa")
    private String secret2fa;

    @Column(name = "id_rol") 
    private Long idRol;

    @Column(name = "id_empleado") 
    private Long idEmpleado;

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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

    public boolean isDosfaActivado() {
        return dosfaActivado;
    }

    public void setDosfaActivado(boolean dosfaActivado) {
        this.dosfaActivado = dosfaActivado;
    }

    public String getSecret2fa() {
        return secret2fa;
    }

    public void setSecret2fa(String secret2fa) {
        this.secret2fa = secret2fa;
    }

    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
    }

    public Long getIdEmpleado() {
        return idEmpleado;
    }

    public void setIdEmpleado(Long idEmpleado) {
        this.idEmpleado = idEmpleado;
    }

    
}
