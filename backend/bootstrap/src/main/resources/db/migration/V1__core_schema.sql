-- Core schema: observed transactions, configurable rules, raised alerts.

CREATE TABLE transactions (
    id                uuid PRIMARY KEY,
    source_clabe      varchar(18) NOT NULL,
    destination_clabe varchar(18) NOT NULL,
    amount            numeric(14, 2) NOT NULL CHECK (amount > 0),
    currency          varchar(3) NOT NULL DEFAULT 'MXN',
    concept           text NOT NULL DEFAULT '',
    ts                timestamptz NOT NULL,
    created_at        timestamptz NOT NULL DEFAULT now()
);

-- Backs the velocity rule: "transfers from this CLABE since <instant>"
CREATE INDEX idx_transactions_source_ts ON transactions (source_clabe, ts DESC);
CREATE INDEX idx_transactions_ts ON transactions (ts DESC);

CREATE TABLE rules (
    id          uuid PRIMARY KEY,
    type        varchar(40) NOT NULL,
    name        varchar(120) NOT NULL UNIQUE,
    description text NOT NULL DEFAULT '',
    enabled     boolean NOT NULL DEFAULT true,
    severity    varchar(16) NOT NULL,
    params      jsonb NOT NULL DEFAULT '{}'::jsonb,
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE alerts (
    id             uuid PRIMARY KEY,
    transaction_id uuid NOT NULL REFERENCES transactions (id),
    rule_id        uuid NOT NULL REFERENCES rules (id),
    rule_name      varchar(120) NOT NULL,
    severity       varchar(16) NOT NULL,
    explanation    text NOT NULL,
    status         varchar(24) NOT NULL DEFAULT 'NEW',
    created_at     timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_alerts_created_at ON alerts (created_at DESC);
CREATE INDEX idx_alerts_status ON alerts (status);
CREATE INDEX idx_alerts_transaction ON alerts (transaction_id);
