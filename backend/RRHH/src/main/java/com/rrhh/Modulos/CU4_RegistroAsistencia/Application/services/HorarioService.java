package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Domain.entities.Horario;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HorarioService {

    @PersistenceContext
    private EntityManager em;

    public List<HorarioModel> listar() {
        return em.createQuery("SELECT h FROM HorarioModel h", HorarioModel.class).getResultList();
    }

    public HorarioModel buscarPorId(Integer id) {
        return em.find(HorarioModel.class, id);
    }

    @Transactional
    public HorarioModel guardar(Horario horario) {
        HorarioModel model;
        if (horario.getIdHorario() != null) {
            model = em.find(HorarioModel.class, horario.getIdHorario());
            if (model == null) throw new RuntimeException("Horario no encontrado");
        } else {
            model = new HorarioModel();
        }
        model.setNombreTurno(horario.getNombreTurno());
        model.setHoraEntrada(horario.getHoraEntrada());
        model.setHoraSalida(horario.getHoraSalida());
        model.setTolerancia(horario.getTolerancia());
        model.setUmbralExtra(horario.getUmbralExtra());
        model.setActivo(true);
        return em.merge(model);
    }

    @Transactional
    public void eliminar(Integer id) {
        HorarioModel model = em.find(HorarioModel.class, id);
        if (model != null) {
            model.setActivo(false);
            em.merge(model);
        }
    }

    @Transactional
    public AsignacionHorarioModel asignarHorario(Integer idEmpleado, Integer idHorario) {
        AsignacionHorarioModel asignacion = new AsignacionHorarioModel();
        asignacion.setEmpleado(em.find(
                com.rrhh.Shared.persistence.EmpleadoModel.class, idEmpleado.longValue()));
        asignacion.setHorario(em.find(HorarioModel.class, idHorario));
        asignacion.setFechaDesde(java.time.LocalDate.now());
        asignacion.setFechaHasta(null);
        em.persist(asignacion);
        return asignacion;
    }
}
