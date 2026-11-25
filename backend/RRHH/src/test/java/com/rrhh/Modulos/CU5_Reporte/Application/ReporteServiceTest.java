package com.rrhh.Modulos.CU5_Reporte.Application;

import com.rrhh.Modulos.CU5_Reporte.Application.dto.FiltroAsistenciaDTO;
import com.rrhh.Modulos.CU5_Reporte.Application.factory.IReporteFactory;
import com.rrhh.Modulos.CU5_Reporte.Application.services.ReporteService;
import com.rrhh.Modulos.CU5_Reporte.Domain.repository.IReporteGeneradoRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteAsistenciaRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteEmpleadoRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteNominaRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteSolicitudRepository;
import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.export.csv.interfaces.CSVExporter;
import com.rrhh.Shared.export.excel.interfaces.ExcelExporter;
import com.rrhh.Shared.export.pdf.interfaces.PDFExporter;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ReporteService - Generación de reportes y validaciones (RF37, RF38)")
class ReporteServiceTest {

    @Mock private IReporteGeneradoRepository reporteGeneradoRepository;
    @Mock private JpaReporteEmpleadoRepository jpaEmpleadoRepository;
    @Mock private JpaReporteAsistenciaRepository jpaAsistenciaRepository;
    @Mock private JpaReporteNominaRepository jpaNominaRepository;
    @Mock private JpaReporteSolicitudRepository jpaSolicitudRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private IReporteFactory reporteFactory;
    @Mock private EntityManager em;

    @InjectMocks
    private ReporteService reporteService;

    private static final Long ID_USUARIO = 1L;

