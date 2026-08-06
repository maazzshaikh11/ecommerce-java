# 01 — Requirement Understanding

**How to extract requirements when migrating an existing system.**

---

## Principle

> The code is the spec. No external requirements document is used. All requirements are reverse-engineered from the existing source code. What the code actually does IS the requirement — not what was planned, not what was documented.

---

## Process

1. **Read all source code** — controllers, services, repositories, models, DTOs, configuration, tests
2. **Catalog every behavior** — endpoint signatures, business rules, data contracts, auth patterns, error handling
3. **Document every parity trap** — behaviors that look wrong but must be preserved (deliberate tech debt, stubs, quirks)

---

## Parity Trap Register

A parity trap is a behavior that a well-meaning developer would "fix" but must be preserved for feature parity. Identifying these before implementation starts is the most important output of this phase.

| Trap | Why It Looks Wrong | Why It Must Be Preserved |
|------|-------------------|------------------------|
| Login accepts any password for a known email | Security vulnerability | .NET backend also skips verification; ASP.NET Identity hashes are opaque |
| `PUT /api/cart/items/{id}` body is raw integer `3` | Bad API design | Frontend sends raw int; changing to `{"quantity":3}` breaks the SPA |
| `GET /api/checkout/review` returns `{}` | Useless endpoint | Frontend expects empty object |
| Category leaves serialize `Children: null` | Should be `[]` | Frontend checks `if (category.children)` — null means "no children" |
| `FormatAddress()` returns `null` | Unimplemented | Frontend handles null gracefully |
| `Order` table quoted as `"Order"` | Quirk | SQL Server reserved word; must be bracketed in all SQL |

---

## Deliverable

The output is a **Repository Understanding Document** covering:

| Section | Content |
|---------|---------|
| Solution Topology | Projects, dependency graph, target frameworks |
| Entry Point | DI registrations, middleware pipeline, configuration |
| Domain Model | Every entity, field, type, relationship |
| DTO Catalog | Every DTO, field name, JSON serialization behavior |
| Data Access Map | Every SQL query, table, column, parameter |
| Business Logic | Every service method, decision branch, calculation |
| API Surface | Every controller, route, method, auth, status code |
| Security | Auth mechanism, token format, claims, validation rules |
| Test Inventory | Every test class, test count, what they cover |
| Build/CI | Build commands, CI workflows, Docker, Makefile |

This document is the input to [02-repository-analysis.md](02-repository-analysis.md) and all subsequent planning.

---

## "Fixing" Bugs During Migration Is Itself a Bug

Every "improvement" breaks parity. The agent must be told what not to fix. Parity traps are encoded as hard constraints in every task prompt (see [07-prompt-generation.md](07-prompt-generation.md)).

Tech debt is preserved explicitly. Improvements are a separate project, not part of the migration.