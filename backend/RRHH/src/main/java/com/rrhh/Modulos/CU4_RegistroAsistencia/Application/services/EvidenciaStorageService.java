package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class EvidenciaStorageService {

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".jpg", ".jpeg", ".png", ".pdf");

    @Value("${app.upload-dir:uploads/evidencias}")
    private String uploadDir;

    public String guardar(MultipartFile file) throws IOException {
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."))
                    .toLowerCase()
                    .replaceAll("[^a-z0-9.]", "");
        }
        if (!EXTENSIONES_PERMITIDAS.contains(ext)) {
            throw new IllegalArgumentException("Tipo de archivo no permitido. Use jpg, png o pdf.");
        }
        String filename = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir).toAbsolutePath();
        Files.createDirectories(dir);
        Files.copy(file.getInputStream(), dir.resolve(filename));
        return "/uploads/evidencias/" + filename;
    }
}
