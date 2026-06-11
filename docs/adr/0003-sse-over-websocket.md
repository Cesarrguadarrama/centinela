# ADR-0003: Server-Sent Events for the live dashboard

**Status:** Accepted — 2026-06-11

## Context

The dashboard needs a live feed of alerts and metrics. Data flows strictly server → browser; the
browser never pushes data back over the realtime channel (commands go through the REST API).

## Decision

Use **SSE** (`text/event-stream`) instead of WebSocket/STOMP.

## Consequences

- Plain HTTP: no protocol upgrade, works through proxies, `EventSource` reconnects automatically
  with `Last-Event-ID` support.
- Less moving parts than a STOMP broker for a unidirectional feed.
- If a future feature needs browser → server push over the same channel (e.g. collaborative
  triage), this decision must be revisited.
