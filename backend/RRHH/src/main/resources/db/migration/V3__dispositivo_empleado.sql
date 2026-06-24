CREATE TABLE dispositivo_empleado (
    id_dispositivo BIGSERIAL PRIMARY KEY,
    id_empleado    BIGINT NOT NULL REFERENCES empleado(id_empleado) ON DELETE CASCADE,
    device_token   VARCHAR(255) NOT NULL,
    registrado_el  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_empleado)
);
