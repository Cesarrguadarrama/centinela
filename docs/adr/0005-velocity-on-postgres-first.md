# ADR-0005: Velocity windows on Postgres first, Redis in phase 3

**Status:** Superseded in phase 3 as planned — `ActivityWindowPort` is now implemented by
`RedisActivityWindowAdapter` (sorted sets, event-time scores); the Postgres COUNT adapter was
removed. — 2026-06-12

## Context

The velocity rule asks "how many transfers did this account send in the last N minutes?" on every
incoming transaction. Phase 3 introduces Redis sliding windows for exactly this kind of query, but
phase 2 needs the rule working with the infrastructure already in place.

## Decision

Phase 2 answers the question with an indexed `COUNT` on the transactions table
(`idx_transactions_source_ts` on `(source_clabe, ts DESC)`), behind the domain port
`AccountActivityPort`. Phase 3 will swap the adapter for Redis sorted-set windows without touching
the rule or the use case.

## Consequences

- The rule ships now with zero new infrastructure; at demo rates (~10 tps) an index-only count is
  sub-millisecond.
- Every transaction adds one read query to Postgres — at higher throughput this couples detection
  latency to the OLTP store, which is the wrong place for hot-path counters.
- The port boundary makes the migration a one-file change and an honest before/after comparison
  for the README metrics section.
