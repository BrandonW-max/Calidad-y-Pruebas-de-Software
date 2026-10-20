package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.JustificacionTardanzaModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class JustificacionService {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public JustificacionTardanzaModel registrar(Integer idRegistroAsistencia, Long idEmpleado,
                                                 String motivo, String evidenciaUrl) {
        if (idRegistroAsistencia == null) {
            throw new IllegalArgumentException("Error: debe enviar el ID del registro de asistencia");
        }
        RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, idRegistroAsistencia);
        if (r == null) {
            throw new com.rrhh.Shared.exception.RecursoNoEncontradoException("Registro de asistencia no encontrado");
        }

        if (r.getEmpleado() == null || !idEmpleado.equals(r.getEmpleado().getIdEmpleado())) {
            throw new RuntimeException("El registro de asistencia no corresponde al empleado.");
        }

        boolean tardanzaJustificable = r.getMinutosTardanza() != null && r.getMinutosTardanza() >= 60;
        boolean esInasistencia = "INASISTENCIA".equals(r.getEstado());
        if (!tardanzaJustificable && !esInasistencia) {
            throw new RuntimeException("Solo se pueden justificar tardanzas de una hora o más e inasistencias.");
        }

        Long existentes = em.createQuery(
                        "SELECT COUNT(j) FROM JustificacionTardanzaModel j WHERE j.idRegistroAsistencia = :idRegistro AND j.estado = 'PENDIENTE'",
                        Long.class)
                .setParameter("idRegistro", idRegistroAsistencia)
                .getSingleResult();
        if (existentes > 0) {
            throw new RuntimeException("Ya existe una justificacion pendiente para este registro.");
        }

        LocalDate fechaRegistro = r.getFecha();
        if (LocalDate.now().isAfter(fechaRegistro.plusDays(1))) {
            throw new RuntimeException("El plazo de justificacion vencio. Solo puedes justificar el mismo dia o el dia siguiente.");
        }

        JustificacionTardanzaModel j = new JustificacionTardanzaModel();
        j.setIdRegistroAsistencia(idRegistroAsistencia);
        j.setIdEmpleado(idEmpleado);
        j.setMotivo(motivo);
        j.setEvidenciaUrl(evidenciaUrl);
        j.setEstado("PENDIENTE");
        j.setFechaJustificacion(LocalDateTime.now());

        r.setObservacion("Justificacion pendiente de aprobacion");
        em.merge(r);

        em.persist(j);
        return j;
    }

    @Transactional
    public JustificacionTardanzaModel revisar(Long idJustificacion, String decision,
                                                Long idRevisor, String comentario) {
        JustificacionTardanzaModel j = em.find(JustificacionTardanzaModel.class, idJustificacion);
        if (j == null) throw new com.rrhh.Shared.exception.RecursoNoEncontradoException("Justificacion no encontrada");

        if (!"PENDIENTE".equals(j.getEstado())) {
            throw new RuntimeException("La justificacion ya fue revisada");
        }

        j.setEstado(decision);
        j.setRevisadoPor(idRevisor);
        j.setFechaRevision(LocalDateTime.now());

        if (comentario != null && !comentario.isBlank()) {
            j.setComentarioRevision(comentario);
        }

        // Si se aprueba: anular la tardanza en el registro de asistencia
        // para que la nómina no descuente esos minutos
        if ("APROBADA".equals(decision) && j.getIdRegistroAsistencia() != null) {
            RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
            if (r != null) {
                r.setMinutosTardanza(0);
                r.setObservacion("Tardanza justificada y aprobada");
                em.merge(r);
            }
        }

        return em.merge(j);
    }

    public List<JustificacionTardanzaModel> listarPendientes() {
        List<JustificacionTardanzaModel> pendientes = em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.estado = 'PENDIENTE'",
                JustificacionTardanzaModel.class).getResultList();
        pendientes.forEach(this::enriquecerConDatosAsistencia);
        return pendientes;
    }

    public List<JustificacionTardanzaModel> listarPorEmpleado(Long idEmpleado) {
        List<JustificacionTardanzaModel> lista = em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.idEmpleado = :idEmpleado ORDER BY j.fechaJustificacion DESC",
                JustificacionTardanzaModel.class)
                .setParameter("idEmpleado", idEmpleado)
                .getResultList();
        lista.forEach(this::enriquecerConDatosAsistencia);
        return lista;
    }

    private void enriquecerConDatosAsistencia(JustificacionTardanzaModel j) {
        if (j.getIdRegistroAsistencia() == null) return;

        RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
        if (r == null) return;

        if (r.getHoraEntrada() != null) {
            j.setHoraEntrada(r.getHoraEntrada().toString().substring(0, 5));
        }
        if (r.getFecha() != null) {
            j.setFechaAsistencia(r.getFecha().toString());
        }
        j.setMinutosTardanza(r.getMinutosTardanza());

        // Buscar hora de entrada esperada según el horario asignado del empleado en esa fecha
        if (j.getIdEmpleado() != null && r.getFecha() != null) {
            List<AsignacionHorarioModel> asignaciones = em.createQuery(
                    "SELECT a FROM AsignacionHorarioModel a " +
                    "WHERE a.empleado.idEmpleado = :idEmpleado AND a.activo = true " +
                    "AND a.fechaDesde <= :fecha " +
                    "AND (a.fechaHasta IS NULL OR a.fechaHasta >= :fecha) " +
                    "ORDER BY a.fechaDesde DESC",
                    AsignacionHorarioModel.class)
                    .setParameter("idEmpleado", j.getIdEmpleado())
                    .setParameter("fecha", r.getFecha())
                    .setMaxResults(1)
                    .getResultList();

            if (!asignaciones.isEmpty() && asignaciones.get(0).getHorario() != null) {
                HorarioModel horario = asignaciones.get(0).getHorario();
                if (horario.getHoraEntrada() != null) {
                    j.setHoraEntradaEsperada(horario.getHoraEntrada().toString().substring(0, 5));
                }
            }
        }
    }
}
