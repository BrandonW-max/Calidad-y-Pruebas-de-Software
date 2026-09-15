package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes;

public class AsistenciaApiRoutes {

    private AsistenciaApiRoutes() {}

    public static final String BASE = "/api/v1/asistencia";

    public static final String UPLOAD_EVIDENCIA = "/upload-evidencia";
    public static final String MARCAR = "/marcar";
    public static final String JUSTIFICAR = "/justificar";
    public static final String JUSTIFICAR_REVISAR = "/justificar/{id}/revisar";
    public static final String POR_EMPLEADO = "/empleado/{idEmpleado}";
    public static final String HORARIO_EMPLEADO = "/horario/{idEmpleado}";
    public static final String HOY = "/hoy/{idEmpleado}";
    public static final String BIOMETRICO_ESTADO = "/biometrico/estado/{idEmpleado}";
    public static final String BIOMETRICO = "/biometrico";
    public static final String MANUAL = "/manual";
    public static final String JUSTIFICACIONES_PENDIENTES = "/justificaciones/pendientes";
    public static final String JUSTIFICACIONES_POR_EMPLEADO = "/justificaciones/empleado/{idEmpleado}";
}
