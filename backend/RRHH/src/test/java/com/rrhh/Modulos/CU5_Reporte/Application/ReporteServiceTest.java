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
}
