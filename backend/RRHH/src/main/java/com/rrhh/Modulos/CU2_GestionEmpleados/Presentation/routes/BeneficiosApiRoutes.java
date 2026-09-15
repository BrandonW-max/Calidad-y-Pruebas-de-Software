package com.rrhh.Modulos.CU2_GestionEmpleados.Presentation.routes;

public class BeneficiosApiRoutes {

    private BeneficiosApiRoutes() {}

    public static final String BASE = "/api/v1/empleados/{idEmpleado}/beneficios";

    public static final String FAMILIARES = "/familiares";
    public static final String FAMILIAR_POR_ID = "/familiares/{idFamiliar}";
}
