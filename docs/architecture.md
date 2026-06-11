# Centinela — Architecture

> Living document. Expanded with sequence diagrams and metrics in phase 6.

## System context

```mermaid
flowchart LR
    subgraph traffic [Synthetic traffic]
        GEN[Generator<br/>Spring Boot :8081]
    end

    subgraph streaming [Streaming]
        K[(Kafka<br/>spei.transactions)]
    end

    subgraph detection [Detection platform]
        BE[Backend<br/>Spring Boot :8080<br/>hexagonal core]
        PG[(PostgreSQL<br/>transactions · alerts · rules)]
        RD[(Redis<br/>sliding windows)]
    end

    subgraph ui [Analyst UI]
        FE[Angular dashboard :4200]
    end

    GEN -- transaction events --> K
    K -- consume --> BE
    BE -- persist --> PG
    BE -- velocity windows --> RD
    BE -- "REST + SSE (/api)" --> FE
```

## Backend module graph (hexagonal)

```mermaid
flowchart TD
    BOOT[bootstrap<br/>Spring Boot · adapters in/out] --> APP[application<br/>use cases · rule engine]
    APP --> DOM[domain<br/>model · ports · pure Java]
    GEN2[generator<br/>Spring Boot] --> DOM
```

Dependencies point inward only; `domain` and `application` are framework-free by construction
(see [ADR-0001](adr/0001-hexagonal-multi-module.md)).
