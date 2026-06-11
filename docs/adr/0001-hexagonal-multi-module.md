# ADR-0001: Hexagonal architecture enforced by Maven modules

**Status:** Accepted — 2026-06-11

## Context

Centinela's detection logic (rules, scoring, alert semantics) must be testable in isolation and
independent of delivery/persistence technology. A package-only separation relies on developer
discipline; nothing stops an accidental `import org.springframework...` inside the domain.

## Decision

Split the backend into Maven modules with one-way dependencies:

```
domain  <--  application  <--  bootstrap (Spring Boot, adapters)
domain  <--  generator    (separate Spring Boot app)
```

`domain` and `application` declare **no framework dependencies** — the compiler fails the build if
infrastructure leaks inward.

## Consequences

- Domain and use-case tests run without Spring context (fast unit tests).
- Slightly more build complexity (parent POM, inter-module versions).
- Adapters (Kafka, JPA, Redis, REST, SSE) all live in `bootstrap`, implementing domain ports.
