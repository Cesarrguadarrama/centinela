-- Phase 3: composite scoring and the window-based rules (mule, smurfing).

ALTER TABLE transactions ADD COLUMN score integer NOT NULL DEFAULT 0
    CHECK (score BETWEEN 0 AND 100);
CREATE INDEX idx_transactions_score ON transactions (score DESC);

-- Each rule contributes its weight to the transaction's 0-100 composite score
ALTER TABLE rules ADD COLUMN weight integer NOT NULL DEFAULT 25
    CHECK (weight BETWEEN 0 AND 100);

UPDATE rules SET weight = 35 WHERE type = 'SUB_THRESHOLD_AMOUNT';
UPDATE rules SET weight = 40 WHERE type = 'VELOCITY';
UPDATE rules SET weight = 15 WHERE type = 'OFF_HOURS';

INSERT INTO rules (id, type, name, description, enabled, severity, weight, params) VALUES
(
    'a1000000-0000-0000-0000-000000000004',
    'MULE_ACCOUNT',
    'Cuenta mula (recibe y dispersa)',
    'Detecta cuentas que reciben minIncoming depósitos de minDistinctSenders remitentes distintos y dispersan al menos dispersalRatio de lo recibido dentro de windowMinutes.',
    true,
    'CRITICAL',
    60,
    '{"minIncoming": 8, "minDistinctSenders": 5, "dispersalRatio": 0.5, "windowMinutes": 15}'::jsonb
),
(
    'a1000000-0000-0000-0000-000000000005',
    'SMURFING',
    'Montos hormiga',
    'Detecta cuentas que envían minCount o más transferencias menores a maxAmountPesos dentro de windowMinutes acumulando al menos minTotalPesos.',
    true,
    'HIGH',
    45,
    '{"maxAmountPesos": 10000, "minCount": 15, "minTotalPesos": 50000, "windowMinutes": 10}'::jsonb
);
