package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.factory.INominaFactory;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.AjustarNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.DetalleNominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.DetalleNomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Infrastructure.interfaces.JpaUsuarioRepository;
import com.rrhh.Shared.persistence.UsuarioModel;
import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.config.RRHHProperties;
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import com.rrhh.Shared.persistence.NominaVersionModel;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaVersionDTO;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaNominaVersionRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaAjusteNominaManualRepository;
import com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaNominaVersionAnteriorRepository;
import com.rrhh.Shared.persistence.NominaVersionAnteriorModel;
import com.rrhh.Shared.persistence.AjusteNominaManualModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class NominaService implements INominaService {

    private final RRHHProperties rrhhProperties;

    private static final int MINUTOS_EN_HORA = 60;
    private static final int HORAS_JORNADA_DIARIA = 8;

    // Departamentos con bonificación de guardia y riesgo
    private static final java.util.Set<String> DPTOS_CLINICOS = java.util.Set.of(
        "Emergencia", "Medicina", "Enfermería", "Enfermeria",
        "Radiología", "Radiologia", "Laboratorio"
    );

    // RNF101: solo los usuarios con estos roles reciben bonificación por cargo
    private static final java.util.Set<String> ROLES_CON_BONIF_CARGO = java.util.Set.of("RRHH", "GERENCIA");


    private final INominaRepository nominaRepository;
    private final IDetalleNominaRepository detalleNominaRepository;
    private final IHistorialRepository historialRepository;
    private final INominaFactory nominaFactory;
    private final com.rrhh.Modulos.CU3_Nomina.Infrastructure.export.NominaComprobantePDFGenerator comprobantePDFGenerator;

    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository;
    private final com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository;
    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository;
    private final com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository;
    private final JpaUsuarioRepository jpaUsuarioRepository;
    private final JpaNominaVersionRepository jpaNominaVersionRepository;
    private final JpaAjusteNominaManualRepository jpaAjusteNominaManualRepository;
    private final JpaNominaVersionAnteriorRepository jpaNominaVersionAnteriorRepository;
    private final LiquidacionHorasExtraService liquidacionHorasExtra;
    private final com.rrhh.Shared.service.CambioSueldoService cambioSueldo;

    public NominaService(
            RRHHProperties rrhhProperties,
            INominaRepository nominaRepository,
            IDetalleNominaRepository detalleNominaRepository,
            IHistorialRepository historialRepository,
            INominaFactory nominaFactory,
            com.rrhh.Modulos.CU3_Nomina.Infrastructure.export.NominaComprobantePDFGenerator comprobantePDFGenerator,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository,
            com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository,
            com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository,
            JpaUsuarioRepository jpaUsuarioRepository,
            JpaNominaVersionRepository jpaNominaVersionRepository,
            JpaAjusteNominaManualRepository jpaAjusteNominaManualRepository,
            JpaNominaVersionAnteriorRepository jpaNominaVersionAnteriorRepository,
            LiquidacionHorasExtraService liquidacionHorasExtra,
            com.rrhh.Shared.service.CambioSueldoService cambioSueldo) {
        this.rrhhProperties = rrhhProperties;
        this.liquidacionHorasExtra = liquidacionHorasExtra;
        this.cambioSueldo = cambioSueldo;
        this.nominaRepository = nominaRepository;
        this.detalleNominaRepository = detalleNominaRepository;
        this.historialRepository = historialRepository;
        this.nominaFactory = nominaFactory;
        this.comprobantePDFGenerator = comprobantePDFGenerator;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaRegistroAsistenciaRepository = jpaRegistroAsistenciaRepository;
        this.jpaContratoRepository = jpaContratoRepository;
        this.jpaFamiliaInfoRepository = jpaFamiliaInfoRepository;
        this.jpaUsuarioRepository = jpaUsuarioRepository;
        this.jpaNominaVersionRepository = jpaNominaVersionRepository;
        this.jpaAjusteNominaManualRepository = jpaAjusteNominaManualRepository;
        this.jpaNominaVersionAnteriorRepository = jpaNominaVersionAnteriorRepository;
    }

    @Override
    @Transactional
    public List<NominaResponseDTO> calcularNomina(CalcularNominaRequestDTO request, Long idUsuarioRrhh) {

        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha fin.");
        }

        // No se pueden calcular meses futuros
        YearMonth mesActual = YearMonth.now();
        if (YearMonth.from(request.getFechaInicio()).isAfter(mesActual)
                || YearMonth.from(request.getFechaFin()).isAfter(mesActual)) {
            throw new IllegalArgumentException(
                "No se puede calcular la nómina de meses futuros."
            );
        }

        List<EmpleadoModel> empleados = obtenerEmpleadosActivos(request).stream()
            .filter(e -> contratadoHasta(e, request.getFechaFin()))
            .collect(Collectors.toList());

        if (empleados.isEmpty()) {
            throw new IllegalStateException(
                "No se encontraron empleados activos para el periodo seleccionado. " +
                "No es posible realizar el calculo de nomina."
            );
        }

        String periodo = generarPeriodo(request.getFechaInicio(), request.getFechaFin());

        List<Long> idsEmpleados = empleados.stream()
            .map(EmpleadoModel::getIdEmpleado)
            .collect(Collectors.toList());

        boolean sinFiltro = request.getIdEmpleado() == null && request.getIdDepartamento() == null;

        // Un cambio de sueldo que rige después del inicio del período haría que el período use un sueldo que no le corresponde
        if (!Boolean.TRUE.equals(request.getConfirmarCambiosSueldo())) {
            List<Long> conCambio = idsEmpleados.stream()
                .filter(id -> cambioSueldo.tieneCambioPosteriorAl(id, request.getFechaInicio()))
                .collect(Collectors.toList());
            if (!conCambio.isEmpty()) {
                throw new com.rrhh.Shared.exception.CambioSueldoPendienteException(
                    "El período " + YearMonth.from(request.getFechaInicio()) + " tiene cambios de sueldo posteriores a su inicio "
                    + "(empleados: " + conCambio.stream().map(String::valueOf).collect(Collectors.joining(", "))
                    + "). Confirme para calcularlo igual.");
            }
        }

        // Una nómina pagada no se reemplaza: la corrección se hace como ajuste nuevo
        List<String> pagadas = nominaRepository.buscarPorPeriodo(periodo).stream()
            .filter(n -> "PAGADA".equals(n.getEstadoPago()))
            .filter(n -> sinFiltro || idsEmpleados.contains(n.getIdEmpleado()))
            .map(n -> String.valueOf(n.getIdEmpleado()))
            .collect(Collectors.toList());
        if (!pagadas.isEmpty()) {
            throw new IllegalStateException(
                "El período " + periodo + " tiene nóminas pagadas (empleados: "
                + String.join(", ", pagadas) + "). No se pueden recalcular."
            );
        }

        // Sin filtro se reemplaza todo el período; con filtro solo se recalculan los empleados seleccionados
        if (sinFiltro) {
            nominaRepository.copiarHistorialAjustes(periodo);
            nominaRepository.guardarVersionAnterior(periodo);
            nominaRepository.eliminarPorPeriodo(periodo);
        } else {
            nominaRepository.copiarHistorialAjustesDeEmpleados(periodo, idsEmpleados);
            nominaRepository.guardarVersionAnteriorDeEmpleados(periodo, idsEmpleados);
            nominaRepository.eliminarPorPeriodoYEmpleados(periodo, idsEmpleados);
        }

        // Pre-cargar TODO en 3 queries antes del bucle (evita N+1)
        Map<Long, List<RegistroAsistenciaModel>> asistenciasPorEmpleado =
            jpaRegistroAsistenciaRepository
                .findByEmpleadoIdEmpleadoInAndFechaBetween(
                    idsEmpleados,
                    request.getFechaInicio(),
                    request.getFechaFin()
                )
                .stream()
                .collect(Collectors.groupingBy(r -> r.getEmpleado().getIdEmpleado()));

        Map<Long, ContratoModel> contratosPorEmpleado =
            jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(idsEmpleados, "ACTIVO")
                .stream()
                .collect(Collectors.toMap(
                    c -> c.getEmpleado().getIdEmpleado(),
                    c -> c,
                    (c1, c2) -> c1
                ));

        Map<Long, List<FamiliaInfoModel>> familiaPorEmpleado =
            jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(idsEmpleados)
                .stream()
                .collect(Collectors.groupingBy(f -> f.getEmpleado().getIdEmpleado()));

        Map<Long, UsuarioModel> usuarioPorEmpleado = jpaUsuarioRepository.findByEmpleadoIdEmpleadoIn(idsEmpleados)
            .stream()
            .collect(Collectors.toMap(u -> u.getEmpleado().getIdEmpleado(), u -> u, (u1, u2) -> u1));

        // Ajustes manuales guardados del periodo: el recálculo vuelve a aplicarlos
        Map<Long, AjusteNominaManualModel> ajustesManuales = jpaAjusteNominaManualRepository.findByPeriodo(periodo)
            .stream()
            .collect(Collectors.toMap(AjusteNominaManualModel::getIdEmpleado, a -> a, (a, b) -> a));

        List<NominaResponseDTO> resultados = new ArrayList<>();

        try {
            for (EmpleadoModel empleado : empleados) {

                List<RegistroAsistenciaModel> registros = filtrarRegistrosValidos(
                    asistenciasPorEmpleado.getOrDefault(empleado.getIdEmpleado(), List.of())
                );
                // Empleados sin registros completos aún (mes en curso) se incluyen con 0 días trabajados

                ContratoModel contrato = contratosPorEmpleado.get(empleado.getIdEmpleado());
                if (contrato == null) continue; // sin contrato activo → se excluye de nómina
                BigDecimal sueldoBase = contrato.getSueldo();
                String tipoPension = (contrato.getTipoPension() != null &&
                    (contrato.getTipoPension().equals("ONP") || contrato.getTipoPension().equals("AFP")))
                    ? contrato.getTipoPension() : "ONP";

                validarDatosContrato(sueldoBase, empleado);

                BigDecimal totalHorasTrabajadas = calcularTotalHorasTrabajadas(registros);

                int totalMinutosTardanza = registros.stream()
                    .mapToInt(r -> r.getMinutosTardanza() != null ? r.getMinutosTardanza() : 0)
                    .sum();

                BigDecimal valorHoraDiaria = sueldoBase
                    .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP);

                // Cada registro aporta sus horas extra una sola vez: se liquidan los nuevos y el total sale del libro
                liquidacionHorasExtra.liquidarAsistencia(empleado.getIdEmpleado(), periodo, registros, idUsuarioRrhh);
                BigDecimal totalHorasExtra = liquidacionHorasExtra.horasTotales(empleado.getIdEmpleado(), periodo);
                BigDecimal horasExtraAsistencia = liquidacionHorasExtra.horasAsistencia(empleado.getIdEmpleado(), periodo);
                BigDecimal montoHorasExtra = calcularMontoHorasExtraPorTurno(empleado.getIdEmpleado(), periodo, valorHoraDiaria);

                BigDecimal valorMinuto = sueldoBase
                    .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 4, RoundingMode.HALF_UP);
                BigDecimal descuentoTardanzas = valorMinuto
                    .multiply(BigDecimal.valueOf(totalMinutosTardanza))
                    .setScale(2, RoundingMode.HALF_UP);
                descuentoTardanzas = descuentoTardanzas.add(descuentoPlanesRecuperacion(empleado.getIdEmpleado(), periodo));

                int cantidadHijos = contarHijosDesdeCache(
                    familiaPorEmpleado.getOrDefault(empleado.getIdEmpleado(), List.of())
                );
                BigDecimal bonifFamiliar = calcularAsignacionFamiliar(cantidadHijos);

                int diasTrabajados = (int) registros.stream()
                    .filter(r -> r.getHoraEntrada() != null && r.getHoraSalida() != null)
                    .count();

                // Sueldo devengado: proporcional a los días trabajados en el período (S/contrato / 30 × días)
                // Permite cálculo progresivo durante el mes sin mostrar el sueldo completo del contrato
                BigDecimal sueldoDevengado = calcularSueldoDevengado(sueldoBase, diasTrabajados);

                String nombreDpto = contrato.getDepartamento() != null
                    ? contrato.getDepartamento().getNombre() : "";
                boolean esClinico = DPTOS_CLINICOS.contains(nombreDpto);
                boolean esNocturno = esTurnoNocturno(registros, diasTrabajados);

                // Bonifs proporcionales al sueldo devengado, no al sueldo completo del contrato
                BigDecimal bonifTurnoNocturno = esNocturno
                    ? sueldoDevengado.multiply(rrhhProperties.getBonifNocturno()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                // RNF101: guardia es un monto fijo mensual para personal clínico
                BigDecimal bonifGuardia = esClinico
                    ? rrhhProperties.getBonifGuardia().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                BigDecimal bonifRiesgo = esClinico
                    ? sueldoDevengado.multiply(rrhhProperties.getBonifRiesgo()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                // RNF101: bonificación por cargo, 20% del sueldo devengado, solo para roles RRHH y GERENCIA
                BigDecimal bonifCargo = tieneBonifCargo(usuarioPorEmpleado.get(empleado.getIdEmpleado()))
                    ? sueldoDevengado.multiply(rrhhProperties.getBonifCargo()).setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

                BigDecimal sueldoBruto = sueldoDevengado
                    .add(montoHorasExtra)
                    .add(bonifFamiliar)
                    .add(bonifTurnoNocturno)
                    .add(bonifGuardia)
                    .add(bonifRiesgo)
                    .add(bonifCargo)
                    .setScale(2, RoundingMode.HALF_UP);

                BigDecimal tasaDescuento = "AFP".equals(tipoPension)
                    ? rrhhProperties.getTasaDescuentoAfp()
                    : rrhhProperties.getTasaDescuentoLey();
                BigDecimal descuentoLey = sueldoBruto.multiply(tasaDescuento).setScale(2, RoundingMode.HALF_UP);
                BigDecimal sueldoNeto = sueldoBruto
                    .subtract(descuentoTardanzas)
                    .subtract(descuentoLey)
                    .setScale(2, RoundingMode.HALF_UP);

                validarPrecision(sueldoDevengado, montoHorasExtra, bonifFamiliar, bonifTurnoNocturno,
                        bonifGuardia, bonifRiesgo, bonifCargo, sueldoBruto, descuentoTardanzas, descuentoLey, sueldoNeto, empleado);

                List<DetalleNomina> detalles = generarDetallesDiarios(
                    registros, valorHoraDiaria, valorMinuto
                );

                Nomina nomina = nominaFactory.crearNominaBase();
                nomina.setPeriodo(periodo);
                nomina.setFechaInicio(request.getFechaInicio());
                nomina.setFechaFin(request.getFechaFin());
                nomina.setFechaEmision(LocalDate.now());
                nomina.setSueldoBase(sueldoDevengado); // devengado proporcional a días trabajados
                nomina.setTotalHorasTrabajadas(totalHorasTrabajadas);
                nomina.setTotalHorasExtra(totalHorasExtra);
                nomina.setTotalHorasExtraAsistencia(horasExtraAsistencia);
                nomina.setTotalMinutosTardanza(totalMinutosTardanza);
                nomina.setBonifFamiliar(bonifFamiliar);
                nomina.setBonifTurnoNocturno(bonifTurnoNocturno);
                nomina.setBonifGuardia(bonifGuardia);
                nomina.setBonifRiesgo(bonifRiesgo);
                nomina.setBonifCargo(bonifCargo);
                nomina.setEsEmpleadoClinico(esClinico);
                nomina.setTieneHorarioNocturno(esNocturno);
                nomina.setDescuentoTardanzas(descuentoTardanzas);
                nomina.setDescuentoLey(descuentoLey);
                nomina.setAsignacionFamiliar(bonifFamiliar);
                nomina.setSueldoBruto(sueldoBruto);
                nomina.setSueldoNeto(sueldoNeto);
                nomina.setEstadoPago("CALCULADA");
                nomina.setIdEmpleado(empleado.getIdEmpleado());
                nomina.setCalculadoPor(idUsuarioRrhh);
                nomina.setCantidadHijos(cantidadHijos);
                nomina.setTieneHijos(cantidadHijos > 0);
                nomina.setCantidadGuardias(diasTrabajados);
                nomina.setTipoPensionAplicada(tipoPension);
                nomina.setDetalles(detalles);

                nomina.setVersion(1);

                Nomina nominaGuardada = nominaRepository.guardar(nomina);
                detalles.forEach(d -> d.setIdNomina(nominaGuardada.getIdNomina()));
                detalleNominaRepository.guardarTodos(detalles);

                AjusteNominaManualModel ajusteManual = ajustesManuales.get(empleado.getIdEmpleado());
                if (ajusteManual != null) {
                    // Las horas extra del ajuste ya están en el libro de liquidación; aquí solo se reaplican los demás campos
                    AjustarNominaRequestDTO ajuste = aDtoAjuste(ajusteManual);
                    ajuste.setTotalHorasExtra(BigDecimal.ZERO);
                    resultados.add(ajustarNomina(nominaGuardada.getIdNomina(), ajuste, idUsuarioRrhh, true));
                    jpaAjusteNominaManualRepository.delete(ajusteManual);
                } else {
                    resultados.add(mapearAResponse(nominaGuardada, empleado, contrato.getSueldo()));
                }
            }

            Historial log = new Historial(idUsuarioRrhh, "CALCULO_NOMINA", "127.0.0.1");
            log.setTablaAfectada("NOMINA");
            log.setValorNuevo("Periodo: " + periodo + " | Empleados procesados: " + resultados.size());
            historialRepository.save(log);

        } catch (Exception e) {
            Historial log = new Historial(idUsuarioRrhh, "ERROR_CALCULO_NOMINA", "127.0.0.1");
            log.setTablaAfectada("NOMINA");
            log.setValorNuevo("Error: " + e.getMessage());
            historialRepository.save(log);
            throw e;
        }

        return resultados;
    }

    @Override
    public NominaResponseDTO buscarNominaPorId(Integer idNomina) {
        Nomina nomina = nominaRepository.buscarPorId(idNomina)
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Nomina no encontrada con id: " + idNomina));
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(nomina.getIdEmpleado()))
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Empleado no encontrado."));
        return mapearAResponse(nomina, empleado);
    }

    @Override
    public List<NominaResponseDTO> buscarPorPeriodo(String periodo) {
        List<Nomina> nominas = nominaRepository.buscarPorPeriodo(periodo);

        // Deduplicar: quedarse con la nómina de mayor id por empleado
        List<Nomina> deduplicadas = new ArrayList<>(nominas.stream()
            .collect(Collectors.toMap(
                Nomina::getIdEmpleado,
                n -> n,
                (n1, n2) -> (n2.getIdNomina() != null && (n1.getIdNomina() == null || n2.getIdNomina() > n1.getIdNomina())) ? n2 : n1
            ))
            .values());
        deduplicadas.sort((a, b) -> {
            if (a.getIdNomina() == null) return 1;
            if (b.getIdNomina() == null) return -1;
            return Integer.compare(a.getIdNomina(), b.getIdNomina());
        });

        // Una sola query para todos los empleados en vez de N queries
        List<Long> ids = deduplicadas.stream()
            .map(Nomina::getIdEmpleado)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, EmpleadoModel> empleadosPorId = ids.isEmpty() ? Map.of() :
            jpaEmpleadoRepository.findAllById(ids)
            .stream()
            .collect(Collectors.toMap(EmpleadoModel::getIdEmpleado, e -> e));

        // Pre-cargar contratos activos en una sola query para evitar N+1
        Map<Long, BigDecimal> sueldoContratoPorEmpleado = ids.isEmpty() ? Map.of() :
            jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(ids, "ACTIVO")
            .stream()
            .collect(Collectors.toMap(
                c -> c.getEmpleado().getIdEmpleado(),
                ContratoModel::getSueldo,
                (c1, c2) -> c1
            ));

        return deduplicadas.stream()
            .map(n -> mapearAResponse(n, empleadosPorId.get(n.getIdEmpleado()),
                sueldoContratoPorEmpleado.get(n.getIdEmpleado())))
            .collect(Collectors.toList());
    }

    @Override
    public List<NominaResponseDTO> buscarPorEmpleado(Long idEmpleado) {
        EmpleadoModel emp = jpaEmpleadoRepository.findById(Objects.requireNonNull(idEmpleado)).orElse(null);
        BigDecimal sueldoContrato = jpaContratoRepository
            .findFirstByEmpleadoIdEmpleadoAndEstado(idEmpleado, "ACTIVO")
            .map(ContratoModel::getSueldo).orElse(null);
        return nominaRepository.buscarPorEmpleadoYPeriodo(idEmpleado, null).stream()
            .map(n -> mapearAResponse(n, emp, sueldoContrato))
            .collect(Collectors.toList());
    }

    @Override
    public List<NominaResponseDTO> buscarPorNombreEmpleado(String nombre) {
        String filtro = nombre.toLowerCase().trim();
        List<EmpleadoModel> empleadosCoincidentes = jpaEmpleadoRepository.findAll().stream()
            .filter(e -> {
                String nombreCompleto = (e.getNombres() + " " + e.getApellidos()).toLowerCase();
                return nombreCompleto.contains(filtro);
            })
            .collect(Collectors.toList());

        List<Long> idsEmpleados = empleadosCoincidentes.stream()
            .map(EmpleadoModel::getIdEmpleado)
            .collect(Collectors.toList());

        if (idsEmpleados.isEmpty()) return List.of();

        // Pre-cargar contratos activos en bulk
        Map<Long, BigDecimal> sueldoContratoPorEmpleado =
            jpaContratoRepository.findByEmpleadoIdEmpleadoInAndEstado(idsEmpleados, "ACTIVO")
            .stream()
            .collect(Collectors.toMap(
                c -> c.getEmpleado().getIdEmpleado(),
                ContratoModel::getSueldo,
                (c1, c2) -> c1
            ));

        return nominaRepository.buscarPorNombreEmpleado(nombre).stream()
            .filter(n -> idsEmpleados.contains(n.getIdEmpleado()))
            .map(n -> {
                EmpleadoModel emp = empleadosCoincidentes.stream()
                    .filter(e -> e.getIdEmpleado().equals(n.getIdEmpleado()))
                    .findFirst().orElse(null);
                return mapearAResponse(n, emp, sueldoContratoPorEmpleado.get(n.getIdEmpleado()));
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NominaResponseDTO ajustarNomina(Integer idNomina, AjustarNominaRequestDTO request, Long idUsuarioRrhh) {
        return ajustarNomina(idNomina, request, idUsuarioRrhh, false);
    }

    // En un recálculo el ajuste se aplica sin historial: la nómina nueva queda en versión 1
    private NominaResponseDTO ajustarNomina(Integer idNomina, AjustarNominaRequestDTO request, Long idUsuarioRrhh, boolean esRecalculo) {
        Nomina nomina = nominaRepository.buscarPorId(idNomina)
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Nomina no encontrada con id: " + idNomina));
        // Una nómina pagada no se modifica: la corrección se hace en el siguiente período
        if ("PAGADA".equals(nomina.getEstadoPago())) {
            throw new IllegalStateException("La nómina " + idNomina + " ya está pagada y no se puede ajustar.");
        }

        ContratoModel contratoActivo = jpaContratoRepository
            .findFirstByEmpleadoIdEmpleadoAndEstado(nomina.getIdEmpleado(), "ACTIVO")
            .orElseThrow(() -> new IllegalStateException(
                "El empleado asociado a la nomina no tiene contrato activo."
            ));
        // Si la petición trae sueldoBase se usa ese valor; si no, se toma el del contrato activo.
        BigDecimal sueldoBase = (request.getSueldoBase() != null && request.getSueldoBase().compareTo(BigDecimal.ZERO) > 0)
            ? normalizar(request.getSueldoBase())
            : normalizar(contratoActivo.getSueldo());
        if (sueldoBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El sueldo base debe ser mayor a cero.");
        }
        if (sueldoBase.compareTo(rrhhProperties.getSalarioMinimo()) < 0) {
            throw new IllegalArgumentException(
                "El sueldo base debe ser mayor o igual al salario minimo legal (S/ "
                + rrhhProperties.getSalarioMinimo().stripTrailingZeros().toPlainString() + ")."
            );
        }

        if (esNegativo(request.getTotalHorasTrabajadas()) || esNegativo(request.getTotalHorasExtra())
                || (request.getTotalMinutosTardanza() != null && request.getTotalMinutosTardanza() < 0)
                || (request.getCantidadGuardias() != null && request.getCantidadGuardias() < 0)) {
            throw new IllegalArgumentException("Error: el valor debe ser mayor o igual a 0");
        }

        BigDecimal totalHorasTrabajadas = normalizar(request.getTotalHorasTrabajadas());
        BigDecimal deltaHorasExtra = normalizar(request.getTotalHorasExtra());
        int totalMinutosTardanza = Math.max(0, request.getTotalMinutosTardanza() != null ? request.getTotalMinutosTardanza() : 0);
        int cantidadGuardias = Math.max(0, request.getCantidadGuardias() != null ? request.getCantidadGuardias() : 0);
        // Los hijos salen de la familia registrada del empleado en cada cálculo, no del ajuste digitado
        int cantidadHijos = contarHijosValidados(nomina.getIdEmpleado());
        boolean tieneHijos = cantidadHijos > 0;
        String tipoPension = "AFP".equalsIgnoreCase(request.getTipoPensionAplicada()) ? "AFP" : "ONP";

        BigDecimal sueldoAnteriorContrato = contratoActivo.getSueldo();

        // Sueldo devengado: solo por los días que realmente trabajó en el periodo
        List<RegistroAsistenciaModel> registrosPeriodo = filtrarRegistrosValidos(
            jpaRegistroAsistenciaRepository.findByEmpleadoIdEmpleadoInAndFechaBetween(
                List.of(nomina.getIdEmpleado()), nomina.getFechaInicio(), nomina.getFechaFin())
        );
        int diasTrabajados = (int) registrosPeriodo.stream()
            .filter(r -> r.getHoraEntrada() != null && r.getHoraSalida() != null)
            .count();
        BigDecimal sueldoDevengado = calcularSueldoDevengado(sueldoBase, diasTrabajados);
        // El ajuste de horas extra se registra una sola vez en el libro; después el total sale de lo liquidado
        if (!esRecalculo && deltaHorasExtra.compareTo(BigDecimal.ZERO) > 0) {
            liquidacionHorasExtra.registrarAjuste(nomina.getIdEmpleado(), nomina.getPeriodo(), deltaHorasExtra,
                "Ajuste manual de RRHH", idUsuarioRrhh);
        }
        BigDecimal horasExtraAsistencia = liquidacionHorasExtra.horasAsistencia(nomina.getIdEmpleado(), nomina.getPeriodo());
        BigDecimal totalHorasExtra = liquidacionHorasExtra.horasTotales(nomina.getIdEmpleado(), nomina.getPeriodo());

        BigDecimal valorHora = sueldoBase
            .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
            .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP);
        BigDecimal valorMinuto = valorHora.divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 4, RoundingMode.HALF_UP);

        BigDecimal montoHorasExtra = calcularMontoHorasExtraPorTurno(nomina.getIdEmpleado(), nomina.getPeriodo(), valorHora);
        BigDecimal bonifFamiliar = calcularAsignacionFamiliar(cantidadHijos);
        // Mismas reglas que calcularNomina: porcentaje del sueldo devengado solo si el turno es nocturno
        boolean esNocturno = esTurnoNocturno(registrosPeriodo, diasTrabajados);
        BigDecimal bonifTurnoNocturno = esNocturno
            ? sueldoDevengado.multiply(rrhhProperties.getBonifNocturno()).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        // Mismas reglas que calcularNomina: guardia fija y riesgo para personal clínico
        boolean esClinico = DPTOS_CLINICOS.contains(contratoActivo.getDepartamento() != null
            ? contratoActivo.getDepartamento().getNombre() : "");
        BigDecimal bonifGuardia = esClinico
            ? rrhhProperties.getBonifGuardia().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal bonifRiesgo = esClinico
            ? sueldoDevengado.multiply(rrhhProperties.getBonifRiesgo()).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        UsuarioModel usuario = jpaUsuarioRepository.findByEmpleadoIdEmpleado(nomina.getIdEmpleado()).orElse(null);
        BigDecimal bonifCargo = tieneBonifCargo(usuario)
            ? sueldoDevengado.multiply(rrhhProperties.getBonifCargo()).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal descuentoTardanzas = valorMinuto.multiply(BigDecimal.valueOf(totalMinutosTardanza))
            .setScale(2, RoundingMode.HALF_UP)
            .add(descuentoPlanesRecuperacion(nomina.getIdEmpleado(), nomina.getPeriodo()));

        BigDecimal sueldoBruto = sueldoDevengado
            .add(montoHorasExtra)
            .add(bonifFamiliar)
            .add(bonifTurnoNocturno)
            .add(bonifGuardia)
            .add(bonifRiesgo)
            .add(bonifCargo)
            .setScale(2, RoundingMode.HALF_UP);
        BigDecimal tasaDescuento = "AFP".equals(tipoPension)
            ? rrhhProperties.getTasaDescuentoAfp()
            : rrhhProperties.getTasaDescuentoLey();
        BigDecimal descuentoLey = sueldoBruto.multiply(tasaDescuento).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sueldoNeto = sueldoBruto.subtract(descuentoTardanzas).subtract(descuentoLey)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

        // Antes de sobrescribir la fila, se guarda la versión vigente para el historial
        if (!esRecalculo) {
            NominaVersionModel versionAnterior = aVersion(nomina);
            versionAnterior.setReemplazadoPor(idUsuarioRrhh);
            versionAnterior.setReemplazadoEl(LocalDateTime.now());
            jpaNominaVersionRepository.save(versionAnterior);
        }

        nomina.setSueldoBase(sueldoDevengado);
        nomina.setTotalHorasTrabajadas(totalHorasTrabajadas);
        nomina.setTotalHorasExtra(totalHorasExtra);
        nomina.setTotalHorasExtraAsistencia(horasExtraAsistencia);
        nomina.setTotalMinutosTardanza(totalMinutosTardanza);
        nomina.setTieneHijos(tieneHijos);
        nomina.setCantidadHijos(cantidadHijos);
        nomina.setCantidadGuardias(cantidadGuardias);
        nomina.setTipoPensionAplicada(tipoPension);
        nomina.setBonifFamiliar(bonifFamiliar);
        nomina.setAsignacionFamiliar(bonifFamiliar);
        nomina.setBonifTurnoNocturno(bonifTurnoNocturno);
        nomina.setBonifGuardia(bonifGuardia);
        nomina.setBonifRiesgo(bonifRiesgo);
        nomina.setBonifCargo(bonifCargo);
        nomina.setDescuentoTardanzas(descuentoTardanzas);
        nomina.setDescuentoLey(descuentoLey);
        nomina.setSueldoBruto(sueldoBruto);
        nomina.setSueldoNeto(sueldoNeto);
        nomina.setEstadoPago("AJUSTADA");
        if (!esRecalculo) {
            nomina.setVersion(nomina.getVersion() != null ? nomina.getVersion() + 1 : 2);
        }
        nomina.setCalculadoPor(idUsuarioRrhh);

        if (!esRecalculo) {
            guardarAjusteManual(nomina.getIdEmpleado(), nomina.getPeriodo(), request, idUsuarioRrhh);
        }

        Nomina guardada = nominaRepository.guardar(nomina);

        cambioSueldo.registrar(nomina.getIdEmpleado(), LocalDate.now(), sueldoAnteriorContrato, sueldoBase, idUsuarioRrhh);
        contratoActivo.setSueldo(sueldoBase);
        contratoActivo.setTipoPension(tipoPension);
        contratoActivo.setActualizadoEl(LocalDateTime.now());
        jpaContratoRepository.save(contratoActivo);

        Historial log = new Historial(idUsuarioRrhh, "AJUSTE_NOMINA", "127.0.0.1");
        log.setTablaAfectada("NOMINA");
        log.setIdRegistroAfectado(Long.valueOf(idNomina));
        log.setValorAnterior("Sueldo contrato anterior: " + sueldoAnteriorContrato);
        log.setValorNuevo("Nomina ajustada manualmente | Sueldo contrato nuevo: " + sueldoBase + " | Neto: " + sueldoNeto);
        historialRepository.save(log);

        EmpleadoModel empleado = jpaEmpleadoRepository.findById(Objects.requireNonNull(guardada.getIdEmpleado()))
            .orElse(null);
        return mapearAResponse(guardada, empleado, sueldoBase);
    }

    public byte[] generarComprobantePDF(Integer idNomina) {
        NominaResponseDTO dto = buscarNominaPorId(idNomina);
        return comprobantePDFGenerator.generate(dto);
    }

    // ================================
    // PRIVATE METHODS
    // ================================

    // RNF46: 1.5x hasta el umbral de horas dobles de cada turno y 2.0x a partir de ese umbral
    private BigDecimal calcularMontoHorasExtraPorTurno(Long idEmpleado, String periodo, BigDecimal valorHora) {
        BigDecimal umbral = BigDecimal.valueOf(rrhhProperties.getUmbralHorasDobles());
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal horas : liquidacionHorasExtra.horasPorTurno(idEmpleado, periodo)) {
            if (horas.signum() <= 0) continue;
            BigDecimal normales = horas.min(umbral);
            BigDecimal dobles = horas.subtract(normales);
            total = total
                .add(valorHora.multiply(rrhhProperties.getTasaHoraExtra1_5()).multiply(normales))
                .add(valorHora.multiply(rrhhProperties.getTasaHoraExtra2_0()).multiply(dobles));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularMontoHorasExtra(BigDecimal totalHorasExtra, BigDecimal valorHora) {
        if (totalHorasExtra.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return valorHora.multiply(rrhhProperties.getTasaHoraExtra1_5())
                .multiply(totalHorasExtra)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean esNegativo(BigDecimal valor) {
        return valor != null && valor.signum() < 0;
    }

    private BigDecimal descuentoPlanesRecuperacion(Long idEmpleado, String periodo) {
        BigDecimal monto = nominaRepository.sumarDescuentosPlanes(idEmpleado, periodo);
        return monto != null ? monto : BigDecimal.ZERO;
    }

    private BigDecimal calcularSueldoDevengado(BigDecimal sueldoBase, int diasTrabajados) {
        return sueldoBase
            .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(diasTrabajados))
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return valor != null ? valor.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NominaVersionDTO> listarVersiones(Integer idNomina) {
        Nomina actual = nominaRepository.buscarPorId(idNomina)
            .orElseThrow(() -> new com.rrhh.Shared.exception.RecursoNoEncontradoException("Nomina no encontrada con id: " + idNomina));

        List<NominaVersionDTO> versiones = new ArrayList<>();
        versiones.add(aDto(aVersion(actual), true));
        jpaNominaVersionAnteriorRepository.findByIdEmpleadoAndPeriodo(actual.getIdEmpleado(), actual.getPeriodo())
            .ifPresent(anterior -> versiones.add(aDtoAnterior(anterior)));
        jpaNominaVersionRepository.findByIdNominaOrderByVersionDesc(idNomina)
            .forEach(v -> versiones.add(aDto(v, false)));
        return versiones;
    }

    // Guarda (o reemplaza) los valores manuales del empleado en el periodo para que un recálculo los reaplique
    private void guardarAjusteManual(Long idEmpleado, String periodo, AjustarNominaRequestDTO request, Long idUsuarioRrhh) {
        AjusteNominaManualModel ajuste = jpaAjusteNominaManualRepository
            .findByIdEmpleadoAndPeriodo(idEmpleado, periodo)
            .orElseGet(AjusteNominaManualModel::new);

        ajuste.setIdEmpleado(idEmpleado);
        ajuste.setPeriodo(periodo);
        ajuste.setTotalHorasTrabajadas(normalizar(request.getTotalHorasTrabajadas()));
        ajuste.setTotalHorasExtra(normalizar(request.getTotalHorasExtra()));
        ajuste.setTotalMinutosTardanza(Math.max(0, request.getTotalMinutosTardanza() != null ? request.getTotalMinutosTardanza() : 0));
        ajuste.setCantidadGuardias(Math.max(0, request.getCantidadGuardias() != null ? request.getCantidadGuardias() : 0));
        ajuste.setTipoPensionAplicada("AFP".equalsIgnoreCase(request.getTipoPensionAplicada()) ? "AFP" : "ONP");
        ajuste.setActualizadoPor(idUsuarioRrhh);
        ajuste.setActualizadoEl(LocalDateTime.now());

        jpaAjusteNominaManualRepository.save(ajuste);
    }

    private AjustarNominaRequestDTO aDtoAjuste(AjusteNominaManualModel ajuste) {
        AjustarNominaRequestDTO dto = new AjustarNominaRequestDTO();
        dto.setTotalHorasTrabajadas(ajuste.getTotalHorasTrabajadas());
        dto.setTotalHorasExtra(ajuste.getTotalHorasExtra());
        dto.setTotalMinutosTardanza(ajuste.getTotalMinutosTardanza());
        dto.setCantidadGuardias(ajuste.getCantidadGuardias());
        dto.setTipoPensionAplicada(ajuste.getTipoPensionAplicada());
        return dto;
    }

    private NominaVersionModel aVersion(Nomina nomina) {
        NominaVersionModel v = new NominaVersionModel();
        v.setIdNomina(nomina.getIdNomina());
        v.setVersion(nomina.getVersion() != null ? nomina.getVersion() : 1);
        v.setSueldoBase(nomina.getSueldoBase());
        v.setTotalHorasTrabajadas(nomina.getTotalHorasTrabajadas());
        v.setTotalHorasExtra(nomina.getTotalHorasExtra());
        v.setTotalMinutosTardanza(nomina.getTotalMinutosTardanza());
        v.setBonifFamiliar(nomina.getBonifFamiliar());
        v.setBonifTurnoNocturno(nomina.getBonifTurnoNocturno());
        v.setBonifGuardia(nomina.getBonifGuardia());
        v.setBonifRiesgo(nomina.getBonifRiesgo());
        v.setDescuentoTardanzas(nomina.getDescuentoTardanzas());
        v.setDescuentoLey(nomina.getDescuentoLey());
        v.setSueldoBruto(nomina.getSueldoBruto());
        v.setSueldoNeto(nomina.getSueldoNeto());
        v.setTipoPensionAplicada(nomina.getTipoPensionAplicada());
        return v;
    }

    private NominaVersionDTO aDtoAnterior(NominaVersionAnteriorModel v) {
        NominaVersionDTO dto = new NominaVersionDTO();
        dto.setVersion(v.getVersion());
        dto.setVigente(false);
        dto.setSueldoBase(v.getSueldoBase());
        dto.setTotalHorasExtra(v.getTotalHorasExtra());
        dto.setTotalMinutosTardanza(v.getTotalMinutosTardanza());
        dto.setDescuentoTardanzas(v.getDescuentoTardanzas());
        dto.setDescuentoLey(v.getDescuentoLey());
        dto.setSueldoBruto(v.getSueldoBruto());
        dto.setSueldoNeto(v.getSueldoNeto());
        dto.setTipoPensionAplicada(v.getTipoPensionAplicada());
        dto.setReemplazadoEl(v.getReemplazadaEl());
        return dto;
    }

    private NominaVersionDTO aDto(NominaVersionModel v, boolean vigente) {
        NominaVersionDTO dto = new NominaVersionDTO();
        dto.setVersion(v.getVersion());
        dto.setVigente(vigente);
        dto.setSueldoBase(v.getSueldoBase());
        dto.setTotalHorasExtra(v.getTotalHorasExtra());
        dto.setTotalMinutosTardanza(v.getTotalMinutosTardanza());
        dto.setDescuentoTardanzas(v.getDescuentoTardanzas());
        dto.setDescuentoLey(v.getDescuentoLey());
        dto.setSueldoBruto(v.getSueldoBruto());
        dto.setSueldoNeto(v.getSueldoNeto());
        dto.setTipoPensionAplicada(v.getTipoPensionAplicada());
        dto.setReemplazadoEl(v.getReemplazadoEl());
        return dto;
    }

    private void validarPrecision(BigDecimal sueldoBase, BigDecimal horasExtra, BigDecimal fam,
                                   BigDecimal noct, BigDecimal guardia, BigDecimal riesgo, BigDecimal cargo,
                                   BigDecimal bruto, BigDecimal descTardanzas, BigDecimal descLey, BigDecimal neto,
                                   EmpleadoModel empleado) {
        BigDecimal brutoEsperado = sueldoBase.add(horasExtra).add(fam).add(noct)
                .add(guardia).add(riesgo).add(cargo).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netoEsperado = bruto.subtract(descTardanzas).subtract(descLey).setScale(2, RoundingMode.HALF_UP);

        if (bruto.subtract(brutoEsperado).abs().compareTo(new BigDecimal("0.02")) > 0) {
            throw new IllegalStateException("Error: Inconsistencia en sueldo bruto del empleado " +
                    empleado.getNombres() + ". Esperado: " + brutoEsperado + ", Calculado: " + bruto);
        }
        if (neto.subtract(netoEsperado).abs().compareTo(new BigDecimal("0.02")) > 0) {
            throw new IllegalStateException("Error: Inconsistencia en sueldo neto del empleado " +
                    empleado.getNombres() + ". Esperado: " + netoEsperado + ", Calculado: " + neto);
        }
    }



    private boolean contratadoHasta(EmpleadoModel empleado, LocalDate fechaFin) {
        return empleado.getContratos() != null && empleado.getContratos().stream()
            .anyMatch(c -> c.getFechaInicio() != null && !c.getFechaInicio().isAfter(fechaFin));
    }

    private List<EmpleadoModel> obtenerEmpleadosActivos(CalcularNominaRequestDTO request) {
        if (request.getIdEmpleado() != null) {
            return jpaEmpleadoRepository.findById(Objects.requireNonNull(request.getIdEmpleado()))
                .filter(e -> "ACTIVO".equals(e.getEstado()))
                .map(List::of)
                .orElse(List.of());
        }
        if (request.getIdDepartamento() != null) {
            return jpaEmpleadoRepository.buscarAvanzado("ACTIVO", request.getIdDepartamento(), null);
        }
        return jpaEmpleadoRepository.findByEstado("ACTIVO");
    }

    private void validarDatosContrato(BigDecimal sueldoBase, EmpleadoModel empleado) {
        if (sueldoBase == null || sueldoBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                "El sueldo base del empleado " + empleado.getNombres() + " no puede ser negativo o nulo.");
        }
    }

    private BigDecimal calcularTotalHorasTrabajadas(List<RegistroAsistenciaModel> registros) {
        return registros.stream()
            .map(r -> {
                long minutos = com.rrhh.Shared.util.TurnoUtils.minutosTrabajados(r.getHoraEntrada(), r.getHoraSalida());
                return BigDecimal.valueOf(minutos)
                    .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int contarHijosValidados(Long idEmpleado) {
        return contarHijosDesdeCache(jpaFamiliaInfoRepository.findByEmpleadoIdEmpleadoInAndActivoTrue(List.of(idEmpleado)));
    }

    // Asignación familiar: 10% de la RMV, se otorga una sola vez si existe al menos un hijo elegible (no por hijo)
    private BigDecimal calcularAsignacionFamiliar(int cantidadHijos) {
        return cantidadHijos > 0
            ? rrhhProperties.getRmv().multiply(rrhhProperties.getBonifFamiliar()).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    // Turno nocturno: la mayoría de los días trabajados tienen entrada desde las 19:00
    private boolean esTurnoNocturno(List<RegistroAsistenciaModel> registros, int diasTrabajados) {
        long diasNocturnos = registros.stream()
            .filter(r -> r.getHoraEntrada() != null && r.getHoraEntrada().getHour() >= 19)
            .count();
        return diasNocturnos > 0 && diasNocturnos >= diasTrabajados / 2;
    }

    private boolean tieneBonifCargo(UsuarioModel usuario) {
        return usuario != null && usuario.getRol() != null && ROLES_CON_BONIF_CARGO.contains(usuario.getRol().getNombreRol());
    }

    private int contarHijosDesdeCache(List<FamiliaInfoModel> familia) {
        return (int) familia.stream()
            .filter(f -> "HIJO".equals(f.getParentesco())
                    && Boolean.TRUE.equals(f.getElegibleAsignacionFamiliar())
                    && Boolean.TRUE.equals(f.getValidado()))
            .count();
    }

    private List<DetalleNomina> generarDetallesDiarios(
            List<RegistroAsistenciaModel> registros,
            BigDecimal valorHora,
            BigDecimal valorMinuto) {

        return registros.stream().map(r -> {
            long minutosTrabajados = com.rrhh.Shared.util.TurnoUtils.minutosTrabajados(r.getHoraEntrada(), r.getHoraSalida());
            BigDecimal horasTrabajadas = BigDecimal.valueOf(minutosTrabajados)
                .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);

            int minutosExtra = r.getMinutosExtra() != null ? r.getMinutosExtra() : 0;
            BigDecimal totalHorasExtraDia = BigDecimal.valueOf(minutosExtra)
                    .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);
            BigDecimal montoExtra = calcularMontoHorasExtra(totalHorasExtraDia, valorHora);

            int minutosTardanza = r.getMinutosTardanza() != null ? r.getMinutosTardanza() : 0;
            BigDecimal descuento = valorMinuto.multiply(BigDecimal.valueOf(minutosTardanza))
                .setScale(2, RoundingMode.HALF_UP);

            DetalleNomina detalle = new DetalleNomina();
            detalle.setFecha(r.getFecha());
            detalle.setHorasTrabajadas(horasTrabajadas);
            detalle.setMinutosTardanza(minutosTardanza);
            detalle.setMinutosExtra(minutosExtra);
            detalle.setMontoHoraExtra(montoExtra);
            detalle.setDescuentoTardanza(descuento);
            detalle.setIdEmpleado(r.getEmpleado().getIdEmpleado());
            return detalle;
        }).collect(Collectors.toList());
    }

    private String generarPeriodo(LocalDate inicio, LocalDate fin) {
        return inicio.format(DateTimeFormatter.ofPattern("yyyy-MM")) ;
    }

    private NominaResponseDTO mapearAResponse(Nomina nomina, EmpleadoModel empleado) {
        BigDecimal sueldoContrato = jpaContratoRepository
            .findFirstByEmpleadoIdEmpleadoAndEstado(nomina.getIdEmpleado(), "ACTIVO")
            .map(ContratoModel::getSueldo)
            .orElse(null);
        return mapearAResponse(nomina, empleado, sueldoContrato);
    }

    private NominaResponseDTO mapearAResponse(Nomina nomina, EmpleadoModel empleado, BigDecimal sueldoContrato) {
        NominaResponseDTO dto = new NominaResponseDTO();
        dto.setIdNomina(nomina.getIdNomina());
        dto.setPeriodo(nomina.getPeriodo());
        dto.setFechaInicio(nomina.getFechaInicio());
        dto.setFechaFin(nomina.getFechaFin());
        dto.setFechaEmision(nomina.getFechaEmision());
        dto.setIdEmpleado(nomina.getIdEmpleado());
        if (empleado != null) {
            dto.setNombreEmpleado(empleado.getNombres() + " " + empleado.getApellidos());
        }
        dto.setSueldoBase(nomina.getSueldoBase());
        dto.setSueldoContrato(sueldoContrato);
        dto.setTotalHorasTrabajadas(nomina.getTotalHorasTrabajadas());
        dto.setTotalHorasExtra(nomina.getTotalHorasExtra());
        dto.setTotalHorasExtraAsistencia(nomina.getTotalHorasExtraAsistencia());
        dto.setTotalMinutosTardanza(nomina.getTotalMinutosTardanza());
        dto.setBonifFamiliar(nomina.getBonifFamiliar());
        dto.setBonifTurnoNocturno(nomina.getBonifTurnoNocturno());
        dto.setBonifGuardia(nomina.getBonifGuardia());
        dto.setBonifRiesgo(nomina.getBonifRiesgo());
        dto.setBonifCargo(nomina.getBonifCargo());
        dto.setDescuentoTardanzas(nomina.getDescuentoTardanzas());
        dto.setDescuentoLey(nomina.getDescuentoLey());
        dto.setAsignacionFamiliar(nomina.getAsignacionFamiliar());
        dto.setSueldoBruto(nomina.getSueldoBruto());
        dto.setSueldoNeto(nomina.getSueldoNeto());
        dto.setEstadoPago(nomina.getEstadoPago());
        dto.setCantidadHijos(nomina.getCantidadHijos());
        dto.setTieneHijos(nomina.getTieneHijos());
        dto.setCantidadGuardias(nomina.getCantidadGuardias());
        dto.setTipoPensionAplicada(nomina.getTipoPensionAplicada());
        dto.setEsEmpleadoClinico(nomina.getEsEmpleadoClinico() != null ? nomina.getEsEmpleadoClinico() : false);
        dto.setTieneHorarioNocturno(nomina.getTieneHorarioNocturno() != null ? nomina.getTieneHorarioNocturno() : false);

        if (nomina.getDetalles() != null) {
            List<DetalleNominaResponseDTO> detallesDTO = nomina.getDetalles().stream()
                .map(d -> {
                    DetalleNominaResponseDTO detalleDTO = new DetalleNominaResponseDTO();
                    detalleDTO.setIdDetalle(d.getIdDetalle());
                    detalleDTO.setFecha(d.getFecha());
                    detalleDTO.setHorasTrabajadas(d.getHorasTrabajadas());
                    detalleDTO.setMinutosTardanza(d.getMinutosTardanza());
                    detalleDTO.setMinutosExtra(d.getMinutosExtra());
                    detalleDTO.setMontoHoraExtra(d.getMontoHoraExtra());
                    detalleDTO.setDescuentoTardanza(d.getDescuentoTardanza());
                    detalleDTO.setObservacion(d.getObservacion());
                    return detalleDTO;
                })
                .collect(Collectors.toList());
            dto.setDetalles(detallesDTO);
        }

        return dto;
    }

    private List<RegistroAsistenciaModel> filtrarRegistrosValidos(List<RegistroAsistenciaModel> registros) {
        Map<LocalDate, List<RegistroAsistenciaModel>> porFecha = registros.stream()
                // Solo registros completos: ambas horas presentes.
                // No se exige salida > entrada: los turnos nocturnos cruzan la
                // medianoche (ej. 22:00 -> 06:00), donde eso seria falso.
                .filter(r -> r.getHoraEntrada() != null
                        && r.getHoraSalida() != null
                        && !r.getHoraSalida().equals(r.getHoraEntrada()))
                .collect(Collectors.groupingBy(RegistroAsistenciaModel::getFecha));
        return porFecha.values().stream().flatMap(lista -> {
            boolean tieneCorreccion = lista.stream()
                    .anyMatch(r -> "CORRECCION".equals(r.getTipoRegistro()));
            if (tieneCorreccion) {
                return lista.stream()
                        .filter(r -> "CORRECCION".equals(r.getTipoRegistro()));
            }
            return lista.stream()
                    .filter(r -> r.getTipoRegistro() == null || "ORIGINAL".equals(r.getTipoRegistro()));
        }).collect(Collectors.toList());
    }
}
