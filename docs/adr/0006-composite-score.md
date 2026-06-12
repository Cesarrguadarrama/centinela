# ADR-0006: Composite score as a capped sum of DB-configured rule weights

**Status:** Accepted — 2026-06-12

## Context

Analysts need one number to sort the queue by. Each rule fires independently with its own
severity, but "three medium signals on one transfer" should outrank "one medium signal".

## Decision

Every rule row carries a `weight` (0–100). A transaction's score is the **sum of the weights of
the rules that fired, capped at 100**, persisted on the transaction itself. Initial weights:
mule 60, smurfing 45, velocity 40, sub-threshold 35, off-hours 15.

## Consequences

- Tunable in production with an `UPDATE rules SET weight = …` — same philosophy as rule params.
- Simple to explain to an analyst: the score decomposes exactly into the alerts attached to the
  transaction; no opaque model.
- A capped linear sum ignores signal correlation (velocity and smurfing often co-fire). Good
  enough for rule-based scoring; if this evolves into a trained model, the rule matches become
  features and the port boundary stays.

## Alternatives considered

- **Max of severities**: discards corroborating signals; "many weak flags" is precisely the
  smurfing story.
- **Weighted average**: a transaction matching one 60-weight rule would score lower than today
  (60) only if other rules existed — average punishes adding new rules.
