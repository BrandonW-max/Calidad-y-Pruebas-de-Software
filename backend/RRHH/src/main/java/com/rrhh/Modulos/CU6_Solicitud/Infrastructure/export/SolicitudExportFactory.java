package com.rrhh.Modulos.CU6_Solicitud.Infrastructure.export;

import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import com.rrhh.Shared.export.csv.interfaces.CSVExporter;
import com.rrhh.Shared.export.pdf.implementations.GenericPDFExporter;
import com.rrhh.Shared.export.excel.implementations.GenericExcelExporter;
import com.rrhh.Shared.export.csv.implementations.GenericCSVExporter;
import org.springframework.stereotype.Component;

@Component("solicitudExportFactory")
public class SolicitudExportFactory implements ExportAbstractFactory {

    public static final String[] HEADERS = {"Empleado", "Tipo", "Fecha Inicio", "Fecha Fin", "Días", "Estado", "Motivo"};
    public static final String[] KEYS = {"empleado", "tipoSolicitud", "fechaInicio", "fechaFin", "diasSolicitados", "estado", "motivo"};

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
