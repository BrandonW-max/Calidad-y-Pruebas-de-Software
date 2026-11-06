package com.rrhh.Modulos.CU4_RegistroAsistencia.Application.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RecalculoHorasExtraService {

    private static final String CALCULOS = """
        WITH base AS (
            SELECT g.id_registro, g.hora_salida, g.minutos_extra AS actual,
                   (SELECT a.id_horario FROM asignacion_horario a
                     WHERE a.id_empleado = g.id_empleado AND a.activo
                     ORDER BY a.fecha_desde DESC LIMIT 1) AS id_horario
            FROM registro_asistencia g
            WHERE g.hora_salida IS NOT NULL AND g.fecha >= :desde AND g.fecha < :hasta
        ), r AS (
            SELECT b.id_registro, b.hora_salida, b.actual, h.hora_entrada,
                   EXTRACT(EPOCH FROM (b.hora_salida - h.hora_salida)) / 60 AS minutos_despues,
                   (h.hora_salida < h.hora_entrada) AS cruza
            FROM base b JOIN horario h ON h.id_horario = b.id_horario
        ), c AS (
            SELECT id_registro, actual,
                   CASE WHEN cruza AND hora_salida >= hora_entrada THEN 0
                        WHEN minutos_despues < 10 THEN 0
                        ELSE (CEIL(minutos_despues / 60.0) * 60)::int END AS nuevo
            FROM r
        )
        """;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Map<String, Long> recalcular(YearMonth mes, boolean aplicar) {
        Object[] resumen = (Object[]) em.createNativeQuery(CALCULOS + """
            SELECT count(*), count(*) FILTER (WHERE nuevo <> actual),
                   count(*) FILTER (WHERE nuevo > actual), count(*) FILTER (WHERE nuevo < actual)
            FROM c
            """)
            .setParameter("desde", mes.atDay(1))
            .setParameter("hasta", mes.plusMonths(1).atDay(1))
            .getSingleResult();

        long sinHorario = ((Number) em.createNativeQuery("""
            SELECT count(*) FROM registro_asistencia g
            WHERE g.hora_salida IS NOT NULL AND g.fecha >= :desde AND g.fecha < :hasta
              AND NOT EXISTS (SELECT 1 FROM asignacion_horario a WHERE a.id_empleado = g.id_empleado AND a.activo)
            """)
            .setParameter("desde", mes.atDay(1))
            .setParameter("hasta", mes.plusMonths(1).atDay(1))
            .getSingleResult()).longValue();

        if (aplicar) {
            em.createNativeQuery(CALCULOS + """
                UPDATE registro_asistencia g SET minutos_extra = c.nuevo
                FROM c WHERE g.id_registro = c.id_registro AND c.nuevo <> c.actual
                """)
                .setParameter("desde", mes.atDay(1))
                .setParameter("hasta", mes.plusMonths(1).atDay(1))
                .executeUpdate();
        }

        Map<String, Long> resultado = new LinkedHashMap<>();
        resultado.put("registrosConSalida", ((Number) resumen[0]).longValue());
        resultado.put("sinHorario", sinHorario);
        resultado.put("cambiarian", ((Number) resumen[1]).longValue());
        resultado.put("subirian", ((Number) resumen[2]).longValue());
        resultado.put("bajarian", ((Number) resumen[3]).longValue());
        resultado.put("aplicado", aplicar ? 1L : 0L);
        return resultado;
    }
}
