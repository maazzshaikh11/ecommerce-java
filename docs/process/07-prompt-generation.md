# 07 — Prompt Generation

**How to build self-contained prompts that prevent well-meaning "improvements" from breaking parity.**

---

## Prerequisites

- [05-task-breakdown.md](05-task-breakdown.md) complete
- [01-requirement-understanding.md](01-requirement-understanding.md) — parity traps identified

---

## Principle

> Each task gets a self-contained prompt that can be executed by a fresh agent session with zero additional context. The prompt includes everything needed: project context, source files to read, parity constraints, coding conventions, build commands, and expected output format.

---

## Prompt Structure

```mermaid
block-beta
    columns 1
    block:pre["SHARED PREAMBLE (same for all tasks)"]
        A["Project context ('migrating .NET → Spring Boot')"]
        B["Planning docs to read first (by path)"]
        C["Hard parity rules (never violate)"]
        D["Coding conventions (Java 21 records, no comments)"]
        E["'When done' report format"]
    end
    --> arrow[""]
    block:task["TASK-SPECIFIC SECTION (unique per task)"]
        F["Task ID and name"]
        G["Objective"]
        H["Source files to read (by path)"]
        I["Output files to produce (by path)"]
        J["Acceptance criteria"]
        K["Build/test commands to run"]
    end
```

> **Note:** Each prompt = shared preamble + one task-specific section. The preamble is identical for every task; only the task section changes.

---

## The Shared Preamble

Included in every task prompt to prevent well-meaning "improvements":

```
You are migrating the backend of an eCommerce app from .NET 10 to Java 21 + Spring Boot 3.3.

Source of truth for behavior: the .NET code under src/backend.
Read these planning docs first and treat them as authoritative:
  docs/migration/dotnet-to-springboot/01-repository-understanding.md
  docs/migration/dotnet-to-springboot/02-migration-summary.md
  docs/migration/dotnet-to-springboot/03-implementation-plan.md

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

---

## Why the Preamble Matters

Each constraint prevents a specific class of error:

| Constraint | Without it, agent would... | With it, agent... |
|-----------|---------------------------|-------------------|
| Password stub preserved | "Fix" the security vulnerability | Preserves it as documented tech debt |
| Raw integer PUT body | "Improve" to `{"quantity": 3}` | Keeps raw int to match frontend |
| `Children: null` not `[]` | Serialize as empty array | Sends null for leaves |
| Money as BigDecimal | Use `double` (simpler) | Uses BigDecimal with HALF_EVEN |
| `[Order]` quoting | Forget the reserved word | Quotes it in all SQL |

---

## Prompt Quality Checklist

- [ ] Can a fresh agent execute this prompt without asking questions?
- [ ] Are all source files to read explicitly listed by path?
- [ ] Are all output files explicitly listed by path?
- [ ] Is the build/test command specified?
- [ ] Are parity constraints included in the shared preamble?
- [ ] Is the "when done" report format specified?
- [ ] Does the prompt tell the agent what NOT to do?

---

## The "Report What You Didn't Do" Rule

When a task completes, the agent reports not just what was done, but any parity deviations it was tempted to make but did not. This surfaces unconscious bias toward "improving" code.

Example:
```
Deviations I was tempted to make but did NOT:
  - Tempted to add @JsonIgnore to Product.isActive to hide it from API responses
    → Did NOT because .NET serializes it and the SPA doesn't check it
  - Tempted to add validation annotations (@NotNull on Product.name)
    → Did NOT because .NET has no validation and adding it changes behavior
```