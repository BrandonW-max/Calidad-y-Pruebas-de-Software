package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaManualRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaRequestDTO;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Application.dto.MarcarAsistenciaResponseDTO;
import com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository;
import com.rrhh.Modulos.CU4_RegistroAsistencia.Infrastructure.interfaces.JpaRegistroAsistenciaNominaRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.HorarioModel;
import com.rrhh.Shared.persistence.AsignacionHorarioModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@Service
public class AsistenciaService {

    private final JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository;
    private final JpaEmpleadoRepository empleadoRepository;
    private final IHistorialRepository historialRepository;

    public AsistenciaService(JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository,
                             JpaEmpleadoRepository empleadoRepository,
                             IHistorialRepository historialRepository) {
        this.registroAsistenciaRepository = registroAsistenciaRepository;
        this.empleadoRepository = empleadoRepository;
        this.historialRepository = historialRepository;
    }

    public MarcarAsistenciaResponseDTO marcarAsistencia(MarcarAsistenciaRequestDTO request) {

        if (request.getIdEmpleado() == null) {
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar el ID del empleado.");
        }

        Optional<EmpleadoModel> empleadoOptional = empleadoRepository.findById(request.getIdEmpleado().longValue());

        if (empleadoOptional.isEmpty()) {
            return new MarcarAsistenciaResponseDTO(false, "Empleado no encontrado.");
        }

        EmpleadoModel empleado = empleadoOptional.get();

        if (!"ACTIVO".equalsIgnoreCase(empleado.getEstado())) {
            registrarLogFallo(empleado.getIdEmpleado(), "EMPLEADO_INACTIVO");
            return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. El empleado esta inactivo.");
        }

        String huellaTemplate = request.getHuellaTemplate();
        if (huellaTemplate == null || huellaTemplate.isBlank()
                || empleado.getHuellaTemplate() == null
                || !huellaTemplate.equals(empleado.getHuellaTemplate())) {
            registrarLogFallo(empleado.getIdEmpleado(), "HUELLA_NO_COINCIDE");
            return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. La huella no coincide.");
        }

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
            registrarLogFallo(empleado.getIdEmpleado(), "REGISTRO_DUPLICADO");
            return new MarcarAsistenciaResponseDTO(
                    false,
                    "Registro duplicado. El empleado ya tiene entrada y salida registradas hoy."
            );
        }

