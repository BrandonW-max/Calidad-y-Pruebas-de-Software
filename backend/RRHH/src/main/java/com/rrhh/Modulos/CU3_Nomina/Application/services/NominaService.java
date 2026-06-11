package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.factory.INominaFactory;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.DetalleNominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.DetalleNomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Shared.export.ExportAbstractFactory;
import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NominaService implements INominaService {

    @Value("${rrhh.tasa-hora-extra-1_5}")
    private BigDecimal tasaHoraExtra1_5;

    @Value("${rrhh.tasa-hora-extra-2_0}")
    private BigDecimal tasaHoraExtra2_0;

    @Value("${rrhh.umbral-horas-dobles}")
    private int umbralHorasDobles;

    @Value("${rrhh.bonif-familiar}")
    private BigDecimal tasaBonifFamiliar;

    @Value("${rrhh.bonif-nocturno}")
    private BigDecimal tasaNocturno;

    @Value("${rrhh.bonif-guardia}")
    private BigDecimal montoGuardia;

    @Value("${rrhh.bonif-riesgo}")
    private BigDecimal tasaRiesgo;

    @Value("${rrhh.bonif-cargo}")
    private BigDecimal tasaCargo;

    @Value("${rrhh.tasa-descuento-ley:0.13}")
    private BigDecimal tasaDescuentoLey;

    private static final int MINUTOS_EN_HORA = 60;
    private static final int HORAS_JORNADA_DIARIA = 8;

    private final INominaRepository nominaRepository;
    private final IDetalleNominaRepository detalleNominaRepository;
    private final IHistorialRepository historialRepository;
    private final INominaFactory nominaFactory;
    private final Map<String, ExportAbstractFactory> exportFactories;

    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository;
    private final com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository;
    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository;
    private final com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository;

    public NominaService(
            INominaRepository nominaRepository,
            IDetalleNominaRepository detalleNominaRepository,
            IHistorialRepository historialRepository,
            INominaFactory nominaFactory,
            Map<String, ExportAbstractFactory> exportFactories,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository,
            com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository,
            com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository) {
        this.nominaRepository = nominaRepository;
        this.detalleNominaRepository = detalleNominaRepository;
        this.historialRepository = historialRepository;
        this.nominaFactory = nominaFactory;
        this.exportFactories = exportFactories;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaRegistroAsistenciaRepository = jpaRegistroAsistenciaRepository;
        this.jpaContratoRepository = jpaContratoRepository;
        this.jpaFamiliaInfoRepository = jpaFamiliaInfoRepository;
    }

    @Override
    @Transactional
    public List<NominaResponseDTO> calcularNomina(CalcularNominaRequestDTO request, Long idUsuarioRrhh) {

        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha fin.");
        }

        List<EmpleadoModel> empleados = obtenerEmpleadosActivos(request);

        if (empleados.isEmpty()) {
            throw new IllegalStateException(
                "No se encontraron empleados activos para el periodo seleccionado. " +
                "No es posible realizar el calculo de nomina."
            );
        }

        String periodo = generarPeriodo(request.getFechaInicio(), request.getFechaFin());
        List<NominaResponseDTO> resultados = new ArrayList<>();

        try {
            for (EmpleadoModel empleado : empleados) {

                List<RegistroAsistenciaModel> registros = jpaRegistroAsistenciaRepository
                    .findByEmpleadoIdEmpleadoAndFechaBetween(
                        empleado.getIdEmpleado(),
                        request.getFechaInicio(),
                        request.getFechaFin()
                    );

                validarDatosAsistencia(registros, empleado);

                ContratoModel contrato = jpaContratoRepository
                    .findFirstByEmpleadoIdEmpleadoAndEstado(empleado.getIdEmpleado(), "ACTIVO")
                    .orElseThrow(() -> new IllegalStateException(
                        "El empleado " + empleado.getNombres() + " no tiene contrato activo."
                    ));
                BigDecimal sueldoBase = contrato.getSueldo();

                BigDecimal totalHorasTrabajadas = calcularTotalHorasTrabajadas(registros);

                int totalMinutosTardanza = registros.stream()
                    .mapToInt(r -> r.getMinutosTardanza() != null ? r.getMinutosTardanza() : 0)
                    .sum();

                BigDecimal valorHoraDiaria = sueldoBase
                    .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP);

                BigDecimal montoHorasExtra = BigDecimal.ZERO;
                BigDecimal totalHorasExtra = BigDecimal.ZERO;
                for (RegistroAsistenciaModel r : registros) {
                    int minExtra = r.getMinutosExtra() != null ? r.getMinutosExtra() : 0;
                    if (minExtra > 0) {
                        BigDecimal horasExtraDia = BigDecimal.valueOf(minExtra)
                            .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);
                        totalHorasExtra = totalHorasExtra.add(horasExtraDia);
                        montoHorasExtra = montoHorasExtra.add(calcularMontoHorasExtra(horasExtraDia, valorHoraDiaria));
                    }
                }
                montoHorasExtra = montoHorasExtra.setScale(2, RoundingMode.HALF_UP);

                BigDecimal valorMinuto = sueldoBase
                    .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 4, RoundingMode.HALF_UP);
                BigDecimal descuentoTardanzas = valorMinuto
                    .multiply(BigDecimal.valueOf(totalMinutosTardanza))
                    .setScale(2, RoundingMode.HALF_UP);

                int cantidadHijos = contarHijos(empleado.getIdEmpleado());
                BigDecimal bonifFamiliar = sueldoBase.multiply(tasaBonifFamiliar)
                    .multiply(BigDecimal.valueOf(cantidadHijos))
                    .setScale(2, RoundingMode.HALF_UP);

                int diasTrabajados = (int) registros.stream()
                    .filter(r -> r.getHoraEntrada() != null && r.getHoraSalida() != null)
                    .count();
                BigDecimal bonifTurnoNocturno = sueldoBase.multiply(tasaNocturno).setScale(2, RoundingMode.HALF_UP);
                BigDecimal bonifGuardia = montoGuardia.multiply(BigDecimal.valueOf(diasTrabajados)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal bonifRiesgo = sueldoBase.multiply(tasaRiesgo).setScale(2, RoundingMode.HALF_UP);
                BigDecimal bonifCargo = sueldoBase.multiply(tasaCargo).setScale(2, RoundingMode.HALF_UP);

                BigDecimal sueldoBruto = sueldoBase
                    .add(montoHorasExtra)
                    .add(bonifFamiliar)
                    .add(bonifTurnoNocturno)
                    .add(bonifGuardia)
                    .add(bonifRiesgo)
                    .add(bonifCargo)
                    .setScale(2, RoundingMode.HALF_UP);

                BigDecimal descuentoLey = sueldoBruto.multiply(tasaDescuentoLey).setScale(2, RoundingMode.HALF_UP);
                BigDecimal sueldoNeto = sueldoBruto
                    .subtract(descuentoTardanzas)
                    .subtract(descuentoLey)
                    .setScale(2, RoundingMode.HALF_UP);

                validarPrecision(sueldoBase, montoHorasExtra, bonifFamiliar, bonifTurnoNocturno,
                        bonifGuardia, bonifRiesgo, bonifCargo, sueldoBruto, descuentoTardanzas, descuentoLey, sueldoNeto, empleado);

                List<DetalleNomina> detalles = generarDetallesDiarios(
                    registros, valorHoraDiaria, valorMinuto
                );

                Nomina nomina = nominaFactory.crearNominaBase();
                nomina.setPeriodo(periodo);
                nomina.setFechaInicio(request.getFechaInicio());
                nomina.setFechaFin(request.getFechaFin());
                nomina.setFechaEmision(LocalDate.now());
                nomina.setSueldoBase(sueldoBase);
                nomina.setTotalHorasTrabajadas(totalHorasTrabajadas);
                nomina.setTotalHorasExtra(totalHorasExtra);
                nomina.setTotalMinutosTardanza(totalMinutosTardanza);
                nomina.setBonifFamiliar(bonifFamiliar);
                nomina.setBonifTurnoNocturno(bonifTurnoNocturno);
                nomina.setBonifGuardia(bonifGuardia);
                nomina.setBonifRiesgo(bonifRiesgo);
                nomina.setBonifCargo(bonifCargo);
                nomina.setDescuentoTardanzas(descuentoTardanzas);
                nomina.setDescuentoLey(descuentoLey);
                nomina.setAsignacionFamiliar(bonifFamiliar);
                nomina.setSueldoBruto(sueldoBruto);
                nomina.setSueldoNeto(sueldoNeto);
                nomina.setEstadoPago("CALCULADA");
                nomina.setIdEmpleado(empleado.getIdEmpleado());
                nomina.setCalculadoPor(idUsuarioRrhh);
                nomina.setDetalles(detalles);

                List<Nomina> existentes =
                        nominaRepository.buscarPorEmpleadoYRango(empleado.getIdEmpleado(),
                                request.getFechaInicio(), request.getFechaFin());
                if (!existentes.isEmpty()) {
                    Nomina ultima = existentes.get(existentes.size() - 1);
                    nomina.setVersion(ultima.getVersion() != null ? ultima.getVersion() + 1 : 2);
                    nomina.setNominaAnteriorId(ultima.getIdNomina());
                } else {
                    nomina.setVersion(1);
                }

                Nomina nominaGuardada = nominaRepository.guardar(nomina);
                detalles.forEach(d -> d.setIdNomina(nominaGuardada.getIdNomina()));
                detalleNominaRepository.guardarTodos(detalles);

                resultados.add(mapearAResponse(nominaGuardada, empleado));
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
            .orElseThrow(() -> new IllegalArgumentException("Nomina no encontrada con id: " + idNomina));
        EmpleadoModel empleado = jpaEmpleadoRepository.findById(nomina.getIdEmpleado())
            .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado."));
        return mapearAResponse(nomina, empleado);
    }

    @Override
    public List<NominaResponseDTO> buscarPorPeriodo(String periodo) {
        return nominaRepository.buscarPorPeriodo(periodo).stream()
            .map(n -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(n.getIdEmpleado()).orElse(null);
                return mapearAResponse(n, emp);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<NominaResponseDTO> buscarPorEmpleado(Long idEmpleado) {
        return nominaRepository.buscarPorEmpleadoYPeriodo(idEmpleado, null).stream()
            .map(n -> {
                EmpleadoModel emp = jpaEmpleadoRepository.findById(idEmpleado).orElse(null);
                return mapearAResponse(n, emp);
            })
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

        return nominaRepository.buscarPorNombreEmpleado(nombre).stream()
            .filter(n -> idsEmpleados.contains(n.getIdEmpleado()))
            .map(n -> {
                EmpleadoModel emp = empleadosCoincidentes.stream()
                    .filter(e -> e.getIdEmpleado().equals(n.getIdEmpleado()))
                    .findFirst().orElse(null);
                return mapearAResponse(n, emp);
            })
            .collect(Collectors.toList());
    }

    public byte[] generarComprobantePDF(Integer idNomina) {
        NominaResponseDTO dto = buscarNominaPorId(idNomina);
        return generarPDF(dto);
    }

    // ================================
    // PRIVATE METHODS
    // ================================

    private BigDecimal calcularMontoHorasExtra(BigDecimal totalHorasExtra, BigDecimal valorHora) {
        if (totalHorasExtra.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;

        BigDecimal umbral = BigDecimal.valueOf(umbralHorasDobles);

        if (totalHorasExtra.compareTo(umbral) <= 0) {
            return valorHora.multiply(tasaHoraExtra1_5)
                    .multiply(totalHorasExtra)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal horasBase = umbral;
        BigDecimal horasDobles = totalHorasExtra.subtract(umbral);

        BigDecimal montoBase = valorHora.multiply(tasaHoraExtra1_5)
                .multiply(horasBase);
        BigDecimal montoDoble = valorHora.multiply(tasaHoraExtra2_0)
                .multiply(horasDobles);

        return montoBase.add(montoDoble).setScale(2, RoundingMode.HALF_UP);
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

    private byte[] generarPDF(NominaResponseDTO dto) {
        List<Map<String, Object>> datos = new ArrayList<>();

        // ── Detalle diario ──
        if (dto.getDetalles() != null) {
            for (DetalleNominaResponseDTO det : dto.getDetalles()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("Dia", det.getFecha() != null ? det.getFecha().toString() : "—");
                row.put("Horas Trabajadas", det.getHorasTrabajadas());
                row.put("Min. Tardanza", det.getMinutosTardanza());
                row.put("Desc. Tardanza", "S/ " + det.getDescuentoTardanza());
                row.put("Min. Extra", det.getMinutosExtra());
                row.put("Monto H.Extra", "S/ " + det.getMontoHoraExtra());
                datos.add(row);
            }
        }

        // ── Línea separadora ──
        Map<String, Object> sep = new LinkedHashMap<>();
        sep.put("Dia", "─── RESUMEN ───");
        sep.put("Horas Trabajadas", "");
        sep.put("Min. Tardanza", "");
        sep.put("Desc. Tardanza", "");
        sep.put("Min. Extra", "");
        sep.put("Monto H.Extra", "");
        datos.add(sep);

        // ── Resumen ──
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("Dia", "");
        fila.put("Horas Trabajadas", dto.getTotalHorasTrabajadas());
        fila.put("Min. Tardanza", dto.getTotalMinutosTardanza());
        fila.put("Desc. Tardanza", "S/ " + dto.getDescuentoTardanzas());
        fila.put("Min. Extra", "S/ " + dto.getTotalHorasExtra());
        fila.put("Monto H.Extra", "S/ " + (dto.getSueldoBruto() != null && dto.getSueldoBruto().subtract(dto.getSueldoBase() != null ? dto.getSueldoBase() : BigDecimal.ZERO).compareTo(BigDecimal.ZERO) > 0 ? dto.getSueldoBruto().subtract(dto.getSueldoBase()).subtract(
            (dto.getBonifFamiliar() != null ? dto.getBonifFamiliar() : BigDecimal.ZERO).add(
            dto.getBonifTurnoNocturno() != null ? dto.getBonifTurnoNocturno() : BigDecimal.ZERO).add(
            dto.getBonifGuardia() != null ? dto.getBonifGuardia() : BigDecimal.ZERO).add(
            dto.getBonifRiesgo() != null ? dto.getBonifRiesgo() : BigDecimal.ZERO).add(
            dto.getBonifCargo() != null ? dto.getBonifCargo() : BigDecimal.ZERO)).toString() : "—"));
        datos.add(fila);

        Map<String, Object> totalRow = new LinkedHashMap<>();
        totalRow.put("Dia", "Empleado: " + dto.getNombreEmpleado() + " | Periodo: " + dto.getPeriodo());
        totalRow.put("Horas Trabajadas", "");
        totalRow.put("Min. Tardanza", "");
        totalRow.put("Desc. Tardanza", "");
        totalRow.put("Min. Extra", "");
        totalRow.put("Monto H.Extra", "");
        datos.add(totalRow);

        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("Dia", "Sueldo Base: S/ " + dto.getSueldoBase());
        resumen.put("Horas Trabajadas", "");
        resumen.put("Min. Tardanza", "");
        resumen.put("Desc. Tardanza", "");
        resumen.put("Min. Extra", "");
        resumen.put("Monto H.Extra", "");
        datos.add(resumen);

        Map<String, Object> bonifRow = new LinkedHashMap<>();
        bonifRow.put("Dia", "Bonif. Familiar: S/ " + dto.getBonifFamiliar() + " | Nocturno: S/ " + dto.getBonifTurnoNocturno()
            + " | Guardia: S/ " + dto.getBonifGuardia() + " | Riesgo: S/ " + dto.getBonifRiesgo() + " | Cargo: S/ " + dto.getBonifCargo());
        bonifRow.put("Horas Trabajadas", "");
        bonifRow.put("Min. Tardanza", "");
        bonifRow.put("Desc. Tardanza", "");
        bonifRow.put("Min. Extra", "");
        bonifRow.put("Monto H.Extra", "");
        datos.add(bonifRow);

        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("Dia", "Sueldo Bruto: S/ " + dto.getSueldoBruto() + " | Desc. Tardanza: S/ " + dto.getDescuentoTardanzas()
            + " | Desc. Ley: S/ " + dto.getDescuentoLey() + " | Sueldo Neto: S/ " + dto.getSueldoNeto() + " | Estado: " + dto.getEstadoPago());
        totales.put("Horas Trabajadas", "");
        totales.put("Min. Tardanza", "");
        totales.put("Desc. Tardanza", "");
        totales.put("Min. Extra", "");
        totales.put("Monto H.Extra", "");
        datos.add(totales);

        String[] headers = {"Dia", "Horas Trabajadas", "Min. Tardanza", "Desc. Tardanza", "Min. Extra", "Monto H.Extra"};
        String[] keys = {"Dia", "Horas Trabajadas", "Min. Tardanza", "Desc. Tardanza", "Min. Extra", "Monto H.Extra"};

        ExportAbstractFactory factory = exportFactories.get("nominaExportFactory");
        if (factory == null) {
            throw new IllegalStateException("Fabrica de exportacion de nomina no encontrada.");
        }
        return factory.createPDFExporter().export("Comprobante de Nomina", datos, headers, keys);
    }

    private List<EmpleadoModel> obtenerEmpleadosActivos(CalcularNominaRequestDTO request) {
        if (request.getIdEmpleado() != null) {
            return jpaEmpleadoRepository.findById(request.getIdEmpleado())
                .filter(e -> "ACTIVO".equals(e.getEstado()))
                .map(List::of)
                .orElse(List.of());
        }
        if (request.getIdDepartamento() != null) {
            return jpaEmpleadoRepository.buscarAvanzado("ACTIVO", request.getIdDepartamento(), null);
        }
        return jpaEmpleadoRepository.findByEstado("ACTIVO");
    }

    private void validarDatosAsistencia(List<RegistroAsistenciaModel> registros, EmpleadoModel empleado) {
        for (RegistroAsistenciaModel r : registros) {
            if (r.getHoraEntrada() == null || r.getHoraSalida() == null) {
                throw new IllegalStateException(
                    "Error: El empleado " + empleado.getNombres() +
                    " tiene registros de asistencia incompletos (falta entrada o salida) en la fecha: " + r.getFecha()
                );
            }
            if (!r.getHoraSalida().isAfter(r.getHoraEntrada())) {
                throw new IllegalStateException(
                    "Error: El empleado " + empleado.getNombres() +
                    " tiene horas inconsistentes (salida menor a entrada) en la fecha: " + r.getFecha()
                );
            }
        }
    }

    private BigDecimal calcularTotalHorasTrabajadas(List<RegistroAsistenciaModel> registros) {
        return registros.stream()
            .map(r -> {
                long minutos = java.time.Duration.between(r.getHoraEntrada(), r.getHoraSalida()).toMinutes();
                return BigDecimal.valueOf(minutos)
                    .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int contarHijos(Long idEmpleado) {
        List<FamiliaInfoModel> familia = jpaFamiliaInfoRepository
            .findByEmpleadoIdEmpleadoAndActivoTrue(idEmpleado);
        return (int) familia.stream()
            .filter(f -> "HIJO".equals(f.getParentesco()))
            .count();
    }

    private List<DetalleNomina> generarDetallesDiarios(
            List<RegistroAsistenciaModel> registros,
            BigDecimal valorHora,
            BigDecimal valorMinuto) {

        return registros.stream().map(r -> {
            long minutosTrabajados = java.time.Duration.between(r.getHoraEntrada(), r.getHoraSalida()).toMinutes();
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
        dto.setTotalHorasTrabajadas(nomina.getTotalHorasTrabajadas());
        dto.setTotalHorasExtra(nomina.getTotalHorasExtra());
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
}
