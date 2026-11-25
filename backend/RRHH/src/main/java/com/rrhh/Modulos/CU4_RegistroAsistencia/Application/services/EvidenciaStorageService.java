package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;
import com.rrhh.Shared.exception.ArchivoInvalidoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Service
public class EvidenciaStorageService {

    private static final Logger log = LoggerFactory.getLogger(EvidenciaStorageService.class);

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".jpg", ".jpeg", ".png", ".pdf");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_IMAGE_DIMENSION = 8192; // 8192px ancho/alto máx
    private static final long MAX_IMAGE_PIXELS = 40_000_000L; // 40 Megapíxeles máx

    private static final byte[] PNG_HEADER = new byte[] {
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A
    };

    private static final byte[] PDF_HEADER = new byte[] {
            (byte) '%', (byte) 'P', (byte) 'D', (byte) 'F', (byte) '-'
    };

    @Value("${app.upload-dir:uploads/evidencias}")
    private String uploadDir;

    /**
     * Guarda de forma segura una evidencia previa validación estricta de tamaño,
     * extensión en lista blanca, firmas binarias y decodificación estructural profunda.
     *
     * @param file Archivo subido por el cliente
     * @return Ruta pública relativa del archivo almacenado
     * @throws IOException Si ocurre un error de E/S al escribir en disco
     * @throws ArchivoInvalidoException Si el archivo no supera los controles de validación
     */
    public String guardar(MultipartFile file) throws IOException {
        validarTamano(file);
        String ext = validarExtension(file.getOriginalFilename());
        byte[] bytes = file.getBytes();
        validarContenidoReal(bytes, ext);

        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(dir);
        Path targetPath = dir.resolve(filename);
        Files.write(targetPath, bytes);
        log.info("Archivo de evidencia almacenado exitosamente: {}", filename);
        return "/uploads/evidencias/" + filename;
    }

    private void validarTamano(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() == 0) {
            throw new ArchivoInvalidoException("El archivo está vacío o no contiene datos.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ArchivoInvalidoException("El archivo excede el tamaño máximo permitido (10MB).");
        }
    }

    private String validarExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank() || !originalFilename.contains(".")) {
            throw new ArchivoInvalidoException("El archivo no posee una extensión válida.");
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."))
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9.]", "");
        if (!EXTENSIONES_PERMITIDAS.contains(ext)) {
            throw new ArchivoInvalidoException("Extensión de archivo no permitida. Solo se aceptan archivos JPG, JPEG, PNG o PDF.");
        }
        return ext;
    }

    private void validarContenidoReal(byte[] bytes, String ext) {
        if (bytes == null || bytes.length == 0) {
            throw new ArchivoInvalidoException("El contenido del archivo está vacío.");
        }
        switch (ext) {
            case ".jpg":
            case ".jpeg":
                validarJpeg(bytes);
                break;
            case ".png":
                validarPng(bytes);
                break;
            case ".pdf":
                validarPdf(bytes);
                break;
            default:
                throw new ArchivoInvalidoException("Formato no soportado.");
        }
    }

    private void validarJpeg(byte[] bytes) {
        if (bytes.length < 4) {
            throw new ArchivoInvalidoException("El archivo es demasiado pequeño para ser una imagen JPEG válida.");
        }
        // Verificar magic bytes: FF D8 FF
        if ((bytes[0] & 0xFF) != 0xFF || (bytes[1] & 0xFF) != 0xD8 || (bytes[2] & 0xFF) != 0xFF) {
            throw new ArchivoInvalidoException("El contenido del archivo no corresponde a una imagen JPEG válida.");
        }

        // Inspección y decodificación con ImageIO
        BufferedImage image;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            image = ImageIO.read(bais);
        } catch (Exception e) {
            throw new ArchivoInvalidoException("La imagen JPEG está corrupta o no pudo ser decodificada.");
        }

        if (image == null) {
            throw new ArchivoInvalidoException("El contenido del archivo no pudo ser decodificado como imagen JPEG.");
        }

        validarDimensionesImagen(image);

        // Validar formato detectado por el ImageReader
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new ArchivoInvalidoException("No se encontró un decodificador compatible para la imagen JPEG.");
            }
            ImageReader reader = readers.next();
            String format = reader.getFormatName();
            if (format == null || (!format.equalsIgnoreCase("JPEG") && !format.equalsIgnoreCase("jpg"))) {
                throw new ArchivoInvalidoException("El formato real de la imagen (" + format + ") no coincide con la extensión JPEG declarada.");
            }
        } catch (ArchivoInvalidoException e) {
            throw e;
        } catch (Exception e) {
            throw new ArchivoInvalidoException("Error al verificar la estructura interna de la imagen JPEG.");
        }
    }

    private void validarPng(byte[] bytes) {
        if (bytes.length < PNG_HEADER.length) {
            throw new ArchivoInvalidoException("El archivo es demasiado pequeño para ser una imagen PNG válida.");
        }
        // Verificar magic bytes
        for (int i = 0; i < PNG_HEADER.length; i++) {
            if (bytes[i] != PNG_HEADER[i]) {
                throw new ArchivoInvalidoException("El contenido del archivo no corresponde a una imagen PNG válida.");
            }
        }

        BufferedImage image;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            image = ImageIO.read(bais);
        } catch (Exception e) {
            throw new ArchivoInvalidoException("La imagen PNG está corrupta o no pudo ser decodificada.");
        }

        if (image == null) {
            throw new ArchivoInvalidoException("El contenido del archivo no pudo ser decodificado como imagen PNG.");
        }

        validarDimensionesImagen(image);

        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) {
                throw new ArchivoInvalidoException("No se encontró un decodificador compatible para la imagen PNG.");
            }
            ImageReader reader = readers.next();
            String format = reader.getFormatName();
            if (format == null || !format.equalsIgnoreCase("PNG")) {
                throw new ArchivoInvalidoException("El formato real de la imagen (" + format + ") no coincide con la extensión PNG declarada.");
            }
        } catch (ArchivoInvalidoException e) {
            throw e;
        } catch (Exception e) {
            throw new ArchivoInvalidoException("Error al verificar la estructura interna de la imagen PNG.");
        }
    }

    private void validarDimensionesImagen(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width <= 0 || height <= 0) {
            throw new ArchivoInvalidoException("Dimensiones de imagen inválidas.");
        }
        if (width > MAX_IMAGE_DIMENSION || height > MAX_IMAGE_DIMENSION) {
            throw new ArchivoInvalidoException("Las dimensiones de la imagen (" + width + "x" + height + ") exceden el máximo permitido (8192x8192).");
        }
        long pixels = (long) width * height;
        if (pixels > MAX_IMAGE_PIXELS) {
            throw new ArchivoInvalidoException("La cantidad total de píxeles excede el límite permitido.");
        }
    }

    private void validarPdf(byte[] bytes) {
        if (bytes.length < PDF_HEADER.length) {
            throw new ArchivoInvalidoException("El archivo es demasiado pequeño para ser un documento PDF válido.");
        }

        // 1. Validar magic bytes (%PDF-)
        for (int i = 0; i < PDF_HEADER.length; i++) {
            if (bytes[i] != PDF_HEADER[i]) {
                throw new ArchivoInvalidoException("El contenido del archivo no corresponde a un documento PDF válido.");
            }
        }

        // 2. Parser estructural de PDF (OpenPDF PdfReader)
        PdfReader reader = null;
        try {
            reader = new PdfReader(new ByteArrayInputStream(bytes));

            // Verificar que tenga páginas válidas
            int pages = reader.getNumberOfPages();
            if (pages < 1) {
                throw new ArchivoInvalidoException("El documento PDF no contiene páginas válidas.");
            }

            // Inspección exhaustiva de seguridad (catálogo, páginas, anotaciones, formularios y objetos XREF)
            inspeccionarSeguridadPdf(reader, pages);

        } catch (ArchivoInvalidoException e) {
            throw e;
        } catch (Exception e) {
            throw new ArchivoInvalidoException("El archivo no es un documento PDF válido o está corrupto.");
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void inspeccionarSeguridadPdf(PdfReader reader, int pages) throws IOException {
        // 1. Verificar JavaScript a nivel global de documento
        String js = reader.getJavaScript();
        if (js != null && !js.trim().isEmpty()) {
            throw new ArchivoInvalidoException("El archivo PDF contiene scripts no permitidos a nivel de documento.");
        }

        // 2. Inspección del Catálogo (/Root)
        PdfDictionary catalog = reader.getCatalog();
        if (catalog != null) {
            verificarDiccionarioPeligroso(catalog, "catálogo");

            PdfDictionary names = catalog.getAsDict(PdfName.NAMES);
            if (names != null) {
                if (names.contains(PdfName.JAVASCRIPT) || names.contains(new PdfName("JavaScript"))) {
                    throw new ArchivoInvalidoException("El archivo PDF contiene scripts ejecutables incrustados en el catálogo.");
                }
                if (names.contains(PdfName.EMBEDDEDFILES) || names.contains(new PdfName("EmbeddedFiles"))) {
                    throw new ArchivoInvalidoException("El archivo PDF contiene archivos adjuntos incrustados en el catálogo.");
                }
            }

            PdfDictionary acroForm = catalog.getAsDict(PdfName.ACROFORM);
            if (acroForm != null) {
                if (acroForm.contains(new PdfName("XFA"))) {
                    throw new ArchivoInvalidoException("El archivo PDF contiene formularios dinámicos XFA no permitidos.");
                }
                verificarDiccionarioPeligroso(acroForm, "formulario AcroForm");
            }
        }

        // 3. Inspección por cada página y sus anotaciones/acciones
        for (int p = 1; p <= pages; p++) {
            PdfDictionary page = reader.getPageN(p);
            if (page != null) {
                if (page.contains(PdfName.AA)) {
                    throw new ArchivoInvalidoException("El archivo PDF contiene acciones adicionales automáticas en la página " + p + ".");
                }
                PdfDictionary pageAction = page.getAsDict(PdfName.A);
                if (pageAction != null) {
                    verificarAccionPeligrosa(pageAction);
                } else if (page.contains(PdfName.A)) {
                    PdfObject paObj = page.get(PdfName.A);
                    if (paObj != null && paObj.isDictionary()) {
                        verificarAccionPeligrosa((PdfDictionary) paObj);
                    }
                }

                PdfArray annots = page.getAsArray(PdfName.ANNOTS);
                if (annots != null) {
                    for (int a = 0; a < annots.size(); a++) {
                        PdfObject annotObj = annots.getPdfObject(a);
                        if (annotObj != null && annotObj.isDictionary()) {
                            verificarAnotacion((PdfDictionary) annotObj, p);
                        }
                    }
                }
            }
        }

        // 4. Barrido profundo de todos los objetos indirectos y estructuras anidadas
        Set<PdfObject> visitados = new HashSet<>();
        int xrefSize = reader.getXrefSize();
        for (int i = 1; i < xrefSize; i++) {
            PdfObject obj = reader.getPdfObject(i);
            if (obj != null) {
                inspeccionarObjetoRecursivo(obj, 0, visitados);
            }
        }
    }

    private void verificarDiccionarioPeligroso(PdfDictionary dict, String contexto) {
        if (dict == null) return;

        if (dict.contains(PdfName.OPENACTION)) {
            throw new ArchivoInvalidoException("El archivo PDF contiene acciones de apertura automática (OpenAction).");
        }
        if (dict.contains(PdfName.AA)) {
            throw new ArchivoInvalidoException("El archivo PDF contiene acciones adicionales automáticas (AA).");
        }
        if (dict.contains(new PdfName("JS")) || dict.contains(PdfName.JAVASCRIPT) || dict.contains(new PdfName("JavaScript"))) {
            throw new ArchivoInvalidoException("El archivo PDF contiene código JavaScript en " + contexto + ".");
        }
        if (dict.contains(new PdfName("EmbeddedFiles")) || dict.contains(new PdfName("EF"))) {
            throw new ArchivoInvalidoException("El archivo PDF contiene archivos adjuntos incrustados en " + contexto + ".");
        }

        PdfDictionary aDict = dict.getAsDict(PdfName.A);
        if (aDict != null) {
            verificarAccionPeligrosa(aDict);
        } else {
            PdfObject aObj = dict.get(PdfName.A);
            if (aObj != null && aObj.isDictionary()) {
                verificarAccionPeligrosa((PdfDictionary) aObj);
            }
        }
    }

    private void verificarAnotacion(PdfDictionary annot, int pageNum) {
        if (annot == null) return;

        PdfName subtype = annot.getAsName(PdfName.SUBTYPE);
        if (subtype != null) {
            String subtypeName = subtype.toString().replace("/", "");
            if ("FileAttachment".equalsIgnoreCase(subtypeName)
                    || "RichMedia".equalsIgnoreCase(subtypeName)
                    || "Screen".equalsIgnoreCase(subtypeName)
                    || "Sound".equalsIgnoreCase(subtypeName)
                    || "Movie".equalsIgnoreCase(subtypeName)
                    || "Launch".equalsIgnoreCase(subtypeName)
                    || "3D".equalsIgnoreCase(subtypeName)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene una anotación no permitida (" + subtypeName + ") en la página " + pageNum + ".");
            }
        }

        if (annot.contains(PdfName.AA)) {
            throw new ArchivoInvalidoException("El archivo PDF contiene acciones adicionales en anotación de la página " + pageNum + ".");
        }

        PdfDictionary aDict = annot.getAsDict(PdfName.A);
        if (aDict != null) {
            verificarAccionPeligrosa(aDict);
        } else {
            PdfObject aObj = annot.get(PdfName.A);
            if (aObj != null && aObj.isDictionary()) {
                verificarAccionPeligrosa((PdfDictionary) aObj);
            }
        }
    }

    private void verificarAccionPeligrosa(PdfDictionary actionDict) {
        if (actionDict == null) return;

        PdfName s = actionDict.getAsName(PdfName.S);
        if (s != null) {
            String actionType = s.toString().replace("/", "");
            if ("JavaScript".equalsIgnoreCase(actionType)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene una acción con scripts ejecutables.");
            }
            if ("Launch".equalsIgnoreCase(actionType)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene una acción para ejecutar programas externos (Launch).");
            }
            if ("GoToR".equalsIgnoreCase(actionType)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene una acción de navegación a archivo remoto (GoToR).");
            }
            if ("SubmitForm".equalsIgnoreCase(actionType)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene una acción de envío de formulario (SubmitForm).");
            }
            if ("ImportData".equalsIgnoreCase(actionType)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene una acción de importación de datos externos (ImportData).");
            }
            if ("URI".equalsIgnoreCase(actionType)) {
                PdfObject uriObj = actionDict.get(PdfName.URI);
                if (uriObj != null) {
                    String uri = uriObj.toString().trim().toLowerCase();
                    if (uri.startsWith("javascript:") || uri.startsWith("file:") || uri.startsWith("data:") || uri.startsWith("vbscript:")) {
                        throw new ArchivoInvalidoException("El archivo PDF contiene un enlace URI con un esquema peligroso no permitido.");
                    }
                }
            }
        }

        PdfObject next = actionDict.get(PdfName.NEXT);
        if (next != null) {
            if (next.isDictionary()) {
                verificarAccionPeligrosa((PdfDictionary) next);
            } else if (next.isArray()) {
                PdfArray nextArr = (PdfArray) next;
                for (int i = 0; i < nextArr.size(); i++) {
                    PdfObject subAction = nextArr.getPdfObject(i);
                    if (subAction != null && subAction.isDictionary()) {
                        verificarAccionPeligrosa((PdfDictionary) subAction);
                    }
                }
            }
        }
    }

    private void inspeccionarObjetoRecursivo(PdfObject obj, int depth, Set<PdfObject> visitados) {
        if (obj == null || depth > 25 || !visitados.add(obj)) return;

        if (obj.isIndirect()) {
            PdfObject direct = PdfReader.getPdfObject(obj);
            if (direct != null) {
                inspeccionarObjetoRecursivo(direct, depth + 1, visitados);
            }
            return;
        }

        if (obj.isDictionary()) {
            PdfDictionary dict = (PdfDictionary) obj;
            if (dict.contains(PdfName.S)) {
                verificarAccionPeligrosa(dict);
            }
            if (dict.contains(new PdfName("JS")) || dict.contains(PdfName.JAVASCRIPT) || dict.contains(new PdfName("JavaScript"))) {
                throw new ArchivoInvalidoException("El archivo PDF contiene código JavaScript incrustado.");
            }
            if (dict.contains(PdfName.OPENACTION)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene acciones de apertura automática.");
            }
            if (dict.contains(PdfName.AA)) {
                throw new ArchivoInvalidoException("El archivo PDF contiene acciones adicionales automáticas.");
            }
            if (dict.contains(new PdfName("EmbeddedFiles")) || dict.contains(new PdfName("EF"))) {
                throw new ArchivoInvalidoException("El archivo PDF contiene archivos adjuntos incrustados.");
            }

            PdfName subtype = dict.getAsName(PdfName.SUBTYPE);
            if (subtype != null) {
                String subName = subtype.toString().replace("/", "");
                if ("FileAttachment".equalsIgnoreCase(subName)
                        || "RichMedia".equalsIgnoreCase(subName)
                        || "Screen".equalsIgnoreCase(subName)
                        || "Sound".equalsIgnoreCase(subName)
                        || "Movie".equalsIgnoreCase(subName)
                        || "Launch".equalsIgnoreCase(subName)
                        || "3D".equalsIgnoreCase(subName)) {
                    throw new ArchivoInvalidoException("El archivo PDF contiene objetos no permitidos de tipo " + subName + ".");
                }
            }

            for (PdfName key : dict.getKeys()) {
                inspeccionarObjetoRecursivo(dict.get(key), depth + 1, visitados);
            }
        } else if (obj.isArray()) {
            PdfArray arr = (PdfArray) obj;
            for (int i = 0; i < arr.size(); i++) {
                inspeccionarObjetoRecursivo(arr.getPdfObject(i), depth + 1, visitados);
            }
        }
    }
}
