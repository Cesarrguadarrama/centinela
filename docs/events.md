# Event contracts

## `spei.transactions`

One JSON event per SPEI transfer. Produced by the generator, consumed by the detection engine.
**Key:** source CLABE (per-account ordering within a partition). No type headers — this schema is
the contract, not a Java class name.

```json
{
  "id": "5f1c9a4e-8b3d-4e2a-9c7f-1d2e3f4a5b6c",
  "sourceClabe": "012180001234567895",
  "destinationClabe": "002010777777777719",
  "amount": 4512.3,
  "currency": "MXN",
  "concept": "Pago renta",
  "timestamp": "2026-06-11T17:24:31.123456Z"
}
```

| Field              | Type   | Notes                                          |
| ------------------ | ------ | ---------------------------------------------- |
| `id`               | string | UUID, unique per transaction                   |
| `sourceClabe`      | string | 18 digits, valid check digit                   |
| `destinationClabe` | string | 18 digits, valid check digit                   |
| `amount`           | number | MXN, 2 decimal places, always > 0              |
| `currency`         | string | Always `MXN` (SPEI)                            |
| `concept`          | string | Free-text payment concept, may be empty        |
| `timestamp`        | string | ISO-8601 UTC. May differ from ingestion time — |
|                    |        | off-hours scenarios stamp small-hours times    |
