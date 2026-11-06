package com.rrhh.Modulos.CU6_Solicitud.Application.services;

import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HistorialAuditoriaModel;
import org.springframework.security.access.AccessDeniedException;
import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.DecisionGerenciaRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RegistrarSolicitudRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.RevisionRrhhRequestDTO;
import com.rrhh.Modulos.CU6_Solicitud.Application.dto.SolicitudResponseDTO;
import com.rrhh.Modulos.CU6_Solicitud.Domain.entities.Solicitud;
import com.rrhh.Modulos.CU6_Solicitud.Domain.repository.ISolicitudRepository;
import com.rrhh.Modulos.CU6_Solicitud.Application.factory.ISolicitudFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SolicitudService implements ISolicitudService {

    @PersistenceContext
    private EntityManager em;

    private final ISolicitudRepository solicitudRepository;
    private final ISolicitudFactory solicitudFactory;
    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository;
    private final JpaUsuarioRepository jpaUsuarioRepository;

    public SolicitudService(
            ISolicitudRepository solicitudRepository,
            ISolicitudFactory solicitudFactory,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository,
            JpaUsuarioRepository jpaUsuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.solicitudFactory = solicitudFactory;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaUsuarioRepository = jpaUsuarioRepository;
    }

    @Override
    @Transactional
    public SolicitudResponseDTO registrarSolicitud(RegistrarSolicitudRequestDTO request, Long idUsuario) {

        java.util.List<String> tiposValidos = java.util.List.of(
                "VACACIONES", "PERMISO_PERSONAL", "PERMISO_MEDICO", "OTROS");
        boolean tipoValido = tiposValidos.stream()
                .anyMatch(t -> t.equalsIgnoreCase(request.getTipoSolicitud()));
        if (!tipoValido) {
            throw new IllegalArgumentException("Error: tipo de solicitud invalido");
        }

        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new IllegalArgumentException(
                "La fecha de inicio no puede ser mayor que la fecha fin."
            );
        }

        validarFechasSolicitud(request.getFechaInicio(), request.getFechaFin());

        int diasSolicitados = calcularDiasSolicitados(request.getFechaInicio(), request.getFechaFin());

        UsuarioModel usuario = jpaUsuarioRepository.findById(Objects.requireNonNull(idUsuario))
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Usuario no encontrado."));
        EmpleadoModel empleado = usuario.getEmpleado();
        if (empleado == null) throw new IllegalArgumentException("El usuario no tiene un empleado vinculado.");
        Long idEmpleado = empleado.getIdEmpleado();

        if (!"ACTIVO".equals(empleado.getEstado())) {
            throw new IllegalStateException("El empleado no esta activo en el sistema.");
        }

        validarSinSolapamiento(idEmpleado, request.getFechaInicio(), request.getFechaFin());

        Solicitud solicitud = solicitudFactory.crearSolicitudPermiso(
                request.getTipoSolicitud(), request.getMotivo());
        solicitud.setFechaInicio(request.getFechaInicio());
        solicitud.setFechaFin(request.getFechaFin());
        solicitud.setDiasSolicitados(diasSolicitados);
        solicitud.setArchivoAdjunto(request.getArchivoAdjunto());
        solicitud.setEstado("PENDIENTE");
        solicitud.setIdEmpleado(idEmpleado);
        solicitud.setCreadoEl(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud guardada = solicitudRepository.guardar(solicitud);
        SolicitudResponseDTO response = mapearAResponse(guardada, empleado);
        response.setMensaje("Solicitud registrada correctamente. Estado: PENDIENTE.");
        return response;
    }

    @Override
    @Transactional
    public SolicitudResponseDTO revisarSolicitudRrhh(RevisionRrhhRequestDTO request, Long idUsuarioRrhh) {

        Solicitud solicitud = solicitudRepository.buscarPorId(request.getIdSolicitud())
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Solicitud no encontrada."));

        if (!"PENDIENTE".equals(solicitud.getEstado()) && !"OBSERVADO".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud no esta en estado valido para revision.");
        }

        if (request.isAprobado()) {
            solicitud.setEstado("PROCESADO");
            solicitud.setObservacionRrhh(request.getObservacionRrhh());
        } else {
            if (request.getObservacionRrhh() == null || request.getObservacionRrhh().isBlank()) {
                throw new IllegalArgumentException("Debe ingresar una observacion para devolver la solicitud.");
            }
            solicitud.setEstado("OBSERVADO");
            solicitud.setObservacionRrhh(request.getObservacionRrhh());
        }

        solicitud.setRevisadoPor(idUsuarioRrhh);
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.guardar(solicitud);
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(actualizada.getIdEmpleado())).orElse(null);

        HistorialAuditoriaModel log = new HistorialAuditoriaModel();
        log.setTablaAfectada("SOLICITUD");
        log.setIdRegistroAfectado(actualizada.getIdSolicitud().longValue());
        log.setAccion("REVISION_RRHH");
        log.setValorNuevo("estado=" + actualizada.getEstado() + ", revisadoPor=" + idUsuarioRrhh
                + ", observacion=" + (request.getObservacionRrhh() != null ? request.getObservacionRrhh() : ""));
        em.persist(log);

        SolicitudResponseDTO response = mapearAResponse(actualizada, empleado);
        response.setMensaje(request.isAprobado()
                ? "Solicitud aprobada por RRHH. Estado: PROCESADO."
                : "Solicitud devuelta. Estado: OBSERVADO.");
        return response;
    }

    @Override
    @Transactional
    public SolicitudResponseDTO decidirSolicitudGerencia(DecisionGerenciaRequestDTO request, Long idUsuarioGerencia) {

        Solicitud solicitud = solicitudRepository.buscarPorId(request.getIdSolicitud())
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Solicitud no encontrada."));

        if (!"PROCESADO".equals(solicitud.getEstado())) {
            throw new IllegalStateException("La solicitud no esta en estado PROCESADO para decision de Gerencia.");
        }

        if (request.isAprobado()) {
            solicitud.setEstado("APROBADA");
            solicitud.setRespuesta("Solicitud aprobada por Gerencia. Estado: APROBADA.");
        } else {
            if (request.getRespuesta() == null || request.getRespuesta().isBlank()) {
                throw new IllegalArgumentException("Ingrese el motivo del rechazo.");
            }
            solicitud.setEstado("RECHAZADA");
            solicitud.setRespuesta("Solicitud rechazada. Estado: RECHAZADA.");
        }

        solicitud.setAprobadoPor(idUsuarioGerencia);
        solicitud.setFechaDecision(LocalDateTime.now());
        solicitud.setActualizadoEl(LocalDateTime.now());

        Solicitud actualizada = solicitudRepository.guardar(solicitud);
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(actualizada.getIdEmpleado())).orElse(null);

        HistorialAuditoriaModel log = new HistorialAuditoriaModel();
        log.setTablaAfectada("SOLICITUD");
        log.setIdRegistroAfectado(actualizada.getIdSolicitud().longValue());
        log.setAccion(request.isAprobado() ? "APROBACION_GERENCIA" : "RECHAZO_GERENCIA");
        log.setValorAnterior("estado=PROCESADO");
        log.setValorNuevo("estado=" + actualizada.getEstado() + ", aprobadoPor=" + idUsuarioGerencia
                + ", respuesta=" + actualizada.getRespuesta());
        em.persist(log);

        SolicitudResponseDTO response = mapearAResponse(actualizada, empleado);
        response.setMensaje(request.isAprobado()
                ? "Solicitud aprobada por Gerencia. Estado: APROBADA."
                : "Solicitud rechazada. Estado: RECHAZADA.");
        return response;
    }

    @Override
    public List<SolicitudResponseDTO> listarPorEstado(String estado) {
        return solicitudRepository.buscarPorEstado(estado).stream()
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(Objects.requireNonNull(s.getIdEmpleado())).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<SolicitudResponseDTO> listarPorEmpleado(Long idEmpleado) {
        return solicitudRepository.buscarPorEmpleado(idEmpleado).stream()
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(Objects.requireNonNull(idEmpleado)).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    @Override
    public SolicitudResponseDTO buscarPorId(Integer idSolicitud) {
        Solicitud solicitud = solicitudRepository.buscarPorId(idSolicitud)
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Solicitud no encontrada."));
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(solicitud.getIdEmpleado())).orElse(null);
        return mapearAResponse(solicitud, empleado);
    }

    public List<SolicitudResponseDTO> listarNotificacionesPorEmpleado(Long idEmpleado) {
        return solicitudRepository.buscarPorEmpleado(idEmpleado).stream()
            .filter(s -> ("APROBADA".equals(s.getEstado()) || "RECHAZADA".equals(s.getEstado())))
            .filter(s -> s.getFechaDecision() != null)
            .sorted((a, b) -> b.getFechaDecision().compareTo(a.getFechaDecision()))
            .map(s -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(Objects.requireNonNull(s.getIdEmpleado())).orElse(null);
                return mapearAResponse(s, emp);
            })
            .collect(Collectors.toList());
    }

    // ================================
    // PRIVATE
    // ================================

    /** Una solicitud RECHAZADA no bloquea nuevas fechas; las demás estados activos sí. */
    private void validarSinSolapamiento(Long idEmpleado, LocalDate inicio, LocalDate fin) {
        List<String> estadosActivos = List.of("PENDIENTE", "OBSERVADO", "PROCESADO", "APROBADA");
        boolean solapada = solicitudRepository.buscarPorEmpleado(idEmpleado).stream()
                .filter(s -> estadosActivos.contains(s.getEstado()))
                .anyMatch(s -> !s.getFechaInicio().isAfter(fin) && !s.getFechaFin().isBefore(inicio));
        if (solapada) {
            throw new IllegalStateException(
                "Ya tiene una solicitud activa que se cruza con las fechas indicadas.");
        }
    }

    /** RNF77: el personal de turnos rotativos puede pedir fines de semana; solo se rechazan los feriados del rango. */
    private void validarFechasSolicitud(LocalDate inicio, LocalDate fin) {
        List<LocalDate> feriados = em.createQuery(
                "SELECT f.fecha FROM FeriadoModel f", LocalDate.class).getResultList();

        boolean incluyeFeriado = feriados.stream()
                .anyMatch(f -> !f.isBefore(inicio) && !f.isAfter(fin));
        if (incluyeFeriado) {
            throw new IllegalArgumentException("El rango de fechas incluye un feriado nacional.");
        }
    }

    /** Lanza 403 si el empleado de la sesión no es dueño del registro indicado. */
    public void validarPropiedad(Long idUsuario, Long idEmpleadoDueno) {
        UsuarioModel usuario = jpaUsuarioRepository.findById(Objects.requireNonNull(idUsuario))
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Usuario no encontrado."));
        EmpleadoModel empleado = usuario.getEmpleado();
        if (empleado == null || !Objects.equals(empleado.getIdEmpleado(), idEmpleadoDueno)) {
            throw new AccessDeniedException("No puede acceder a solicitudes de otro empleado.");
        }
    }

    /** Días solicitados: todos los días del rango (fines de semana incluidos), ya validado que no haya feriados. */
    private int calcularDiasSolicitados(LocalDate inicio, LocalDate fin) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(inicio, fin) + 1;
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
