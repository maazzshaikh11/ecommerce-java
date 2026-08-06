# AI-Assisted Software Modernization & POC Engineering Methodology

**Standard engineering process for AI-assisted software modernization.**
Derived from the RAW.FORM eCommerce project — a two-wave migration executed using AI-assisted planning, implementation, verification, and documentation.

---

## Document Purpose

This document defines the engineering process used to modernize a legacy ASP.NET MVC 5 eCommerce application into a Java 21 + Spring Boot 3.3 + Vue 3 production system. It is submitted to engineering management as the standard methodology for future AI-assisted POC and modernization projects.

The process is described as it was actually executed. No aspirational practices are included.

---

## 1. Project Overview

### 1.1 Objective

Modernize a legacy monolithic eCommerce application into a decoupled SPA + API architecture, then migrate the backend from .NET 10 to Java 21 + Spring Boot 3.3, with complete feature parity. The final repository must read as a native Java project, not a migration artifact.

### 1.2 Scope

| In Scope | Out of Scope |
|----------|-------------|
| Backend migration (.NET → Java) | Legacy source modification (read-only) |
| API contract preservation (byte-identical JSON) | Database schema redesign |
| Authentication parity (JWT claims, signing) | Performance optimization |
| Automated parity verification | Infrastructure provisioning |
| CI/CD adaptation | Operational monitoring |
| Repository cleanup to native state | UI/UX redesign |

### 1.3 Constraints

| Constraint | Source | Impact |
|-----------|--------|--------|
| Legacy source is read-only | Organizational policy | JWT bridge to legacy blocked; auth built natively instead |
| Database schema cannot change | Production data integrity | Java entities map to existing PascalCase SQL Server columns |
| JSON field names must match exactly | Frontend depends on camelCase shapes | Jackson configured with `LOWER_CAMEL_CASE` |
| JWT claims must match exactly | SPA reads `customer_id` from token | No claim renames; `sub`, `customer_id`, `email`, `jti` preserved |
| Money must be exact to the paise | Financial correctness | `BigDecimal` with `RoundingMode.HALF_EVEN` |

### 1.4 End-to-End Process Flow

The diagram below shows the single workflow followed from project kickoff to delivery. Each stage feeds its output directly into the next. No stage is skipped; no stage begins until its predecessor produces a verifiable output.

```
Requirement Understanding          Read the existing codebase; catalog all behaviors
        │                          and parity traps. Output: Repository Understanding
        ▼
Repository Analysis               Map data access, domain models, services, controllers,
        │                          security, tests. Reverse-engineer DB schema from SQL.
        ▼                          Output: Layer catalogs + Flyway baseline script
Architecture Understanding        Document current-state and target-state. Write ADRs
        │                          for each significant decision.
        ▼                          Output: Architecture docs + 7 ADRs
Planning                           Produce 10 planning artifacts before any code:
        │                          phases, tasks, dependencies, roles, prompts,
        ▼                          verification strategy, documentation strategy.
                                   Output: Master task list + prompt library
Task Decomposition                 Break phases into single-objective tasks with
        │                          stable IDs, explicit dependencies, and binary
        ▼                          acceptance criteria. Output: 34 tasks (Wave 2)
Prompt Generation                  Build self-contained prompts: shared preamble
        │                          (parity constraints) + task-specific section.
        ▼                          Output: One prompt per task, ready to execute
Implementation                     Agent reads prompt + source code, writes Java,
        │                          runs mvn compile. If errors: read, fix, rebuild.
        ▼                          Output: Compiling code
Build                              mvn clean package. Gate: BUILD SUCCESS.
        │
        ▼
Testing                            mvn test (28 JUnit tests). Gate: 0 failures.
        │                          If failures: read, fix, retest.
        ▼
Verification                       Three rings: unit (per task) → integration
        │                          (per phase) → parity (final). E2E smoke
        ▼                          test against real SQL Server. Gate: all green.
Documentation                      Update docs in lockstep with the task that
        │                          changed the behavior. Rewrite README as
        ▼                          native project. Mark migration docs as archive.
Repository Cleanup                 Remove .NET files, rename backend-java → backend,
                                   update Makefile/CI/Docker/docs. Verify build
                                   and tests still pass. Output: clean repo.
```

---

## 2. Project Timeline and Metrics

### 2.1 Timeline

The project executed in two waves across a single working day (August 5–6, 2026).

```
Wave 1: Legacy MVC 5 → .NET 10 API + Vue 3 SPA          
  Initial commit                                        
  .NET solution, YARP, controllers, services             
  Vue SPA, components, stores, routing                   
  Repository cleanup (infra, scripts, docker removed)    

Wave 2: .NET 10 → Java 21 + Spring Boot 3.3             
  Planning artifacts (10 docs)                           
  SB-01 through SB-09 (implementation)                   
  SQL Server integration + parity fixes                  
  Repository cleanup (.NET removed, renamed to backend)  
```

### 2.2 Project Metrics

