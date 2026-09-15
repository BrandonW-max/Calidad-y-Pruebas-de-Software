package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.routes;

public class DispositivoApiRoutes {

    private DispositivoApiRoutes() {}

    public static final String BASE = "/api/v1/dispositivos";

    public static final String ENVIAR_OTP = "/enviar-otp";
    public static final String REGISTRAR = "/registrar";
    public static final String POR_DNI = "/dni/{numeroDi}";
}
