package com.rrhh;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * PRUEBA DE INTEGRACIÓN – Contexto de Spring Boot
 * Verifica que el contexto de la aplicación levanta correctamente
 * con todas las dependencias, auto-configuraciones y beans.
 * Framework: JUnit 5 + Spring Boot Test (@SpringBootTest)
 */
@SpringBootTest
@DisplayName("Integración – Spring Boot Context Load")
class RrhhApplicationTests {

    @Test
    @DisplayName("TC-INT-01: El contexto de Spring Boot carga sin errores")
    void contextLoads() {
        // Si el contexto no carga, el test falla automáticamente.
        // Este test valida la configuración de toda la aplicación:
        // - Beans de Spring (Services, Repositories, Controllers)
        // - Configuración de Seguridad (JWT, Spring Security)
        // - Conexión a Base de Datos (PostgreSQL + Flyway)
        // - Propiedades personalizadas (RRHHProperties)
    }
}
