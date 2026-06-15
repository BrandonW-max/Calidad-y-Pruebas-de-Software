-- ============================================================
-- V3: DATOS DE PRUEBA ADICIONALES
-- Asistencias, Justificaciones, Solicitudes, Historial
-- ============================================================


-- ============================================================
-- A) REGISTROS DE ASISTENCIA — Mayo 2026
-- Para empleados: Ana García, Carlos Méndez, Rosa Vargas, Elena Castro, Sofía Delgado, Miguel Paredes
-- ============================================================

-- Ana García (12345678) — Enfermera Jefe
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:45:00',       '17:45:00',       9.00,          45, 'TARDANZA'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '09:10:00',       '17:00:00',       7.83,          70, 'TARDANZA'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:30:00',       '17:30:00',       9.00,          30, 'TARDANZA'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '12345678'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Carlos Méndez (23456789) — Médico General
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:50:00',       '17:50:00',       9.00,          50, 'TARDANZA'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '09:05:00',       '17:00:00',       7.92,          65, 'TARDANZA'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '23456789'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Rosa Vargas (56789012) — Enfermera
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '09:30:00',       '17:00:00',       7.50,          90, 'TARDANZA'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:15:00',       '17:15:00',       9.00,          15, 'TARDANZA'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '56789012'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Elena Castro (78901234) — Contadora
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:40:00',       '17:40:00',       9.00,          40, 'TARDANZA'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '09:00:00',       '17:00:00',       8.00,          60, 'TARDANZA'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '78901234'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Sofía Delgado (90123456) — Recepcionista
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-12',       '08:20:00',       '17:20:00',       9.00,          20, 'TARDANZA'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '09:45:00',       '17:00:00',       7.25,          105,'TARDANZA'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '90123456'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);

-- Miguel Paredes (01234567) — Radiólogo
INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado, tipo_registro, id_empleado)
SELECT v.fecha, v.hora_entrada, v.hora_salida, v.horas_trabajadas, v.minutos_tardanza, v.estado, 'ORIGINAL', e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-05-02'::date, '08:00:00'::time, '17:00:00'::time, 9.00::decimal, 0,  'PUNTUAL'),
    ('2026-05-05',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-06',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-07',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-08',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-09',       '08:35:00',       '17:35:00',       9.00,          35, 'TARDANZA'),
    ('2026-05-12',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-13',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-14',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-15',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-16',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-19',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-20',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-21',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-22',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-23',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-26',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-27',       '09:20:00',       '17:00:00',       7.67,          80, 'TARDANZA'),
    ('2026-05-28',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-29',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL'),
    ('2026-05-30',       '08:00:00',       '17:00:00',       9.00,          0,  'PUNTUAL')
) AS v(fecha, hora_entrada, hora_salida, horas_trabajadas, minutos_tardanza, estado)
WHERE e.numero_di = '01234567'
  AND NOT EXISTS (SELECT 1 FROM registro_asistencia ra WHERE ra.id_empleado = e.id_empleado AND ra.fecha = v.fecha);


-- ============================================================
-- B) JUSTIFICACIONES PENDIENTES (referencian registros con tardanza)
-- ============================================================

-- Justificación de Ana García por tardanza del 2026-05-05 (08:45 = 45 min tardanza)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Tuve un accidente de tránsito en la Av. Arequipa que bloqueó el tráfico durante más de una hora. Adjunto la constancia policial.',
    'PENDIENTE', '2026-05-06 08:30:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '12345678' AND ra.fecha = '2026-05-05'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Ana García por tardanza del 2026-05-07 (09:10 = 70 min tardanza)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Mi hijo tuvo una emergencia médica en la madrugada y tuve que llevarlo a urgencias. Tengo el parte médico del Hospital Rebagliati.',
    'PENDIENTE', '2026-05-08 07:45:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '12345678' AND ra.fecha = '2026-05-07'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Carlos Méndez por tardanza del 2026-05-06 (08:50 = 50 min)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'El servicio de tren eléctrico sufrió una interrupción desde la estación Grau hasta las 08:40. Tengo el comprobante de retraso emitido por el Metro de Lima.',
    'PENDIENTE', '2026-05-07 08:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '23456789' AND ra.fecha = '2026-05-06'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Carlos Méndez por tardanza del 2026-05-12 (09:05 = 65 min)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Corte de luz en mi domicilio desde las 06:00 hasta las 08:50 según Enel. El ascensor del edificio no funcionó y tuve que bajar 12 pisos a pie con mis equipos médicos.',
    'PENDIENTE', '2026-05-13 07:30:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '23456789' AND ra.fecha = '2026-05-12'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Rosa Vargas por tardanza del 2026-05-08 (09:30 = 90 min)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Paro de transporte público en el distrito de Breña. Ningún bus pasó por mi zona durante más de dos horas. Tuve que caminar hasta la Av. Brasil para tomar taxi.',
    'PENDIENTE', '2026-05-09 08:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '56789012' AND ra.fecha = '2026-05-08'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Elena Castro por tardanza del 2026-05-07 (08:40 = 40 min)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Acudí al RENIEC para renovación urgente de DNI previo a un trámite bancario. Me indicaron el día anterior que debía presentarme a primera hora. Tengo el comprobante del trámite.',
    'PENDIENTE', '2026-05-08 07:55:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '78901234' AND ra.fecha = '2026-05-07'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Elena Castro por tardanza del 2026-05-21 (09:00 = 60 min)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Cita médica de control pre-programada en el Hospital Loayza a las 07:00. La consulta se extendió más de lo esperado por resultados de laboratorio pendientes. Adjunto el carné de atención médica.',
    'PENDIENTE', '2026-05-22 08:10:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '78901234' AND ra.fecha = '2026-05-21'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Sofía Delgado por tardanza del 2026-05-23 (09:45 = 105 min)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Accidente de tránsito en el cruce de Av. Colonial y Av. Universitaria bloqueó completamente la vía durante más de 90 minutos según informó RPP. Tengo capturas de pantalla de las noticias.',
    'PENDIENTE', '2026-05-24 09:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '90123456' AND ra.fecha = '2026-05-23'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);

