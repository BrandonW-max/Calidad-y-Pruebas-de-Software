package com.rrhh.Modulos.CU2_GestionEmpleados.Domain.catalogo;

import java.util.List;
import java.util.Map;

/**
 * Cargos válidos por departamento. Debe mantenerse igual a DEPT_CARGOS
 * en frontend/rrhh-frontend/src/pages/GestionEmpleados.jsx.
 */
public final class CatalogoCargos {

    private static final Map<String, List<String>> CARGOS_POR_DEPARTAMENTO = Map.of(
            "Recursos Humanos", List.of("Especialista RRHH", "Analista RRHH", "Coordinador RRHH", "Asistente RRHH", "Auxiliar RRHH", "Administrador RRHH"),
            "Dirección General", List.of("Director General", "Subgerente", "Gerente Administrativo", "Asistente de Gerencia"),
            "Emergencia", List.of("Médico de Emergencia", "Paramédico", "Triagista", "Técnico de Emergencias"),
            "Enfermería", List.of("Enfermera Jefe", "Enfermera", "Enfermero", "Auxiliar de Enfermería"),
            "Medicina", List.of("Médico General", "Médico Especialista", "Médico Residente", "Médico de Guardia"),
            "Administración", List.of("Administrador", "Administradora", "Recepcionista", "Asistente Administrativo", "Secretaria"),
            "Laboratorio", List.of("Técnico de Laboratorio", "Analista de Laboratorio", "Biólogo", "Químico Farmacéutico"),
            "Finanzas", List.of("Contador", "Contadora", "Analista Financiero", "Tesorero", "Asistente Contable"),
            "Radiología", List.of("Radiólogo", "Técnico en Radiología", "Operador de Equipos de Imagen")
    );

    private CatalogoCargos() {}

    public static List<String> cargosDe(String departamento) {
        return CARGOS_POR_DEPARTAMENTO.getOrDefault(departamento, List.of());
    }

    public static boolean pertenece(String departamento, String cargo) {
        return cargosDe(departamento).contains(cargo);
    }
}
