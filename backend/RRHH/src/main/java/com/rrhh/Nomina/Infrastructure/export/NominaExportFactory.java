package com.rrhh.Nomina.Infrastructure.export;

import com.rrhh.Shared.export.*;
import org.springframework.stereotype.Component;

@Component("nominaExportFactory")
public class NominaExportFactory implements ExportAbstractFactory {

    public static final String[] HEADERS = {"Empleado", "Periodo", "Sueldo Base", "Horas Extra", "Bonificaciones", "Descuentos", "Sueldo Bruto", "Sueldo Neto", "Estado"};
    public static final String[] KEYS = {"empleado", "periodo", "sueldoBase", "horasExtra", "bonificaciones", "descuentos", "sueldoBruto", "sueldoNeto", "estado"};

    @Override
    public PDFExporter createPDFExporter() {
        return new GenericPDFExporter();
    }

    @Override
    public ExcelExporter createExcelExporter() {
        return new GenericExcelExporter();
    }
}
