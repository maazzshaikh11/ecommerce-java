# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records (ADRs) for the eCommerce project.

## Index

| ADR | Title | Status |
|-----|-------|--------|
| [ADR-001](001-monorepo-structure.md) | Monorepo Structure | Accepted |
| [ADR-002](002-yarp-reverse-proxy.md) | YARP Reverse Proxy for Ingress | Historical |
| [ADR-003](003-adonet-direct-sql.md) | ADO.NET Direct SQL over ORM | Historical |
| [ADR-004](004-jwt-auth-bridge.md) | JWT Authentication Bridge (Option A) | Superseded |
| [ADR-005](005-vertical-slice-migration.md) | Vertical Slice Migration Order | Historical |
| [ADR-006](006-native-api-auth.md) | Native API Authentication (replaces bridge) | Accepted |
| [ADR-007](007-adopt-spring-boot-backend.md) | Adopt Spring Boot as Primary Backend | Accepted |
| [Model Assignment](model-assignment.md) | Three-model execution system | Reference |

## What is an ADR?

An ADR captures a single architectural decision: the context, the options considered, the decision, and the consequences. ADRs are immutable once accepted — superseded decisions are marked as such and a new ADR is created.

## Format

Each ADR follows:

```markdown
# ADR-NNN: Title

## Status
Accepted | Superseded by ADR-NNN | Deprecated

## Context
Why this decision was needed.

## Decision
What was decided.

## Consequences
What follows from this decision.
```

## Related Documents

- [Architecture Migration Plan](../architecture/architecture-migration-plan.md)
- [Monorepo Design](../architecture/monorepo-design.md)