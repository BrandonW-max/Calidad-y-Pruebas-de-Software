package com.rrhh.Modulos.CU4_RegistroAsistencia.Application;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfFileSpecification;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfWriter;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services.EvidenciaStorageService;
import com.rrhh.Shared.exception.ArchivoInvalidoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class EvidenciaStorageServiceTest {

    private EvidenciaStorageService storageService;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        storageService = new EvidenciaStorageService();
        ReflectionTestUtils.setField(storageService, "uploadDir", tempUploadDir.toString());
    }

    // =========================================================================
    // Helpers para generar archivos de prueba válidos y controlados
    // =========================================================================

    private byte[] generarPdfValido() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("Evidencia médica de asistencia"));
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarJpegValido() throws Exception {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }

    private byte[] generarPngValido() throws Exception {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private byte[] generarPdfConJavascript() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        writer.addJavaScript("app.alert('XSS Script');");
        document.add(new Paragraph("Documento con script malicioso"));
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPdfConAccionPagina() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        writer.setPageAction(PdfWriter.PAGE_OPEN, new PdfAction("calc.exe", null, null, null));
        document.add(new Paragraph("PDF con accion de apertura de pagina"));
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPdfConAnotacionLaunch() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("PDF con anotacion Launch"));
        PdfAnnotation annot = new PdfAnnotation(writer, 10f, 10f, 100f, 100f, new PdfAction("calc.exe", null, null, null));
        writer.addAnnotation(annot);
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPdfConAnotacionJavascript() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("PDF con anotacion con script JS"));
        PdfAnnotation annot = new PdfAnnotation(writer, 10, 10, 100, 100, PdfAction.javaScript("app.alert('annot js');", writer));
        writer.addAnnotation(annot);
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPdfConAnotacionFileAttachment() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("PDF con archivo adjunto"));
        PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(writer, null, "malware.exe", new byte[]{0x4D, 0x5A});
        PdfAnnotation annot = PdfAnnotation.createFileAttachment(writer, new Rectangle(10, 10, 100, 100), "Adjunto ejecutable", fs);
        writer.addAnnotation(annot);
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPdfConAnotacionUriPeligrosa() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("PDF con link javascript"));
        PdfAnnotation annot = new PdfAnnotation(writer, 10, 10, 100, 100, new PdfAction("javascript:alert(1)"));
        writer.addAnnotation(annot);
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPdfConAcroFormFieldAccion() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        document.add(new Paragraph("PDF con campo de formulario y accion"));
        PdfFormField field = PdfFormField.createTextField(writer, false, false, 50);
        field.setFieldName("testInput");
        field.setAction(PdfAction.javaScript("app.alert('form action');", writer));
        field.setWidget(new Rectangle(50, 50, 150, 70), PdfAnnotation.HIGHLIGHT_INVERT);
        writer.addAnnotation(field);
        document.close();
        return baos.toByteArray();
    }

    private byte[] generarPdfConEmbeddedFilesEnCatalogo() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document, baos);
        document.open();
        PdfFileSpecification fs = PdfFileSpecification.fileEmbedded(writer, null, "secreto.bin", new byte[]{1, 2, 3});
        writer.addFileAttachment("secreto.bin", fs);
        document.add(new Paragraph("PDF con EmbeddedFiles en catalogo"));
        document.close();
        return baos.toByteArray();
    }

    // =========================================================================
    // 1-4. Archivos válidos son aceptados y almacenados
    // =========================================================================

    @Test
    @DisplayName("Escenario 1: Un PDF válido se acepta y almacena correctamente")
    void guardar_pdfValido_exitoso() throws Exception {
        byte[] pdfBytes = generarPdfValido();
        MockMultipartFile file = new MockMultipartFile("file", "justificante.pdf", "application/pdf", pdfBytes);

        String url = storageService.guardar(file);

        assertNotNull(url);
        assertTrue(url.startsWith("/uploads/evidencias/"));
        assertTrue(url.endsWith(".pdf"));

        // Verificar que el archivo existe en el directorio de subida
        String filename = url.substring("/uploads/evidencias/".length());
        assertTrue(Files.exists(tempUploadDir.resolve(filename)));
        assertEquals(pdfBytes.length, Files.size(tempUploadDir.resolve(filename)));
    }

    @Test
    @DisplayName("Escenario 2: Un JPG válido se acepta y almacena correctamente")
    void guardar_jpgValido_exitoso() throws Exception {
        byte[] jpgBytes = generarJpegValido();
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", jpgBytes);

        String url = storageService.guardar(file);

        assertNotNull(url);
        assertTrue(url.endsWith(".jpg"));
        String filename = url.substring("/uploads/evidencias/".length());
        assertTrue(Files.exists(tempUploadDir.resolve(filename)));
    }

    @Test
    @DisplayName("Escenario 3: Un JPEG válido se acepta y almacena correctamente")
    void guardar_jpegValido_exitoso() throws Exception {
        byte[] jpegBytes = generarJpegValido();
        MockMultipartFile file = new MockMultipartFile("file", "certificado.jpeg", "image/jpeg", jpegBytes);

        String url = storageService.guardar(file);

        assertNotNull(url);
        assertTrue(url.endsWith(".jpeg"));
        String filename = url.substring("/uploads/evidencias/".length());
        assertTrue(Files.exists(tempUploadDir.resolve(filename)));
    }

    @Test
    @DisplayName("Escenario 4: Un PNG válido se acepta y almacena correctamente")
    void guardar_pngValido_exitoso() throws Exception {
        byte[] pngBytes = generarPngValido();
        MockMultipartFile file = new MockMultipartFile("file", "captura.png", "image/png", pngBytes);

        String url = storageService.guardar(file);

        assertNotNull(url);
        assertTrue(url.endsWith(".png"));
        String filename = url.substring("/uploads/evidencias/".length());
        assertTrue(Files.exists(tempUploadDir.resolve(filename)));
    }

    // =========================================================================
    // 5-7. Archivos de texto renombrados con extensiones permitidas se rechazan
    // =========================================================================

    @Test
    @DisplayName("Escenario 5: Un archivo de texto con extensión .pdf se rechaza")
    void guardar_textoConExtensionPdf_seRechaza() {
        byte[] textoBytes = "Este es un simple archivo de texto que no es PDF".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "falso.pdf", "application/pdf", textoBytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("no corresponde a un documento PDF válido"));

        // Verificar que no se guardó ningún archivo
        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 6: Un archivo de texto con extensión .jpg se rechaza")
    void guardar_textoConExtensionJpg_seRechaza() {
        byte[] textoBytes = "Texto plano disfrazado de imagen jpg".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "falso.jpg", "image/jpeg", textoBytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("no corresponde a una imagen JPEG válida"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 7: Un archivo de texto con extensión .png se rechaza")
    void guardar_textoConExtensionPng_seRechaza() {
        byte[] textoBytes = "Texto plano disfrazado de imagen png".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "falso.png", "image/png", textoBytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("no corresponde a una imagen PNG válida"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    // =========================================================================
    // 8. Encabezado MIME falsificado se rechaza si el contenido no corresponde
    // =========================================================================

    @Test
    @DisplayName("Escenario 8: Archivo con encabezado MIME application/pdf pero contenido PNG se rechaza")
    void guardar_mimeFalsificado_seRechaza() throws Exception {
        byte[] pngBytes = generarPngValido();
        // Cliente envía extensión .pdf y Content-Type application/pdf, pero los bytes son PNG
        MockMultipartFile file = new MockMultipartFile("file", "engano.pdf", "application/pdf", pngBytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("no corresponde a un documento PDF válido"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    // =========================================================================
    // 9. Archivos con firma/extensión pero estructura corrupta se rechazan
    // =========================================================================

    @Test
    @DisplayName("Escenario 9a: PDF con header %PDF- pero estructura corrupta se rechaza")
    void guardar_pdfCorrupto_seRechaza() {
        byte[] corruptPdf = "%PDF-1.4\nCorrupt bytes no valid xref or trailer EOF".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "corrupto.pdf", "application/pdf", corruptPdf);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("no es un documento PDF válido o está corrupto"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 9b: JPEG con magic bytes pero estructura corrupta se rechaza")
    void guardar_jpegCorrupto_seRechaza() {
        byte[] corruptJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x01, 0x02, 0x03, 0x04};
        MockMultipartFile file = new MockMultipartFile("file", "corrupto.jpg", "image/jpeg", corruptJpeg);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("corrupta") || ex.getMessage().contains("no pudo ser"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 9c: PNG con magic bytes pero estructura corrupta se rechaza")
    void guardar_pngCorrupto_seRechaza() {
        byte[] corruptPng = new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x01, 0x02, 0x03
        };
        MockMultipartFile file = new MockMultipartFile("file", "corrupto.png", "image/png", corruptPng);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("corrupta") || ex.getMessage().contains("no pudo ser"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 9d: PDF con JavaScript incrustado se rechaza por seguridad")
    void guardar_pdfConJavascript_seRechaza() throws Exception {
        byte[] pdfWithJs = generarPdfConJavascript();
        MockMultipartFile file = new MockMultipartFile("file", "script.pdf", "application/pdf", pdfWithJs);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("scripts no permitidos") || ex.getMessage().contains("scripts ejecutables"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    // =========================================================================
    // 10. Archivo vacío se rechaza
    // =========================================================================

    @Test
    @DisplayName("Escenario 10: Un archivo vacío (0 bytes) se rechaza")
    void guardar_archivoVacio_seRechaza() {
        MockMultipartFile file = new MockMultipartFile("file", "vacio.pdf", "application/pdf", new byte[0]);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("está vacío"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    // =========================================================================
    // 11. Extensión no permitida se rechaza
    // =========================================================================

    @Test
    @DisplayName("Escenario 11a: Extensión .exe se rechaza")
    void guardar_extensionExe_seRechaza() {
        MockMultipartFile file = new MockMultipartFile("file", "script.exe", "application/octet-stream", "dummy binary".getBytes());

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("Extensión de archivo no permitida"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 11b: Extensión .txt se rechaza")
    void guardar_extensionTxt_seRechaza() {
        MockMultipartFile file = new MockMultipartFile("file", "nota.txt", "text/plain", "Texto plano".getBytes());

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("Extensión de archivo no permitida"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    // =========================================================================
    // 12. Archivo que excede el límite máximo configurado se rechaza
    // =========================================================================

    @Test
    @DisplayName("Escenario 12: Archivo mayor a 10MB se rechaza")
    void guardar_archivoExcedeLimite_seRechaza() {
        // Simular un MockMultipartFile de tamaño mayor a 10MB sin alocar 10MB en heap
        MockMultipartFile file = new MockMultipartFile("file", "pesado.pdf", "application/pdf", new byte[100]) {
            @Override
            public long getSize() {
                return 10 * 1024 * 1024 + 1; // 10MB + 1 byte
            }
        };

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("excede el tamaño máximo permitido"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    // =========================================================================
    // 13. Archivo rechazado no queda almacenado y no genera URL utilizable
    // =========================================================================

    @Test
    @DisplayName("Escenario 13: Ningún archivo rechazado deja rastro en disco")
    void guardar_rechazosNoPersistenEnDisco() {
        MockMultipartFile fileTxt = new MockMultipartFile("file", "malicioso.pdf", "application/pdf", "malicious payload".getBytes());
        assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(fileTxt));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0, "No debe haber ningún archivo en el directorio de subidas");
    }

    // =========================================================================
    // 14-20. Inspección profunda de seguridad de PDFs (Acciones, Anotaciones, Formularios)
    // =========================================================================

    @Test
    @DisplayName("Escenario 14: PDF con acción de apertura a nivel de página (Page Action / Open) se rechaza")
    void guardar_pdfConAccionPagina_seRechaza() throws Exception {
        byte[] bytes = generarPdfConAccionPagina();
        MockMultipartFile file = new MockMultipartFile("file", "page_action.pdf", "application/pdf", bytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("acciones") || ex.getMessage().contains("Launch") || ex.getMessage().contains("programas"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 15: PDF con anotación Launch (ejecución externa) se rechaza")
    void guardar_pdfConAnotacionLaunch_seRechaza() throws Exception {
        byte[] bytes = generarPdfConAnotacionLaunch();
        MockMultipartFile file = new MockMultipartFile("file", "annot_launch.pdf", "application/pdf", bytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("Launch") || ex.getMessage().contains("programas") || ex.getMessage().contains("anotación no permitida"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 16: PDF con anotación con script JavaScript se rechaza")
    void guardar_pdfConAnotacionJavascript_seRechaza() throws Exception {
        byte[] bytes = generarPdfConAnotacionJavascript();
        MockMultipartFile file = new MockMultipartFile("file", "annot_js.pdf", "application/pdf", bytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("scripts") || ex.getMessage().contains("JavaScript"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 17: PDF con anotación FileAttachment (archivos adjuntos incrustados) se rechaza")
    void guardar_pdfConAnotacionFileAttachment_seRechaza() throws Exception {
        byte[] bytes = generarPdfConAnotacionFileAttachment();
        MockMultipartFile file = new MockMultipartFile("file", "annot_file.pdf", "application/pdf", bytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("FileAttachment") || ex.getMessage().contains("anotación no permitida") || ex.getMessage().contains("adjuntos"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 18: PDF con enlace URI peligroso (esquema javascript:) se rechaza")
    void guardar_pdfConAnotacionUriPeligrosa_seRechaza() throws Exception {
        byte[] bytes = generarPdfConAnotacionUriPeligrosa();
        MockMultipartFile file = new MockMultipartFile("file", "annot_uri.pdf", "application/pdf", bytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("URI con un esquema peligroso") || ex.getMessage().contains("peligroso"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 19: PDF con campo de formulario AcroForm que contiene acción se rechaza")
    void guardar_pdfConAcroFormFieldAccion_seRechaza() throws Exception {
        byte[] bytes = generarPdfConAcroFormFieldAccion();
        MockMultipartFile file = new MockMultipartFile("file", "acroform_action.pdf", "application/pdf", bytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("scripts") || ex.getMessage().contains("JavaScript") || ex.getMessage().contains("acción"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }

    @Test
    @DisplayName("Escenario 20: PDF con archivos incrustados en catálogo (EmbeddedFiles) se rechaza")
    void guardar_pdfConEmbeddedFilesEnCatalogo_seRechaza() throws Exception {
        byte[] bytes = generarPdfConEmbeddedFilesEnCatalogo();
        MockMultipartFile file = new MockMultipartFile("file", "embedded_catalog.pdf", "application/pdf", bytes);

        ArchivoInvalidoException ex = assertThrows(ArchivoInvalidoException.class, () -> storageService.guardar(file));
        assertTrue(ex.getMessage().contains("archivos adjuntos incrustados") || ex.getMessage().contains("EmbeddedFiles"));

        File[] files = tempUploadDir.toFile().listFiles();
        assertTrue(files == null || files.length == 0);
    }
}
