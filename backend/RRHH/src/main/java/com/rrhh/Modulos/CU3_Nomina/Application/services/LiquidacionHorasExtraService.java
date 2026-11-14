package com.rrhh.Modulos.CU3_Nomina.Application.services;

import com.rrhh.Shared.persistence.RegistroAsistenciaModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class LiquidacionHorasExtraService {

    private static final int MINUTOS_EN_HORA = 60;

    @PersistenceContext
    private EntityManager em;

    // Liquida solo lo que cambió: cada registro aporta sus minutos una vez y las correcciones se guardan como diferencia
    public void liquidarAsistencia(Long idEmpleado, String periodo, List<RegistroAsistenciaModel> registrosValidos, Long idUsuario) {
        Set<Integer> validos = new HashSet<>();
        for (RegistroAsistenciaModel r : registrosValidos) {
            validos.add(r.getIdRegistro());
            int actual = r.getMinutosExtra() != null ? r.getMinutosExtra() : 0;
            int diferencia = actual - minutosDeRegistro(r.getIdRegistro());
            if (diferencia == 0) continue;
            String tipo = existeAsistencia(r.getIdRegistro()) ? "CORRECCION" : "ASISTENCIA";
            LocalDate fin = r.getHoraSalida().isBefore(r.getHoraEntrada()) ? r.getFecha().plusDays(1) : r.getFecha();
            insertar(idEmpleado, periodo, r.getIdRegistro(), tipo, r.getFecha(), fin,
                    r.getHoraEntrada(), r.getHoraSalida(), diferencia, null, idUsuario);
        }

        // Un registro que dejó de ser válido (lo reemplazó una corrección) revierte lo que se liquidó de él
        List<?> liquidados = em.createNativeQuery(
                "SELECT DISTINCT id_registro FROM horas_extra_liquidada "
                + "WHERE id_empleado = :idEmpleado AND periodo = :periodo AND id_registro IS NOT NULL")
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("periodo", periodo)
            .getResultList();
        for (Object o : liquidados) {
            int idRegistro = ((Number) o).intValue();
            int liquidado = minutosDeRegistro(idRegistro);
            if (!validos.contains(idRegistro) && liquidado != 0) {
                insertar(idEmpleado, periodo, idRegistro, "CORRECCION", null, null, null, null,
                        -liquidado, "Registro reemplazado por corrección", idUsuario);
            }
        }
    }

    public void registrarAjuste(Long idEmpleado, String periodo, BigDecimal horas, String motivo, Long idUsuario) {
        int minutos = horas.multiply(BigDecimal.valueOf(MINUTOS_EN_HORA)).setScale(0, RoundingMode.HALF_UP).intValue();
        if (minutos == 0) return;
        insertar(idEmpleado, periodo, null, "AJUSTE_MANUAL", LocalDate.now(), LocalDate.now(), null, null,
                minutos, motivo, idUsuario);
    }

    // Horas de cada turno (y de cada ajuste) del período: el umbral de horas dobles se aplica turno por turno
    public List<BigDecimal> horasPorTurno(Long idEmpleado, String periodo) {
        List<?> filas = em.createNativeQuery(
                "SELECT COALESCE(id_registro::text, 'A' || id_liquidacion), SUM(minutos) FROM horas_extra_liquidada "
                + "WHERE id_empleado = :idEmpleado AND periodo = :periodo GROUP BY 1")
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("periodo", periodo)
            .getResultList();
        List<BigDecimal> resultado = new ArrayList<>();
        for (Object o : filas) {
            resultado.add(horas(((Number) ((Object[]) o)[1]).intValue()));
        }
        return resultado;
    }

    public BigDecimal horasTotales(Long idEmpleado, String periodo) {
        return horas(sumarMinutos(idEmpleado, periodo, false));
    }

    public BigDecimal horasAsistencia(Long idEmpleado, String periodo) {
        return horas(sumarMinutos(idEmpleado, periodo, true));
    }

    // Desglose por horario: un turno (incluso nocturno) es una fila, con sus correcciones sumadas
    public List<Map<String, Object>> detalle(Long idEmpleado, String periodo) {
        List<?> filas = em.createNativeQuery(
                "SELECT id_liquidacion, id_registro, tipo, fecha_inicio, fecha_fin, hora_entrada, hora_salida, minutos, motivo "
                + "FROM horas_extra_liquidada WHERE id_empleado = :idEmpleado AND periodo = :periodo "
                + "ORDER BY id_liquidacion")
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("periodo", periodo)
            .getResultList();

        Map<String, Map<String, Object>> horarios = new LinkedHashMap<>();
        for (Object o : filas) {
            Object[] f = (Object[]) o;
            String clave = f[1] != null ? "R" + f[1] : "A" + f[0];
            Map<String, Object> horario = horarios.computeIfAbsent(clave, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("tipo", f[1] != null ? "TURNO" : "AJUSTE_MANUAL");
                m.put("minutos", 0);
                m.put("tieneCorrecciones", false);
                m.put("motivo", null);
                return m;
            });
            horario.put("minutos", (int) horario.get("minutos") + ((Number) f[7]).intValue());
            if ("CORRECCION".equals(f[2]) && f[1] != null) horario.put("tieneCorrecciones", true);
            if (f[3] != null && horario.get("fechaInicio") == null) {
                horario.put("fechaInicio", aFecha(f[3]));
                horario.put("fechaFin", aFecha(f[4]));
                horario.put("horaEntrada", aHora(f[5]));
                horario.put("horaSalida", aHora(f[6]));
            }
            if (f[8] != null) horario.put("motivo", f[8]);
        }

        List<Map<String, Object>> resultado = new ArrayList<>();
        for (Map<String, Object> h : horarios.values()) {
            h.put("horas", horas((int) h.get("minutos")));
            resultado.add(h);
        }
        resultado.sort(Comparator.comparing(
                (Map<String, Object> h) -> (LocalDate) h.get("fechaInicio"),
                Comparator.nullsLast(Comparator.naturalOrder())));
        return resultado;
    }

    private int sumarMinutos(Long idEmpleado, String periodo, boolean soloAsistencia) {
        Number total = (Number) em.createNativeQuery(
                "SELECT COALESCE(SUM(minutos), 0) FROM horas_extra_liquidada "
                + "WHERE id_empleado = :idEmpleado AND periodo = :periodo"
                + (soloAsistencia ? " AND tipo <> 'AJUSTE_MANUAL'" : ""))
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("periodo", periodo)
            .getSingleResult();
        return total.intValue();
    }

    private int minutosDeRegistro(Integer idRegistro) {
        Number total = (Number) em.createNativeQuery(
                "SELECT COALESCE(SUM(minutos), 0) FROM horas_extra_liquidada WHERE id_registro = :idRegistro")
            .setParameter("idRegistro", idRegistro)
            .getSingleResult();
        return total.intValue();
    }

    private boolean existeAsistencia(Integer idRegistro) {
        Number total = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM horas_extra_liquidada WHERE id_registro = :idRegistro AND tipo = 'ASISTENCIA'")
            .setParameter("idRegistro", idRegistro)
            .getSingleResult();
        return total.longValue() > 0;
    }

    private void insertar(Long idEmpleado, String periodo, Integer idRegistro, String tipo,
                          LocalDate fechaInicio, LocalDate fechaFin, LocalTime horaEntrada, LocalTime horaSalida,
                          int minutos, String motivo, Long idUsuario) {
        em.createNativeQuery("""
            INSERT INTO horas_extra_liquidada
                (id_empleado, periodo, id_registro, tipo, fecha_inicio, fecha_fin, hora_entrada, hora_salida, minutos, motivo, liquidado_por)
            VALUES (:idEmpleado, :periodo, :idRegistro, :tipo, :fechaInicio, :fechaFin, :horaEntrada, :horaSalida, :minutos, :motivo, :idUsuario)
            """)
            .setParameter("idEmpleado", idEmpleado)
            .setParameter("periodo", periodo)
            .setParameter("idRegistro", idRegistro)
            .setParameter("tipo", tipo)
            .setParameter("fechaInicio", fechaInicio)
            .setParameter("fechaFin", fechaFin)
            .setParameter("horaEntrada", horaEntrada)
            .setParameter("horaSalida", horaSalida)
            .setParameter("minutos", minutos)
            .setParameter("motivo", motivo)
            .setParameter("idUsuario", idUsuario)
            .executeUpdate();
    }

    private BigDecimal horas(int minutos) {
        return BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(MINUTOS_EN_HORA), 2, RoundingMode.HALF_UP);
    }

    private static LocalDate aFecha(Object valor) {
        return valor instanceof java.sql.Date d ? d.toLocalDate() : (LocalDate) valor;
    }

    private static LocalTime aHora(Object valor) {
        return valor instanceof java.sql.Time t ? t.toLocalTime() : (LocalTime) valor;
    }
}
