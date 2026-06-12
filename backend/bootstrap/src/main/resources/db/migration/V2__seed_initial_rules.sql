-- Initial rule set. Tuning happens by updating these rows — no redeploy needed.

INSERT INTO rules (id, type, name, description, enabled, severity, params) VALUES
(
    'a1000000-0000-0000-0000-000000000001',
    'SUB_THRESHOLD_AMOUNT',
    'Monto bajo umbral de reporte',
    'Detecta transferencias apenas por debajo del umbral de reporte regulatorio (estructuración). Dispara cuando threshold - margin <= monto < threshold.',
    true,
    'HIGH',
    '{"thresholdPesos": 50000, "marginPesos": 5000}'::jsonb
),
(
    'a1000000-0000-0000-0000-000000000002',
    'VELOCITY',
    'Velocidad anómala de transferencias',
    'Detecta cuentas que exceden maxTransfers transferencias salientes dentro de una ventana de windowMinutes minutos (tiempo de evento).',
    true,
    'HIGH',
    '{"maxTransfers": 10, "windowMinutes": 5}'::jsonb
),
(
    'a1000000-0000-0000-0000-000000000003',
    'OFF_HOURS',
    'Operación en horario atípico',
    'Detecta transferencias entre startHour (inclusive) y endHour (exclusivo) en la zona horaria configurada. La ventana puede cruzar medianoche.',
    true,
    'MEDIUM',
    '{"startHour": 0, "endHour": 5, "timezone": "America/Mexico_City"}'::jsonb
);
