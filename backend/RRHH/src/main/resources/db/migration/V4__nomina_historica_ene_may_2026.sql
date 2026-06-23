-- =============================================================
-- V4: Registros de asistencia y nómina histórica Enero-Mayo 2026
-- Empleados activos con contratos activos
-- =============================================================

-- =========================
-- 1. REGISTRO DE ASISTENCIA (días hábiles Ene–May 2026)
--    Genera ~22 días/mes × 9 empleados = ~990 registros
-- =========================
INSERT INTO registro_asistencia (
    fecha, hora_entrada, hora_salida, horas_trabajadas,
    minutos_tardanza, minutos_extra, estado,
    tipo_registro, tipo_ultimo_registro, observacion, id_empleado
)
SELECT
    d::date,
    '08:00:00'::time,
    '17:00:00'::time,
    8.00,
    0,
    0,
    'PUNTUAL',
    'ORIGINAL',
    'SALIDA',
    'Registro histórico',
    e.id_empleado
FROM generate_series('2026-01-02'::date, '2026-05-31'::date, '1 day'::interval) d
CROSS JOIN empleado e
WHERE e.estado = 'ACTIVO'
  AND EXTRACT(DOW FROM d::date) NOT IN (0, 6)  -- excluir sábados y domingos
  AND NOT EXISTS (
      SELECT 1 FROM registro_asistencia ra
      WHERE ra.id_empleado = e.id_empleado
        AND ra.fecha = d::date
        AND ra.tipo_registro = 'ORIGINAL'
  );

-- Algunos días de tardanza para que la nómina sea más realista
UPDATE registro_asistencia SET
    hora_entrada       = '08:47:00',
    minutos_tardanza   = 47,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-01-14'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('12345678','56789012','90123456'));

UPDATE registro_asistencia SET
    hora_entrada       = '09:15:00',
    minutos_tardanza   = 75,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-02-18'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('23456789','78901234'));

UPDATE registro_asistencia SET
    hora_entrada       = '08:32:00',
    minutos_tardanza   = 32,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-03-05'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('34567890','67890123','01234567'));

UPDATE registro_asistencia SET
    hora_entrada       = '08:55:00',
    minutos_tardanza   = 55,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-04-22'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('45678901','23456789'));

UPDATE registro_asistencia SET
    hora_entrada       = '09:02:00',
    minutos_tardanza   = 62,
    estado             = 'TARDANZA',
    observacion        = 'Tardanza registrada'
WHERE fecha = '2026-05-07'
  AND id_empleado IN (SELECT id_empleado FROM empleado WHERE numero_di IN ('56789012','01234567'));