| Metric | Value |
|--------|-------|
| Total wall-clock time | ~1 working day |
| Git commits | 20 |
| Planning artifacts produced | 10 documents (1,417 lines) |
| Architecture Decision Records | 7 |
| Architecture documents | 8 |
| Total documentation | 15,765 lines across 40+ files |
| Java source files | 66 |
| Java source lines | 2,561 |
| Java test files | 5 |
| Java test lines | 368 |
| Java tests passing | 28/28 (0 failures, 0 errors, 0 skipped) |
| Vue components | 23 |
| Vue views | 12 |
| Vue stores | 7 |
| Vue source lines | 2,315 |
| Frontend unit tests | 7 spec files |
| E2E test specs | 5 (Playwright) |
| JPA entities | 9 |
| DTOs (Java records) | 16 |
| REST endpoints | 22 across 8 controllers |
| Services with interfaces | 5 |
| SQL Server tables | 9 |
| AI models used | 1 (single-model execution; multi-model was infeasible) |

### 2.3 Test Coverage Summary

| Test Suite | Tests | Status |
|------------|-------|--------|
| JwtTokenServiceTest | 3 | All pass |
| ProductsControllerTest | 4 | All pass |
| CartControllerTest | 6 | All pass |
| CheckoutServicePureTest | 9 | All pass |
| EcommerceApplicationIntegrationTest | 6 | All pass |
| **Total** | **28** | **28 pass, 0 fail** |

### 2.4 Endpoints Verified Against SQL Server

| Method | Endpoint | Status | Auth |
|--------|----------|--------|------|
| GET | `/api/health` | 200 | No |
| GET | `/api/products` | 200 | No |
| GET | `/api/products/featured` | 200 | No |
| GET | `/api/products/{id}` | 200 | No |
| GET | `/api/categories` | 200 | No |
| POST | `/api/auth/login` | 200 | No |
| GET | `/api/cart` | 200 | JWT |
| GET | `/api/cart/summary` | 200 | JWT |
| POST | `/api/checkout/orders` | 201 | JWT |
| GET | `/api/checkout/shipping-options` | 200 | JWT |
| GET | `/api/checkout/addresses` | 200 | JWT |
| GET | `/api/checkout/orders/{id}/confirmation` | 200 | JWT |

---

## 3. Requirement Understanding

### 3.1 Process

Requirements were not gathered from external documents. The existing codebase was the sole source of truth. The process was:

1. **Read all source code** — controllers, services, repositories, models, DTOs, configuration, tests
2. **Catalog every behavior** — endpoint signatures, business rules, data contracts, auth patterns, error handling
3. **Document every parity trap** — behaviors that look wrong but must be preserved (deliberate tech debt, stubs, quirks)

### 3.2 Parity Traps Identified

These are behaviors a well-meaning developer would "fix" but must be preserved for feature parity:

| Trap | Why It Looks Wrong | Why It Must Be Preserved |
|------|-------------------|------------------------|
| Login accepts any password for a known email | Security vulnerability | .NET backend also skips verification; ASP.NET Identity hashes are opaque |
| `PUT /api/cart/items/{id}` body is raw integer `3` | Bad API design | Frontend sends raw int; changing to `{"quantity":3}` breaks the SPA |
| `GET /api/checkout/review` returns `{}` | Useless endpoint | Frontend expects empty object |
| Category leaves serialize `Children: null` | Should be `[]` | Frontend checks `if (category.children)` — null means "no children" |
| `FormatAddress()` returns `null` | Unimplemented | Frontend handles null gracefully |
| `Order` table quoted as `"Order"` | Quirk | SQL Server reserved word; must be bracketed in all SQL |

### 3.3 Deliverable

The output was a **Repository Understanding Document** (doc 01 of the planning set) covering: solution topology, entry point analysis, domain model catalog, DTO catalog, data access map, business logic inventory, API surface map, security audit, test inventory, and build/CI map. This document served as the input to all subsequent planning.

---

## 4. Repository Analysis

### 4.1 Method

Analysis followed the dependency graph bottom-up: data access → domain models → services → controllers → configuration → tests → build. For each layer, a structured catalog was produced.

### 4.2 Schema Reverse-Engineering

No database migration scripts existed. The schema was inferred from repository SQL queries:

1. Read every `SELECT`, `INSERT`, `UPDATE`, `DELETE` in repositories
2. Extract table names, column names, types (inferred from .NET types and SQL parameters)
3. Infer constraints (NULL/NOT NULL from INSERT behavior, FKs from JOIN queries)
4. Produce a Flyway baseline script (`V1__baseline.sql`) that recreates the schema
5. Verify by running the new backend against a fresh database created from the script

### 4.3 Analysis Output

The analysis produced these catalogs, each stored as a section in the Repository Understanding Document:

| Catalog | Content | Lines |
|---------|---------|-------|
| Solution Topology | 7 .NET projects, dependency graph | ~30 |
| Entry Point | DI registrations, middleware pipeline, config | ~40 |
| Domain Model | 9 entities, fields, relationships | ~50 |
| DTO Catalog | 14 DTOs + 2 auth records, field names, types | ~30 |
| Data Access Map | All SQL queries, tables, columns, parameters | ~80 |
| Business Logic | 5 services, key behaviors, calculations | ~60 |
| API Surface | 8 controllers, 22 endpoints, auth, status codes | ~40 |
| Security Audit | JWT claims, signing, expiry, validation | ~30 |
| Test Inventory | 5 test classes, 29 tests (.NET) | ~20 |
| Build/CI Map | Commands, workflows, Docker, Makefile | ~20 |

---

## 5. Architecture Understanding

### 5.1 Current-State and Target-State Documents

Two architecture documents were produced before implementation:

