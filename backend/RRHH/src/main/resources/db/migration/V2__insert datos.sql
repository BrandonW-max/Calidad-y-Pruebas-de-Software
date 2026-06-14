-- =========================
-- ROLES BASE
-- =========================

INSERT INTO rol(nombre_rol, creado_el, actualizado_el, activo)
VALUES ('RRHH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (nombre_rol) DO NOTHING;

INSERT INTO rol(nombre_rol, creado_el, actualizado_el, activo)
VALUES ('GERENCIA', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (nombre_rol) DO NOTHING;

INSERT INTO rol(nombre_rol, creado_el, actualizado_el, activo)
VALUES ('EMPLEADO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (nombre_rol) DO NOTHING;


-- =========================
-- DEPARTAMENTOS BASE
-- =========================

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo)
VALUES ('Recursos Humanos', 'RRHH01', 'Piso 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo)
VALUES ('Dirección General', 'DIR01', 'Piso 5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo)
VALUES ('Emergencia', 'EMG01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;


-- =========================
-- EMPLEADO ADMIN RRHH
-- =========================

INSERT INTO empleado(
    nombres,
    apellidos,
    doc_identidad,
    numero_di,
    fecha_nac,
    sexo,
    estado_civil,
    direccion,
    correo,
    telefono,
    estado,
    creado_el,
    actualizado_el
)
VALUES (
    'Admin',
    'RRHH',
    'DNI',
    '00000001',
    '1995-01-01',
    'M',
    'SOLTERO',
    'Lima',
    'admin.rrhh.personal@gmail.com',
    '999999999',
    'ACTIVO',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
)
ON CONFLICT (numero_di) DO NOTHING;


-- =========================
-- CONTRATO ADMIN RRHH
-- =========================

INSERT INTO contrato(
    cargo,
    tipo_contrato,
    fecha_inicio,
    fecha_fin,
    sueldo,
    estado,
    id_empleado,
    id_dpto,
    creado_el,
    actualizado_el
)
SELECT
    'Administrador RRHH',
    'INDEFINIDO',
    CURRENT_DATE,
    NULL,
    5000.00,
    'ACTIVO',
    e.id_empleado,
    d.id_dpto,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM empleado e
INNER JOIN departamento d ON d.cod_dpto = 'RRHH01'
WHERE e.numero_di = '00000001'
  AND NOT EXISTS (
      SELECT 1
      FROM contrato c
      WHERE c.id_empleado = e.id_empleado
        AND c.estado = 'ACTIVO'
  );


-- =========================
-- USUARIO ADMIN RRHH
-- Contraseña: admin123
-- =========================

INSERT INTO usuario(
    nombre_usuario,
    correo_inst,
    contrasenia,
    intentos_fallidos,
    cuenta_bloqueada,
    fecha_bloqueo,
    dosfa_activo,
    dosfa_secret,
    activo,
    id_empleado,
    id_rol,
    creado_el,
    actualizado_el
)
SELECT
    'admin.rrhh',
    'admin.rrhh@hospitalsangabriel.com',
    '$2b$10$FqG7zov.m5p.vDKZ.G1VqeHnAixs73S9Isob9DaAGf3LJBjAC/Qki',
    0,
    false,
    NULL,
    false,
    NULL,
    true,
    e.id_empleado,
    r.id_rol,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM empleado e
INNER JOIN rol r ON r.nombre_rol = 'RRHH'
WHERE e.numero_di = '00000001'
  AND NOT EXISTS (
      SELECT 1
      FROM usuario u
      WHERE u.nombre_usuario = 'admin.rrhh'
         OR u.correo_inst = 'admin.rrhh@hospitalsangabriel.com'
  );


INSERT INTO registro_asistencia (fecha, hora_entrada, hora_salida, minutos_tardanza, minutos_extra, id_empleado)
SELECT 
    v.fecha, v.hora_entrada, v.hora_salida, v.minutos_tardanza, v.minutos_extra, e.id_empleado
FROM empleado e
CROSS JOIN (VALUES
    ('2026-01-02'::date, '08:00:00'::time, '17:00:00'::time, 0, 30),
    ('2026-01-03', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-05', '08:05:00', '17:00:00', 5, 0),
    ('2026-01-06', '08:00:00', '18:30:00', 0, 90),
    ('2026-01-07', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-08', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-09', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-12', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-13', '08:00:00', '17:00:00', 0, 0),
    ('2026-01-14', '08:00:00', '17:00:00', 0, 0)
) AS v(fecha, hora_entrada, hora_salida, minutos_tardanza, minutos_extra)
WHERE e.numero_di = '00000001';


-- =========================
-- FAMILIA ADMIN RRHH (para bonificación familiar en nómina)
-- =========================

INSERT INTO familia_info(nombres, parentesco, numero_di, fecha_nacimiento, id_empleado, activo)
SELECT 'Maria RRHH', 'HIJO', '11111111', '2018-03-15', e.id_empleado, true
FROM empleado e WHERE e.numero_di = '00000001'
ON CONFLICT (numero_di) DO NOTHING;

INSERT INTO familia_info(nombres, parentesco, numero_di, fecha_nacimiento, id_empleado, activo)
SELECT 'Carlos RRHH', 'HIJO', '22222222', '2020-07-22', e.id_empleado, true
FROM empleado e WHERE e.numero_di = '00000001'
ON CONFLICT (numero_di) DO NOTHING;

-- =========================
-- FERIADOS
-- =========================

INSERT INTO feriado (fecha, descripcion) VALUES
    ('2026-01-01', 'Año Nuevo'),
    ('2026-05-01', 'Día del Trabajo'),
    ('2026-07-28', 'Fiestas Patrias'),
    ('2026-07-29', 'Fiestas Patrias'),
    ('2026-12-25', 'Navidad')
ON CONFLICT (fecha) DO NOTHING;


-- =========================
-- DEPARTAMENTOS ADICIONALES
-- =========================

INSERT INTO departamento(nombre, cod_dpto, ubicacion, creado_el, actualizado_el, activo) VALUES
    ('Enfermería',     'ENF01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Medicina',       'MED01', 'Piso 3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Administración', 'ADM01', 'Piso 2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Laboratorio',    'LAB01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Finanzas',       'FIN01', 'Piso 4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true),
    ('Radiología',     'RAD01', 'Piso 1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
ON CONFLICT (cod_dpto) DO NOTHING;


-- =========================
-- 10 EMPLEADOS (PERSONAL HOSPITALARIO)
-- =========================

INSERT INTO empleado(nombres, apellidos, doc_identidad, numero_di, fecha_nac, sexo, estado_civil, direccion, correo, telefono, estado, creado_el, actualizado_el) VALUES
    ('Ana',    'García López',    'DNI', '12345678', '1990-05-15', 'M', 'CASADO',    'Av. Lima 123, Miraflores',        'ana.garcia@hospital.pe',    '987654321', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Carlos', 'Méndez Torres',   'DNI', '23456789', '1985-08-22', 'H', 'CASADO',    'Jr. Arequipa 456, San Isidro',   'c.mendez@hospital.pe',      '976543210', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('María',  'Quispe Riva',     'DNI', '34567890', '1992-11-30', 'M', 'SOLTERO',   'Av. Tacna 789, Lima',            'm.quispe@hospital.pe',      '965432109', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Luis',   'Fernández Paz',   'DNI', '45678901', '1988-03-10', 'H', 'SOLTERO',   'Calle Miraflores 101, Lince',    'l.fernandez@hospital.pe',   '954321098', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Rosa',   'Vargas Chávez',   'DNI', '56789012', '1995-07-18', 'M', 'SOLTERO',   'Av. Brasil 202, Breña',          'r.vargas@hospital.pe',      '943210987', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Pedro',  'Ramos Soto',      'DNI', '67890123', '1982-12-05', 'H', 'CASADO',    'Jr. Cusco 303, La Victoria',     'p.ramos@hospital.pe',       '932109876', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Elena',  'Castro Neira',    'DNI', '78901234', '1987-04-25', 'M', 'CASADO',    'Av. Salaverry 404, Jesús María', 'e.castro@hospital.pe',      '921098765', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Jorge',  'Huamán Pinto',    'DNI', '89012345', '1990-09-12', 'H', 'SOLTERO',   'Jr. Puno 505, Lima',             'j.huaman@hospital.pe',      '910987654', 'DESVINCULADO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Sofía',  'Delgado Ríos',    'DNI', '90123456', '1998-02-28', 'M', 'SOLTERO',   'Av. Colonial 606, Pueblo Libre', 's.delgado@hospital.pe',     '909876543', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Miguel', 'Paredes Luna',    'DNI', '01234567', '1983-06-14', 'H', 'CASADO',    'Calle Piura 707, Magdalena',     'm.paredes@hospital.pe',     '998765432', 'ACTIVO',       CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (numero_di) DO NOTHING;


-- =========================
-- CONTRATOS (10 EMPLEADOS)
-- =========================

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Enfermera Jefe', 'INDEFINIDO', '2020-03-15', NULL, 3200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '12345678' AND d.cod_dpto = 'ENF01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Médico General', 'INDEFINIDO', '2018-06-01', NULL, 5500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '23456789' AND d.cod_dpto = 'MED01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Administradora', 'PLAZO FIJO', '2021-01-10', '2027-01-10', 3200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '34567890' AND d.cod_dpto = 'ADM01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Técnico de Laboratorio', 'INDEFINIDO', '2019-09-20', NULL, 2400.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '45678901' AND d.cod_dpto = 'LAB01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Enfermera', 'INDEFINIDO', '2022-02-14', NULL, 2800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '56789012' AND d.cod_dpto = 'ENF01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Médico Especialista', 'INDEFINIDO', '2017-11-05', NULL, 7200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '67890123' AND d.cod_dpto = 'MED01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Contadora', 'INDEFINIDO', '2020-07-22', NULL, 3800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '78901234' AND d.cod_dpto = 'FIN01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Enfermero', 'PLAZO FIJO', '2019-04-08', '2023-04-08', 2600.00, 'RESUELTO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '89012345' AND d.cod_dpto = 'ENF01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado);

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Recepcionista', 'INDEFINIDO', '2023-01-03', NULL, 2200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '90123456' AND d.cod_dpto = 'ADM01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Radiólogo', 'INDEFINIDO', '2016-08-15', NULL, 6500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d
WHERE e.numero_di = '01234567' AND d.cod_dpto = 'RAD01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');


-- =========================
-- USUARIOS (10 EMPLEADOS — rol EMPLEADO)
-- Contraseña: admin123
-- =========================

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'a.garcia', 'a.garcia@hospitalsangabriel.com', '$2b$10$w/uWN.m..o/zmaKknuDvCe7RHb4hSXd63Pwz7SvLseanwU6UY8Wwq',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '12345678' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'a.garcia');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'c.mendez', 'c.mendez@hospitalsangabriel.com', '$2b$10$XFy37U71XdxTRwKxuzyiu.eH9Deve05T/xou/Rc1VhSuD9foKlMR6',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '23456789' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'c.mendez');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'm.quispe', 'm.quispe@hospitalsangabriel.com', '$2b$10$OuikuPA2VBb0PPCbQVSE4OqFMTnhej7bOK1SFucfkxhJImPbXGQ2i',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '34567890' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'm.quispe');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'l.fernandez', 'l.fernandez@hospitalsangabriel.com', '$2b$10$1u5k/3C/LW7WTqN6tQ4fyuYUS4I2ffk4KGNpFfMtkMJiyHYTjPYcO',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '45678901' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'l.fernandez');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'r.vargas', 'r.vargas@hospitalsangabriel.com', '$2b$10$75Gy2b2GS0J.DJI.XjTxe.Gg5xwnhWe/8aU9TBqk6gvBNZtyW85eG',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '56789012' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'r.vargas');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'p.ramos', 'p.ramos@hospitalsangabriel.com', '$2b$10$pCphzZWKPbMdem6JtMfX7OhmSX11khgKhQKH/HqWiqFp2xrasmema',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '67890123' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'p.ramos');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'e.castro', 'e.castro@hospitalsangabriel.com', '$2b$10$H5B7AagrBptWHe8p9ytmtuIbwwoSsO6FxMJTw0hF5ZxTgcQop7F/S',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '78901234' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'e.castro');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'j.huaman', 'j.huaman@hospitalsangabriel.com', '$2b$10$M5Y7eof9G0KgKmOsPR1rcuoD5W6utUnoUXJpOG3Iy2GUztYCf1VaG',
       0, false, NULL, false, NULL, false, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '89012345' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'j.huaman');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 's.delgado', 's.delgado@hospitalsangabriel.com', '$2b$10$rXEKDje.fxL6uzdcsuNVDOmq0x6F/fUc1e3113MDuDGy5q1cmikUG',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '90123456' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 's.delgado');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'm.paredes', 'm.paredes@hospitalsangabriel.com', '$2b$10$wYFL5rtKu8XPQ9cZGreYneksRBzVdIPIbZ.nGK2RVOAqkPK1NbJMS',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '01234567' AND r.nombre_rol = 'EMPLEADO'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'm.paredes');


