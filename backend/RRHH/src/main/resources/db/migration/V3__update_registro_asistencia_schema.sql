ALTER TABLE registro_asistencia
    RENAME COLUMN estado_asistencia TO estado;

ALTER TABLE registro_asistencia
    ADD COLUMN horas_trabajadas DECIMAL(10,2) DEFAULT 0,
    ADD COLUMN tipo_ultimo_registro VARCHAR(20),
    ADD COLUMN observacion VARCHAR(255);
