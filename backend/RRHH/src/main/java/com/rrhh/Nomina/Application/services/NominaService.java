package com.rrhh.Nomina.Application.services;

import com.rrhh.Nomina.Application.dto.CalcularNominaRequestDTO;
import com.rrhh.Nomina.Application.dto.DetalleNominaResponseDTO;
import com.rrhh.Nomina.Application.dto.NominaResponseDTO;
import com.rrhh.Nomina.Domain.entities.DetalleNomina;
import com.rrhh.Nomina.Domain.entities.Nomina;
import com.rrhh.Nomina.Domain.repository.IDetalleNominaRepository;
import com.rrhh.Nomina.Domain.repository.INominaRepository;
import com.rrhh.Shared.persistence.ContratoModel;
import com.rrhh.Shared.persistence.EmpleadoModel;
import com.rrhh.Shared.persistence.FamiliaInfoModel;
import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NominaService implements INominaService {

    // Tasas configurables
    private static final BigDecimal TASA_HORA_EXTRA_1_5 = new BigDecimal("1.5");
    private static final BigDecimal TASA_HORA_EXTRA_2_0 = new BigDecimal("2.0");
    private static final int MINUTOS_EN_HORA = 60;
    private static final int HORAS_JORNADA_DIARIA = 8;

    private final INominaRepository nominaRepository;
    private final IDetalleNominaRepository detalleNominaRepository;

    // Repositorios JPA de Shared (inyectados directamente)
    private final com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository;
    private final com.rrhh.Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository;
    private final com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository;
    private final com.rrhh.Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository;

    public NominaService(
            INominaRepository nominaRepository,
            IDetalleNominaRepository detalleNominaRepository,
            com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaEmpleadoRepository jpaEmpleadoRepository,
            com.rrhh.Nomina.Infrastructure.interfaces.JpaRegistroAsistenciaRepository jpaRegistroAsistenciaRepository,
            com.rrhh.GestionEmpleados.Infrastructure.interfaces.JpaContratoRepository jpaContratoRepository,
            com.rrhh.Nomina.Infrastructure.interfaces.JpaFamiliaInfoRepository jpaFamiliaInfoRepository) {
        this.nominaRepository = nominaRepository;
        this.detalleNominaRepository = detalleNominaRepository;
        this.jpaEmpleadoRepository = jpaEmpleadoRepository;
        this.jpaRegistroAsistenciaRepository = jpaRegistroAsistenciaRepository;
        this.jpaContratoRepository = jpaContratoRepository;
        this.jpaFamiliaInfoRepository = jpaFamiliaInfoRepository;
    }

    @Override
    @Transactional
    public List<NominaResponseDTO> calcularNomina(CalcularNominaRequestDTO request, Long idUsuarioRrhh) {

        // 1. Validar rango de fechas
        if (request.getFechaInicio().isAfter(request.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser mayor a la fecha fin.");
        }

        // 2. Obtener empleados activos según filtro
        List<EmpleadoModel> empleados = obtenerEmpleadosActivos(request);

        // 3. Verificar que existan empleados activos
        if (empleados.isEmpty()) {
            throw new IllegalStateException(
                "No se encontraron empleados activos para el periodo seleccionado. " +
                "No es posible realizar el cálculo de nómina."
            );
        }

        String periodo = generarPeriodo(request.getFechaInicio(), request.getFechaFin());
        List<NominaResponseDTO> resultados = new ArrayList<>();

        for (EmpleadoModel empleado : empleados) {

            // 4. Obtener registros de asistencia del periodo
            List<RegistroAsistenciaModel> registros = jpaRegistroAsistenciaRepository
                .findByEmpleadoIdEmpleadoAndFechaBetween(
                    empleado.getIdEmpleado(),
                    request.getFechaInicio(),
                    request.getFechaFin()
                );

            // 5. Validar integridad de datos de asistencia
            validarDatosAsistencia(registros, empleado);

            // 6. Obtener sueldo base del contrato activo
            ContratoModel contrato = jpaContratoRepository
                .findFirstByEmpleadoIdEmpleadoAndEstado(empleado.getIdEmpleado(), "ACTIVO")
                .orElseThrow(() -> new IllegalStateException(
                    "El empleado " + empleado.getNombres() + " no tiene contrato activo."
                ));
            BigDecimal sueldoBase = contrato.getSueldo();

            // 7. Calcular total horas trabajadas
            BigDecimal totalHorasTrabajadas = calcularTotalHorasTrabajadas(registros);

            // 8. Calcular total minutos tardanza
            int totalMinutosTardanza = registros.stream()
                .mapToInt(r -> r.getMinutosTardanza() != null ? r.getMinutosTardanza() : 0)
                .sum();

            // 9. Calcular horas extra y su monto
            BigDecimal totalMinutosExtra = registros.stream()
                .map(r -> BigDecimal.valueOf(r.getMinutosExtra() != null ? r.getMinutosExtra() : 0))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalHorasExtra = totalMinutosExtra
                .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);

            // Valor hora base
            BigDecimal valorHoraDiaria = sueldoBase
                .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP);
            BigDecimal montoHorasExtra = valorHoraDiaria
                .multiply(TASA_HORA_EXTRA_1_5)
                .multiply(totalHorasExtra)
                .setScale(2, RoundingMode.HALF_UP);

            // 10. Calcular descuento por tardanzas
            BigDecimal valorMinuto = sueldoBase
                .divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(HORAS_JORNADA_DIARIA), 4, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 4, RoundingMode.HALF_UP);
            BigDecimal descuentoTardanzas = valorMinuto
                .multiply(BigDecimal.valueOf(totalMinutosTardanza))
                .setScale(2, RoundingMode.HALF_UP);

            // 11. Calcular bonificaciones
            int cantidadHijos = contarHijos(empleado.getIdEmpleado());
            BigDecimal bonifFamiliar = calcularBonifFamiliar(cantidadHijos, sueldoBase);
            BigDecimal bonifTurnoNocturno = BigDecimal.ZERO;
            BigDecimal bonifGuardia = BigDecimal.ZERO;
            BigDecimal bonifRiesgo = BigDecimal.ZERO;
            BigDecimal bonifCargo = BigDecimal.ZERO;

            // 12. Calcular sueldo bruto
            BigDecimal sueldoBruto = sueldoBase
                .add(montoHorasExtra)
                .add(bonifFamiliar)
                .add(bonifTurnoNocturno)
                .add(bonifGuardia)
                .add(bonifRiesgo)
                .add(bonifCargo)
                .setScale(2, RoundingMode.HALF_UP);

            // 13. Calcular sueldo neto
            BigDecimal sueldoNeto = sueldoBruto
                .subtract(descuentoTardanzas)
                .setScale(2, RoundingMode.HALF_UP);

            // 14. Generar detalles por día
            List<DetalleNomina> detalles = generarDetallesDiarios(
                registros, valorHoraDiaria, valorMinuto
            );

            // 15. Construir entidad Nomina
            Nomina nomina = new Nomina();
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

            // 16. Guardar nómina y detalles
            Nomina nominaGuardada = nominaRepository.guardar(nomina);
            detalles.forEach(d -> d.setIdNomina(nominaGuardada.getIdNomina()));
            detalleNominaRepository.guardarTodos(detalles);

            // 17. Mapear a response
            resultados.add(mapearAResponse(nominaGuardada, empleado));
        }

        return resultados;
    }

    @Override
    public NominaResponseDTO buscarNominaPorId(Integer idNomina) {
        Nomina nomina = nominaRepository.buscarPorId(idNomina)
            .orElseThrow(() -> new IllegalArgumentException("Nómina no encontrada con id: " + idNomina));
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

    // ================================
    // MÉTODOS PRIVADOS
    // ================================

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

    private BigDecimal calcularBonifFamiliar(int cantidadHijos, BigDecimal sueldoBase) {
        if (cantidadHijos == 0) return BigDecimal.ZERO;
        // 10% del sueldo base por hijo (configurable)
        return sueldoBase.multiply(new BigDecimal("0.10"))
            .multiply(BigDecimal.valueOf(cantidadHijos))
            .setScale(2, RoundingMode.HALF_UP);
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
            BigDecimal montoExtra = valorHora.multiply(TASA_HORA_EXTRA_1_5)
                .multiply(BigDecimal.valueOf(minutosExtra))
                .divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);

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
        return dto;
    }
}