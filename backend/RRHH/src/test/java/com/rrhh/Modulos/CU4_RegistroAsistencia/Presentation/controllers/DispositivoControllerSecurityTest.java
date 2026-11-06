package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.services.EmailService;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.OtpService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.config.security.JwtAuthFilter;
import com.rrhh.config.security.JwtConfig;
import com.rrhh.config.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DispositivoController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class DispositivoControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JpaDispositivoEmpleadoRepository dispositivoRepository;

    @MockBean
    private JpaEmpleadoRepository empleadoRepository;

    @MockBean
    private OtpService otpService;

    @MockBean
    private EmailService emailService;

    @MockBean
    private JwtConfig jwtConfig;

    private static final String DNI_REGISTRADO_ACTIVO = "12345678";
    private static final String DNI_REGISTRADO_INACTIVO = "87654321";
    private static final String DNI_NO_REGISTRADO = "99999999";

    private EmpleadoModel empleadoActivo;
    private EmpleadoModel empleadoInactivo;

    @BeforeEach
    void setUp() {
        empleadoActivo = new EmpleadoModel();
        empleadoActivo.setIdEmpleado(101L);
        empleadoActivo.setNumeroDi(DNI_REGISTRADO_ACTIVO);
        empleadoActivo.setNombres("Juan");
        empleadoActivo.setApellidos("Perez");
        empleadoActivo.setEstado("ACTIVO");
        empleadoActivo.setCorreo("juan.perez@hospital.edu.pe");

        empleadoInactivo = new EmpleadoModel();
        empleadoInactivo.setIdEmpleado(102L);
        empleadoInactivo.setNumeroDi(DNI_REGISTRADO_INACTIVO);
        empleadoInactivo.setNombres("Maria");
        empleadoInactivo.setApellidos("Gomez");
        empleadoInactivo.setEstado("INACTIVO");
        empleadoInactivo.setCorreo("maria.gomez@hospital.edu.pe");

        when(empleadoRepository.findByNumeroDi(DNI_REGISTRADO_ACTIVO)).thenReturn(Optional.of(empleadoActivo));
        when(empleadoRepository.findByNumeroDi(DNI_REGISTRADO_INACTIVO)).thenReturn(Optional.of(empleadoInactivo));
        when(empleadoRepository.findByNumeroDi(DNI_NO_REGISTRADO)).thenReturn(Optional.empty());
    }

    // =========================================================================
    // 1. Peticiones sin sesión (Unauthenticated) -> No se puede determinar si existe
    // =========================================================================

    @Test
    @DisplayName("CWE-203/CWE-862: Petición anónima con DNI existente debe retornar 401 Unauthorized sin revelar existencia")
    void peticionAnonima_dniExistente_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_REGISTRADO_ACTIVO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje", containsString("Se requiere autenticación")))
                .andExpect(content().string(not(containsString("existe"))))
                .andExpect(content().string(not(containsString("Juan"))));

        // Verificamos que el repositorio no fue consultado
        verify(empleadoRepository, never()).findByNumeroDi(anyString());
    }

    @Test
    @DisplayName("CWE-203/CWE-862: Petición anónima con DNI no registrado debe retornar 401 Unauthorized con la misma respuesta")
    void peticionAnonima_dniInexistente_retorna401() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_NO_REGISTRADO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje", containsString("Se requiere autenticación")))
                .andExpect(content().string(not(containsString("existe"))));

        // Mismo código HTTP y estructura para ambos casos: cero discrepancia observable
        verify(empleadoRepository, never()).findByNumeroDi(anyString());
    }

    // =========================================================================
    // 2. Usuario autenticado sin rol necesario (EMPLEADO) -> Retorna 403 Forbidden
    // =========================================================================

    @Test
    @WithMockUser(username = "empleado.user", roles = {"EMPLEADO"})
    @DisplayName("Usuario con rol EMPLEADO no puede consultar DNI existente -> 403 Forbidden")
    void rolEmpleado_dniExistente_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_REGISTRADO_ACTIVO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(content().string(not(containsString("existe"))))
                .andExpect(content().string(not(containsString("Juan"))));

        verify(empleadoRepository, never()).findByNumeroDi(anyString());
    }

    @Test
    @WithMockUser(username = "empleado.user", roles = {"EMPLEADO"})
    @DisplayName("Usuario con rol EMPLEADO no puede consultar DNI no registrado -> 403 Forbidden")
    void rolEmpleado_dniInexistente_retorna403() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_NO_REGISTRADO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"))
                .andExpect(jsonPath("$.message").value("Access Denied"))
                .andExpect(content().string(not(containsString("existe"))));

        verify(empleadoRepository, never()).findByNumeroDi(anyString());
    }

    // =========================================================================
    // 3. Usuario con rol autorizado (RRHH / GERENCIA) -> Funcionalidad legítima
    // =========================================================================

    @Test
    @WithMockUser(username = "admin.rrhh", roles = {"RRHH"})
    @DisplayName("Usuario con rol RRHH consulta DNI activo registrado -> 200 OK con {existe: true}")
    void rolRrhh_dniExistenteActivo_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_REGISTRADO_ACTIVO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(true))
                .andExpect(content().string(not(containsString("Juan"))))
                .andExpect(content().string(not(containsString("Perez"))));

        verify(empleadoRepository, times(1)).findByNumeroDi(DNI_REGISTRADO_ACTIVO);
    }

    @Test
    @WithMockUser(username = "gerente.general", roles = {"GERENCIA"})
    @DisplayName("Usuario con rol GERENCIA consulta DNI activo registrado -> 200 OK con {existe: true}")
    void rolGerencia_dniExistenteActivo_retorna200() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_REGISTRADO_ACTIVO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.existe").value(true));

        verify(empleadoRepository, times(1)).findByNumeroDi(DNI_REGISTRADO_ACTIVO);
    }

    @Test
    @WithMockUser(username = "admin.rrhh", roles = {"RRHH"})
    @DisplayName("Usuario con rol RRHH consulta DNI no registrado -> 404 Not Found")
    void rolRrhh_dniInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_NO_REGISTRADO))
                .andExpect(status().isNotFound());

        verify(empleadoRepository, times(1)).findByNumeroDi(DNI_NO_REGISTRADO);
    }

    @Test
    @WithMockUser(username = "admin.rrhh", roles = {"RRHH"})
    @DisplayName("Usuario con rol RRHH consulta DNI inactivo -> 400 Bad Request")
    void rolRrhh_dniInactivo_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", DNI_REGISTRADO_INACTIVO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Empleado inactivo o desvinculado."));

        verify(empleadoRepository, times(1)).findByNumeroDi(DNI_REGISTRADO_INACTIVO);
    }

    // =========================================================================
    // 4. Validación de formato de DNI (8 dígitos numéricos)
    // =========================================================================

    @Test
    @WithMockUser(username = "admin.rrhh", roles = {"RRHH"})
    @DisplayName("DNI con letras -> 400 Bad Request por formato inválido")
    void rolRrhh_dniConLetras_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", "1234abcd"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Formato de DNI inválido")));

        verify(empleadoRepository, never()).findByNumeroDi(anyString());
    }

    @Test
    @WithMockUser(username = "admin.rrhh", roles = {"RRHH"})
    @DisplayName("DNI con longitud menor a 8 dígitos -> 400 Bad Request")
    void rolRrhh_dniCorto_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", "12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Formato de DNI inválido")));

        verify(empleadoRepository, never()).findByNumeroDi(anyString());
    }

    @Test
    @WithMockUser(username = "admin.rrhh", roles = {"RRHH"})
    @DisplayName("DNI con longitud mayor a 8 dígitos -> 400 Bad Request")
    void rolRrhh_dniLargo_retorna400() throws Exception {
        mockMvc.perform(get("/api/v1/dispositivos/dni/{numeroDi}", "12345678901"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Formato de DNI inválido")));

        verify(empleadoRepository, never()).findByNumeroDi(anyString());
    }

    // =========================================================================
    // 5. Preservación de endpoints públicos legítimos (enviar-otp y registrar)
    // =========================================================================

    @Test
    @DisplayName("Endpoint legítimo público /enviar-otp sigue siendo accesible sin autenticación")
    void endpointEnviarOtp_sigueSiendoPublico() throws Exception {
        when(otpService.generarOtp(DNI_REGISTRADO_ACTIVO))
                .thenReturn(new OtpService.OtpResult("session-test-id", "123456"));

        mockMvc.perform(post("/api/v1/dispositivos/enviar-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(Map.of("numeroDi", DNI_REGISTRADO_ACTIVO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("session-test-id"));
    }

    @Test
    @DisplayName("Endpoint legítimo /enviar-otp valida formato de DNI")
    void endpointEnviarOtp_validaFormatoDni() throws Exception {
        mockMvc.perform(post("/api/v1/dispositivos/enviar-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(Map.of("numeroDi", "invalid-dni"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("Formato de DNI inválido")));
    }
}
