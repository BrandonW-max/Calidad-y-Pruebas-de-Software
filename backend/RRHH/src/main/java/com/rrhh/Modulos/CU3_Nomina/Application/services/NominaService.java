package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Modulos.CU3_Nomina.Application.factory.NominaFactory;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.DetalleNominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.DetalleNomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.entities.Nomina;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Modulos.CU3_Nomina.Domain.repository.INominaRepository;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.entities.Historial;
import com.rrhh.Modulos.CU1_AutenticacionYRol.Domain.repository.IHistorialRepository;
import com.rrhh.Shared.export.pdf.implementations.GenericPDFExporter;
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

    private static final int MINUTOS_EN_HORA = 60;
    private static final int HORAS_JORNADA_DIARIA = 8;

    private final INominaRepository nominaRepository;
    private final IDetalleNominaRepository detalleNominaRepository;
    private final IHistorialRepository historialRepository;
    private final NominaFactory nominaFactory;

    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository;
    private final com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository;
    private final com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository;
    private final com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository;

    public NominaService(
            INominaRepository nominaRepository,
            IDetalleNominaRepository detalleNominaRepository,
            IHistorialRepository historialRepository,
            NominaFactory nominaFactory,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository,
            com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository,
            com.rrhh.Modulos.CU2_GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository,
            com.rrhh.Modulos.CU3_Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository) {
        this.nominaRepository = nominaRepository;
        this.detalleNominaRepository = detalleNominaRepository;
        this.historialRepository = historialRepository;
        this.nominaFactory = nominaFactory;
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

                BigDecimal totalMinutosExtra = registros.stream()
                    .map(r -> BigDecimal.valueOf(r.getMinutosExtra() != null ? r.getMinutosExtra() : 0))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal totalHorasExtra = totalMinutosExtra
                    .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);

                BigDecimal valorHoraDiaria = sueldoBase
                    .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP);

                BigDecimal montoHorasExtra = calcularMontoHorasExtra(totalHorasExtra, valorHoraDiaria);

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

                BigDecimal sueldoNeto = sueldoBruto
                    .subtract(descuentoTardanzas)
                    .setScale(2, RoundingMode.HALF_UP);

                validarPrecision(sueldoBase, montoHorasExtra, bonifFamiliar, bonifTurnoNocturno,
                        bonifGuardia, bonifRiesgo, bonifCargo, sueldoBruto, descuentoTardanzas, sueldoNeto, empleado);

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
                nomina.setDescuentoLey(BigDecimal.ZERO);
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
                                   BigDecimal bruto, BigDecimal descTardanzas, BigDecimal neto,
                                   EmpleadoModel empleado) {
        BigDecimal brutoEsperado = sueldoBase.add(horasExtra).add(fam).add(noct)
                .add(guardia).add(riesgo).add(cargo).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netoEsperado = bruto.subtract(descTardanzas).setScale(2, RoundingMode.HALF_UP);

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
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("Empleado", dto.getNombreEmpleado());
        fila.put("Periodo", dto.getPeriodo());
        fila.put("Sueldo Base", "S/ " + dto.getSueldoBase());
        fila.put("Horas Trabajadas", dto.getTotalHorasTrabajadas());
        fila.put("Horas Extra", dto.getTotalHorasExtra());
        fila.put("Bonif. Familiar", "S/ " + dto.getBonifFamiliar());
        fila.put("Bonif. Nocturno", "S/ " + dto.getBonifTurnoNocturno());
        fila.put("Bonif. Guardia", "S/ " + dto.getBonifGuardia());
        fila.put("Bonif. Riesgo", "S/ " + dto.getBonifRiesgo());
        fila.put("Bonif. Cargo", "S/ " + dto.getBonifCargo());
        fila.put("Desc. Tardanzas", "S/ " + dto.getDescuentoTardanzas());
        fila.put("Sueldo Bruto", "S/ " + dto.getSueldoBruto());
        fila.put("Sueldo Neto", "S/ " + dto.getSueldoNeto());
        fila.put("Estado", dto.getEstadoPago());
        datos.add(fila);

        List<String> headers = new ArrayList<>(fila.keySet());
        String[] headerArray = headers.toArray(new String[0]);
        String[] keyArray = headers.toArray(new String[0]);
        GenericPDFExporter exporter = new GenericPDFExporter();
        return exporter.export("Comprobante de Nomina", datos, headerArray, keyArray);
    }

    private List<EmpleadoModel> obtenerEmpleadosActivos(CalcularNominaRequestDTO request) {
        if (request.getIdEmpleado() != null) {
            return jpaEmpleadoRepository.findById(request.getIdEmpleado())
                .filter(e -> "ACTIVO".equals(e.getEstado()))
                .map(List::of)
                .orElse(List.of());
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