        return new MarcarAsistenciaResponseDTO(false, "Registro inconsistente. Revise la asistencia manualmente.");
    }

    public MarcarAsistenciaResponseDTO registrarManual(MarcarAsistenciaManualRequestDTO request) {

        if (request.getIdEmpleado() == null) {
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar el ID del empleado.");
        }

        Optional<EmpleadoModel> empleadoOptional = empleadoRepository.findById(request.getIdEmpleado().longValue());

        if (empleadoOptional.isEmpty()) {
            return new MarcarAsistenciaResponseDTO(false, "Empleado no encontrado.");
        }

        EmpleadoModel empleado = empleadoOptional.get();

        if (!"ACTIVO".equalsIgnoreCase(empleado.getEstado())) {
            return new MarcarAsistenciaResponseDTO(false, "El empleado no esta activo.");
        }

        if (request.getFechaHora() == null) {
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar la fecha y hora del registro.");
        }

        LocalDate fecha = request.getFechaHora().toLocalDate();
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy.minusDays(1)) || fecha.isAfter(hoy.plusDays(1))) {
            return new MarcarAsistenciaResponseDTO(false,
                    "El registro manual solo permite un desfase maximo de 1 dia.");
        }

        boolean esEntrada = "ENTRADA".equalsIgnoreCase(request.getTipo());

        Optional<RegistroAsistenciaModel> registroOptional =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFecha(request.getIdEmpleado(), fecha);

        RegistroAsistenciaModel registro;
        if (registroOptional.isEmpty()) {
            registro = new RegistroAsistenciaModel();
            registro.setEmpleado(empleado);
            registro.setFecha(fecha);
        } else {
            registro = registroOptional.get();
        }

        String motivo = request.getMotivo() != null ? request.getMotivo() : "Ajuste manual por RRHH";

        if (esEntrada) {
            registro.setHoraEntrada(request.getFechaHora().toLocalTime());
            registro.setTipoUltimoRegistro("ENTRADA");

            LocalTime horaLimite = obtenerHoraEntradaProgramada(empleado).plusMinutes(obtenerTolerancia(empleado));
            LocalTime horaEntradaReal = registro.getHoraEntrada();

            if (horaEntradaReal.isAfter(horaLimite)) {
                int minutosTardanza = (int) Duration.between(
                        obtenerHoraEntradaProgramada(empleado), horaEntradaReal).toMinutes();
                registro.setMinutosTardanza(minutosTardanza);
                registro.setEstado("TARDANZA");
                registro.setObservacion("Registro manual - Entrada con tardanza. Motivo: " + motivo);
            } else {
                registro.setMinutosTardanza(0);
                registro.setEstado("PUNTUAL");
                registro.setObservacion("Registro manual - Entrada puntual. Motivo: " + motivo);
            }
        } else {
            registro.setHoraSalida(request.getFechaHora().toLocalTime());
            registro.setTipoUltimoRegistro("SALIDA");

            if (registro.getHoraEntrada() != null) {
                BigDecimal horas = calcularHorasTrabajadas(registro.getHoraEntrada(), registro.getHoraSalida());
                int minutosExtra = calcularMinutosExtra(registro.getHoraEntrada(), registro.getHoraSalida(),
                        obtenerJornadaLaboralMinutos(empleado));
                registro.setHorasTrabajadas(horas);
                registro.setMinutosExtra(minutosExtra);
            }

            registro.setObservacion("Registro manual - Salida. Motivo: " + motivo);
        }

        RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

        return new MarcarAsistenciaResponseDTO(
                true,
                "Registro manual de asistencia almacenado correctamente.",
                esEntrada ? "ENTRADA" : "SALIDA",
                guardado.getFecha(),
                guardado.getHoraEntrada(),
                guardado.getHoraSalida(),
                guardado.getHorasTrabajadas(),
                guardado.getMinutosTardanza(),
                guardado.getMinutosExtra(),
                guardado.getEstado()
        );
    }

    private void registrarLogFallo(Long idEmpleado, String motivo) {
        Historial log = new Historial(idEmpleado, "INTENTO_FALLIDO_ASISTENCIA", "127.0.0.1");
        log.setTablaAfectada("REGISTRO_ASISTENCIA");
        log.setValorNuevo(motivo);
        historialRepository.save(log);
    }

    private LocalTime obtenerHoraEntradaProgramada(EmpleadoModel empleado) {
        Optional<RegistroAsistenciaModel> registroHoy =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFecha(
                        empleado.getIdEmpleado().intValue(), LocalDate.now());

        if (registroHoy.isPresent() && registroHoy.get().getHorario() != null) {
            return registroHoy.get().getHorario().getHoraEntrada();
        }

        return LocalTime.of(8, 0);
    }

    private int obtenerTolerancia(EmpleadoModel empleado) {
        Optional<RegistroAsistenciaModel> registroHoy =
                registroAsistenciaRepository.findByEmpleado_IdEmpleadoAndFecha(
                        empleado.getIdEmpleado().intValue(), LocalDate.now());

        if (registroHoy.isPresent() && registroHoy.get().getHorario() != null
                && registroHoy.get().getHorario().getTolerancia() != null) {
            return registroHoy.get().getHorario().getTolerancia();
        }

        return 10;
    }

    private int obtenerJornadaLaboralMinutos(EmpleadoModel empleado) {
        return 8 * 60;
    }

    private MarcarAsistenciaResponseDTO registrarEntrada(EmpleadoModel empleado, LocalDate fechaActual, LocalTime horaActual) {

        RegistroAsistenciaModel registro = new RegistroAsistenciaModel();
        registro.setEmpleado(empleado);
        registro.setFecha(fechaActual);
        registro.setHoraEntrada(horaActual);
        registro.setTipoUltimoRegistro("ENTRADA");

        LocalTime horaLimite = obtenerHoraEntradaProgramada(empleado).plusMinutes(obtenerTolerancia(empleado));
        int minutosTardanza = 0;

        if (horaActual.isAfter(horaLimite)) {
            minutosTardanza = (int) Duration.between(obtenerHoraEntradaProgramada(empleado), horaActual).toMinutes();
        }

        if (minutosTardanza > 0) {
            registro.setMinutosTardanza(minutosTardanza);
            registro.setEstado("TARDANZA");
            registro.setObservacion("Entrada registrada con tardanza. Pendiente de justificacion.");
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
        int minutosExtra = calcularMinutosExtra(registro.getHoraEntrada(), horaSalida, 8 * 60);

        registro.setHorasTrabajadas(horasTrabajadas);
        registro.setMinutosExtra(minutosExtra);
        registro.setObservacion("Salida registrada correctamente.");
        registro.setActualizadoEl(LocalDateTime.now());

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

    private BigDecimal calcularHorasTrabajadas(LocalTime horaEntrada, LocalTime horaSalida) {
        long minutos = Duration.between(horaEntrada, horaSalida).toMinutes();

        if (minutos < 0) {
            minutos = 0;
        }

        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private int calcularMinutosExtra(LocalTime horaEntrada, LocalTime horaSalida, int jornadaMinutos) {
        long minutosTrabajados = Duration.between(horaEntrada, horaSalida).toMinutes();

        if (minutosTrabajados > jornadaMinutos) {
            return (int) (minutosTrabajados - jornadaMinutos);
        }

        return 0;
    }
}
