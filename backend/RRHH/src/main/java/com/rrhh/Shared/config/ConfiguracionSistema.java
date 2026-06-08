package com.rrhh.Shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionSistema {

    @Value("${sistema.nombre-empresa:SAN GABRIEL S.A.C.}")
    private String nombreEmpresa;

    @Value("${sistema.max-intentos-login:5}")
    private int maxIntentosLogin;

    @Value("${sistema.zona-horaria:America/Lima}")
    private String zonaHoraria;

    public ConfiguracionSistema() {
    }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public int getMaxIntentosLogin() { return maxIntentosLogin; }
    public void setMaxIntentosLogin(int maxIntentosLogin) { this.maxIntentosLogin = maxIntentosLogin; }

    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String zonaHoraria) { this.zonaHoraria = zonaHoraria; }
}
