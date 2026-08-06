# 7. AI Role / Model Assignment per Task

Every task is executed by a single AI agent session scoped to that task. The "role" describes the agent's persona and guardrails; the "model tier" reflects reasoning depth needed. All sessions run the same base model family but with different `reasoning_effort` where the harness supports it. If a stronger model is unavailable, fall back one tier (High→Medium→Low) but keep the role.

## Tier key
- **High** — complex, multi-file, judgment-heavy, or high-blast-radius (security, transactions, parity).
- **Medium** — straightforward translation with known patterns.
- **Low** — mechanical, single-file, well-specified.

## Assignment table

| Task | Role | Reasoning tier | Why |
|---|---|---|---|
| SB-01 | **Spring Architect** — bootstraps module structure, build, config, Flyway baseline; owns non-functional defaults. | High | Sets every downstream convention; mistakes propagate. |
| SB-02 | **JPA Mapper** — converts POCOs to entities with exact table/column fidelity; reserved-word handling. | Medium | Mechanical mapping; one tricky spot (`[Order]`). |
| SB-03 | **DTO Transcriber** — records only; no logic; must match JSON shapes. | Low | Pure transcription; verified by contract test later. |
| SB-04a | **Repository Engineer (Catalog)** — Spring Data JPA + custom queries for product/variant/image. | Medium | Standard repos; one join. |
| SB-04b | **Repository Engineer (Category)** — `GetAllWithProducts` in-memory grouping logic. | Medium | Slight logic to port. |
| SB-04c | **Repository Engineer (Identity/Address)** — simple CRUD + lookups. | Low | Trivial repos. |
| SB-04d | **JdbcTemplate Specialist (Cart)** — join query RowMapper returning enriched CartItem. | Medium | Raw-SQL parity matters. |
| SB-04e | **Repository Engineer (Orders)** — batch item fetch + idempotency lookup + unique index. | High | Idempotency correctness + reserved word. |
| SB-04f | **Integration Test Engineer** — Testcontainers + Flyway + seed; proves repos against real SQL Server. | High | Infra setup is fiddly; catches all repo bugs. |
| SB-05a | **Security Engineer (JWT)** — jjwt claim set + expiry + validation parity. | Medium | Bounded, well-specified. |
| SB-05b | **Security Engineer (Spring Security)** — filter chain, path rules, principal, CORS. | High | Misconfig breaks every protected endpoint. |
| SB-05c | **Security Test Engineer** — anonymous/auth/expired cases. | Low | Follows 05b. |
| SB-06a | **Domain Logic Engineer (Catalog)** — pagination + category BFS + sort switch; port tests. | Medium | Logic is already in doc 01; careful port. |
| SB-06b | **Domain Logic Engineer (Category)** — recursive tree build. | Low | Small. |
| SB-06c | **Domain Logic Engineer (Cart)** — merge + ownership; port tests. | Medium | Ownership + qty rules. |
| SB-06d | **Domain Logic Engineer (Orders)** — DTO mapping. | Low | Thin. |
| SB-06e | **Checkout/Transaction Specialist** — `@Transactional`, idempotency, GST BigDecimal, shipping, payment regex. | High | Highest business-criticality; transaction + money. |
| SB-07a | **Error Envelope Engineer** — `@RestControllerAdvice` mapping to parity `{message}`. | Medium | Cross-cutting but small. |
| SB-07b | **API Engineer (Auth)** — login/register/me + stub preservation; MockMvc. | Medium | Must preserve stub behavior. |
| SB-07c | **API Engineer (Products)** — query-param binding; MockMvc. | Medium | Query param parity. |
| SB-07d | **API Engineer (Categories)** — trivial. | Low | Thin controller. |
| SB-07e | **API Engineer (Cart)** — raw-int body quirk + qty validation + summary return shape. | High | The `PUT` raw-int body is a subtle parity trap. |
| SB-07f | **API Engineer (Checkout)** — Idempotency-Key header + 201 + address hardcoding. | High | Idempotency + status code parity. |
| SB-07g | **API Engineer (Orders)** — trivial. | Low | Thin controller. |
| SB-07h | **API Engineer (Health)** — SELECT 1. | Low | Trivial. |
| SB-08a | **Contract Capture Technician** — run .NET app, record responses. | Low | Manual capture; no logic. |
| SB-08b | **Parity Test Engineer** — contract assertions JSON-vs-JSON. | High | Final correctness gate; subtle diffs matter. |
| SB-08c | **E2E Runner** — run Playwright against Spring. | Medium | Env plumbing. |
| SB-08d | **Numerics/Security Auditor** — BigDecimal rounding + JWT + regex spot tests. | Medium | Targeted but precise. |
| SB-09a | **DevOps Engineer (Docker)** — Maven multi-stage image. | Medium | Standard. |
| SB-09b | **DevOps Engineer (CI)** — workflow YAML for Maven. | Medium | Standard. |
| SB-09c | **Build Engineer (Makefile)** — target additions. | Low | Trivial. |
| SB-09d | **Technical Writer** — docs update per doc 10. | Medium | Must preserve accuracy. |

## Orchestration guidance
- Tasks marked High should be reviewed by a human (or a second-agent reviewer pass) before merge.
- Parallel tasks within a phase should be dispatched as **concurrent sub-agent sessions** (see doc 8 prompts), each with a tightly scoped instruction and the relevant slice of docs 1–3 as context.
- A single **Coordinator** session owns the phase gates (G1–G6) and does not write production code.