    private static final String MSG_FILTROS_INVALIDOS =
            "Los filtros ingresados no son validos, verifique la informacion.";
    private static final String MSG_FORMATO_NO_SOPORTADO =
            "Formato no soportado: XML. Use PDF, EXCEL o CSV.";
    private static final String MSG_TIPO_SIN_FACTORY =
            "No existe fabrica de exportacion para: INVENTARIO";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(reporteService, "em", em);
        ReflectionTestUtils.setField(reporteService, "exportFactories", new HashMap<>());
    }

    // -----------------------------------------------------------------------
    // Test 51 — RF37: fechaFin anterior a fechaInicio → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 51 — Filtro con fechaInicio posterior a fechaFin → IllegalArgumentException")
    void generarReporte_FechasInvalidas_LanzaExcepcion() {
        FiltroAsistenciaDTO filtro = new FiltroAsistenciaDTO();
        filtro.setFechaInicio(LocalDate.of(2026, 3, 10));
        filtro.setFechaFin(LocalDate.of(2026, 3, 1));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reporteService.generarReporteAsistencia(filtro, ID_USUARIO));

        assertEquals(MSG_FILTROS_INVALIDOS, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 52 — RF38: formato no soportado → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 52 — Formato XML no soportado → IllegalArgumentException")
    void exportar_FormatoNoSoportado_LanzaExcepcion() {
        ExportAbstractFactory factory = mock(ExportAbstractFactory.class);
        Map<String, ExportAbstractFactory> factories = new HashMap<>();
        factories.put("registroAsistenciaExportFactory", factory);
        ReflectionTestUtils.setField(reporteService, "exportFactories", factories);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reporteService.exportarReporte("ASISTENCIA", "XML", List.of()));

        assertEquals(MSG_FORMATO_NO_SOPORTADO, ex.getMessage());
    }

    // -----------------------------------------------------------------------
    // Test 53 — RF37: filtros vacíos → reporte generado correctamente
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 53 — Filtros de asistencia vacíos → reporte generado con 0 registros")
    void generarReporte_FiltrosVacios_ReporteGenerado() throws Exception {
        FiltroAsistenciaDTO filtro = new FiltroAsistenciaDTO();

        com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado reporte =
                new com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado();
        reporte.setIdReporte(1);
        reporte.setTipoReporte("ASISTENCIA");
        reporte.setTotalRegistros(0);

        when(jpaAsistenciaRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class))).thenReturn(List.of());
        when(reporteFactory.crearReporteBase()).thenReturn(reporte);
        when(reporteGeneradoRepository.guardar(any())).thenReturn(reporte);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        com.rrhh.Modulos.CU5_Reporte.Application.dto.ReporteResponseDTO resultado =
                reporteService.generarReporteAsistencia(filtro, ID_USUARIO);

        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalRegistros());
    }

    // -----------------------------------------------------------------------
    // Test 54 — RF38: tipo sin factory registrada → excepción
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Test 55 — Tipo INVENTARIO sin factory registrada → IllegalArgumentException")
    void exportar_TipoSinFactory_LanzaExcepcion() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> reporteService.exportarReporte("INVENTARIO", "PDF", List.of()));

        assertEquals(MSG_TIPO_SIN_FACTORY, ex.getMessage());
    }

    @Test
    @DisplayName("RF10: el historial laboral incluye los cambios salariales (CAMBIO_SALARIAL) con su tipo")
    void historialLaboral_IncluyeCambioSalarial() throws Exception {
        com.rrhh.Shared.persistence.HistorialAuditoriaModel cambio = new com.rrhh.Shared.persistence.HistorialAuditoriaModel();
        cambio.setTablaAfectada("EMPLEADO");
        cambio.setIdRegistroAfectado(7L);
        cambio.setAccion("CAMBIO_SALARIAL");
        cambio.setValorNuevo("sueldo=3500.00 | Motivo: Reajuste anual");
        cambio.setFechaDeCambio(java.time.LocalDateTime.of(2026, 6, 1, 9, 0));

        jakarta.persistence.TypedQuery<com.rrhh.Shared.persistence.HistorialAuditoriaModel> qHistorial =
                mock(jakarta.persistence.TypedQuery.class, RETURNS_SELF);
        when(qHistorial.getResultList()).thenReturn(List.of(cambio));
        jakarta.persistence.TypedQuery<com.rrhh.Shared.persistence.EmpleadoModel> qEmpleados =
                mock(jakarta.persistence.TypedQuery.class, RETURNS_SELF);
        when(qEmpleados.getResultList()).thenReturn(List.of());
        jakarta.persistence.TypedQuery<com.rrhh.Shared.persistence.SancionModel> qSanciones =
                mock(jakarta.persistence.TypedQuery.class, RETURNS_SELF);
        when(qSanciones.getResultList()).thenReturn(List.of());

        doReturn(qEmpleados).when(em).createQuery(anyString(), eq(com.rrhh.Shared.persistence.EmpleadoModel.class));
        doReturn(qHistorial).when(em).createQuery(anyString(), eq(com.rrhh.Shared.persistence.HistorialAuditoriaModel.class));
        doReturn(qSanciones).when(em).createQuery(anyString(), eq(com.rrhh.Shared.persistence.SancionModel.class));

        com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado reporte =
                new com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado();
        reporte.setIdReporte(1);
        when(reporteFactory.crearReporteBase()).thenReturn(reporte);
        when(reporteGeneradoRepository.guardar(any())).thenReturn(reporte);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        com.rrhh.Modulos.CU5_Reporte.Application.dto.FiltroHistorialLaboralDTO filtro =
                new com.rrhh.Modulos.CU5_Reporte.Application.dto.FiltroHistorialLaboralDTO();
        com.rrhh.Modulos.CU5_Reporte.Application.dto.ReporteResponseDTO resultado =
                reporteService.generarReporteHistorialLaboral(filtro, ID_USUARIO);

        org.mockito.ArgumentCaptor<String> consulta = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(em).createQuery(consulta.capture(), eq(com.rrhh.Shared.persistence.HistorialAuditoriaModel.class));
        assertTrue(consulta.getValue().contains("'CAMBIO_SALARIAL'"));

        assertEquals(1, resultado.getTotalRegistros());
        Map<String, Object> fila = resultado.getDatos().get(0);
        assertEquals("Cambio salarial", fila.get("tipo"));
        assertEquals("sueldo=3500.00 | Motivo: Reajuste anual", fila.get("detalle"));
    }

    // -----------------------------------------------------------------------
    // PI5-01, PI5-02, PI5-03 — generación de archivos por tipo y formato
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("PI5-01 — ASISTENCIA en PDF usa el exportador PDF de la fábrica de asistencia")
    void exportar_AsistenciaPdf_UsaExportadorPdf() {
        ExportAbstractFactory factory = mock(ExportAbstractFactory.class);
        PDFExporter pdf = mock(PDFExporter.class);
        byte[] esperado = {1, 2, 3};
        when(factory.createPDFExporter()).thenReturn(pdf);
        when(factory.getHeaders()).thenReturn(new String[]{"Nombre"});
        when(factory.getKeys()).thenReturn(new String[]{"nombre"});
        when(pdf.export(anyString(), anyList(), any(), any())).thenReturn(esperado);
        ReflectionTestUtils.setField(reporteService, "exportFactories", Map.of("registroAsistenciaExportFactory", factory));

        byte[] resultado = reporteService.exportarReporte("ASISTENCIA", "PDF", List.of());

        assertArrayEquals(esperado, resultado);
        verify(pdf).export(eq("Reporte de ASISTENCIA"), anyList(), any(), any());
    }

    @Test
    @DisplayName("PI5-02 — NOMINA en EXCEL usa el exportador Excel de la fábrica de nómina")
    void exportar_NominaExcel_UsaExportadorExcel() {
        ExportAbstractFactory factory = mock(ExportAbstractFactory.class);
        ExcelExporter excel = mock(ExcelExporter.class);
        byte[] esperado = {4, 5, 6};
        when(factory.createExcelExporter()).thenReturn(excel);
        when(factory.getHeaders()).thenReturn(new String[]{"Empleado"});
        when(factory.getKeys()).thenReturn(new String[]{"empleado"});
        when(excel.export(anyString(), anyList(), any(), any())).thenReturn(esperado);
        ReflectionTestUtils.setField(reporteService, "exportFactories", Map.of("nominaExportFactory", factory));

        byte[] resultado = reporteService.exportarReporte("NOMINA", "EXCEL", List.of());

        assertArrayEquals(esperado, resultado);
        verify(excel).export(eq("Reporte de NOMINA"), anyList(), any(), any());
    }

    @Test
    @DisplayName("PI5-03 — SOLICITUDES en PDF usa el exportador PDF de la fábrica de solicitudes")
    void exportar_SolicitudesPdf_UsaExportadorPdf() {
        ExportAbstractFactory factory = mock(ExportAbstractFactory.class);
        PDFExporter pdf = mock(PDFExporter.class);
        byte[] esperado = {7, 8, 9};
        when(factory.createPDFExporter()).thenReturn(pdf);
        when(factory.getHeaders()).thenReturn(new String[]{"Tipo"});
        when(factory.getKeys()).thenReturn(new String[]{"tipo"});
        when(pdf.export(anyString(), anyList(), any(), any())).thenReturn(esperado);
        ReflectionTestUtils.setField(reporteService, "exportFactories", Map.of("solicitudExportFactory", factory));

        byte[] resultado = reporteService.exportarReporte("SOLICITUDES", "pdf", List.of());

        assertArrayEquals(esperado, resultado);
        verify(pdf).export(eq("Reporte de SOLICITUDES"), anyList(), any(), any());
    }

    @Test
    @DisplayName("Generar reporte de empleados con datos")
    void generarReporteEmpleados_Exitoso() {
        com.rrhh.Modulos.CU5_Reporte.Application.dto.FiltroEmpleadoDTO filtro =
                new com.rrhh.Modulos.CU5_Reporte.Application.dto.FiltroEmpleadoDTO();

        com.rrhh.Shared.persistence.EmpleadoModel emp = new com.rrhh.Shared.persistence.EmpleadoModel();
        emp.setIdEmpleado(1L);
        emp.setNombres("Carlos");
        emp.setApellidos("Mendoza");
        emp.setNumeroDi("12345678");
        emp.setCorreo("carlos@hospital.com");
        emp.setEstado("ACTIVO");

        when(jpaEmpleadoRepository.buscarConFiltros(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(emp));

        com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado reporte =
                new com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado();
        reporte.setIdReporte(10);
        reporte.setTipoReporte("EMPLEADOS");

        when(reporteFactory.crearReporteBase()).thenReturn(reporte);
        when(reporteGeneradoRepository.guardar(any())).thenReturn(reporte);

        jakarta.persistence.Query countQuery = mock(jakarta.persistence.Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(10);

        com.rrhh.Modulos.CU5_Reporte.Application.dto.ReporteResponseDTO res =
                reporteService.generarReporteEmpleados(filtro, ID_USUARIO);

        assertNotNull(res);
        assertEquals(1, res.getTotalRegistros());
        assertEquals("EMPLEADOS", res.getTipoReporte());
    }

    @Test
    @DisplayName("Generar reporte de solicitudes con datos")
    void generarReporteSolicitudes_Exitoso() {
        com.rrhh.Modulos.CU5_Reporte.Application.dto.FiltroSolicitudDTO filtro =
                new com.rrhh.Modulos.CU5_Reporte.Application.dto.FiltroSolicitudDTO();

        com.rrhh.Shared.persistence.SolicitudModel sol = new com.rrhh.Shared.persistence.SolicitudModel();
        sol.setIdSolicitud(1);
        sol.setTipoSolicitud("PERMISO");

        when(jpaSolicitudRepository.buscarConFiltros(anyLong(), anyString(), anyString(), anyString(), anyString(), any(), any()))
                .thenReturn(List.of(sol));

        com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado reporte =
                new com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado();
        reporte.setIdReporte(11);
        reporte.setTipoReporte("SOLICITUDES");

        when(reporteFactory.crearReporteBase()).thenReturn(reporte);
        when(reporteGeneradoRepository.guardar(any())).thenReturn(reporte);

        jakarta.persistence.Query countQuery = mock(jakarta.persistence.Query.class);
        when(em.createNativeQuery(anyString())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(10);

        com.rrhh.Modulos.CU5_Reporte.Application.dto.ReporteResponseDTO res =
                reporteService.generarReporteSolicitudes(filtro, ID_USUARIO);

        assertNotNull(res);
        assertEquals(1, res.getTotalRegistros());
        assertEquals("SOLICITUDES", res.getTipoReporte());
    }
}
