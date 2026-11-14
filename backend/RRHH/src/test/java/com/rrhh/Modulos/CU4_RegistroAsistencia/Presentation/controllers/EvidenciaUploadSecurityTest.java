package com.rrhh.Modulos.CU4_RegistroAsistencia.Presentation.controllers;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.AsistenciaService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.EvidenciaStorageService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.JustificacionService;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaDispositivoEmpleadoRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Shared.exception.GlobalExceptionHandler;
import com.rrhh.config.security.JwtAuthFilter;
import com.rrhh.config.security.JwtConfig;
import com.rrhh.config.security.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AsistenciaController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class, GlobalExceptionHandler.class, EvidenciaStorageService.class})
class EvidenciaUploadSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EvidenciaStorageService storageService;

    @MockBean
    private AsistenciaService asistenciaService;

    @MockBean
    private JustificacionService justificacionService;

    @MockBean
    private JpaUsuarioRepository usuarioRepository;

    @MockBean
    private JpaDispositivoEmpleadoRepository dispositivoRepository;

    @MockBean
    private JwtConfig jwtConfig;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(storageService, "uploadDir", tempUploadDir.toString());
    }

    private byte[] generarPdfValido() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Justificante de tardanza laboral"));
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarJpegValido() throws Exception {
        BufferedImage img = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }

    // =========================================================================
    // 1. Petición no autenticada -> 401 Unauthorized
    // =========================================================================

    @Test
    @DisplayName("Petición sin sesión para subir evidencia -> 401 Unauthorized")
    void uploadEvidencia_sinAutenticacion_retorna401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "justificante.pdf", "application/pdf", generarPdfValido());

        mockMvc.perform(multipart("/api/v1/asistencia/upload-evidencia").file(file))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    // =========================================================================
    // 2. Usuario autenticado con archivo válido -> 200 OK con { url: "..." }
    // =========================================================================

    @Test
    @WithMockUser(username = "empleado.user", roles = {"EMPLEADO"})
    @DisplayName("Usuario con rol EMPLEADO sube PDF válido -> 200 OK con URL")
    void uploadEvidencia_empleadoPdfValido_retorna200ConUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "justificante.pdf", "application/pdf", generarPdfValido());

        mockMvc.perform(multipart("/api/v1/asistencia/upload-evidencia").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", startsWith("/uploads/evidencias/")))
                .andExpect(jsonPath("$.url", endsWith(".pdf")));
    }

    @Test
    @WithMockUser(username = "admin.rrhh", roles = {"RRHH"})
    @DisplayName("Usuario con rol RRHH sube JPG válido -> 200 OK con URL")
    void uploadEvidencia_rrhhJpgValido_retorna200ConUrl() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "receta.jpg", "image/jpeg", generarJpegValido());

        mockMvc.perform(multipart("/api/v1/asistencia/upload-evidencia").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", startsWith("/uploads/evidencias/")))
                .andExpect(jsonPath("$.url", endsWith(".jpg")));
    }

    // =========================================================================
    // 3. Archivos inválidos rechazados con 400 Bad Request
    // =========================================================================

    @Test
    @WithMockUser(username = "empleado.user", roles = {"EMPLEADO"})
    @DisplayName("Texto renombrado a .pdf -> 400 Bad Request con ARCHIVO_INVALIDO")
    void uploadEvidencia_textoConExtensionPdf_retorna400() throws Exception {
        byte[] fakePdf = "Este texto plano simula ser un archivo PDF malicioso".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "falso.pdf", "application/pdf", fakePdf);

        mockMvc.perform(multipart("/api/v1/asistencia/upload-evidencia").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("ARCHIVO_INVALIDO"))
                .andExpect(jsonPath("$.message", containsString("no corresponde a un documento PDF válido")));
    }

    @Test
    @WithMockUser(username = "empleado.user", roles = {"EMPLEADO"})
    @DisplayName("Extensión no permitida (.exe) -> 400 Bad Request con ARCHIVO_INVALIDO")
    void uploadEvidencia_extensionNoPermitida_retorna400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "malware.exe", "application/octet-stream", "dummy binary".getBytes());

        mockMvc.perform(multipart("/api/v1/asistencia/upload-evidencia").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("ARCHIVO_INVALIDO"))
                .andExpect(jsonPath("$.message", containsString("Extensión de archivo no permitida")));
    }

    @Test
    @WithMockUser(username = "empleado.user", roles = {"EMPLEADO"})
    @DisplayName("Archivo vacío (0 bytes) -> 400 Bad Request con ARCHIVO_INVALIDO")
    void uploadEvidencia_archivoVacio_retorna400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vacio.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/v1/asistencia/upload-evidencia").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("ARCHIVO_INVALIDO"))
                .andExpect(jsonPath("$.message", containsString("vacío")));
    }
}
