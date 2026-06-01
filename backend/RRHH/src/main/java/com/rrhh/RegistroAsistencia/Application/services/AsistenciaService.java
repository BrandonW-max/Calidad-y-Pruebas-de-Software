package com.rrhh.RegistroAsistencia.Application.services;

import com.rrhh.RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class AsistenciaService {

    private final JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository;
    private final JpaEmpleadoRepository empleadoRepository;

    private static final LocalTime HORA_ENTRADA_PROGRAMADA = LocalTime.of(8, 0);
    private static final int MINUTOS_TOLERANCIA = 10;
    private static final int JORNADA_LABORAL_MINUTOS = 8 * 60;

    public AsistenciaService(JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository,
                             JpaEmpleadoRepository empleadoRepository) {
        this.registroAsistenciaRepository = registroAsistenciaRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public MarcarAsistenciaResponseDTO marcarAsistencia(MarcarAsistenciaRequestDTO request) {

        if (request.getIdEmpleado() == null) {
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar el ID del empleado.");
        }

        if (!request.isHuellaValida()) {
            return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. La huella no coincide.");
        }

        Optional<EmpleadoModel> empleadoOptional = empleadoRepository.findById(request.getIdEmpleado().longValue());

        if (empleadoOptional.isEmpty()) {
            return new MarcarAsistenciaResponseDTO(false, "Empleado no encontrado.");
        }

        EmpleadoModel empleado = empleadoOptional.get();

        /*
         * Si tu EmpleadoModel tiene un campo estado, puedes activar esto.
         * Ajusta el nombre del getter según tu clase real.
         *
         * if (!"ACTIVO".equalsIgnoreCase(empleado.getEstado())) {
         *     return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. El empleado está inactivo.");
         * }
         */

        LocalDate fechaActual = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        Optional<RegistroAsistenciaModel> registroOptional =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFecha(request.getIdEmpleado(), fechaActual);

        if (registroOptional.isEmpty()) {
            return registrarEntrada(empleado, fechaActual, horaActual);
        }

        RegistroAsistenciaModel registro = registroOptional.get();

        if (registro.getHoraEntrada() != null && registro.getHoraSalida() == null) {
            return registrarSalida(registro, horaActual);
        }

        if (registro.getHoraEntrada() != null && registro.getHoraSalida() != null) {
            return new MarcarAsistenciaResponseDTO(
                    false,
                    "Registro duplicado. El empleado ya tiene entrada y salida registradas hoy."
            );
        }

        return new MarcarAsistenciaResponseDTO(false, "Registro inconsistente. Revise la asistencia manualmente.");
    }

    private MarcarAsistenciaResponseDTO registrarEntrada(EmpleadoModel empleado, LocalDate fechaActual, LocalTime horaActual) {

        RegistroAsistenciaModel registro = new RegistroAsistenciaModel();
        registro.setEmpleado(empleado);
        registro.setFecha(fechaActual);
        registro.setHoraEntrada(horaActual);
        registro.setTipoUltimoRegistro("ENTRADA");

        int minutosTardanza = calcularMinutosTardanza(horaActual);

        if (minutosTardanza > 0) {
            registro.setMinutosTardanza(minutosTardanza);
            registro.setEstado("TARDANZA");
            registro.setObservacion("Entrada registrada con tardanza. Pendiente de justificación.");
        } else {
            registro.setMinutosTardanza(0);
            registro.setEstado("PUNTUAL");
            registro.setObservacion("Entrada registrada correctamente.");
        }

        RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

        return new MarcarAsistenciaResponseDTO(
                true,
                "Entrada registrada correctamente.",
                "ENTRADA",
                guardado.getFecha(),
                guardado.getHoraEntrada(),
                guardado.getHoraSalida(),
                guardado.getHorasTrabajadas(),
                guardado.getMinutosTardanza(),
                guardado.getMinutosExtra(),
                guardado.getEstado()
        );
    }

    private MarcarAsistenciaResponseDTO registrarSalida(RegistroAsistenciaModel registro, LocalTime horaSalida) {

        registro.setHoraSalida(horaSalida);
        registro.setTipoUltimoRegistro("SALIDA");

        BigDecimal horasTrabajadas = calcularHorasTrabajadas(registro.getHoraEntrada(), horaSalida);
        int minutosExtra = calcularMinutosExtra(registro.getHoraEntrada(), horaSalida);

        registro.setHorasTrabajadas(horasTrabajadas);
        registro.setMinutosExtra(minutosExtra);
        registro.setObservacion("Salida registrada correctamente.");
        registro.setActualizadoEl(java.time.LocalDateTime.now());

        RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

        return new MarcarAsistenciaResponseDTO(
                true,
                "Salida registrada correctamente. El registro de asistencia ha sido almacenado correctamente en el sistema.",
                "SALIDA",
                guardado.getFecha(),
                guardado.getHoraEntrada(),
                guardado.getHoraSalida(),
                guardado.getHorasTrabajadas(),
                guardado.getMinutosTardanza(),
                guardado.getMinutosExtra(),
                guardado.getEstado()
        );
    }

    private int calcularMinutosTardanza(LocalTime horaEntradaReal) {
        LocalTime horaLimite = HORA_ENTRADA_PROGRAMADA.plusMinutes(MINUTOS_TOLERANCIA);

        if (horaEntradaReal.isAfter(horaLimite)) {
            return (int) Duration.between(HORA_ENTRADA_PROGRAMADA, horaEntradaReal).toMinutes();
        }

        return 0;
    }

    private BigDecimal calcularHorasTrabajadas(LocalTime horaEntrada, LocalTime horaSalida) {
        long minutos = Duration.between(horaEntrada, horaSalida).toMinutes();

        if (minutos < 0) {
            minutos = 0;
        }

        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private int calcularMinutosExtra(LocalTime horaEntrada, LocalTime horaSalida) {
        long minutosTrabajados = Duration.between(horaEntrada, horaSalida).toMinutes();

        if (minutosTrabajados > JORNADA_LABORAL_MINUTOS) {
            return (int) (minutosTrabajados - JORNADA_LABORAL_MINUTOS);
        }

        return 0;
    }
}