-- Justificación de Miguel Paredes por tardanza del 2026-05-27 (09:20 = 80 min)
INSERT INTO justificacion_tardanza (id_registro_asistencia, id_empleado, motivo, estado, fecha_justificacion)
SELECT ra.id_registro, ra.id_empleado,
    'Falla en el sistema de agua del edificio donde resido causó inundación en el estacionamiento. Tuve que esperar al técnico de mantenimiento para sacar mi vehículo. Tengo el reporte de la administración del edificio.',
    'PENDIENTE', '2026-05-28 08:00:00'::timestamp
FROM registro_asistencia ra
INNER JOIN empleado e ON e.id_empleado = ra.id_empleado
WHERE e.numero_di = '01234567' AND ra.fecha = '2026-05-27'
  AND NOT EXISTS (SELECT 1 FROM justificacion_tardanza j WHERE j.id_registro_asistencia = ra.id_registro);


-- ============================================================
-- C) SOLICITUDES DE PERMISOS (PENDIENTE, APROBADA, RECHAZADA)
-- ============================================================

-- Ana García — Vacaciones APROBADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-04-07', '2026-04-18', 10,
    'Solicito 10 días de vacaciones anuales para viajar con mi familia a Cusco. Es el único período disponible según coordinación con mi jefa inmediata.',
    'APROBADA', 'Vacaciones aprobadas según programación anual. Disfrute su descanso.',
    e.id_empleado, '2026-03-25 10:00:00'::timestamp, '2026-03-20 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '12345678'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-04-07');

-- Ana García — Permiso Personal PENDIENTE
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-06-20', '2026-06-20', 1,
    'Necesito permiso de un día para atender la graduación de mi hijo en la Universidad Nacional Mayor de San Marcos. Evento el 20 de junio a las 10:00 am.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '12345678'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-20');

-- Carlos Méndez — Permiso Médico APROBADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_MEDICO', '2026-03-10', '2026-03-12', 3,
    'Requiero 3 días de reposo por intervención quirúrgica de rodilla derecha (menisco). Adjunto indicación del traumatólogo Dr. Ramírez del Hospital Rebagliati.',
    'APROBADA', 'Permiso médico aprobado. Preséntese con certificado médico al reincorporarse.',
    e.id_empleado, '2026-03-08 14:00:00'::timestamp, '2026-03-07 11:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '23456789'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-03-10');

-- Carlos Méndez — Vacaciones PENDIENTE
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'VACACIONES', '2026-07-14', '2026-07-25', 10,
    'Solicito mis vacaciones anuales correspondientes al período julio 2026. Coordiné previamente con el jefe de Medicina para la cobertura durante mi ausencia.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '23456789'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-07-14');

-- María Quispe — Otros RECHAZADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'OTROS', '2026-04-01', '2026-04-02', 2,
    'Solicito 2 días para asistir a un congreso de administración hospitalaria en Arequipa. El evento es organizado por la SUNEDU.',
    'RECHAZADA', 'Solicitud no aprobada. No hay presupuesto asignado para este tipo de eventos en el período. Puede coordinar para el próximo ejercicio.',
    e.id_empleado, '2026-03-28 09:30:00'::timestamp, '2026-03-27 16:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '34567890'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-04-01');

