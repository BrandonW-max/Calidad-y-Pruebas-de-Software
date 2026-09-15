package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes;

public class HorarioApiRoutes {

    private HorarioApiRoutes() {}

    public static final String BASE = "/api/v1/horarios";

    public static final String BUSCAR_ID = "/{id}";
    public static final String ASIGNAR = "/asignar";
    public static final String ASIGNACIONES_POR_EMPLEADO = "/asignaciones/empleado/{idEmpleado}";
    public static final String EMPLEADOS_POR_HORARIO = "/{id}/empleados";
    public static final String ASIGNACIONES_ACTIVAS = "/asignaciones/activas";
}