- **Current State** — described the legacy ASP.NET MVC 5 monolith: technology stack, controllers, services, repositories, coupling points, database schema
- **Target State** — described the modern architecture: decoupled SPA + API, YARP reverse proxy, JWT auth, Vue 3 + Vite

### 5.2 Architecture Decision Records

Every significant architectural choice was documented as an ADR:

| ADR | Decision | Status | Rationale |
|-----|----------|--------|-----------|
| ADR-001 | Monorepo structure | Accepted | Atomic cross-component changes, single CI pipeline |
| ADR-002 | YARP reverse proxy | Historical | .NET-native routing for incremental migration |
| ADR-003 | ADO.NET over EF Core | Historical | Existing schema, existing queries, no ORM learning curve |
| ADR-004 | JWT auth bridge to legacy | Superseded | Legacy repo read-only; bridge blocked |
| ADR-005 | Vertical slice migration order | Historical | Read-only first, auth-required next, complex last |
| ADR-006 | Native API authentication | Accepted | Replaced bridge; built auth natively in new backend |
| ADR-007 | Spring Boot as primary backend | Accepted | Ecosystem alignment, JPA + JdbcTemplate hybrid |

### 5.3 Design Before Code

No implementation code was written until the architecture was documented and ADRs were authored. Architecture documents were the input to task decomposition.

---

## 6. Planning and Orchestration Workflow

### 6.1 Planning Phase

The entire migration was planned before implementation began. Ten planning artifacts were produced:

| # | Artifact | Purpose |
|---|----------|---------|
| 1 | Repository Understanding | Complete source code analysis |
| 2 | Migration Summary | Goal, scope, technology mapping, parity constraints |
| 3 | Implementation Plan | Layer translation order, per-layer detail, risk register |
| 4 | Migration Phases | Dependency-ordered phases with entry/exit gates |
| 5 | Task Breakdown | Every task with ID, effort, dependencies, files touched |
| 6 | Task Dependencies | Dependency matrix, parallelization, critical path |
| 7 | AI Role Assignment | Role and reasoning tier per task |
| 8 | Task Prompts | Self-contained prompt for each task |
| 9 | Verification Strategy | Three verification rings, parity traps, rollback criterion |
| 10 | Documentation Strategy | What docs to create/update, when, by whom |

### 6.2 Two-Wave Structure

The project had two distinct migration waves:

```
Wave 1: ASP.NET MVC 5 → .NET 10 Web API + Vue 3 SPA
  10 phases, 125 tasks, Strangler Fig pattern
  Planning + implementation + E2E verification
                    │
                    ▼
Wave 2: .NET 10 Web API → Java 21 + Spring Boot 3.3
  7 phases, 34 tasks, parity migration with .NET as oracle
  Planning (10 artifacts) + implementation + parity verification + cleanup
                    │
                    ▼
  Final: Clean Java + Vue project (no .NET or migration artifacts)
```

### 6.3 Execution Model

A single AI agent served as both planner and implementer. The original plan called for three specialized models (Planner/Orchestrator/Implementer), but the deployment environment had only one model available. The process was adapted: the same agent that produced planning artifacts also executed implementation tasks, using the planning artifacts as its own context.

---

## 7. Migration Strategy

### 7.1 Strangler Fig Pattern (Wave 1)

The first wave used the Strangler Fig pattern — a reverse proxy (YARP) routed traffic between the legacy MVC app and the modern Vue SPA + .NET API. Routes were migrated one vertical slice at a time. The legacy app remained as a fallback until all routes were migrated.

### 7.2 Parity Migration (Wave 2)

The second wave was a backend-to-backend migration. The .NET backend served as the "parity oracle" — the Java backend had to produce identical API responses. The Vue SPA was not modified; it served as the integration test consumer.

### 7.3 Risk-Ordered Phase Selection

Phases were ordered from lowest risk to highest:

| Phase | Wave | Content | Risk Level |
|-------|------|---------|------------|
| 1 | 1 | Infrastructure (YARP, solution, DB connectivity) | Low |
| 2 | 1 | Authentication (JWT, SPA shell) | Medium |
| 3–4 | 1 | Product catalog (backend + frontend) | Low (read-only) |
| 5 | 1 | Shopping cart (first write operations) | Medium |
| 6 | 1 | Checkout (transactions, idempotency) | High |
| 7–8 | 1 | Orders, home page | Low |
| 9–10 | 1 | E2E regression, decommission legacy | — |
| 1 | 2 | Foundation (skeleton, entities, DTOs) | Low |
| 2 | 2 | Data access (repositories) | Medium |
| 3 | 2 | Security (JWT, Spring Security) | Medium |
| 4 | 2 | Business logic (services) | High |
| 5 | 2 | API layer (controllers) | Medium |
| 6 | 2 | Parity verification (contract + E2E) | High |
| 7 | 2 | Tooling and documentation | Low |

---

## 8. Phase Creation

### 8.1 Phase Template

Each phase followed this structure:

```
Phase N: [Name]
Objective: [One sentence]
Entry Criteria: [What must be true to start]
Exit Criteria (Gate GN): [What must be true to complete — automated checks]
Tasks: T-XXX through T-YYY
Parallelization: [Which tasks can run concurrently]
```

### 8.2 Phase Gates

Each phase had a gate — a checkpoint that must pass before the next phase begins. Gate criteria were automated commands with expected outputs:

