# .NET → Java Spring Boot Migration — Planning Artifacts

Complete planning set for migrating the `src/backend` (.NET 10) backend to a Java 21 + Spring Boot 3.3 backend with **full feature parity**. All artifacts are derived solely from the source code; no external architectural input was used.

> Status: **Planning complete. Implementation not started.**

## Index

| # | Artifact | Purpose |
|---|---|---|
| 1 | [Repository Understanding](01-repository-understanding.md) | Architecture, models, DTOs, data access, services, controllers, security, tests, build — the full picture of the .NET backend. |
| 2 | [Migration Summary](02-migration-summary.md) | Goal, scope, .NET→Spring concept mapping, parity constraints, data-access decision, deliverable structure. |
| 3 | [Implementation Plan](03-implementation-plan.md) | Strategy, build tooling, layer translation order, per-layer detail, risk register. |
| 4 | [Dependency-Aware Migration Phases](04-migration-phases.md) | 8 phases (P0–P7), per-phase tasks, parallelization, phase gates G1–G6, critical path. |
| 5 | [Task Breakdown](05-task-breakdown.md) | 34 tasks (SB-01 … SB-09d) with effort, dependencies, files touched. |
| 6 | [Task Dependencies](06-task-dependencies.md) | Dependency matrix, parallelization opportunities, critical path, dependency graph. |
| 7 | [AI Role / Model Assignment](07-ai-role-assignment.md) | Per-task AI role, reasoning tier, orchestration guidance. |
| 8 | [Task Prompts](08-task-prompts.md) | Shared preamble + a self-contained prompt for each of the 34 tasks. |
| 9 | [Verification Strategy](09-verification-strategy.md) | Three verification rings (unit / integration / parity), phase gates, parity traps, 401-vs-403, money rounding, non-determinism, rollback criterion. |
| 10 | [Documentation Update Strategy](10-documentation-strategy.md) | New docs to create, existing docs to update, style rules, review/sign-off. |

## How to use this

1. Start at doc 1 to understand the source.
2. Read doc 2 for the parity contract and decisions.
3. Follow the phases in doc 4; within a phase, dispatch the parallel tasks from doc 5 using the prompts in doc 8 with the role/tier from doc 7.
4. Enforce the gates from doc 4 + doc 9 before advancing.
5. Update docs per doc 10 as each task lands (most concentrated in SB-09d).

## Critical path (fastest end-to-end sequence)
```
SB-01 → SB-02 → SB-04e → SB-06e → SB-07f → SB-08b → SB-09d
```

## Next action (when implementation is authorized)
Open task **SB-01** from doc 08, paste the shared preamble + the SB-01 block into a fresh agent session, and proceed.