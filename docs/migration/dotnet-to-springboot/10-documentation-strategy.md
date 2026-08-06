# 10. Documentation Update Strategy

## 10.1 Principles
1. **Truth lives in code; docs explain.** No doc claims behavior the code doesn't exhibit. Contract tests (SB-08b) and E2E (SB-08c) are the parity oracle; docs cite them.
2. **Additive, not destructive.** The .NET backend remains the legacy reference until the team explicitly decommissions it. Docs describe both during the transition window, then the .NET sections move to a "Legacy" subsection.
3. **Single source per fact.** Each fact (port, endpoint, env var, run command) is stated in exactly one authoritative doc; others link to it.
4. **Co-located with code where possible.** Spring-specific gotchas (reserved `[Order]` table, raw-int PUT body, GST rounding) are documented in code-adjacent ADRs, not buried in runbooks.
5. **Updated in lockstep with the task that changes the fact,** not in a separate "docs sprint." Every SB-0x task that changes a documented behavior updates the relevant doc in the same PR (or immediately after, in SB-09d).

## 10.2 Documents to create

| New doc | Location | Author task | Content |
|---|---|---|---|
| Spring target state | `docs/architecture/target-state-spring.md` | SB-09d | Spring Boot topology, module layout, security model, data access choice (JPA + JdbcTemplate), how it maps to the existing `.NET` layers. |
| Spring migration report | `docs/migration/dotnet-to-springboot/MIGRATION_REPORT.md` | SB-09d | Outcome: what was migrated, ContractTests + E2E results, known deviations (none expected), carried-forward tech debt (login stub, `FormatAddress`=null). |
| ADR — Adopt Spring Boot | `docs/decisions/ADR-XXX-adopt-spring-boot-backend.md` | SB-09d | Context, decision, consequences, parity constraints from doc 02. |
| Parity audit checklist | `docs/migration/dotnet-to-springboot/parity-checklist.md` | SB-09d | The traps from doc 09 §9.4 as a tick-box checklist, mapped to the test that proves each. |
| Spring runbook | `docs/runbooks/spring-setup.md` | SB-09d | Prereqs (JDK 21, Maven, SQL Server / Docker), `application.yml` env vars, run/test/debug commands, Testcontainers notes. |

## 10.3 Documents to update

| Existing doc | Updates | When |
|---|---|---|
| `README.md` | Add a "Backend (Java Spring Boot)" section alongside the existing .NET section; update the Architecture mermaid diagram to show a Spring Boot node + the .NET node marked "(legacy)"; update "Running the Application" with Java commands; update Prerequisites table (Java 21, Maven 3.9+). | SB-09d (post Phase 6) |
| `docs/architecture/README.md` | Link `target-state-spring.md`; note both backends serve the same API contract. | SB-09d |
| `docs/architecture/monorepo-design.md` | Add `src/backend-java/` to the folder tree; describe its internal package layout. | SB-09d |
| `docs/architecture/auth-strategy.md` | Add a section "Spring Boot JWT" mirroring the .NET JWT description; note identical claim set (`sub`, `customer_id`, `email`, `jti`), 1h expiry, HS256, no iss/aud, ClockSkew=0. Emphasize **no behavioral change**. | SB-09d |
| `docs/architecture/data-flow.md` | Add a Spring request-lifecycle variant (DispatcherServlet → Security filter → Controller → Service → Repository → SQL Server). | SB-09d |
| `docs/architecture/deployment-model.md` | Add the Java Docker image; update the compose to include the Spring service; note port 5001 is now served by Spring. | SB-09d (post SB-09a) |
| `docs/api/endpoint-catalog.md` | Add a top note: "Endpoints are served by the Java Spring Boot backend (`src/backend-java`) and are contract-equivalent to the former .NET backend." No endpoint table changes (parity). | SB-09d |
| `docs/api/openapi.yaml` | Regenerate from `springdoc` (Spring exposes `/v3/api-docs`); diff against the .NET-generated spec; any diff is a parity bug → file back to the owning SB-07 task. | SB-09d |
| `docs/runbooks/local-setup.md` | Add "Backend (Java)" setup steps; keep .NET steps under a "Legacy backend" heading. | SB-09d |
| `docs/runbooks/incident-response.md` | No content change; add a note that logs now come from Spring (Logback) and the health endpoint is unchanged. | SB-09d |
| `docs/migration/README.md` | Link the new `dotnet-to-springboot/` folder. | SB-09d |
| `Makefile` | Document the new `*-java` targets in the `help` target (auto via the `##` convention already in use). | SB-09c |
| `.github/workflows/*` | Comments at top of Java jobs pointing to the Spring runbook. | SB-09b |

## 10.4 Documents NOT to change
- `docs/migration/master-task-list.md`, `progress.md`, `MIGRATION_REPORT.md` (the **legacy** migration report) — historical; leave intact.
- `docs/reference/legacy-codebase-notes.md` — historical.
- Anything under `docs/decisions/` except the new ADR — leave existing ADRs untouched.
- The .NET source under `src/backend/` — frozen as the parity oracle.

## 10.5 Style & formatting rules (carry forward from existing repo)
- Mermaid diagrams for topology.
- Tables for tech stack and endpoints.
- Code blocks for commands and config snippets; label language.
- CamelCase for JSON field references; `backticks` for code identifiers.
- ADRs follow the existing ADR template (Context / Decision / Consequences).
- No emojis (per repo convention).

## 10.6 Review & sign-off
- SB-09d's PR is reviewed by the Coordinator (or a human reviewer) who cross-checks each doc claim against the ContractTests + E2E results from SB-08b/c.
- The parity-checklist.md is the reviewer's working document.
- Docs are considered "done" when: every fact links to either (a) source code, (b) a test, or (c) an ADR; and no doc contradicts a passing test.