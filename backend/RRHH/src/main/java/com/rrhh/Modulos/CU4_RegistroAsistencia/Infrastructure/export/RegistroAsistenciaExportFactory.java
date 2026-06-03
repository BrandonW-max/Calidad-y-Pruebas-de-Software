package com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.export;

import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import com.rrhh.Shared.export.csv.interfaces.CSVExporter;
import com.rrhh.Shared.export.pdf.implementations.GenericPDFExporter;
import com.rrhh.Shared.export.excel.implementations.GenericExcelExporter;
import com.rrhh.Shared.export.csv.implementations.GenericCSVExporter;
import org.springframework.stereotype.Component;

@Component("registroAsistenciaExportFactory")
public class RegistroAsistenciaExportFactory implements ExportAbstractFactory {

    public static final String[] HEADERS = {"Empleado", "Fecha", "Hora Entrada", "Hora Salida", "Min. Tardanza", "Min. Extra", "Estado"};
    public static final String[] KEYS = {"empleado", "fecha", "horaEntrada", "horaSalida", "minutosTardanza", "minutosExtra", "estado"};

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