-- =========================
-- 2. NÓMINA Ene–May 2026 (calculada por el primer usuario RRHH)
--    Fórmula:
--      bruto = sueldo + sueldo*0.15 + 50*dias_laborables + sueldo*0.10 + sueldo*0.20
--      ley   = bruto * 0.13  (ONP)
--      neto  = bruto - descuento_tardanzas - ley
--
--    días laborables por mes:
--      Enero (sin 01/01): 21  Febrero: 20  Marzo: 21  Abril: 22  Mayo: 22
-- =========================
WITH empleados_activos AS (
    SELECT e.id_empleado, c.sueldo
    FROM empleado e
    JOIN contrato c ON c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO'
    WHERE e.estado = 'ACTIVO'
),
meses AS (
    SELECT * FROM (VALUES
        ('2026-01', '2026-01-02'::date, '2026-01-31'::date, 21),
        ('2026-02', '2026-02-01'::date, '2026-02-28'::date, 20),
        ('2026-03', '2026-03-01'::date, '2026-03-31'::date, 21),
        ('2026-04', '2026-04-01'::date, '2026-04-30'::date, 22),
        ('2026-05', '2026-05-01'::date, '2026-05-31'::date, 22)
    ) AS t(periodo, fecha_inicio, fecha_fin, dias_lab)
),
calculo AS (
    SELECT
        m.periodo,
        m.fecha_inicio,
        m.fecha_fin,
        m.dias_lab,
        ea.id_empleado,
        ea.sueldo                                                      AS sueldo_base,
        CAST(m.dias_lab * 8 AS DECIMAL(10,2))                         AS total_horas,
        ROUND(ea.sueldo * 0.15, 2)                                    AS bonif_noct,
        ROUND(50.00 * m.dias_lab, 2)                                  AS bonif_guard,
        ROUND(ea.sueldo * 0.10, 2)                                    AS bonif_riesgo,
        ROUND(ea.sueldo * 0.20, 2)                                    AS bonif_cargo,
        -- Tardanzas reales del mes
        COALESCE((
            SELECT SUM(ra.minutos_tardanza)
            FROM registro_asistencia ra
            WHERE ra.id_empleado = ea.id_empleado
              AND ra.fecha BETWEEN m.fecha_inicio AND m.fecha_fin
              AND ra.tipo_registro = 'ORIGINAL'
        ), 0)                                                          AS total_min_tard
    FROM meses m CROSS JOIN empleados_activos ea
),
con_bruto AS (
    SELECT *,
        ROUND(sueldo_base + bonif_noct + bonif_guard + bonif_riesgo + bonif_cargo, 2) AS bruto,
        -- descuento por minuto = sueldo / 30 / 8 / 60
        ROUND(
            (total_min_tard * sueldo_base) /
            NULLIF((30.0 * 8.0 * 60.0), 0)
        , 2)                                                           AS desc_tardanzas
    FROM calculo
)
INSERT INTO nomina (
    periodo, fecha_inicio, fecha_fin, fecha_emision,
    sueldo_base, total_horas_trabajadas, total_horas_extra, total_minutos_tardanza,
    bonif_familiar, bonif_turno_nocturno, bonif_guardia, bonif_riesgo, bonif_cargo,
    descuento_tardanzas, descuento_ley, asignacion_familiar,
    sueldo_bruto, sueldo_neto,
    cantidad_hijos, tiene_hijos, cantidad_guardias, total_horas_nocturnas,
    tipo_pension_aplicada, estado_pago, version,
    id_empleado, calculado_por
)
SELECT
    cb.periodo,
    cb.fecha_inicio,
    cb.fecha_fin,
    cb.fecha_fin                                                       AS fecha_emision,
    cb.sueldo_base,
    cb.total_horas                                                     AS total_horas_trabajadas,
    0.00                                                               AS total_horas_extra,
    cb.total_min_tard                                                  AS total_minutos_tardanza,
    0.00                                                               AS bonif_familiar,
    cb.bonif_noct                                                      AS bonif_turno_nocturno,
    cb.bonif_guard                                                     AS bonif_guardia,
    cb.bonif_riesgo,
    cb.bonif_cargo,
    cb.desc_tardanzas                                                  AS descuento_tardanzas,
    ROUND(cb.bruto * 0.13, 2)                                         AS descuento_ley,
    0.00                                                               AS asignacion_familiar,
    cb.bruto                                                           AS sueldo_bruto,
    ROUND(cb.bruto - cb.desc_tardanzas - ROUND(cb.bruto * 0.13, 2), 2) AS sueldo_neto,
    0                                                                  AS cantidad_hijos,
    false                                                              AS tiene_hijos,
    cb.dias_lab                                                        AS cantidad_guardias,
    0.00                                                               AS total_horas_nocturnas,
    'ONP'                                                              AS tipo_pension_aplicada,
    'PAGADA'                                                           AS estado_pago,
    1                                                                  AS version,
    cb.id_empleado,
    (SELECT u.id_usuario FROM usuario u
     JOIN rol r ON u.id_rol = r.id_rol
     WHERE r.nombre_rol = 'RRHH' AND u.activo = true
     LIMIT 1)                                                          AS calculado_por
FROM con_bruto cb
WHERE NOT EXISTS (
    SELECT 1 FROM nomina n
    WHERE n.id_empleado = cb.id_empleado AND n.periodo = cb.periodo
);
