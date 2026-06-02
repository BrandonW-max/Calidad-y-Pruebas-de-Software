package com.rrhh.Reporte.Infrastructure.export;

import com.rrhh.Shared.export.*;
import org.springframework.stereotype.Component;

@Component("reporteExportFactory")
public class ReporteExportFactory implements ExportAbstractFactory {

    // Este factory es genérico para exportar cualquier reporte ya generado
    // Los headers/keys vienen del ReporteService y se pasan dinámicamente

    @Override
    public PDFExporter createPDFExporter() {
        return new GenericPDFExporter();
    }

    @Override
    public ExcelExporter createExcelExporter() {
        return new GenericExcelExporter();
    }
}