-- =========================
-- 5 EMPLEADOS RRHH
-- =========================

INSERT INTO empleado(nombres, apellidos, doc_identidad, numero_di, fecha_nac, sexo, estado_civil, direccion, correo, telefono, estado, creado_el, actualizado_el) VALUES
    ('Laura',   'Huanca Quispe',    'DNI', '11223300', '1989-03-22', 'M', 'CASADO',  'Av. Universitaria 150, Los Olivos',  'l.huanca@hospital.pe',   '981100001', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Marco',   'Díaz Villanueva',  'DNI', '22334400', '1991-07-14', 'H', 'SOLTERO', 'Jr. Huancavelica 280, Cercado',      'm.diaz@hospital.pe',      '981100002', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Isabel',  'Torres Mamani',    'DNI', '33445500', '1994-11-08', 'M', 'SOLTERO', 'Av. Próceres 440, San Juan de Lurigancho', 'i.torres@hospital.pe', '981100003', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Rodrigo', 'Benítez Lima',     'DNI', '44556600', '1986-05-30', 'H', 'CASADO',  'Calle Las Flores 62, Surco',         'r.benitez@hospital.pe',   '981100004', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Claudia', 'Morales Vásquez',  'DNI', '55667700', '1993-09-17', 'M', 'SOLTERO', 'Av. Angamos 311, Surquillo',         'c.morales@hospital.pe',   '981100005', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (numero_di) DO NOTHING;


-- Contratos RRHH → departamento Recursos Humanos

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Especialista RRHH', 'INDEFINIDO', '2021-03-01', NULL, 3500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '11223300' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Analista RRHH', 'INDEFINIDO', '2022-06-15', NULL, 3200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '22334400' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Asistente RRHH', 'PLAZO FIJO', '2024-01-10', '2026-01-10', 2800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '33445500' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Coordinador RRHH', 'INDEFINIDO', '2019-08-20', NULL, 4200.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '44556600' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Auxiliar RRHH', 'PLAZO FIJO', '2023-09-01', '2025-09-01', 2400.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '55667700' AND d.cod_dpto = 'RRHH01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');


-- Usuarios RRHH (rol RRHH)
-- Contraseña: admin123

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'l.huanca', 'l.huanca@hospitalsangabriel.com', '$2b$10$LjhcIkAFh.HV6uBFfWUyIO6iIp5g6vAutYzWsNzAaGuyCtTKI1e1C',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '11223300' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'l.huanca');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'm.diaz', 'm.diaz@hospitalsangabriel.com', '$2b$10$S5ssXTw0Ta0urpx05Ye/QO13mcrcCJI1niFWEKlwOTq2ybyBp36sm',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '22334400' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'm.diaz');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'i.torres', 'i.torres@hospitalsangabriel.com', '$2b$10$BWqsqcFtcvrBXr8zI6wsL.PbPTJXR/azq84dwI96feJyLgmB549H6',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '33445500' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'i.torres');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'r.benitez', 'r.benitez@hospitalsangabriel.com', '$2b$10$vq0pu5UX5kwUymfCcnIRmOJT9VB8FptccpPFUliLtv/vhA49z7EyS',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '44556600' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'r.benitez');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'c.morales', 'c.morales@hospitalsangabriel.com', '$2b$10$ozVAQTjeu6NKYJrZwtgZNe79s212DeQ8ose7J6PKLkbRUrCUcbutG',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '55667700' AND r.nombre_rol = 'RRHH'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'c.morales');


