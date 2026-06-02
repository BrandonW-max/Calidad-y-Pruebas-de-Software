package com.rrhh.Reporte.Application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.Reporte.Application.dto.*;
import com.rrhh.Reporte.Domain.entities.ReporteGenerado;
import com.rrhh.Reporte.Domain.repository.IReporteGeneradoRepository;
import com.rrhh.Reporte.Infrastructure.interfaces.JpaReporteEmpleadoRepository;
import com.rrhh.Reporte.Infrastructure.interfaces.JpaReporteAsistenciaRepository;
import com.rrhh.Reporte.Infrastructure.interfaces.JpaReporteNominaRepository;
import com.rrhh.Reporte.Infrastructure.interfaces.JpaReporteSolicitudRepository;
import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.persistence.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReporteService implements IReporteService {

    private static final Map<String, String> FACTORY_BY_TIPO = Map.of(
        "EMPLEADOS", "gestionEmpleadosExportFactory",
        "ASISTENCIA", "registroAsistenciaExportFactory",
        "NOMINA", "nominaExportFactory",
        "SOLICITUDES", "solicitudExportFactory"
    );

    private final IReporteGeneradoRepository reporteGeneradoRepository;
    private final JpaReporteEmpleadoRepository jpaEmpleadoRepository;
    private final JpaReporteAsistenciaRepository jpaAsistenciaRepository;
    private final JpaReporteNominaRepository jpaNominaRepository;
    private final JpaReporteSolicitudRepository jpaSolicitudRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, ExportAbstractFactory> exportFactories;

    public ReporteService(
            IReporteGeneradoRepository reporteGeneradoRepository,
            JpaReporteEmpleadoRepository jpaEmpleadoRepository,
            JpaReporteAsistenciaRepository jpaAsistenciaRepository,
            JpaReporteNominaRepository jpaNominaRepository,
            JpaReporteSolicitudRepository jpaSolicitudRepository,
            ObjectMapper objectMapper,
            Map<String, ExportAbstractFactory> exportFactories) {
        this.reporteGeneradoRepository = reporteGeneradoRepository;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaAsistenciaRepository = jpaAsistenciaRepository;
        this.jpaNominaRepository = jpaNominaRepository;
        this.jpaSolicitudRepository = jpaSolicitudRepository;
        this.objectMapper = objectMapper;
        this.exportFactories = exportFactories;
    }

    @Override
    public ReporteResponseDTO generarReporteEmpleados(FiltroEmpleadoDTO filtro, Long idUsuario) {

        // Validar filtros
        validarFiltroEmpleado(filtro);

        // Consultar BD
        List<EmpleadoModel> empleados = jpaEmpleadoRepository.buscarConFiltros(
            filtro.getEstado(),
            filtro.getCargo(),
            filtro.getIdEmpleado()
        );

        if (empleados.isEmpty()) {
            throw new IllegalStateException("No se encontraron datos para los filtros seleccionados.");
        }

        // Mapear datos
        List<Map<String, Object>> datos = empleados.stream().map(e -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("id", e.getIdEmpleado());
            fila.put("nombres", e.getNombres() + " " + e.getApellidos());
            fila.put("documento", e.getNumeroDi());
            fila.put("correo", e.getCorreo());
            fila.put("estado", e.getEstado());
            fila.put("fechaIngreso", e.getCreadoEl() != null ? e.getCreadoEl().toLocalDate() : null);
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("EMPLEADOS", filtro, datos, idUsuario);
    }

    @Override
    public ReporteResponseDTO generarReporteAsistencia(FiltroAsistenciaDTO filtro, Long idUsuario) {

        // Validar filtros
        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        // Consultar BD
        List<RegistroAsistenciaModel> registros = jpaAsistenciaRepository.buscarConFiltros(
            filtro.getIdEmpleado(),
            filtro.getFechaInicio(),
            filtro.getFechaFin()
        );

        if (registros.isEmpty()) {
            throw new IllegalStateException("No se encontraron datos para los filtros seleccionados.");
        }

        // Mapear datos
        List<Map<String, Object>> datos = registros.stream().map(r -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("empleado", r.getEmpleado() != null ?
                r.getEmpleado().getNombres() + " " + r.getEmpleado().getApellidos() : "");
            fila.put("fecha", r.getFecha());
            fila.put("horaEntrada", r.getHoraEntrada());
            fila.put("horaSalida", r.getHoraSalida());
            fila.put("minutosTardanza", r.getMinutosTardanza());
            fila.put("minutosExtra", r.getMinutosExtra());
            fila.put("estado", r.getEstado());
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("ASISTENCIA", filtro, datos, idUsuario);
    }

    @Override
    public ReporteResponseDTO generarReporteNomina(FiltroNominaDTO filtro, Long idUsuario) {

        // Validar filtros
        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        // Consultar BD
        List<NominaModel> nominas = jpaNominaRepository.buscarConFiltros(
            filtro.getIdEmpleado(),
            filtro.getFechaInicio(),
            filtro.getFechaFin(),
            filtro.getSalarioMin(),
            filtro.getSalarioMax()
        );

        if (nominas.isEmpty()) {
            throw new IllegalStateException("No se encontraron datos para los filtros seleccionados.");
        }

        // Mapear datos
        List<Map<String, Object>> datos = nominas.stream().map(n -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("empleado", n.getEmpleado() != null ?
                n.getEmpleado().getNombres() + " " + n.getEmpleado().getApellidos() : "");
            fila.put("periodo", n.getPeriodo());
            fila.put("sueldoBase", n.getSueldoBase());
            fila.put("horasExtra", n.getTotalHorasExtra());
            fila.put("bonificaciones", sumaBonificaciones(n));
            fila.put("descuentos", n.getDescuentoTardanzas());
            fila.put("sueldoBruto", n.getSueldoBruto());
            fila.put("sueldoNeto", n.getSueldoNeto());
            fila.put("estado", n.getEstadoPago());
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("NOMINA", filtro, datos, idUsuario);
    }

    @Override
    public ReporteResponseDTO generarReporteSolicitudes(FiltroSolicitudDTO filtro, Long idUsuario) {

        // Validar filtros
        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        // Consultar BD
        List<SolicitudModel> solicitudes = jpaSolicitudRepository.buscarConFiltros(
            filtro.getIdEmpleado(),
            filtro.getTipoSolicitud(),
            filtro.getEstado(),
            filtro.getFechaInicio(),
            filtro.getFechaFin()
        );

        if (solicitudes.isEmpty()) {
            throw new IllegalStateException("No se encontraron datos para los filtros seleccionados.");
        }

        // Mapear datos
        List<Map<String, Object>> datos = solicitudes.stream().map(s -> {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("empleado", s.getEmpleado() != null ?
                s.getEmpleado().getNombres() + " " + s.getEmpleado().getApellidos() : "");
            fila.put("tipoSolicitud", s.getTipoSolicitud());
            fila.put("fechaInicio", s.getFechaInicio());
            fila.put("fechaFin", s.getFechaFin());
            fila.put("diasSolicitados", s.getDiasSolicitados());
            fila.put("estado", s.getEstado());
            fila.put("motivo", s.getMotivo());
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("SOLICITUDES", filtro, datos, idUsuario);
    }

    // ================================
    // MÉTODOS PRIVADOS
    // ================================

    private void validarFiltroEmpleado(FiltroEmpleadoDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                "Los filtros ingresados no son válidos, verifique la información."
            );
        }
    }

    private void validarFiltroFechas(java.time.LocalDate inicio, java.time.LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                "Los filtros ingresados no son válidos, verifique la información."
            );
        }
    }

    private java.math.BigDecimal sumaBonificaciones(NominaModel n) {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        if (n.getBonifFamiliar() != null) total = total.add(n.getBonifFamiliar());
        if (n.getBonifTurnoNocturno() != null) total = total.add(n.getBonifTurnoNocturno());
        if (n.getBonifGuardia() != null) total = total.add(n.getBonifGuardia());
        if (n.getBonifRiesgo() != null) total = total.add(n.getBonifRiesgo());
        if (n.getBonifCargo() != null) total = total.add(n.getBonifCargo());
        return total;
    }

    private ReporteResponseDTO guardarYRetornar(
            String tipo, Object filtro,
            List<Map<String, Object>> datos, Long idUsuario) {
        return guardarYRetornar(tipo, filtro, datos, idUsuario, null);
    }

    private ReporteResponseDTO guardarYRetornar(
            String tipo, Object filtro,
            List<Map<String, Object>> datos, Long idUsuario, String formato) {

        // Guardar registro del reporte
        ReporteGenerado reporte = new ReporteGenerado();
        reporte.setTipoReporte(tipo);
        reporte.setTotalRegistros(datos.size());
        reporte.setGeneradoPor(idUsuario);
        reporte.setFormatoExportacion(formato);
        reporte.setCreadoEl(LocalDateTime.now());
        try {
            reporte.setFiltrosAplicados(objectMapper.writeValueAsString(filtro));
        } catch (Exception e) {
            reporte.setFiltrosAplicados("{}");
        }
        ReporteGenerado guardado = reporteGeneradoRepository.guardar(reporte);

        // Construir response
        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setIdReporte(guardado.getIdReporte());
        response.setTipoReporte(tipo);
        response.setTotalRegistros(datos.size());
        response.setGeneradoEl(guardado.getCreadoEl());
        response.setDatos(datos);
        return response;
    }

    @Override
    public byte[] exportarReporte(String tipo, String formato,
                                   List<Map<String, Object>> datos,
                                   String[] headers, String[] keys) {
        String factoryBeanName = FACTORY_BY_TIPO.getOrDefault(tipo, tipo.toLowerCase() + "ExportFactory");
        ExportAbstractFactory factory = exportFactories.get(factoryBeanName);
        if (factory == null) {
            throw new IllegalArgumentException("No existe fabrica de exportacion para: " + tipo);
        }

        String titulo = "Reporte de " + tipo;

        if ("PDF".equalsIgnoreCase(formato)) {
            return factory.createPDFExporter().export(titulo, datos, headers, keys);
        }
        if ("EXCEL".equalsIgnoreCase(formato)) {
            return factory.createExcelExporter().export(titulo, datos, headers, keys);
        }
        throw new IllegalArgumentException("Formato no soportado: " + formato + ". Use PDF o EXCEL.");
    }
}