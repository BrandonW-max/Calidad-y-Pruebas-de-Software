package com.rrhh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SistemaRrhhApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaRrhhApplication.class, args);
    }
}