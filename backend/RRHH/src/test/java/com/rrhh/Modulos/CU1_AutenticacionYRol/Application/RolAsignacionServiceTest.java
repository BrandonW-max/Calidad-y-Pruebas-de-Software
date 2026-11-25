package com.rrhh.Modulos.CU1_AutenticacionYRol.Application;

import com.rrhh.Modulos.CU1_AutenticacionYRol.Application.services.RolAsignacionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RolAsignacionService - rol según departamento y cargo (RF02)")
class RolAsignacionServiceTest {

    private final RolAsignacionService servicio = new RolAsignacionService();

    @ParameterizedTest(name = "{0} / {1} -> {2}")
    @CsvSource({
            "Recursos Humanos, Administrador RRHH, RRHH",
            "Recursos Humanos, Especialista RRHH, RRHH",
            "Recursos Humanos, Asistente RRHH, RRHH",
            "Recursos Humanos, Jefe de Personal, GERENCIA",
            "Finanzas, Director Financiero, GERENCIA",
            "Emergencia, Médico de Emergencia, EMPLEADO"
    })
    void asignaElRolCorrecto(String departamento, String cargo, String rolEsperado) {
        assertEquals(rolEsperado, servicio.determinarRol(departamento, cargo));
    }
}
