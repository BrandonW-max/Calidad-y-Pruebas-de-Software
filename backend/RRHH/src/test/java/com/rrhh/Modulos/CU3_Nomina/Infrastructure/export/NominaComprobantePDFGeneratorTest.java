package com.rrhh.Modulos.CU3_Nomina.Infrastructure.export;

import com.lowagie.text.pdf.PdfReader;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.DetalleNominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Shared.export.pdf.implementations.GenericPDFExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas de regresión: Generación de PDF (Nómina y Comprobantes con OpenPDF)")
class NominaComprobantePDFGeneratorTest {

    private NominaComprobantePDFGenerator generator;
    private GenericPDFExporter genericExporter;

    @BeforeEach
    void setUp() {
        generator = new NominaComprobantePDFGenerator();
        genericExporter = new GenericPDFExporter();
    }

    private NominaResponseDTO buildNominaMock() {
        NominaResponseDTO dto = new NominaResponseDTO();
        dto.setIdNomina(101);
        dto.setPeriodo("2026-03");
        dto.setFechaInicio(LocalDate.of(2026, 3, 1));
        dto.setFechaFin(LocalDate.of(2026, 3, 31));
        dto.setFechaEmision(LocalDate.now());
        dto.setNombreEmpleado("Juan Carlos Perez");
        dto.setIdEmpleado(42L);
        dto.setEstadoPago("PAGADO");

        dto.setSueldoBase(new BigDecimal("3500.00"));
        dto.setSueldoContrato(new BigDecimal("3500.00"));
        dto.setTotalHorasTrabajadas(new BigDecimal("160.00"));
        dto.setTotalHorasExtra(new BigDecimal("10.00"));
        dto.setTotalMinutosTardanza(15);

        dto.setBonifFamiliar(new BigDecimal("113.00"));
        dto.setBonifTurnoNocturno(new BigDecimal("200.00"));
        dto.setBonifGuardia(new BigDecimal("150.00"));
        dto.setBonifRiesgo(new BigDecimal("175.00"));

        dto.setDescuentoLey(new BigDecimal("455.00"));
        dto.setDescuentoTardanzas(new BigDecimal("32.50"));

        dto.setSueldoBruto(new BigDecimal("4138.00"));
        dto.setSueldoNeto(new BigDecimal("3650.50"));

        List<DetalleNominaResponseDTO> detalles = new ArrayList<>();
        DetalleNominaResponseDTO det1 = new DetalleNominaResponseDTO();
        det1.setFecha(LocalDate.of(2026, 3, 2));
        det1.setHorasTrabajadas(new BigDecimal("8.00"));
        det1.setMinutosTardanza(15);
        det1.setDescuentoTardanza(new BigDecimal("32.50"));
        det1.setMinutosExtra(60);
        det1.setMontoHoraExtra(new BigDecimal("45.00"));
        detalles.add(det1);

        DetalleNominaResponseDTO det2 = new DetalleNominaResponseDTO();
        det2.setFecha(LocalDate.of(2026, 3, 3));
        det2.setHorasTrabajadas(new BigDecimal("8.00"));
        det2.setMinutosTardanza(0);
        det2.setDescuentoTardanza(BigDecimal.ZERO);
        det2.setMinutosExtra(0);
        det2.setMontoHoraExtra(BigDecimal.ZERO);
        detalles.add(det2);

        dto.setDetalles(detalles);
        return dto;
    }

    @Test
    @DisplayName("Regresión NominaComprobante: Genera comprobante PDF completo con tablas, colores y detalle diario")
    void generate_conDatosCompletos_retornaPdfValido() throws Exception {
        NominaResponseDTO dto = buildNominaMock();

        byte[] pdfBytes = generator.generate(dto);

        assertNotNull(pdfBytes, "El PDF generado no debe ser nulo");
        assertTrue(pdfBytes.length > 500, "El PDF generado debe contener bytes significativos");

        // Validar cabecera PDF
        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header, "El documento generado debe comenzar con '%PDF-'");

        // Parsear con PdfReader para verificar estructura interna válida
        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        try {
            assertTrue(reader.getNumberOfPages() >= 1, "El comprobante debe tener al menos 1 página");
            assertNotNull(reader.getCatalog(), "El catálogo del PDF debe ser accesible");
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Regresión NominaComprobante: Genera comprobante PDF sin detalles diarios sin fallar")
    void generate_sinDetallesDiarios_retornaPdfValido() throws Exception {
        NominaResponseDTO dto = buildNominaMock();
        dto.setDetalles(null);

        byte[] pdfBytes = generator.generate(dto);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 500);

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        try {
            assertEquals(1, reader.getNumberOfPages());
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Regresión GenericPDFExporter: Exporta tabla de datos a PDF correctamente")
    void genericExporter_exportConDatos_retornaPdfValido() throws Exception {
        String[] headers = {"DNI", "Empleado", "Cargo", "Fecha Ingreso"};
        String[] keys = {"dni", "nombre", "cargo", "fecha"};

        List<Map<String, Object>> rows = List.of(
                Map.of("dni", "72345678", "nombre", "Maria Lopez", "cargo", "Enfermera", "fecha", LocalDate.of(2023, 1, 15)),
                Map.of("dni", "73456789", "nombre", "Carlos Perez", "cargo", "Médico", "fecha", LocalDate.of(2022, 5, 20))
        );

        byte[] pdfBytes = genericExporter.export("Reporte de Personal Hospital San Gabriel", rows, headers, keys);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 200);

        String header = new String(pdfBytes, 0, 5, StandardCharsets.US_ASCII);
        assertEquals("%PDF-", header);

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        try {
            assertTrue(reader.getNumberOfPages() >= 1);
        } finally {
            reader.close();
        }
    }

    @Test
    @DisplayName("Regresión GenericPDFExporter: Exporta mensaje informativo cuando no hay datos")
    void genericExporter_exportSinDatos_retornaPdfValido() throws Exception {
        String[] headers = {"Columna1", "Columna2"};
        String[] keys = {"c1", "c2"};

        byte[] pdfBytes = genericExporter.export("Reporte Vacío", List.of(), headers, keys);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100);

        PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        try {
            assertEquals(1, reader.getNumberOfPages());
        } finally {
            reader.close();
        }
    }
}
