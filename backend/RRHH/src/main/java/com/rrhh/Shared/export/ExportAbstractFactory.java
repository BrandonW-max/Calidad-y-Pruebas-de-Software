package com.rrhh.Shared.export;

public interface ExportAbstractFactory {
    PDFExporter createPDFExporter();
    ExcelExporter createExcelExporter();
}