-- María Quispe — Permiso Personal PENDIENTE
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-06-25', '2026-06-25', 1,
    'Permiso para trámites de notaría por fallecimiento de familiar. Liquidación de herencia requiere presencia física en notaría.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '34567890'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-25');

-- Luis Fernández — Permiso Médico RECHAZADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_MEDICO', '2026-05-19', '2026-05-19', 1,
    'Cita odontológica de urgencia. Absceso dental que no permite trabajar con normalidad.',
    'RECHAZADA', 'La solicitud se recibió extemporáneamente. Para permisos médicos de urgencia debe comunicarse directamente con RRHH. Se recomienda programar citas fuera del horario laboral.',
    e.id_empleado, '2026-05-20 08:00:00'::timestamp, '2026-05-18 17:30:00'::timestamp
FROM empleado e WHERE e.numero_di = '45678901'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-05-19');

-- Luis Fernández — Vacaciones APROBADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-02-02', '2026-02-13', 10,
    'Solicito vacaciones anuales. Tengo 10 días pendientes del período anterior.',
    'APROBADA', 'Vacaciones aprobadas. Coordine entrega de pendientes antes de su salida.',
    e.id_empleado, '2026-01-25 11:00:00'::timestamp, '2026-01-22 10:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '45678901'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-02-02');

-- Rosa Vargas — Permiso Personal PENDIENTE
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-06-18', '2026-06-18', 1,
    'Solicito un día de permiso para acompañar a mi madre a cirugía programada en el Hospital Guillermo Almenara. No hay nadie más que pueda acompañarla.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '56789012'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-18');

-- Pedro Ramos — Vacaciones APROBADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-01-05', '2026-01-16', 10,
    'Vacaciones del período diciembre 2025 - enero 2026 según cronograma establecido por jefatura.',
    'APROBADA', 'Aprobado según programación anual de vacaciones del área de Medicina.',
    e.id_empleado, '2025-12-20 10:00:00'::timestamp, '2025-12-15 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '67890123'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-01-05');

-- Pedro Ramos — Otros PENDIENTE
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'OTROS', '2026-06-30', '2026-07-01', 2,
    'Solicito 2 días para capacitación externa en manejo de equipos de imagen de última generación. El curso es dictado por Phillips Medical en Lima.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '67890123'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-06-30');

-- Elena Castro — Permiso Médico APROBADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_MEDICO', '2026-04-22', '2026-04-23', 2,
    'Reposo médico por cuadro viral agudo con diagnóstico de faringitis bacteriana. El médico indica 2 días de reposo.',
    'APROBADA', 'Permiso médico aprobado. Adjunte descanso médico al reincorporarse.',
    e.id_empleado, '2026-04-21 20:00:00'::timestamp, '2026-04-21 19:30:00'::timestamp
FROM empleado e WHERE e.numero_di = '78901234'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-04-22');

-- Sofía Delgado — Vacaciones PENDIENTE
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, id_empleado, creado_el)
SELECT 'VACACIONES', '2026-07-07', '2026-07-11', 5,
    'Solicito 5 días de vacaciones para la primera quincena de julio. Coordiné con la jefa de recepción para mi reemplazo.',
    'PENDIENTE', e.id_empleado, CURRENT_TIMESTAMP
FROM empleado e WHERE e.numero_di = '90123456'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-07-07');

-- Miguel Paredes — Permiso Personal RECHAZADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'PERMISO_PERSONAL', '2026-05-02', '2026-05-02', 1,
    'Necesito un día para asistir a diligencia judicial citada por el Poder Judicial. Tengo que dar declaración como testigo.',
    'RECHAZADA', 'La notificación judicial debe presentarse a RRHH con 5 días de anticipación para procesar el permiso. Presente la documentación para el registro.',
    e.id_empleado, '2026-04-30 16:00:00'::timestamp, '2026-04-29 14:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '01234567'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-05-02');

-- Miguel Paredes — Vacaciones APROBADA
INSERT INTO solicitud (tipo_solicitud, fecha_inicio, fecha_fin, dias_solicitados, motivo, estado, respuesta, id_empleado, fecha_decision, creado_el)
SELECT 'VACACIONES', '2026-03-16', '2026-03-27', 10,
    'Solicito mis 10 días de vacaciones anuales para el mes de marzo según cronograma del área de Radiología.',
    'APROBADA', 'Vacaciones aprobadas. Asegúrese de dejar los equipos calibrados antes de salir.',
    e.id_empleado, '2026-03-05 10:00:00'::timestamp, '2026-03-03 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '01234567'
  AND NOT EXISTS (SELECT 1 FROM solicitud s WHERE s.id_empleado = e.id_empleado AND s.fecha_inicio = '2026-03-16');


-- ============================================================
-- D) HISTORIAL DE AUDITORIA POR EMPLEADO
-- ============================================================