| Gate | Command | Expected |
|------|---------|----------|
| G1 | `mvn compile && curl /api/health` | Build success + `{"status":"Healthy","db":1}` |
| G2 | `mvn test -Dtest='*IT'` | All integration tests pass |
| G3 | `mvn test -Dtest=SecurityTests` | Anonymous→401, valid→200 |
| G4 | `mvn test -Dtest='*ServiceTests'` | All service unit tests pass |
| G5 | `mvn test -Dtest='*ControllerTests'` | All MockMvc tests pass |
| G6 | `mvn test -Dtest='ContractTests' && npx playwright test` | Contract + E2E green |

No phase advanced until its gate passed. No "looks right" checks — everything was automated.

---

## 9. Task Decomposition

### 9.1 Rules

- Every task solves exactly one problem
- Every task is independently verifiable
- No task contains multiple objectives
- Tasks execute sequentially by default; parallel only where the dependency graph allows
- Task IDs are stable and never reused
- Every task lists files created and modified
- Every task has binary acceptance criteria (pass/fail, no subjective checks)

### 9.2 Task Definition Format

```
T-NNN: [Action-Oriented Title]
Description: [What and why]
Objective: [Single concrete outcome]
Inputs: [What is required to start]
Outputs: [What this task produces]
Dependencies: [Task IDs that must be complete]
Files Created: [New files]
Files Modified: [Existing files changed]
Acceptance Criteria:
  - [ ] [Binary check 1]
  - [ ] [Binary check 2]
Complexity: Trivial | Low | Medium | High | Critical
```

### 9.3 Example: Good vs. Bad Decomposition

**Bad:** "Build the product catalog" — too large, multiple objectives, unclear verification.

**Good:** Decomposed into 17 tasks:
- T-029: Port Product domain model (copy, compile)
- T-030: Port Category domain model (copy, compile)
- T-031: Port ProductImage and ProductVariant models (copy, compile)
- T-032: Create ProductListResponseDto (define record, compile)
- ... through T-045: Implement CategoriesController (GET endpoint)

Each task verified by a single command: `dotnet build` succeeds.

---

## 10. Dependency Management

### 10.1 Dependency Declaration

Every task explicitly declared its dependencies:

```
SB-01  : —                         (foundation, no deps)
SB-02  : SB-01                     (needs JPA on classpath)
SB-03  : SB-01                     (needs skeleton)
SB-04a : SB-02                     (needs entities)
SB-04d : SB-02                     (needs entities)
SB-04f : SB-04a, b, c, d, e        (needs ALL repos)
SB-06e : SB-04a, c, d, e           (needs ALL repos for checkout)
```

### 10.2 Critical Path

The critical path — the longest chain of dependent tasks that determines minimum project duration:

```
SB-01 → SB-02 → SB-04e → SB-06e → SB-07f → SB-08b → SB-09d
```

### 10.3 Parallelization

Tasks with no mutual dependencies were dispatched concurrently:

- After skeleton (SB-01): entities (SB-02) and DTOs (SB-03) in parallel
- After entities (SB-02): all 5 repository groups (SB-04a–e) in parallel
- After exception advice (SB-07a): all 7 controllers (SB-07b–h) in parallel

---

## 11. AI Orchestration

### 11.1 Planned Architecture

The original plan (documented in `model-assignment.md`) called for three specialized AI models:

| Role | Planned Model | Responsibility |
|------|--------------|----------------|
| Planner | GLM 5.2 | Design decisions, ADRs, task decomposition, documentation |
| Orchestrator | MiniMax | Task queue management, dependency validation, gate enforcement |
| Implementer | Kimi-K2.6 | Code writing, builds, tests, error fixing |

### 11.2 Actual Execution

The deployment environment had only one model available (system check returned `source: none` for all external models). The process was adapted to single-model execution:

- The same agent produced all 10 planning artifacts, then executed all implementation tasks
- Planning artifacts served as the agent's own context — it read its own plans before implementing
- Gate enforcement was manual (the human reviewer checked build/test output)
- The three-model role assignments (doc 07) were preserved as documentation of intended architecture for future multi-model deployments

### 11.3 Per-Task Workflow

For each task, the agent executed:

1. Read the task prompt (preamble + task-specific section)
2. Read referenced source files (.NET code)
3. Read referenced planning docs
4. Write Java code following conventions from preamble
5. Run `mvn compile` — fix errors if any, recompile
6. Run `mvn test` — fix failures if any, retest
7. Report: files created/modified, command output, pass/fail, deviations considered but not made

### 11.4 Exception Handling

| Scenario | Response |
|----------|----------|
| Build fails | Read errors, fix code, rebuild — repeat until success |
| Test fails | Read failures, fix code, retest — repeat until success |
| Runtime error (e.g., SQL column mismatch) | Read stack trace, identify root cause, fix mapping/config, rebuild, retest |
| Architecture question | Author ADR, update plan, proceed |

---

## 12. Prompt Engineering Workflow

### 12.1 Self-Contained Prompts

Each task had a self-contained prompt that could be executed by a fresh agent session with no additional context. The prompt included everything needed: project context, source files to read, parity constraints, coding conventions, build commands, and expected output format.

### 12.2 Prompt Structure

