# Documentation

Complete documentation for the RAW.FORM eCommerce project — a full-stack application built with Java 21 + Spring Boot 3.3 and Vue 3.

## Structure

```
docs/
├── api/              ← API endpoint reference and OpenAPI spec
├── architecture/     ← Architecture overview, data flow, auth, deployment
├── decisions/        ← Architecture Decision Records (ADRs)
├── migration/        ← Historical migration documentation (read-only archive)
├── reference/        ← Legacy codebase notes, prompt library
└── runbooks/         ← Operational guides (setup, seeding, incidents, rollback)
```

## Quick Links

### Getting Started
- [Local Setup](runbooks/local-setup.md) — Step-by-step environment setup
- [Spring Setup](runbooks/spring-setup.md) — Backend setup and configuration
- [README (root)](../README.md) — Project overview and quick start

### Architecture
- [Architecture Overview](architecture/README.md) — High-level topology, tech stack, request lifecycle
- [Data Flow](architecture/data-flow.md) — Request lifecycle diagrams
- [Auth Strategy](architecture/auth-strategy.md) — JWT authentication
- [Deployment Model](architecture/deployment-model.md) — Containers, CI/CD, infrastructure

### Decisions
- [ADR Index](decisions/README.md) — All architecture decision records

### API
- [Endpoint Catalog](api/endpoint-catalog.md) — All REST endpoints
- [OpenAPI Spec](api/openapi.yaml) — Machine-readable spec

### Reference
- [Legacy Codebase Notes](reference/legacy-codebase-notes.md) — Historical migration notes

### Runbooks
- [Local Setup](runbooks/local-setup.md)
- [Database Seeding](runbooks/database-seeding.md)
- [Incident Response](runbooks/incident-response.md)
- [Rollback Procedures](runbooks/rollback-procedures.md)