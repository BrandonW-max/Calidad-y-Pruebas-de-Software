package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

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
        JustificacionTardanzaModel j = new JustificacionTardanzaModel();
        j.setIdRegistroAsistencia(idRegistroAsistencia);
        j.setIdEmpleado(idEmpleado);
        j.setMotivo(motivo);
        j.setEvidenciaUrl(evidenciaUrl);
        j.setEstado("PENDIENTE");
        j.setFechaJustificacion(LocalDateTime.now());

        RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, idRegistroAsistencia);
        if (r != null) {
            r.setObservacion("Justificacion pendiente de aprobacion");
            em.merge(r);
        }

        em.persist(j);
        return j;
    }

    @Transactional
    public JustificacionTardanzaModel revisar(Long idJustificacion, String decision,
                                                Long idRevisor, String comentario) {
        JustificacionTardanzaModel j = em.find(JustificacionTardanzaModel.class, idJustificacion);
        if (j == null) throw new RuntimeException("Justificacion no encontrada");

        if (!"PENDIENTE".equals(j.getEstado())) {
            throw new RuntimeException("La justificacion ya fue revisada");
        }

        j.setEstado(decision);
        j.setRevisadoPor(idRevisor);
        j.setFechaRevision(LocalDateTime.now());

        if (comentario != null && !comentario.isBlank()) {
            j.setComentarioRevision(comentario);
        }

        RegistroAsistenciaModel r = em.find(RegistroAsistenciaModel.class, j.getIdRegistroAsistencia());
        if (r != null && "APROBADA".equals(decision)) {
            r.setMinutosTardanza(0);
            r.setEstado("PUNTUAL");
            r.setObservacion("Tardanza justificada y aprobada por RRHH");
            if (r.getHoraEntrada() != null && r.getHoraSalida() != null) {
                long minutos = java.time.Duration.between(r.getHoraEntrada(), r.getHoraSalida()).toMinutes();
                if (minutos > 0) {
                    r.setHorasTrabajadas(java.math.BigDecimal.valueOf(minutos)
                            .divide(java.math.BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP));
                }
            }
            em.merge(r);
        }

        return em.merge(j);
    }

    public List<JustificacionTardanzaModel> listarPendientes() {
        return em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.estado = 'PENDIENTE'",
                JustificacionTardanzaModel.class).getResultList();
    }

    public List<JustificacionTardanzaModel> listarPorEmpleado(Long idEmpleado) {
        return em.createQuery(
                "SELECT j FROM JustificacionTardanzaModel j WHERE j.idEmpleado = :idEmpleado ORDER BY j.fechaJustificacion DESC",
                JustificacionTardanzaModel.class)
                .setParameter("idEmpleado", idEmpleado)
                .getResultList();
    }
}