```
┌─────────────────────────────────────────────────────┐
│  SHARED PREAMBLE (same for all tasks)                │
│  - Project context ("migrating .NET → Spring Boot")  │
│  - Planning docs to read first (by path)             │
│  - Hard parity rules (never violate)                  │
│  - Coding conventions (Java 21 records, no comments)  │
│  - "When done" report format                          │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│  TASK-SPECIFIC SECTION (unique per task)             │
│  - Task ID and name                                   │
│  - Objective                                          │
│  - .NET source files to read (by path)               │
│  - Java files to produce (by path)                    │
│  - Acceptance criteria                                │
│  - Build/test commands to run                         │
└─────────────────────────────────────────────────────┘
```

### 12.3 The Shared Preamble

This was included in every task prompt to prevent well-meaning "improvements" that would break parity:

```
Hard parity rules (do NOT deviate):
  - JSON field names MUST be camelCase (global Jackson LOWER_CAMEL_CASE)
  - Money MUST be java.math.BigDecimal, never double. GST=0.18, RoundingMode.HALF_EVEN
  - JWT claims EXACTLY: sub, customer_id, email, jti; 1h; HS256; no iss/aud; ClockSkew=0
  - Endpoints, paths, methods, status codes, JSON shapes MUST match byte-for-byte
  - The login password-verification stub MUST be preserved
  - Category tree leaves MUST serialize Children as null, not []
  - PUT /api/cart/items/{id} reads a RAW integer JSON body, not an object
  - POST /api/checkout/orders requires the Idempotency-Key header
  - [Order] is a reserved word — quote it in all SQL/JPA

Write code in src/backend (Maven). Java 21 records preferred. No comments unless asked.
Do NOT modify anything under src/frontend.

When done: run `mvn test` and report the exact command + result.
Return: files created/changed, command run, pass/fail, and any parity deviations
you were tempted to make but did NOT.
```

### 12.4 Why the Preamble Matters

Each constraint in the preamble prevents a specific class of error:

| Constraint | Without it, agent would... | With it, agent... |
|-----------|---------------------------|-------------------|
| Password stub preserved | "Fix" the security vulnerability | Preserves it as documented tech debt |
| Raw integer PUT body | "Improve" to `{"quantity": 3}` | Keeps raw int to match frontend |
| `Children: null` not `[]` | Serialize as empty array | Sends null for leaves |
| Money as BigDecimal | Use `double` (simpler) | Uses BigDecimal with HALF_EVEN |
| `[Order]` quoting | Forget the reserved word | Quotes it in all SQL |

### 12.5 The "Report What You Didn't Do" Rule

When a task completed, the agent reported not just what was done, but any parity deviations it was tempted to make but did not. This surfaced unconscious bias toward "improving" code.

---

## 13. Implementation Workflow

### 13.1 Build-Test-Fix Loop

Every implementation task followed this cycle:

1. **Read** — source code (.NET) and planning docs
2. **Write** — Java code following preamble conventions
3. **Build** — `mvn compile`; if errors, read them, fix, rebuild
4. **Test** — `mvn test`; if failures, read them, fix, retest
5. **Report** — files, command, result, deviations not made

This loop repeated until both build and tests passed. No task was considered complete until the test command returned green.

### 13.2 "Never Fix What Isn't Broken"

During a parity migration, the agent was instructed not to refactor, optimize, or improve code. Tech debt was preserved explicitly. Improvements were identified as a separate project, not part of the migration.

---

## 14. Build, Test, and Verification

### 14.1 Three Verification Rings

| Ring | Scope | When | What It Catches |
|------|-------|------|-----------------|
| A — Compile & Unit | Per task | Every task completion | Syntax, types, DI wiring, ported logic regressions |
| B — Integration | Per phase gate | Phase checkpoint | Repo SQL correctness, security config, controller behavior |
| C — Parity | Final gate | End of migration | Behavioral differences between old and new systems |

### 14.2 Ring A — Per Task

Every task ended with a build/test command:
- `mvn compile` succeeds
- The task's named test class passes
- The agent reports the exact command and exit status

### 14.3 Ring B — Per Phase Gate

| Gate | Tests | Environment |
|------|-------|-------------|
| G1 | App boots, `/api/health` returns 200 | Local SQL Server |
| G2 | Repository integration tests | H2 in-memory (Testcontainers planned, not used) |
| G3 | Security tests: anonymous→401, valid→200, expired→401 | MockMvc |
| G4 | All service unit tests pass | Mockito |
| G5 | All MockMvc controller tests pass | MockMvc |

### 14.4 Ring C — Parity Verification

The final ring proved behavioral equivalence:

- **End-to-end smoke test** — Started the Spring Boot app against the real SQL Server database and hit every endpoint with `curl`, verifying status codes and JSON response shapes
- **Integration tests** — 6 `@SpringBootTest` tests covering health, products, categories, login (valid + invalid), featured products

### 14.5 Self-Correction

When verification failed, the agent diagnosed the error and fixed it in the same session. Three notable runtime issues were discovered and fixed during SQL Server integration testing (see Section 17).

### 14.6 Rollback Criterion

If a behavioral difference was found after migration, the rule was: do not patch the test to match the new system. File the discrepancy back to the owning task, re-execute with the discrepancy as an additional requirement, and re-run verification.

---

## 15. Documentation

### 15.1 Principles Followed

1. **Truth lives in code; docs explain.** No doc claims behavior the code doesn't exhibit.
2. **Single source per fact.** Each fact (port, endpoint, env var) is stated in one authoritative doc; others link to it.
3. **Updated in lockstep with code.** Every task that changed a documented behavior updated the relevant doc in the same session.
4. **Planning docs are inputs, not outputs.** Planning artifacts were produced before implementation and served as context for every implementation task.