-- =========================
-- 3 EMPLEADOS GERENCIA
-- =========================

INSERT INTO empleado(nombres, apellidos, doc_identidad, numero_di, fecha_nac, sexo, estado_civil, direccion, correo, telefono, estado, creado_el, actualizado_el) VALUES
    ('Fernando',   'Castillo Ruiz',   'DNI', '66778800', '1975-04-10', 'H', 'CASADO',  'Av. La Molina 520, La Molina',   'f.castillo@hospital.pe',  '991200001', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Patricia',   'Medina Solís',    'DNI', '77889900', '1980-08-25', 'M', 'CASADO',  'Jr. Monte Bello 89, Surco',      'p.medina@hospital.pe',    '991200002', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Alejandro',  'Vargas Flores',   'DNI', '88990011', '1978-12-03', 'H', 'CASADO',  'Av. El Polo 740, Surco',         'a.vargas@hospital.pe',    '991200003', 'ACTIVO', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (numero_di) DO NOTHING;


-- Contratos GERENCIA → departamento Dirección General

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Director General', 'INDEFINIDO', '2015-01-05', NULL, 12000.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '66778800' AND d.cod_dpto = 'DIR01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Subgerente', 'INDEFINIDO', '2018-03-12', NULL, 9500.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '77889900' AND d.cod_dpto = 'DIR01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');

INSERT INTO contrato(cargo, tipo_contrato, fecha_inicio, fecha_fin, sueldo, estado, id_empleado, id_dpto, creado_el, actualizado_el)
SELECT 'Gerente Administrativo', 'INDEFINIDO', '2017-06-20', NULL, 8800.00, 'ACTIVO',
       e.id_empleado, d.id_dpto, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN departamento d WHERE e.numero_di = '88990011' AND d.cod_dpto = 'DIR01'
  AND NOT EXISTS (SELECT 1 FROM contrato c WHERE c.id_empleado = e.id_empleado AND c.estado = 'ACTIVO');


-- Usuarios GERENCIA (rol GERENCIA)
-- Contraseña: admin123

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'f.castillo', 'f.castillo@hospitalsangabriel.com', '$2b$10$IzZT1F77HDUn5nYszFXeRO7Sav4RFPcDCnGUSQcpwD4c/SQyJNoqK',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '66778800' AND r.nombre_rol = 'GERENCIA'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'f.castillo');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'p.medina', 'p.medina@hospitalsangabriel.com', '$2b$10$CAOJiULFFxbiq9bVOGezFOfnmvpl/SaLN2bWWNuX9VuYkS6MG8s/a',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '77889900' AND r.nombre_rol = 'GERENCIA'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'p.medina');

INSERT INTO usuario(nombre_usuario, correo_inst, contrasenia, intentos_fallidos, cuenta_bloqueada, fecha_bloqueo, dosfa_activo, dosfa_secret, activo, id_empleado, id_rol, creado_el, actualizado_el)
SELECT 'a.vargas', 'a.vargas@hospitalsangabriel.com', '$2b$10$XaS9WUSx58M4kYRQnKFrmuLl8BDQMiKmq48jGbT1RIOEPdkgvgLIC',
       0, false, NULL, false, NULL, true, e.id_empleado, r.id_rol, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
FROM empleado e CROSS JOIN rol r WHERE e.numero_di = '88990011' AND r.nombre_rol = 'GERENCIA'
  AND NOT EXISTS (SELECT 1 FROM usuario u WHERE u.nombre_usuario = 'a.vargas');
