# ADR-0007: Writes through use cases, reads as direct SQL projections

**Status:** Accepted — 2026-06-12

## Context

The API has two kinds of operations. Triaging an alert or editing a rule changes state under
domain invariants (an alert never returns to NEW; weights stay 0–100). Listing alerts with
filters, account drill-downs and metric aggregations have no invariants — they are projections
shaped by what the dashboard renders, with pagination and joins.

## Decision

- **Commands** go through inbound ports (`TriageAlertUseCase`, `ManageRulesUseCase`) into the
  framework-free application layer; transition rules live in the domain objects themselves.
- **Queries** are served by `*QueryService` classes in the bootstrap module running parametrized
  SQL into response DTOs directly (CQRS-lite). They never touch domain objects.

## Consequences

- Queries stay one JOIN away from the data and can use Postgres features (window functions,
  `generate_series`) without leaking them into the domain.
- The domain model only grows when behavior grows — not for every screen.
- Trade-off: the read SQL duplicates knowledge of table shapes with JPA entities. Accepted: both
  live in the same module and Flyway owns the schema either way.
