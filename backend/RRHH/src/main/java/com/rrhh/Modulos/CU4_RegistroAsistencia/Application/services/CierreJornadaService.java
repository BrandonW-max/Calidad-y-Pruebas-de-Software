package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CierreJornadaService {

    private final JpaRegistroAsistenciaNominaRepository repository;
    private final JpaEmpleadoRepository empleadoRepository;

    @PersistenceContext
    private EntityManager em;

    public CierreJornadaService(JpaRegistroAsistenciaNominaRepository repository,
                                JpaEmpleadoRepository empleadoRepository) {
        this.repository = repository;
        this.empleadoRepository = empleadoRepository;
    }

    /** Busca la hora de salida del horario vigente para el empleado en una fecha dada. */
    private LocalTime getHoraSalidaVigente(Long idEmpleado, LocalDate fecha) {
        List<AsignacionHorarioModel> asignaciones = em.createQuery(
                "SELECT a FROM AsignacionHorarioModel a " +
                "WHERE a.empleado.idEmpleado = :idEmpleado AND a.activo = true " +
                "AND a.fechaDesde <= :fecha " +
                "AND (a.fechaHasta IS NULL OR a.fechaHasta >= :fecha) " +
                "ORDER BY a.fechaDesde DESC",
                AsignacionHorarioModel.class)
                .setParameter("idEmpleado", idEmpleado)
                .setParameter("fecha", fecha)
                .setMaxResults(1)
                .getResultList();

        if (!asignaciones.isEmpty() && asignaciones.get(0).getHorario() != null
                && asignaciones.get(0).getHorario().getHoraSalida() != null) {
            return asignaciones.get(0).getHorario().getHoraSalida();
        }
        return LocalTime.of(17, 0); // fallback si no tiene horario asignado
    }

    @Transactional
    @Scheduled(cron = "${asistencia.cierre.cron:0 0 22 * * *}", zone = "${sistema.zona-horaria:America/Lima}")
    public void cerrarJornadasAbiertas() {
        LocalDate hoy = LocalDate.now();

        List<RegistroAsistenciaModel> abiertos =
                repository.findByFechaAndHoraSalidaIsNullAndTipoRegistro(hoy, "ORIGINAL");

        List<RegistroAsistenciaModel> cerrados = new ArrayList<>();

        for (RegistroAsistenciaModel r : abiertos) {
            if (r.getHoraEntrada() != null) {
                LocalTime horaCierre = getHoraSalidaVigente(r.getEmpleado().getIdEmpleado(), hoy);
                long minutos = Duration.between(r.getHoraEntrada(), horaCierre).toMinutes();
                if (minutos > 0) {
                    r.setHoraSalida(horaCierre);
                    BigDecimal horas = BigDecimal.valueOf(minutos)
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                    r.setHorasTrabajadas(horas);
                    r.setTipoUltimoRegistro("CIERRE_AUTOMATICO");
                    r.setObservacion("Jornada cerrada automaticamente. Salida no registrada.");
                    cerrados.add(r);
                }
            }
        }

        if (!cerrados.isEmpty()) {
            repository.saveAll(cerrados);
        }

        registrarInasistencias(hoy);
    }

    /**
     * Cada 5 minutos revisa empleados con entrada pero sin salida.
     * Si ya pasaron 15 minutos desde su horaSalida programada → cierra automáticamente.
     */
    @Transactional
    @Scheduled(fixedDelay = 300000, initialDelay = 60000)
    public void autoSalidaPorHorario() {
        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();

        List<RegistroAsistenciaModel> abiertos =
                repository.findByFechaAndHoraSalidaIsNullAndTipoRegistro(hoy, "ORIGINAL");

        List<RegistroAsistenciaModel> cerrados = new ArrayList<>();

        for (RegistroAsistenciaModel r : abiertos) {
            if (r.getHoraEntrada() == null) continue;

            LocalTime horaSalidaProgramada = getHoraSalidaVigente(r.getEmpleado().getIdEmpleado(), hoy);

            LocalTime limite = horaSalidaProgramada.plusMinutes(15);
            if (ahora.isAfter(limite)) {
                long minutos = Duration.between(r.getHoraEntrada(), horaSalidaProgramada).toMinutes();
                if (minutos > 0) {
                    r.setHoraSalida(horaSalidaProgramada);
                    r.setHorasTrabajadas(BigDecimal.valueOf(minutos)
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                    r.setTipoUltimoRegistro("SALIDA_AUTOMATICA");
                    r.setObservacion("Salida registrada automáticamente (15 min después del horario de salida).");
                    cerrados.add(r);
                }
            }
        }

        if (!cerrados.isEmpty()) {
            repository.saveAll(cerrados);
        }
    }

    private void registrarInasistencias(LocalDate fecha) {
        List<RegistroAsistenciaModel> inasistencias = new ArrayList<>();

        for (EmpleadoModel empleado : empleadoRepository.findByEstado("ACTIVO")) {
            Integer idEmpleado = Math.toIntExact(empleado.getIdEmpleado());
            boolean tieneRegistro = repository
                    .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, fecha, "ORIGINAL")
                    .isPresent();

            if (!tieneRegistro) {
                RegistroAsistenciaModel inasistencia = new RegistroAsistenciaModel();
                inasistencia.setEmpleado(empleado);
                inasistencia.setFecha(fecha);
                inasistencia.setTipoRegistro("ORIGINAL");
                inasistencia.setTipoUltimoRegistro("CIERRE_AUTOMATICO");
                inasistencia.setEstado("INASISTENCIA");
                inasistencia.setObservacion("Inasistencia detectada automáticamente al cierre de jornada.");
                inasistencias.add(inasistencia);
            }
        }

        if (!inasistencias.isEmpty()) {
            repository.saveAll(inasistencias);
        }
    }
}
