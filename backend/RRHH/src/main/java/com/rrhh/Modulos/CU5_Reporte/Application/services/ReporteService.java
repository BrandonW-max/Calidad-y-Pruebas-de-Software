package com.rrhh.Modulos.CU5_Reporte.Application.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rrhh.Modulos.CU5_Reporte.Application.dto.*;
import com.rrhh.Modulos.CU5_Reporte.Domain.entities.ReporteGenerado;
import com.rrhh.Modulos.CU5_Reporte.Domain.repository.IReporteGeneradoRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteEmpleadoRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteAsistenciaRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteNominaRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.interfaces.JpaReporteSolicitudRepository;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.specifications.ReporteAsistenciaSpecification;
import com.rrhh.Modulos.CU5_Reporte.Infrastructure.specifications.ReporteNominaSpecification;
import com.rrhh.Modulos.CU5_Reporte.Application.factory.IReporteFactory;
import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.persistence.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReporteService implements IReporteService {

    private static final Map<String, String> FACTORY_BY_TIPO = Map.of(
        "EMPLEADOS", "gestionEmpleadosExportFactory",
        "ASISTENCIA", "registroAsistenciaExportFactory",
        "NOMINA", "nominaExportFactory",
        "SOLICITUDES", "solicitudExportFactory",
        "HISTORIAL_LABORAL", "historialLaboralExportFactory"
    );

    private final IReporteGeneradoRepository reporteGeneradoRepository;
    private final JpaReporteEmpleadoRepository jpaEmpleadoRepository;
    private final JpaReporteAsistenciaRepository jpaAsistenciaRepository;
    private final JpaReporteNominaRepository jpaNominaRepository;
    private final JpaReporteSolicitudRepository jpaSolicitudRepository;
    private final ObjectMapper objectMapper;
    private final Map<String, ExportAbstractFactory> exportFactories;
    private final IReporteFactory reporteFactory;

    @PersistenceContext
    private EntityManager em;

    public ReporteService(
            IReporteGeneradoRepository reporteGeneradoRepository,
            JpaReporteEmpleadoRepository jpaEmpleadoRepository,
            JpaReporteAsistenciaRepository jpaAsistenciaRepository,
            JpaReporteNominaRepository jpaNominaRepository,
            JpaReporteSolicitudRepository jpaSolicitudRepository,
            ObjectMapper objectMapper,
            Map<String, ExportAbstractFactory> exportFactories,
            IReporteFactory reporteFactory) {
        this.reporteGeneradoRepository = reporteGeneradoRepository;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaAsistenciaRepository = jpaAsistenciaRepository;
        this.jpaNominaRepository = jpaNominaRepository;
        this.jpaSolicitudRepository = jpaSolicitudRepository;
        this.objectMapper = objectMapper;
        this.exportFactories = exportFactories;
        this.reporteFactory = reporteFactory;
    }

    @Override
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteEmpleados(FiltroEmpleadoDTO filtro, Long idUsuario) {

        validarFiltroEmpleado(filtro);
        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        LocalDateTime inicio = filtro.getFechaInicio() != null
            ? filtro.getFechaInicio().atStartOfDay()
            : LocalDateTime.of(1900, 1, 1, 0, 0);
        LocalDateTime fin = filtro.getFechaFin() != null
            ? filtro.getFechaFin().atTime(23, 59, 59)
            : LocalDateTime.of(2999, 12, 31, 23, 59, 59);

        List<EmpleadoModel> empleados = jpaEmpleadoRepository.buscarConFiltros(
            filtro.getEstado(),
            filtro.getArea(),
            filtro.getCargo(),
            filtro.getTipoContrato(),
            filtro.getIdEmpleado(),
            inicio,
            fin
        );

        if (empleados.isEmpty()) {
            return guardarYRetornar("EMPLEADOS", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("EMPLEADOS", empleados);

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
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteAsistencia(FiltroAsistenciaDTO filtro, Long idUsuario) {

        if (filtro == null) throw new IllegalArgumentException("Los filtros ingresados no son validos, verifique la informacion.");
        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        List<RegistroAsistenciaModel> registros = jpaAsistenciaRepository.findAll(
            ReporteAsistenciaSpecification.conFiltros(
                filtro.getIdEmpleado(),
                filtro.getArea(),
                filtro.getFechaInicio(),
                filtro.getFechaFin()
            )
        );

        if (registros.isEmpty()) {
            return guardarYRetornar("ASISTENCIA", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("ASISTENCIA", registros);

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
            fila.put("tipoRegistro", r.getTipoRegistro() != null ? r.getTipoRegistro() : "ORIGINAL");
            return fila;
        }).collect(Collectors.toList());

        return guardarYRetornar("ASISTENCIA", filtro, datos, idUsuario);
    }

    @Override
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteNomina(FiltroNominaDTO filtro, Long idUsuario) {

        if (filtro == null) throw new IllegalArgumentException("Los filtros ingresados no son validos, verifique la informacion.");
        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        List<NominaModel> nominas = jpaNominaRepository.findAll(
            ReporteNominaSpecification.conFiltros(
                filtro.getIdEmpleado(),
                filtro.getArea(),
                filtro.getFechaInicio(),
                filtro.getFechaFin(),
                filtro.getSalarioMin(),
                filtro.getSalarioMax()
            )
        );

        if (nominas.isEmpty()) {
            return guardarYRetornar("NOMINA", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("NOMINA", nominas);

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
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteSolicitudes(FiltroSolicitudDTO filtro, Long idUsuario) {

        if (filtro == null) throw new IllegalArgumentException("Los filtros ingresados no son validos, verifique la informacion.");
        validarFiltroFechas(filtro.getFechaInicio(), filtro.getFechaFin());

        // Valores centinela en vez de null: PostgreSQL no puede tipar un parámetro nulo en "IS NULL".
        List<SolicitudModel> solicitudes = jpaSolicitudRepository.buscarConFiltros(
            filtro.getIdEmpleado() != null ? filtro.getIdEmpleado() : 0L,
            filtro.getArea() != null ? filtro.getArea() : "",
            filtro.getTipoSolicitud() != null ? filtro.getTipoSolicitud() : "",
            filtro.getEstado() != null ? filtro.getEstado() : "",
            filtro.getCargo() != null ? filtro.getCargo() : "",
            filtro.getFechaInicio() != null ? filtro.getFechaInicio() : LocalDate.of(1900, 1, 1),
            filtro.getFechaFin() != null ? filtro.getFechaFin() : LocalDate.of(2999, 12, 31)
        );

        if (solicitudes.isEmpty()) {
            return guardarYRetornar("SOLICITUDES", filtro, java.util.List.of(), idUsuario);
        }

        validarConsistenciaDatos("SOLICITUDES", solicitudes);

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

    @Override
    @Transactional(timeout = 30)
    public ReporteResponseDTO generarReporteHistorialLaboral(FiltroHistorialLaboralDTO filtro, Long idUsuario) {

        if (filtro == null) throw new IllegalArgumentException("Los filtros ingresados no son validos, verifique la informacion.");

        // 0 como centinela de "todos los empleados" (PostgreSQL no tipa un parámetro nulo)
        Long id = filtro.getIdEmpleado() != null ? filtro.getIdEmpleado() : 0L;

        Map<Long, String> nombres = em.createQuery(
                "SELECT e FROM EmpleadoModel e WHERE (:id = 0L OR e.idEmpleado = :id)", EmpleadoModel.class)
            .setParameter("id", id)
            .getResultList().stream()
            .collect(Collectors.toMap(EmpleadoModel::getIdEmpleado, e -> e.getNombres() + " " + e.getApellidos()));

        List<HistorialAuditoriaModel> cambios = em.createQuery(
                "SELECT h FROM HistorialAuditoriaModel h WHERE h.tablaAfectada = 'EMPLEADO' " +
                "AND h.accion IN ('ASCENSO_EMPLEADO', 'ACTUALIZACION_EMPLEADO') " +
                "AND (:id = 0L OR h.idRegistroAfectado = :id)", HistorialAuditoriaModel.class)
            .setParameter("id", id)
            .getResultList();

        List<SancionModel> sanciones = em.createQuery(
                "SELECT s FROM SancionModel s WHERE (:id = 0L OR s.empleado.idEmpleado = :id)", SancionModel.class)
            .setParameter("id", id)
            .getResultList();

        List<Map<String, Object>> datos = new ArrayList<>();

        for (HistorialAuditoriaModel h : cambios) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", h.getFechaDeCambio() != null ? h.getFechaDeCambio().toLocalDate() : null);
            fila.put("empleado", nombres.getOrDefault(h.getIdRegistroAfectado(), ""));
            fila.put("tipo", "ASCENSO_EMPLEADO".equals(h.getAccion()) ? "Ascenso / cambio de sueldo" : "Actualización de datos");
            fila.put("detalle", h.getValorNuevo());
            datos.add(fila);
        }

        for (SancionModel s : sanciones) {
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("fecha", s.getFechaInicio());
            fila.put("empleado", s.getEmpleado() != null ? nombres.getOrDefault(s.getEmpleado().getIdEmpleado(), "") : "");
            fila.put("tipo", "Sanción");
            fila.put("detalle", s.getMotivo() + " | Estado: " + s.getEstado()
                + (s.getFechaFin() != null ? " | Hasta: " + s.getFechaFin() : ""));
            datos.add(fila);
        }

        datos.sort(Comparator.comparing((Map<String, Object> m) -> (LocalDate) m.get("fecha"),
                Comparator.nullsLast(Comparator.reverseOrder())));

        if (datos.isEmpty()) {
            return guardarYRetornar("HISTORIAL_LABORAL", filtro, List.of(), idUsuario);
        }

        return guardarYRetornar("HISTORIAL_LABORAL", filtro, datos, idUsuario);
    }

    // ================================
    // METODOS PRIVADOS
    // ================================

    private void validarFiltroEmpleado(FiltroEmpleadoDTO filtro) {
        if (filtro == null) {
            throw new IllegalArgumentException(
                "Los filtros ingresados no son validos, verifique la informacion."
            );
        }
    }

    private void validarFiltroFechas(java.time.LocalDate inicio, java.time.LocalDate fin) {
        if (inicio != null && fin != null && inicio.isAfter(fin)) {
            throw new IllegalArgumentException(
                "Los filtros ingresados no son validos, verifique la informacion."
            );
        }
    }

    private void validarConsistenciaDatos(String tipoReporte, List<?> datos) {
        if (datos == null || datos.isEmpty()) {
            return;
        }

        int size = datos.size();

        String query = switch (tipoReporte) {
            case "EMPLEADOS" -> "SELECT COUNT(*) FROM empleado";
            case "ASISTENCIA" -> "SELECT COUNT(*) FROM registro_asistencia";
            case "NOMINA" -> "SELECT COUNT(*) FROM nomina";
            case "SOLICITUDES" -> "SELECT COUNT(*) FROM solicitud";
            default -> null;
        };

        if (query != null) {
            try {
                Number dbCount = (Number) em.createNativeQuery(query).getSingleResult();
                if (size > dbCount.intValue()) {
                    throw new IllegalStateException("Error de consistencia en reporte " + tipoReporte +
                            ": " + size + " registros reportados superan los " + dbCount.intValue() +
                            " registros en la base de datos.");
                }
            } catch (Exception ignore) {
                // Si falla la consulta de conteo, se omite la validación
            }
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

        ReporteGenerado reporte = reporteFactory.crearReporteBase();
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
                                   List<Map<String, Object>> datos) {
        String factoryBeanName = tipo != null
                ? FACTORY_BY_TIPO.getOrDefault(tipo, tipo.toLowerCase() + "ExportFactory")
                : null;
        ExportAbstractFactory factory = factoryBeanName != null ? exportFactories.get(factoryBeanName) : null;
        if (factory == null) {
            throw new IllegalArgumentException("No existe fabrica de exportacion para: " + tipo);
        }

        String titulo = "Reporte de " + tipo;

        if ("PDF".equalsIgnoreCase(formato)) {
            return factory.createPDFExporter().export(titulo, datos, factory.getHeaders(), factory.getKeys());
        }
        if ("EXCEL".equalsIgnoreCase(formato)) {
            return factory.createExcelExporter().export(titulo, datos, factory.getHeaders(), factory.getKeys());
        }
        if ("CSV".equalsIgnoreCase(formato)) {
            return factory.createCSVExporter().export(titulo, datos, factory.getHeaders(), factory.getKeys());
        }
        throw new IllegalArgumentException("Formato no soportado: " + formato + ". Use PDF, EXCEL o CSV.");
    }
}
