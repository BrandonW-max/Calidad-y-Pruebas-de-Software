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
VALUES ('Direccion General', 'DIR01', 'Piso 5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, true)
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
    '$2y$10$W.a0C2YO9qMRSElm6abDOeqfJv3cYLC8w5Ee1xdfihvOtywfq2xm6',
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