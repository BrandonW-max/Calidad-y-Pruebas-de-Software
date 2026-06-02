package com.rrhh.GestionEmpleados.Infrastructure.export;

import com.rrhh.Shared.export.*;
import org.springframework.stereotype.Component;

@Component("gestionEmpleadosExportFactory")
public class GestionEmpleadosExportFactory implements ExportAbstractFactory {

    public static final String[] HEADERS = {"ID", "Nombres", "Documento", "Correo", "Estado", "Fecha Ingreso"};
    public static final String[] KEYS = {"id", "nombres", "documento", "correo", "estado", "fechaIngreso"};

    @Override
    public PDFExporter createPDFExporter() {
        return new GenericPDFExporter();
    }

    @Override
    public ExcelExporter createExcelExporter() {
        return new GenericExcelExporter();
    }
}
