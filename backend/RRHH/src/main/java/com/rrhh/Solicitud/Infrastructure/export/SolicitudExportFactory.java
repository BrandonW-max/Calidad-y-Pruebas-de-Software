package com.rrhh.Solicitud.Infrastructure.export;

import com.rrhh.Shared.export.*;
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
}
