# Centinela

Real-time fraud detection platform for SPEI (Mexico's interbank transfer system) transactions.

> **Status:** Phase 0 — repository scaffold, infrastructure and CI. Full README with architecture
> diagrams, live attack demo and metrics lands in phase 6.

## Quick start

```bash
cp .env.example .env
docker compose up --build
```

| Service   | URL                                    |
| --------- | -------------------------------------- |
| Backend   | http://localhost:8080/actuator/health |
| Generator | http://localhost:8081/actuator/health |
| Frontend  | http://localhost:4200                  |

## Local development

Backend requires JDK 21 (`brew install openjdk@21`):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
cd backend && mvn verify
```

Infrastructure only (run apps from your IDE):

```bash
docker compose up kafka postgres redis
```

Architecture decisions are documented in [docs/adr/](docs/adr/).
