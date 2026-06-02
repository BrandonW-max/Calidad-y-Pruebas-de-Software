package com.rrhh.RegistroAsistencia.Infrastructure.export;

import com.rrhh.Shared.export.*;
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
}