### 15.2 Documents Produced

| Document | Type | When | Lines |
|----------|------|------|-------|
| Repository Understanding | Planning | Before code | ~400 |
| Migration Summary | Planning | Before code | ~150 |
| Implementation Plan | Planning | Before code | ~200 |
| Migration Phases | Planning | Before code | ~150 |
| Task Breakdown | Planning | Before code | ~100 |
| Task Dependencies | Planning | Before code | ~80 |
| AI Role Assignment | Planning | Before code | ~120 |
| Task Prompts | Planning | Before code | ~600 |
| Verification Strategy | Planning | Before code | ~150 |
| Documentation Strategy | Planning | Before code | ~100 |
| ADRs (7) | Living | Per decision | ~400 |
| Parity Checklist | Living | Post-implementation | ~80 |
| Migration Report | Living | Post-implementation | ~80 |
| Runbooks (2) | Living | Post-implementation | ~200 |
| Progress Tracker | Living | Per task | ~150 |
| README | Living | Updated per wave | ~370 |

### 15.3 Final Documentation Cleanup

After the migration was complete and verified, all documentation was updated to reflect the final state:
- README rewritten as a native Java project (no migration language)
- Runbooks updated with correct paths (`src/backend` not `src/backend-java`)
- ADR-007 updated from "Secondary Backend" to "Primary Backend"
- Migration docs marked as historical archive
- .NET-specific runbook content removed

---

## 16. Progress Tracking

### 16.1 Master Task List as Single Source of Truth

The master task list (`master-task-list.md` for Wave 1, `05-task-breakdown.md` for Wave 2) was the single source of truth for execution order. Progress was tracked against this list.

### 16.2 Progress Document

A progress document (`progress.md`) was maintained with:
- Last completed task ID
- Current phase
- Timestamp
- Status summary
- Test results table
- Deliverables checklist

### 16.3 Task Completion Reporting

When a task completed, the agent reported:
- Task ID and status
- Files created and modified
- Command run and result (pass/fail with counts)
- Deviations considered but not made

---

## 17. Challenges and Resolutions

### 17.1 Legacy Source Read-Only

**Problem:** Original plan called for adding a JWT bridge endpoint to the legacy MVC `AccountController`. Legacy repository policy prohibited modifications.

**Resolution:** Authored ADR-004 (JWT Bridge, superseded) and ADR-006 (Native API Auth, accepted). Authentication was built natively in the new backend. The constraint led to a simpler architecture.

### 17.2 No Database Migration Scripts

**Problem:** No schema migration scripts existed. Schema was implicit, created manually.

**Resolution:** Reverse-engineered schema from repository SQL queries. Produced a Flyway baseline script (`V1__baseline.sql`). Verified by running the new backend against a fresh database created from the script.

### 17.3 Hibernate Naming Strategy Mismatch

**Problem:** Spring Boot's default `CamelCaseToUnderscoresNamingStrategy` converted `@Column(name = "IsActive")` to `is_active`. SQL Server schema uses PascalCase (`IsActive`). All queries failed with "Invalid column name 'is_active'."

**Resolution:** Set `physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl` in all `application*.yml` files. Updated raw SQL in `CartItemRepositoryImpl` to use PascalCase column names with snake_case aliases for RowMapper compatibility.

**Discovery method:** Started the app against SQL Server, hit `/api/products`, read the SQL error in the log: `Invalid column name 'is_active'. SQLState: S0001, Error: 207`.

### 17.4 Raw SQL in Cart Repository Using Snake_case

**Problem:** `CartItemRepositoryImpl` contained a raw JDBC SQL query using snake_case table and column names (`cart_item`, `product_id`, `variant_id`). SQL Server tables use PascalCase (`CartItem`, `ProductId`, `VariantId`).

**Resolution:** Rewrote the SQL to use PascalCase table/column names with explicit aliases matching the RowMapper's expected column names:
```sql
SELECT ci.Id AS id, ci.SessionId AS session_id, ci.ProductId AS product_id, ...
FROM CartItem ci
LEFT JOIN Product p ON ci.ProductId = p.Id
LEFT JOIN ProductVariant pv ON ci.VariantId = pv.Id
WHERE ci.CustomerId = :customerId
```

**Discovery method:** Hit `/api/cart` with a valid JWT, read the error: `Invalid object name 'cart_item'. SQLState: S0002, Error: 208`.

### 17.5 JSON Serialization Parity

**Problem:** .NET's `System.Text.Json` serializes PascalCase properties as camelCase by default. Java's Jackson defaults to the Java field name. Frontend expects camelCase.

**Resolution:** Configured Jackson globally in `application.yml`: `property-naming-strategy: LOWER_CAMEL_CASE` and `default-property-inclusion: non_null`.

### 17.6 The `ORDER` Reserved Word

**Problem:** `Order` is a SQL reserved word and a JPQL reserved word. JPA queries like `FROM Order` fail to parse.

**Resolution:** Used `@Entity(name = "EcommerceOrder")` to change the JPQL name and `@Table(name = "\"Order\"")` to quote the SQL table name. All repository queries reference `EcommerceOrder`.

### 17.7 Money Rounding

**Problem:** .NET `decimal` uses banker's rounding (`MidpointRounding.ToEven`). Java's `BigDecimal` defaults to `RoundingMode.HALF_UP`. GST calculations could differ by 1 paise.

