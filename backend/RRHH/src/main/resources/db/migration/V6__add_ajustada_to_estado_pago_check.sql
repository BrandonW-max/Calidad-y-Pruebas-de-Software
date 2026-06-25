ALTER TABLE nomina DROP CONSTRAINT IF EXISTS nomina_estado_pago_check;
ALTER TABLE nomina ADD CONSTRAINT nomina_estado_pago_check
    CHECK (estado_pago IN ('PENDIENTE','CALCULADA','DISPONIBLE','PAGADA','AJUSTADA'));
