# 03 — Architecture Understanding

**How to document current-state and target-state architecture, and capture decisions as ADRs.**

---

## Prerequisites

- [02-repository-analysis.md](02-repository-analysis.md) complete
- Repository Understanding Document produced

---

## Process

### Step 1: Current-State Document

Describe the legacy architecture as it exists today:

- Technology stack (framework, data access, auth, DI container, client script)
- Controllers and their responsibilities
- Service layer and methods
- Repository layer and data access patterns
- Coupling points (controller-view coupling, auth in pipeline, HttpContext leakage, shared layout state)
- Database schema (tables, relationships)

### Step 2: Target-State Document

Describe the target architecture before implementation begins:

- High-level topology (client → proxy → API/SPA → database)
- Technology mapping (old concept → new equivalent)
- Authentication design
- REST API design
- Component mapping (old view → new component)
- Testing strategy

### Step 3: Architecture Decision Records

Every significant architectural choice is documented as an ADR using this format:

```markdown
# ADR-NNN: Title

## Status
Accepted | Superseded by ADR-NNN | Historical

## Context
Why this decision was needed — the problem, constraints, options considered.

## Decision
What was decided — the specific choice made.

## Consequences
What follows — positive, negative, trade-offs.
```

### Step 4: Design Before Code

No implementation code is written until:
- Current-state document is complete
- Target-state document is complete
- All significant decisions are captured as ADRs

Architecture documents are the input to [04-planning.md](04-planning.md).

---

## ADRs From This Project

| ADR | Decision | Status | Rationale |
|-----|----------|--------|-----------|
| ADR-001 | Monorepo structure | Accepted | Atomic cross-component changes, single CI pipeline |
| ADR-002 | YARP reverse proxy | Historical | .NET-native routing for incremental migration |
| ADR-003 | ADO.NET over EF Core | Historical | Existing schema, existing queries, no ORM learning curve |
| ADR-004 | JWT auth bridge to legacy | Superseded | Legacy repo read-only; bridge blocked |
| ADR-005 | Vertical slice migration order | Historical | Read-only first, auth-required next, complex last |
| ADR-006 | Native API authentication | Accepted | Replaced bridge; built auth natively in new backend |
| ADR-007 | Spring Boot as primary backend | Accepted | Ecosystem alignment, JPA + JdbcTemplate hybrid |

---

## Key Lesson

Constraints lead to better architecture. The "legacy is read-only" constraint blocked the planned JWT bridge (ADR-004), which forced native authentication (ADR-006) — a simpler and cleaner solution. Document the constraint, write the ADR, adapt the plan.