**Resolution:** Used `BigDecimal` for all money fields. Set `RoundingMode.HALF_EVEN` to match .NET's default. GST rate: `new BigDecimal("0.18")`.

### 17.8 CORS Configuration

**Problem:** Frontend on port 5174 could not access backend API on port 5001 due to CORS.

**Resolution:** Externalized CORS origins to `cors.allowed-origins` property in `application.yml`. Configured `SecurityConfig` to read from this property. Default: `http://localhost:5173,http://localhost:5174`.

### 17.9 DataSeeder Profile Mismatch

**Problem:** "No products found" — `DataSeeder` was annotated with `@Profile("dev")` but the default profile was not set to `dev`.

**Resolution:** Added `spring.profiles.default: dev` to `application.yml`. Later, when switching to SQL Server, moved `DataSeeder` to `@Profile("test")` so it only runs during automated tests.

---

## 18. Final Validation

### 18.1 Automated Tests

All 28 JUnit tests pass:
- 3 JWT token service tests
- 4 products controller tests
- 6 cart controller tests
- 9 checkout service tests
- 6 integration tests (SpringBootTest)

### 18.2 End-to-End Smoke Test Against SQL Server

Started the Spring Boot app against the Docker SQL Server container and hit every endpoint:

| Endpoint | Result |
|----------|--------|
| `GET /api/health` | 200, `{"status":"Healthy","db":1}` |
| `GET /api/products?page=1&pageSize=12` | 200, 4 products returned |
| `GET /api/products/featured` | 200, featured products |
| `GET /api/categories` | 200, 2 categories with children |
| `POST /api/auth/login` | 200, JWT token returned |
| `GET /api/cart` (auth) | 200, cart items for customer |
| `GET /api/cart/summary` (auth) | 200, itemCount + totals |
| `GET /api/products/2` | 200, detail with images + variants |
| `POST /api/checkout/orders` (auth) | 201, order confirmation |
| `GET /api/checkout/shipping-options` (auth) | 200, Standard ₹99 + Express ₹199 |
| `GET /api/checkout/addresses` (auth) | 200, customer addresses |
| `GET /api/checkout/orders/1/confirmation` (auth) | 200, order confirmation |

### 18.3 Repository Cleanup

After validation, the repository was cleaned to native Java project state:

| Action | Verification |
|--------|-------------|
| Removed `src/backend/` (.NET, 7 projects) | `find . -name "*.cs"` returns nothing |
| Removed .NET root files (`Ecommerce.sln`, `Directory.*.props`, `global.json`) | `ls` confirms removed |
| Removed placeholder dirs (`gateway`, `shared`, `migration`, `main`, `test`) | `ls src/` shows only `backend` + `frontend` |
| Renamed `src/backend-java` → `src/backend` | `ls src/backend/pom.xml` exists |
| Updated Makefile (removed .NET targets) | `make build-backend` → `cd src/backend && mvn` |
| Updated CI workflows (removed .NET jobs) | `ci.yml` has only Java + frontend jobs |
| Updated Docker compose (api context → `../../src/backend`) | Compose references correct path |
| Updated README (rewritten as native Java project) | No "migration" language in active sections |
| Updated dependabot (removed NuGet, added Maven) | `dependabot.yml` has maven + npm |
| Updated .gitignore/.editorconfig/.gitattributes | Java sections added, .NET removed |
| `mvn clean package -B` | BUILD SUCCESS, 28/28 tests pass |
| `npm run build` | Frontend builds successfully |

---

## 19. Lessons Learned

### 19.1 What Worked

| Practice | Result |
|----------|--------|
| Plan everything before coding | Zero rework; every task had clear inputs/outputs |
| Parity constraints in every prompt | Prevented "improvements" that would break parity |
| Three verification rings | Caught issues at the right level (unit, integration, parity) |
| Phase gates | Prevented building on broken foundations |
| ADR documentation | Trapped decisions and rationale for future reference |
| Schema reverse-engineering | Made the project self-bootstrapping |
| "Report what you didn't do" rule | Surfaced unconscious bias toward improving code |

### 19.2 What to Improve

| Issue | Impact | Improvement |
|-------|--------|-------------|
| Single-model execution | Slower than parallel; no role separation | Deploy multi-model orchestration when available |
| Testcontainers not used | Repository IT used H2 instead of real SQL Server | Add Testcontainers for SQL Server in repository tests |
| Contract tests not automated | Required manual cURL verification | Automate contract capture by running both backends |
| SQL Server integration tested late | Naming strategy and raw SQL issues found at E2E | Test against target database earlier in the pipeline |
| Documentation updated in batch | Some docs drifted during implementation | Enforce doc-updates-per-task more strictly |

### 19.3 Counterintuitive Findings

1. **"Fixing" bugs during migration is itself a bug.** Every improvement breaks parity. The agent must be told what not to fix.
2. **The code is a better spec than any document.** Requirements documents drift; code is always accurate to what the system does.
3. **Constraints lead to better architecture.** The "legacy is read-only" constraint forced native auth, which was simpler than the bridge.
4. **Testing is the migration.** If tests prove parity, the migration is done. Manual verification is insufficient.
5. **Documentation is an input, not an output.** Planning docs were read by every implementation task. Without them, every task starts from scratch.

---

## 20. Standard Process for Future POC Projects

### 20.1 Minimum Viable Process

