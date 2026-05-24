package com.rrhh.Solicitud.Application.services;

import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Solicitud.Application.dto.SolicitudResponseDTO;
import com.rrhh.Solicitud.Domain.entities.Solicitud;
import com.rrhh.Solicitud.Domain.repository.ISolicitudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitudService implements ISolicitudService {

    private static final int DIAS_VACACIONES_ANUALES = 30;

    private final ISolicitudRepository solicitudRepository;
    private final com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository;

    public SolicitudService(
            ISolicitudRepository solicitudRepository,
            com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository) {
        this.solicitudRepository = solicitudRepository;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
    }

    @Override
    @Transactional
    public SolicitudResponseDTO registrarSolicitud(RegistrarSolicitudRequestDTO request, Long idEmpleado) {

        // 1. Validar campos obligatorios
        if (request.getTipoSolicitud() == null || request.getTipoSolicitud().isBlank()) {
            throw new IllegalArgumentException("Debe completar todos los campos obligatorios.");
        }
        if (request.getFechaInicio() == null || request.getFechaFin() == null) {
            throw new IllegalArgumentException("Debe completar todos los campos obligatorios.");
        }
        if (request.getMotivo() == null || request.getMotivo().isBlank()) {
            throw new IllegalArgumentException("Debe completar todos los campos obligatorios.");
        }

        // 2. Validar fechas
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser mayor que la fecha fin."
            );
        }

        // 3. Validar que no incluya fines de semana
        validarDiasLaborables(request.getFechaInicio(), request.getFechaFin());

        // 4. Calcular días solicitados
        int diasSolicitados = calcularDiasLaborables(request.getFechaInicio(), request.getFechaFin());

        // 5. Validar saldo de vacaciones si es tipo VACACIONES
        if ("VACACIONES".equals(request.getTipoSolicitud())) {
            int diasUsados = calcularDiasUsados(idEmpleado);
            int saldoDisponible = DIAS_VACACIONES_ANUALES - diasUsados;
            if (diasSolicitados > saldoDisponible) {
                throw new IllegalStateException(
                    "No cuenta con días disponibles de vacaciones. Saldo disponible: " + saldoDisponible + " días."
                );
            }
        }

        // 6. Verificar empleado activo
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(idEmpleado)
            .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado."));
        if (!"ACTIVO".equals(empleado.getEstado())) {
            throw new IllegalStateException("El empleado no está activo en el sistema.");
        }

        // 7. Registrar solicitud
        Solicitud solicitud = new Solicitud();
        solicitud.setTipoSolicitud(request.getTipoSolicitud());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaFin(request.getFechaFin());
        solicitud.setDiasSolicitados(diasSolicitados);
        solicitud.setMotivo(request.getMotivo());
        solicitud.setArchivoAdjunto(request.getArchivoAdjunto());
        solicitud.setEstado("PENDIENTE");
        solicitud.setIdEmpleado(idEmpleado);
        solicitud.setCreadoEl(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud guardada = solicitudRepository.guardar(solicitud);
        return mapearAResponse(guardada, empleado);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO revisarSolicitudRrhh(RevisionRrhhRequestDTO request, Long idUsuarioRrhh) {

        Solicitud solicitud = solicitudRepository.buscarPorId(request.getIdSolicitud())
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        if (!"PENDIENTE".equals(solicitud.getEstado()) && !"OBSERVADO".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud no está en estado válido para revisión.");
        }

        if (request.isAprobado()) {
            // Enviar a gerencia
            solicitud.setEstado("PROCESADO");
        } else {
            // Devolver al empleado con observación
            if (request.getObservacionRrhh() == null || request.getObservacionRrhh().isBlank()) {
                throw new IllegalArgumentException("Debe ingresar una observación para devolver la solicitud.");
            }
            solicitud.setEstado("OBSERVADO");
            solicitud.setObservacionRrhh(request.getObservacionRrhh());
        }

        solicitud.setRevisadoPor(idUsuarioRrhh);
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.guardar(solicitud);
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(actualizada.getIdEmpleado()).orElse(null);
        return mapearAResponse(actualizada, empleado);
    }

    @Override
    @Transactional
    public SolicitudResponseDTO decidirSolicitudGerencia(DecisionGerenciaRequestDTO request, Long idUsuarioGerencia) {

        Solicitud solicitud = solicitudRepository.buscarPorId(request.getIdSolicitud())
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));

        if (!"PROCESADO".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud no está en estado PROCESADO para decisión de Gerencia.");
        }

        if (request.isAprobado()) {
            solicitud.setEstado("APROBADA");
            solicitud.setRespuesta("Su solicitud de vacaciones o permisos ha sido aprobada por Gerencia.");
        } else {
            if (request.getRespuesta() == null || request.getRespuesta().isBlank()) {
                throw new IllegalArgumentException("Ingrese el motivo del rechazo.");
            }
            solicitud.setEstado("RECHAZADA");
            solicitud.setRespuesta(
                "Su solicitud de vacaciones o permisos ha sido rechazada por Gerencia. Motivo: " + request.getRespuesta()
            );
        }

        solicitud.setAprobadoPor(idUsuarioGerencia);
        solicitud.setFechaDecision(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.guardar(solicitud);
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(actualizada.getIdEmpleado()).orElse(null);
        return mapearAResponse(actualizada, empleado);
    }

    @Override
    public List<SolicitudResponseDTO> listarPorEstado(String estado) {
        return solicitudRepository.buscarPorEstado(estado).stream()
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(s.getIdEmpleado()).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudResponseDTO> listarPorEmpleado(Long idEmpleado) {
        return solicitudRepository.buscarPorEmpleado(idEmpleado).stream()
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(idEmpleado).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    @Override
    public SolicitudResponseDTO buscarPorId(Integer idSolicitud) {
        Solicitud solicitud = solicitudRepository.buscarPorId(idSolicitud)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(solicitud.getIdEmpleado()).orElse(null);
        return mapearAResponse(solicitud, empleado);
    }

    // ================================
    // MÉTODOS PRIVADOS
    // ================================

    private void validarDiasLaborables(LocalDate inicio, LocalDate fin) {
        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin)) {
            if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY || fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
                throw new IllegalArgumentException(
                    "Las fechas seleccionadas incluyen fines de semana o feriados."
                );
            }
            fecha = fecha.plusDays(1);
        }
    }

    private int calcularDiasLaborables(LocalDate inicio, LocalDate fin) {
        int dias = 0;
        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin)) {
            if (fecha.getDayOfWeek() != DayOfWeek.SATURDAY && fecha.getDayOfWeek() != DayOfWeek.SUNDAY) {
                dias++;
            }
            fecha = fecha.plusDays(1);
        }
        return dias;
    }

    private int calcularDiasUsados(Long idEmpleado) {
        return solicitudRepository.buscarPorEmpleado(idEmpleado).stream()
            .filter(s -> "APROBADA".equals(s.getEstado()) && "VACACIONES".equals(s.getTipoSolicitud()))
            .mapToInt(s -> s.getDiasSolicitados() != null ? s.getDiasSolicitados() : 0)
            .sum();
    }

    private SolicitudResponseDTO mapearAResponse(Solicitud solicitud, EmpleadoModel empleado) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setIdSolicitud(solicitud.getIdSolicitud());
        dto.setTipoSolicitud(solicitud.getTipoSolicitud());
        dto.setFechaInicio(solicitud.getFechaInicio());
        dto.setFechaFin(solicitud.getFechaFin());
        dto.setDiasSolicitados(solicitud.getDiasSolicitados());
        dto.setMotivo(solicitud.getMotivo());
        dto.setEstado(solicitud.getEstado());
        dto.setRespuesta(solicitud.getRespuesta());
        dto.setObservacionRrhh(solicitud.getObservacionRrhh());
        dto.setArchivoAdjunto(solicitud.getArchivoAdjunto());
        dto.setFechaRevision(solicitud.getFechaRevision());
        dto.setFechaDecision(solicitud.getFechaDecision());
        dto.setCreadoEl(solicitud.getCreadoEl());
        dto.setIdEmpleado(solicitud.getIdEmpleado());
        if (empleado != null) {
            dto.setNombreEmpleado(empleado.getNombres() + " " + empleado.getApellidos());
        }
        return dto;
    }
}