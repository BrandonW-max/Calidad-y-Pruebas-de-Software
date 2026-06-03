package com.rrhh.Modulos.CU5_Reporte.Infrastructure.export;

import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import com.rrhh.Shared.export.csv.interfaces.CSVExporter;
import com.rrhh.Shared.export.pdf.implementations.GenericPDFExporter;
import com.rrhh.Shared.export.excel.implementations.GenericExcelExporter;
import com.rrhh.Shared.export.csv.implementations.GenericCSVExporter;
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

    @Override
    public CSVExporter createCSVExporter() {
        return new com.rrhh.Shared.export.csv.implementations.GenericCSVExporter();
    }
}
