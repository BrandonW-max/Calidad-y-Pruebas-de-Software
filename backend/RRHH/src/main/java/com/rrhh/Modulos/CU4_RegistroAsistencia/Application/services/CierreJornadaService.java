package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
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

    public CierreJornadaService(JpaRegistroAsistenciaNominaRepository repository,
                                JpaEmpleadoRepository empleadoRepository) {
        this.repository = repository;
        this.empleadoRepository = empleadoRepository;
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
                LocalTime horaCierre = (r.getHorario() != null && r.getHorario().getHoraSalida() != null)
                        ? r.getHorario().getHoraSalida()
                        : LocalTime.of(17, 0);
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