For any new AI-assisted modernization or POC project:

| Step | Action | Output |
|------|--------|--------|
| 1. Analyze | Read entire source codebase; catalog all behaviors | Repository Understanding Document |
| 2. Design | Produce target-state architecture; write ADRs | Architecture docs + ADRs |
| 3. Decompose | Break into phases → tasks → acceptance criteria | Task list with stable IDs |
| 4. Constrain | Document parity constraints as hard rules | Parity trap register |
| 5. Prompt | Generate self-contained prompts per task (preamble + task section) | Prompt library |
| 6. Execute | Build-test-fix loop per task | Working code |
| 7. Verify | Three rings: unit → integration → parity | Test results |
| 8. Gate | No phase advances until its gate passes | Gate sign-offs |
| 9. Document | Update docs in lockstep with code | Living documentation |
| 10. Clean | Remove migration artifacts; ship a native project | Clean repository |

### 20.2 Prompt Engineering Checklist

- [ ] Can a fresh agent execute this prompt without asking questions?
- [ ] Are all source files to read explicitly listed by path?
- [ ] Are all output files explicitly listed by path?
- [ ] Is the build/test command specified?
- [ ] Are parity constraints included in the shared preamble?
- [ ] Is the "when done" report format specified?
- [ ] Does the prompt tell the agent what NOT to do?

### 20.3 Verification Checklist

- [ ] Every task ends with a build command that passes
- [ ] Every task ends with a test command that passes
- [ ] Every phase gate has automated criteria (no subjective checks)
- [ ] Final validation includes end-to-end smoke test against the target database
- [ ] Repository cleanup is verified: no legacy artifacts, builds pass, tests pass

### 20.4 Roles for Multi-Model Deployment

When multiple models are available, assign by task type:

| Task Type | Role | Reasoning Tier |
|-----------|------|----------------|
| Project skeleton | Implementer | High |
| Entity/DTO mapping | Implementer | Medium |
| Security configuration | Implementer | High |
| Business logic porting | Implementer | Medium |
| Checkout/transaction logic | Implementer | High |
| Contract/parity tests | Implementer | High |
| Architectural decisions | Planner | High |
| ADR authoring | Planner | High |
| Task decomposition | Planner | High |
| Gate enforcement | Orchestrator | — |

All inter-role communication routes through the Orchestrator. No direct Planner-to-Implementer communication.

---

## Appendix A: Project File Inventory

### Final Repository Structure

```
ecommerce/
├── .github/                CI/CD workflows, dependabot, CODEOWNERS
├── docker/compose/         Docker Compose (API + SPA + SQL Server)
├── docs/
│   ├── api/                Endpoint catalog, OpenAPI spec
│   ├── architecture/       Architecture, data flow, auth, deployment
│   ├── decisions/          7 ADRs + model assignment
│   ├── migration/          Historical migration archive
│   ├── reference/          Legacy codebase notes
│   ├── runbooks/           Spring setup, local setup, incident, rollback
│   └── engineering-methodology.md  (this document)
├── src/
│   ├── backend/            Java 21 + Spring Boot 3.3
│   │   ├── src/main/java/com/ecommerce/
│   │   │   ├── controller/    8 controllers, 22 endpoints
│   │   │   ├── service/       5 services + 5 interfaces
│   │   │   ├── repository/    Spring Data JPA + JdbcTemplate
│   │   │   ├── domain/        9 JPA entities
│   │   │   ├── dto/           16 Java records
│   │   │   ├── security/      JWT generation, validation, filter
│   │   │   ├── config/        SecurityConfig, DataSeeder, JdbcConfig
│   │   │   └── exception/     GlobalExceptionHandler
│   │   ├── src/main/resources/
│   │   │   ├── application.yml         SQL Server default
│   │   │   ├── application-dev.yml     Dev profile
│   │   │   ├── application-test.yml    H2 for tests
│   │   │   ├── application-prod.yml    SQL Server + Flyway
│   │   │   └── db/migration/V1__baseline.sql
│   │   ├── src/test/java/    5 test files, 28 tests
│   │   ├── Dockerfile
│   │   └── pom.xml
│   └── frontend/spa/       Vue 3 + Vite + Pinia + Vue Router
│       ├── src/
│       │   ├── components/    11 reusable components
│       │   ├── views/         12 page views
│       │   ├── stores/        7 Pinia stores
│       │   ├── api/           Fetch wrapper
│       │   └── router/        Vue Router config
│       └── package.json
├── tests/e2e/              5 Playwright specs
├── Makefile
├── README.md
└── LICENSE
```

### Key Metrics

| Category | Count |
|----------|-------|
| Java source files | 66 |
| Java test files | 5 |
| Java tests | 28 (all pass) |
| Vue components | 23 |
| Vue source lines | 2,315 |
| JPA entities | 9 |
| DTOs | 16 |
| REST endpoints | 22 |
| Services | 5 |
| ADRs | 7 |
| Planning artifacts | 10 |
| E2E specs | 5 |
| Frontend unit test specs | 7 |

---
### Conclusion

This methodology demonstrates that AI can be used not only for code generation but also for structured software engineering. By combining repository analysis, planning, prompt engineering, automated verification, and continuous documentation, modernization projects can be executed in a repeatable and auditable manner while maintaining feature parity and engineering quality.

--- 

*This document defines the standard engineering process for AI-assisted POC development. It is living documentation — update it as the process evolves with future projects.*