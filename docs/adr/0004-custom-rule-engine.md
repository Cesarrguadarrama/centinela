# ADR-0004: Lightweight custom rule engine over Drools/Easy Rules

**Status:** Accepted — 2026-06-11

## Context

Fraud rules (sub-threshold amounts, velocity, off-hours, mule accounts, smurfing) must be
configurable at runtime from the database, weighted into a composite 0–100 score, and every alert
must carry an analyst-readable explanation of which rule fired and why.

## Decision

Implement a small rule engine in the domain: a `Rule` port with typed parameters persisted in
Postgres, evaluated per transaction, each evaluation returning a weighted contribution plus a
human-readable explanation.

## Consequences

- Full control over explanations — first-class requirement, not an afterthought.
- No external DSL to learn or version; rules are plain, unit-testable Java + DB rows.
- Trade-off: we own the evaluator. Acceptable for ~5 pattern families; if rule complexity ever
  approaches real decision-table territory, re-evaluate Drools.
