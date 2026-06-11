# ADR-0002: Transaction generator as a standalone service

**Status:** Accepted — 2026-06-11

## Context

The platform needs realistic SPEI traffic (plus injectable fraud scenarios) to demonstrate
detection. This could live inside the backend behind a Spring profile, or as its own service.

## Decision

`generator` is an independent Spring Boot application with its own container. It only knows the
domain model and Kafka — it never touches Postgres or Redis.

## Consequences

- Producer and consumer are genuinely decoupled, mirroring how a real SPEI participant would feed
  the detection engine.
- Attack scenarios can be started/stopped without touching the detection engine.
- One extra container and Dockerfile to maintain.
