package com.rrhh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.InputStream;
import java.util.Properties;

@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class SistemaRrhhApplication {

    public static void main(String[] args) {
        cargarDotEnv();
        configurarZonaHoraria(System.getenv().getOrDefault("SISTEMA_ZONA_HORARIA", "America/Lima"));
        SpringApplication.run(SistemaRrhhApplication.class, args);
    }

    // "Hoy" y los cron usan la zona del hospital, no la de la máquina donde corre el servidor
    static void configurarZonaHoraria(String zona) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(zona));
    }

    private static void cargarDotEnv() {
        try (InputStream input = SistemaRrhhApplication.class
                .getClassLoader()
                .getResourceAsStream(".env")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                props.forEach((key, value) -> {
                    String k = key.toString().trim();
                    String v = value.toString().trim();
                    if (System.getProperty(k) == null) {
                        System.setProperty(k, v);
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }
}
