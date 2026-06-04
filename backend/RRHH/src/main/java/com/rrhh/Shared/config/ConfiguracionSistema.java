package com.rrhh.Shared.config;

public class ConfiguracionSistema {

    private static volatile ConfiguracionSistema instance;

    private String nombreEmpresa;
    private int maxIntentosLogin;
    private String zonaHoraria;

    private ConfiguracionSistema() {
        this.nombreEmpresa = "SAN GABRIEL S.A.C.";
        this.maxIntentosLogin = 5;
        this.zonaHoraria = "America/Lima";
    }

    public static ConfiguracionSistema getInstance() {
        if (instance == null) {
            synchronized (ConfiguracionSistema.class) {
                if (instance == null) {
                    instance = new ConfiguracionSistema();
                }
            }
        }
        return instance;
    }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public void setNombreEmpresa(String nombreEmpresa) { this.nombreEmpresa = nombreEmpresa; }

    public int getMaxIntentosLogin() { return maxIntentosLogin; }
    public void setMaxIntentosLogin(int maxIntentosLogin) { this.maxIntentosLogin = maxIntentosLogin; }

    public String getZonaHoraria() { return zonaHoraria; }
    public void setZonaHoraria(String zonaHoraria) { this.zonaHoraria = zonaHoraria; }
}
