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
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    private final JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository;
    private final JpaEmpleadoRepository empleadoRepository;
    private final IHistorialRepository historialRepository;

    @PersistenceContext
    private EntityManager em;

    public AsistenciaService(JpaRegistroAsistenciaNominaRepository registroAsistenciaRepository,
                             JpaEmpleadoRepository empleadoRepository,
                             IHistorialRepository historialRepository) {
        this.registroAsistenciaRepository = registroAsistenciaRepository;
        this.empleadoRepository = empleadoRepository;
        this.historialRepository = historialRepository;
    }

    @org.springframework.transaction.annotation.Transactional
    public MarcarAsistenciaResponseDTO marcarAsistencia(MarcarAsistenciaRequestDTO request, String ip) {
        return marcarAsistencia(request, ip, true);
    }

    @org.springframework.transaction.annotation.Transactional
    public MarcarAsistenciaResponseDTO marcarAsistenciaMovil(MarcarAsistenciaRequestDTO request, String ip) {
        return marcarAsistencia(request, ip, false);
    }

    private MarcarAsistenciaResponseDTO marcarAsistencia(MarcarAsistenciaRequestDTO request, String ip,
                                                         boolean validarHuellaTemplate) {

        if (request.getIdEmpleado() == null) {
            registrarLogFallo(null, "ID_EMPLEADO_NULO", ip);
            return new MarcarAsistenciaResponseDTO(false, "Debe enviar el ID del empleado.");
        }

        Optional<EmpleadoModel> empleadoOptional = empleadoRepository.findById(request.getIdEmpleado().longValue());

        if (empleadoOptional.isEmpty()) {
            registrarLogFallo(request.getIdEmpleado().longValue(), "EMPLEADO_NO_ENCONTRADO", ip);
            return new MarcarAsistenciaResponseDTO(false, "Empleado no encontrado.");
        }

        EmpleadoModel empleado = empleadoOptional.get();

        if (!"ACTIVO".equalsIgnoreCase(empleado.getEstado())) {
            registrarLogFallo(empleado.getIdEmpleado(), "EMPLEADO_INACTIVO", ip);
            return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. El empleado esta inactivo.");
        }

        if (validarHuellaTemplate) {
            String huellaTemplate = request.getHuellaTemplate();
            if (huellaTemplate == null || huellaTemplate.isBlank()
                    || empleado.getHuellaTemplate() == null
                    || !huellaTemplate.equals(empleado.getHuellaTemplate())) {
                registrarLogFallo(empleado.getIdEmpleado(), "HUELLA_NO_COINCIDE", ip);
                return new MarcarAsistenciaResponseDTO(false, "Usuario denegado. La huella no coincide.");
            }
        }

        LocalDate fechaActual = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        Optional<RegistroAsistenciaModel> registroOptional =
                registroAsistenciaRepository.findConBloqueoByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                        request.getIdEmpleado(), fechaActual, "ORIGINAL");

        if (registroOptional.isEmpty()) {
            return registrarEntrada(empleado, fechaActual, horaActual);
        }

        RegistroAsistenciaModel registro = registroOptional.get();

        if (registro.getHoraEntrada() != null && registro.getHoraSalida() == null) {
            return registrarSalida(registro, horaActual, empleado);
        }

        if (registro.getHoraEntrada() != null && registro.getHoraSalida() != null) {
            registrarLogFallo(empleado.getIdEmpleado(), "REGISTRO_DUPLICADO", ip);
            return new MarcarAsistenciaResponseDTO(
                    false,
                    "Registro duplicado. El empleado ya tiene entrada y salida registradas hoy."
            );
        }

        registrarLogFallo(empleado.getIdEmpleado(), "REGISTRO_INCONSISTENTE", ip);
        return new MarcarAsistenciaResponseDTO(false, "Registro inconsistente. Revise la asistencia manualmente.");
    }

    @org.springframework.transaction.annotation.Transactional
    public MarcarAsistenciaResponseDTO registrarManual(MarcarAsistenciaManualRequestDTO request,
                                                         Long idUsuarioRrhh, String ip) {

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

        if (request.getMotivo() != null && request.getMotivo().length() > 200) {
            return new MarcarAsistenciaResponseDTO(false, "El motivo no puede superar los 200 caracteres.");
        }

        LocalDate fecha = request.getFechaHora().toLocalDate();
        LocalDate hoy = LocalDate.now();
        if (fecha.isBefore(hoy.minusDays(1)) || fecha.isAfter(hoy.plusDays(1))) {
            return new MarcarAsistenciaResponseDTO(false,
                    "El registro manual solo permite un desfase maximo de 1 dia.");
        }

        boolean esEntrada = "ENTRADA".equalsIgnoreCase(request.getTipo());
        boolean esSalida = "SALIDA".equalsIgnoreCase(request.getTipo());
        if (!esEntrada && !esSalida) {
            return new MarcarAsistenciaResponseDTO(false, "Error: tipo de registro invalido");
        }

        String tipoRegistroRaw = request.getTipoRegistro();
        if (tipoRegistroRaw != null) {
            boolean tipoRegistroValido = "ORIGINAL".equalsIgnoreCase(tipoRegistroRaw)
                    || "CORRECCION".equalsIgnoreCase(tipoRegistroRaw)
                    || tipoRegistroRaw.matches("[0-9]+");
            if (!tipoRegistroValido) {
                return new MarcarAsistenciaResponseDTO(false, "Error: tipo de registro invalido");
            }
        }
        boolean esCorreccion = "CORRECCION".equalsIgnoreCase(tipoRegistroRaw);

        if (esCorreccion) {
            Optional<RegistroAsistenciaModel> existente = registroAsistenciaRepository
                    .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                            request.getIdEmpleado(), fecha, "CORRECCION");
            RegistroAsistenciaModel registro = existente.orElseGet(() -> {
                RegistroAsistenciaModel r = new RegistroAsistenciaModel();
                r.setEmpleado(empleado);
                r.setFecha(fecha);
                r.setTipoRegistro("CORRECCION");
                return r;
            });
            if (esEntrada) {
                registro.setHoraEntrada(request.getFechaHora().toLocalTime());
                registro.setTipoUltimoRegistro("ENTRADA");
                registro.setMinutosTardanza(0);
                registro.setEstado("JUSTIFICADA");
            } else {
                registro.setHoraSalida(request.getFechaHora().toLocalTime());
                registro.setTipoUltimoRegistro("SALIDA");
                if (registro.getHoraEntrada() != null) {
                    long mins = com.rrhh.Shared.util.TurnoUtils.minutosTrabajados(registro.getHoraEntrada(), registro.getHoraSalida());
                    registro.setHorasTrabajadas(BigDecimal.valueOf(mins)
                            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
                }
                registro.setEstado("JUSTIFICADA");
            }
            registro.setObservacion(request.getMotivo() != null ? request.getMotivo() : "Correccion de tardanza justificada");
            RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

            Historial log = new Historial(idUsuarioRrhh, "REGISTRO_MANUAL_CORRECCION", ip);
            log.setTablaAfectada("REGISTRO_ASISTENCIA");
            log.setValorNuevo("CORRECCION | Empleado: " + empleado.getIdEmpleado()
                    + " | Tipo: " + (esEntrada ? "ENTRADA" : "SALIDA")
                    + " | Fecha: " + fecha);
            historialRepository.save(log);

            return new MarcarAsistenciaResponseDTO(
                    true, "Corrección registrada correctamente.",
                    esEntrada ? "ENTRADA" : "SALIDA",
                    guardado.getFecha(), guardado.getHoraEntrada(), guardado.getHoraSalida(),
                    guardado.getHorasTrabajadas(), guardado.getMinutosTardanza(),
                    guardado.getMinutosExtra(), guardado.getEstado()
            );
        }

        Optional<RegistroAsistenciaModel> registroOptional =
                registroAsistenciaRepository.findConBloqueoByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(
                        request.getIdEmpleado(), fecha, "ORIGINAL");

        RegistroAsistenciaModel registro;
        if (registroOptional.isEmpty()) {
            registro = new RegistroAsistenciaModel();
            registro.setEmpleado(empleado);
            registro.setFecha(fecha);
        } else {
            registro = registroOptional.get();
            if (registro.getHoraEntrada() != null && registro.getHoraSalida() != null) {
                return new MarcarAsistenciaResponseDTO(false,
                        "Registro duplicado. El empleado ya tiene entrada y salida registradas hoy.");
            }
            if (esEntrada && registro.getHoraEntrada() != null) {
                return new MarcarAsistenciaResponseDTO(false,
                        "Ya existe una entrada registrada hoy para este empleado. Use la opcion de correccion para modificarla.");
            }
            if (esSalida && registro.getHoraSalida() != null) {
                return new MarcarAsistenciaResponseDTO(false,
                        "Ya existe una salida registrada hoy para este empleado. Use la opcion de correccion para modificarla.");
            }
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
            // Reclama la salida antes de calcular la recuperación: si otra marcación ya la registró, no se toca nada
            if (registro.getIdRegistro() != null && registroAsistenciaRepository.reclamarSalida(
                    registro.getIdRegistro(), request.getFechaHora().toLocalTime()) == 0) {
                return new MarcarAsistenciaResponseDTO(false,
                        "Ya existe una salida registrada hoy para este empleado. Use la opcion de correccion para modificarla.");
            }
            registro.setHoraSalida(request.getFechaHora().toLocalTime());
            registro.setTipoUltimoRegistro("SALIDA");

            if (registro.getHoraEntrada() != null) {
                BigDecimal horas = calcularHorasTrabajadas(registro.getHoraEntrada(), registro.getHoraSalida());
                int minutosDespues = minutosDespuesDeSalida(empleado, registro.getHoraSalida());
                int recuperado = aplicarRecuperacion(empleado.getIdEmpleado(), registro.getFecha(), minutosDespues);
                int minutosExtra = com.rrhh.Shared.util.TurnoUtils.redondearHorasExtra(minutosDespues - recuperado);
                registro.setHorasTrabajadas(horas);
                registro.setMinutosExtra(minutosExtra);
            }

            registro.setObservacion("Registro manual - Salida. Motivo: " + motivo);
        }

        RegistroAsistenciaModel guardado = registroAsistenciaRepository.save(registro);

        Historial log = new Historial(idUsuarioRrhh, "REGISTRO_MANUAL_ASISTENCIA", ip);
        log.setTablaAfectada("REGISTRO_ASISTENCIA");
        log.setValorNuevo("Empleado: " + empleado.getIdEmpleado()
                + " | Tipo: " + (esEntrada ? "ENTRADA" : "SALIDA")
                + " | Motivo: " + motivo
                + " | Fecha: " + fecha);
        historialRepository.save(log);

        String mensajeExito = esEntrada
                ? "Entrada registrada correctamente."
                : "Salida registrada correctamente. El registro de asistencia ha sido almacenado correctamente en el sistema.";
        return new MarcarAsistenciaResponseDTO(
                true,
                mensajeExito,
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

    public List<RegistroAsistenciaModel> listarPorEmpleado(Integer idEmpleado) {
        List<RegistroAsistenciaModel> todos = registroAsistenciaRepository.findByEmpleado_IdEmpleadoOrderByFechaDesc(idEmpleado);
        return todos.stream().filter(r -> "ORIGINAL".equals(r.getTipoRegistro()) || r.getTipoRegistro() == null).toList();
    }

    public Optional<HorarioModel> getHorarioEmpleado(Integer idEmpleado) {
        Optional<EmpleadoModel> empleadoOpt = empleadoRepository.findById(idEmpleado.longValue());
        if (empleadoOpt.isEmpty()) return Optional.empty();
        return Optional.ofNullable(obtenerHorarioAsignado(empleadoOpt.get()));
    }

    public Optional<RegistroAsistenciaModel> buscarHoy(Integer idEmpleado) {
        Optional<RegistroAsistenciaModel> corr = registroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, LocalDate.now(), "CORRECCION");
        if (corr.isPresent()) return corr;
        return registroAsistenciaRepository
                .findByEmpleado_IdEmpleadoAndFechaAndTipoRegistro(idEmpleado, LocalDate.now(), "ORIGINAL");
    }

    private void registrarLogFallo(Long idEmpleado, String motivo, String ip) {
        Historial log = new Historial(idEmpleado != null ? idEmpleado : 0L, "INTENTO_FALLIDO_ASISTENCIA", ip);
        log.setTablaAfectada("REGISTRO_ASISTENCIA");
        log.setValorNuevo(motivo);
        historialRepository.save(log);
    }

    private HorarioModel obtenerHorarioAsignado(EmpleadoModel empleado) {
        List<AsignacionHorarioModel> asignaciones = em.createQuery(
                "SELECT a FROM AsignacionHorarioModel a WHERE a.empleado.idEmpleado = :idEmpleado AND a.activo = true",
                AsignacionHorarioModel.class)
                .setParameter("idEmpleado", empleado.getIdEmpleado())
                .getResultList();

        if (!asignaciones.isEmpty() && asignaciones.get(0).getHorario() != null) {
            return asignaciones.get(0).getHorario();
        }

        return null;
    }

    private LocalTime obtenerHoraEntradaProgramada(EmpleadoModel empleado) {
        HorarioModel horario = obtenerHorarioAsignado(empleado);
        if (horario != null && horario.getHoraEntrada() != null) {
            return horario.getHoraEntrada();
        }
        return LocalTime.of(8, 0);
    }

    private int obtenerTolerancia(EmpleadoModel empleado) {
        HorarioModel horario = obtenerHorarioAsignado(empleado);
        if (horario != null && horario.getTolerancia() != null) {
            return horario.getTolerancia();
        }
        return 10;
    }

    private MarcarAsistenciaResponseDTO registrarEntrada(EmpleadoModel empleado, LocalDate fechaActual, LocalTime horaActual) {

        RegistroAsistenciaModel registro = new RegistroAsistenciaModel();
        registro.setEmpleado(empleado);
        registro.setFecha(fechaActual);
        registro.setHoraEntrada(horaActual);
        registro.setTipoUltimoRegistro("ENTRADA");

        LocalTime horaEntradaProgramada = obtenerHoraEntradaProgramada(empleado);
        int tolerancia = obtenerTolerancia(empleado);
        LocalTime horaLimite = horaEntradaProgramada.plusMinutes(tolerancia);
        int minutosTardanza = 0;

        if (horaActual.isAfter(horaLimite)) {
            minutosTardanza = (int) Duration.between(horaEntradaProgramada, horaActual).toMinutes();
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

    private MarcarAsistenciaResponseDTO registrarSalida(RegistroAsistenciaModel registro, LocalTime horaSalida, EmpleadoModel empleado) {

        HorarioModel horario = obtenerHorarioAsignado(empleado);
        if (horario != null && horario.getHoraEntrada() != null && horario.getHoraSalida() != null
                && horario.getHoraSalida().isAfter(horario.getHoraEntrada())
                && horaSalida.isBefore(horario.getHoraSalida())) {
            return new MarcarAsistenciaResponseDTO(
                    false,
                    "Aun no es hora de salida. Tu horario de salida es "
                            + horario.getHoraSalida().toString().substring(0, 5) + "."
            );
        }

        // Reclama la salida antes de calcular la recuperación: si otra marcación ya la registró, no se toca nada
        if (registroAsistenciaRepository.reclamarSalida(registro.getIdRegistro(), horaSalida) == 0) {
            return new MarcarAsistenciaResponseDTO(false,
                    "Registro duplicado. El empleado ya tiene entrada y salida registradas hoy.");
        }
        registro.setHoraSalida(horaSalida);
        registro.setTipoUltimoRegistro("SALIDA");

        BigDecimal horasTrabajadas = calcularHorasTrabajadas(registro.getHoraEntrada(), horaSalida);
        int minutosDespues = minutosDespuesDeSalida(empleado, horaSalida);
        int recuperado = aplicarRecuperacion(empleado.getIdEmpleado(), registro.getFecha(), minutosDespues);
        int minutosExtra = com.rrhh.Shared.util.TurnoUtils.redondearHorasExtra(minutosDespues - recuperado);

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
        long minutos = com.rrhh.Shared.util.TurnoUtils.minutosTrabajados(horaEntrada, horaSalida);

        return BigDecimal.valueOf(minutos)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private int minutosDespuesDeSalida(EmpleadoModel empleado, LocalTime horaSalida) {
        HorarioModel horario = obtenerHorarioAsignado(empleado);
        LocalTime entradaProgramada = obtenerHoraEntradaProgramada(empleado);
        LocalTime salidaProgramada = horario != null && horario.getHoraSalida() != null
                ? horario.getHoraSalida()
                : LocalTime.of(17, 0);

        // En turnos que cruzan medianoche, una salida antes de la entrada programada es de la noche, no del dia siguiente
        boolean cruzaMedianoche = salidaProgramada.isBefore(entradaProgramada);
        if (cruzaMedianoche && !horaSalida.isBefore(entradaProgramada)) {
            return 0;
        }
        return horaSalida.isAfter(salidaProgramada)
                ? (int) Duration.between(salidaProgramada, horaSalida).toMinutes()
                : 0;
    }

    // Descuenta del plan de recuperacion los minutos trabajados despues de la salida programada
    private int aplicarRecuperacion(Long idEmpleado, LocalDate fecha, int minutosDespuesDeSalida) {
        if (minutosDespuesDeSalida <= 0) {
            return 0;
        }
        List<?> planes = em.createNativeQuery(
                "SELECT id_plan, minutos_pendientes, tipo FROM plan_recuperacion_tardanza "
                + "WHERE id_empleado = :idEmpleado AND estado = 'PENDIENTE' "
                + "AND :fecha BETWEEN fecha_inicio AND fecha_limite "
                + "AND (ultimo_dia_aplicado IS NULL OR ultimo_dia_aplicado <> :fecha) "
                + "ORDER BY fecha_inicio, id_plan FOR UPDATE")
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("fecha", fecha)
            .getResultList();
        // Los minutos cubren primero el plan más antiguo; lo que sobra pasa al siguiente
        int restante = minutosDespuesDeSalida;
        int recuperadoTotal = 0;
        for (Object o : planes) {
            if (restante <= 0) {
                break;
            }
            Object[] plan = (Object[]) o;
            int idPlan = ((Number) plan[0]).intValue();
            int pendientes = ((Number) plan[1]).intValue();
            int recuperable = PlanRecuperacionCalendario.minutosRecuperables((String) plan[2], restante);
            int recuperado = Math.min(pendientes, recuperable);
            if (recuperado == 0) {
                continue;
            }
            int saldo = pendientes - recuperado;
            em.createNativeQuery("UPDATE plan_recuperacion_tardanza SET minutos_pendientes = :saldo, "
                    + "ultimo_dia_aplicado = :fecha, estado = :estado WHERE id_plan = :idPlan")
                .setParameter("saldo", saldo)
                .setParameter("fecha", fecha)
                .setParameter("estado", saldo == 0 ? "CUMPLIDO" : "PENDIENTE")
                .setParameter("idPlan", idPlan)
                .executeUpdate();
            restante -= recuperado;
            recuperadoTotal += recuperado;
        }
        return recuperadoTotal;
    }
}
