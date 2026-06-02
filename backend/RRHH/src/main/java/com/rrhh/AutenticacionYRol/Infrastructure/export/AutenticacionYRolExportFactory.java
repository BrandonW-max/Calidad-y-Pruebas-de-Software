package com.rrhh.AutenticacionYRol.Infrastructure.export;

import com.rrhh.Shared.export.*;
import org.springframework.stereotype.Component;

@Component("autenticacionYRolExportFactory")
public class AutenticacionYRolExportFactory implements ExportAbstractFactory {

    public static final String[] HEADERS = {"ID Usuario", "Nombre Usuario", "Correo Inst.", "Rol", "Activo", "2FA", "Intentos Fallidos"};
    public static final String[] KEYS = {"idUsuario", "nombreUsuario", "correoInst", "nombreRol", "activo", "dosFA", "intentosFallidos"};

    @Override
    public PDFExporter createPDFExporter() {
        return new GenericPDFExporter();
    }

    @Override
    public ExcelExporter createExcelExporter() {
        return new GenericExcelExporter();
    }
}