-- Creación de empleados (evento inicial)
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO',
    e.id_empleado,
    'CREAR_EMPLEADO',
    NULL,
    'Empleado registrado: ' || e.nombres || ' ' || e.apellidos ||
        ' | DNI: ' || e.numero_di ||
        ' | Estado: ACTIVO',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    e.creado_el
FROM empleado e
WHERE e.numero_di IN ('12345678','23456789','34567890','45678901','56789012','67890123','78901234','89012345','90123456','01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'CREAR_EMPLEADO'
  );

-- Asignación de cargo inicial (vinculado a contrato)
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO',
    e.id_empleado,
    'ASIGNACION_CARGO',
    'Sin cargo',
    'Cargo asignado: ' || c.cargo ||
        ' | Tipo contrato: ' || c.tipo_contrato ||
        ' | Sueldo: S/ ' || c.sueldo ||
        ' | Dpto: ' || d.nombre,
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    c.fecha_inicio::timestamp
FROM empleado e
INNER JOIN contrato c ON c.id_empleado = e.id_empleado
INNER JOIN departamento d ON d.id_dpto = c.id_dpto
WHERE e.numero_di IN ('12345678','23456789','34567890','45678901','56789012','67890123','78901234','89012345','90123456','01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'ASIGNACION_CARGO'
  );

-- Cambios de área y cargo para algunos empleados
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'ACTUALIZAR_DATOS',
    'Dirección anterior registrada en sistema',
    'Actualización de datos personales: dirección, teléfono y correo actualizados según solicitud del empleado.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    (e.creado_el + interval '6 months')
FROM empleado e
WHERE e.numero_di IN ('12345678', '34567890', '67890123', '90123456')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'ACTUALIZAR_DATOS'
  );

-- Ascensos registrados para algunos empleados
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'REGISTRAR_ASCENSO',
    'Cargo anterior: ' || c.cargo,
    'Nuevo cargo: ' || c.cargo || ' (confirmado en puesto) | Sueldo actualizado a: S/ ' || (c.sueldo * 1.15)::decimal(10,2),
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    (e.creado_el + interval '18 months')
FROM empleado e
INNER JOIN contrato c ON c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO'
WHERE e.numero_di IN ('12345678', '23456789', '67890123', '01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'REGISTRAR_ASCENSO'
  );

-- Cambio salarial para algunos empleados
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'CAMBIO_SALARIAL',
    'Sueldo anterior: S/ ' || c.sueldo,
    'Nuevo sueldo: S/ ' || (c.sueldo * 1.08)::decimal(10,2) || ' | Motivo: Incremento anual por evaluación de desempeño',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    (e.creado_el + interval '24 months')
FROM empleado e
INNER JOIN contrato c ON c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO'
WHERE e.numero_di IN ('12345678', '23456789', '34567890', '45678901', '56789012', '67890123', '78901234', '90123456', '01234567')
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'CAMBIO_SALARIAL'
  );

-- Desvinculación de Jorge Huamán
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'DESACTIVAR_EMPLEADO',
    'Estado: ACTIVO | Cargo: Enfermero',
    'Estado: DESVINCULADO | Motivo: Fin de contrato a plazo fijo. Contrato vencido el 08/04/2023. No renovación por reestructuración del área de Enfermería.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    '2023-04-09 09:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '89012345'
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'DESACTIVAR_EMPLEADO'
  );

-- Sanción registrada para un empleado (ejemplo)
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'REGISTRAR_SANCION',
    'Sin sanciones previas',
    'Sanción: AMONESTACION_VERBAL | Motivo: Tardanzas reiteradas durante el mes de enero 2026. Se registran 4 tardanzas superiores a 30 minutos.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    '2026-02-05 10:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '90123456'
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'REGISTRAR_SANCION'
  );

-- Cambio de área para un empleado
INSERT INTO historial_auditoria (tabla_afectada, id_registro_afectado, accion, valor_anterior, valor_nuevo, id_usuario, fecha_de_cambio)
SELECT
    'EMPLEADO', e.id_empleado, 'CAMBIO_AREA',
    'Área anterior: Administración',
    'Nueva área: Administración (reasignación interna) | Nuevo cargo: Recepcionista Senior | Motivo: Reorganización del área de atención al paciente.',
    (SELECT u.id_usuario FROM usuario u WHERE u.nombre_usuario = 'admin.rrhh' LIMIT 1),
    '2025-03-01 08:00:00'::timestamp
FROM empleado e WHERE e.numero_di = '90123456'
  AND NOT EXISTS (
    SELECT 1 FROM historial_auditoria ha
    WHERE ha.tabla_afectada = 'EMPLEADO' AND ha.id_registro_afectado = e.id_empleado AND ha.accion = 'CAMBIO_AREA'
  );
