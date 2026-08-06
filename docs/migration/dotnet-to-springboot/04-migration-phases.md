# 4. Dependency-Aware Migration Phases

Phases are ordered so that each phase's inputs are produced by earlier phases. No phase starts until its dependency phase is **compiled + unit-tested green**. Within a phase, tasks may run in parallel where the dependency graph (doc 6) allows.

## Phase 0 — Discovery & Planning (DONE)
- Artifacts 1–10 generated. No code.

## Phase 1 — Foundation (serial)
**Goal:** A Spring Boot app that boots, connects to SQL Server, and serves `/api/health`.

| Task | Depends on | Produces |
|---|---|---|
| SB-01 Project skeleton, pom.xml, application.yml, Flyway baseline V1 | — | `src/backend-java/`, bootable app, `/api/health` 200 |
| SB-02 Domain entities (9) + JPA mappings | SB-01 (entities need JPA on classpath) | `domain/*.java` |
| SB-03 DTO records (14 + 2 auth) | SB-01 (no runtime dependency on SB-02; can start once skeleton exists) | `dto/*.java` |

> SB-02 and SB-03 may run **in parallel** after SB-01.

**Gate G1:** `mvn compile` succeeds; app boots against SQL Server; `/api/health` returns 200 with `{status:Healthy, db:1}` shape.

## Phase 2 — Data Access (serial within, parallel repos)
**Goal:** All repositories return correct data from the real DB.

| Task | Depends on | Produces |
|---|---|---|
| SB-04a Repositories: Product, ProductVariant, ProductImage | SB-02 | Spring Data JPA interfaces + JdbcTemplate queries |
| SB-04b Repositories: Category | SB-02 | |
| SB-04c Repositories: Customer, Address | SB-02 | |
| SB-04d Repositories: CartItem (incl. join query) | SB-02 | |
| SB-04e Repositories: Order, OrderItem (incl. batch fetch, idempotency lookup) | SB-02 | |
| SB-04f Repository integration tests against Testcontainers SQL Server + Flyway baseline | SB-04a..e | `Repository*Tests` |

> SB-04a–e run in parallel; SB-04f runs after all repos exist.

**Gate G2:** Repository integration tests green against a real SQL Server container seeded from Flyway V1 + a seed script mirroring the existing dev DB data.

## Phase 3 — Security (serial)
**Goal:** JWT issuance + Spring Security wired; protected endpoints reject anonymous, accept valid token.

| Task | Depends on | Produces |
|---|---|---|
| SB-05a `JwtTokenService` (encode/decode) + unit tests | SB-03 (claims types), SB-01 (config) | `security/JwtTokenService` + tests |
| SB-05b `SecurityConfig`, `JwtAuthConverter`, principal carrying `customer_id` | SB-05a | `config/SecurityConfig` |
| SB-05c Security integration test (public vs protected) | SB-05b, SB-01 (health controller only) | `SecurityTests` |

**Gate G3:** Anonymous → 401 on `/api/cart`; valid JWT → 200; expired/malformed → 401.

## Phase 4 — Business Logic (parallel by service)
**Goal:** All services behave identically to .NET (validated by ported unit tests).

| Task | Depends on | Produces |
|---|---|---|
| SB-06a `ProductService` + ported tests | SB-04a,b (repos), SB-03 (DTOs) | |
| SB-06b `CategoryService` + ported tests | SB-04b | |
| SB-06c `CartService` + ported tests | SB-04d, SB-04a | |
| SB-06d `OrderService` + ported tests | SB-04e | |
| SB-06e `CheckoutService` + ported tests (idempotency, GST, shipping, payment regex) | SB-04a,d,e,c (Address repo) | |

> SB-06a–e run in parallel. **SB-06e depends on all five repo groups**, so it starts once Phase 2 is fully green.

**Gate G4:** All service unit tests pass (port of `ProductServiceTests`, `CartServiceTests`, plus new tests for `CategoryService`, `OrderService`, `CheckoutService`).

## Phase 5 — API Layer (parallel by controller after advice exists)
**Goal:** All endpoints exposed with parity status codes + JSON shapes.

| Task | Depends on | Produces |
|---|---|---|
| SB-07a `@RestControllerAdvice` + error envelopes | SB-03 | `exception/GlobalExceptionHandler` |
| SB-07b `AuthController` + `AuthTestController` + MockMvc tests | SB-07a, SB-05b, SB-06a(N/A)→SB-04c (Customer repo) | |
| SB-07c `ProductsController` + MockMvc tests | SB-07a, SB-06a | |
| SB-07d `CategoriesController` + MockMvc tests | SB-07a, SB-06b | |
| SB-07e `CartController` + MockMvc tests | SB-07a, SB-05b, SB-06c | |
| SB-07f `CheckoutController` + MockMvc tests | SB-07a, SB-05b, SB-06e | |
| SB-07g `OrdersController` + MockMvc tests | SB-07a, SB-05b, SB-06d | |
| SB-07h `HealthController` | SB-07a | |

> SB-07b–h run in parallel after SB-07a.

**Gate G5:** All MockMvc controller tests pass (port of `ProductsControllerTests`, `CartControllerTests`, `CheckoutControllerTests`, + new ones).

## Phase 6 — Parity Verification (serial)
**Goal:** Spring backend is indistinguishable from .NET for the SPA + E2E suite.

| Task | Depends on | Produces |
|---|---|---|
| SB-08a Capture .NET response contracts (`contracts/*.json`) | Running .NET app | test fixtures |
| SB-08b Contract tests: Spring responses match captured .NET responses | SB-08a, Phase 5 green | `ContractTests` |
| SB-08c Run Playwright E2E against Spring backend | Phase 5 green | E2E report |
| SB-08d Decimal/JWT/payment-regex parity spot tests | SB-06e, SB-05a | targeted tests |

**Gate G6 (FINAL):** Contract tests + Playwright E2E green against Spring backend; SPA unchanged.

## Phase 7 — Tooling & Docs (parallel, can start after Phase 5)
| Task | Depends on | Produces |
|---|---|---|
| SB-09a Maven Dockerfile + `docker-build.yml` update | SB-01 | |
| SB-09b `ci.yml` / `pr-validation.yml` Maven steps | Phase 5 | |
| SB-09c Makefile Java targets | SB-01 | |
| SB-09d Documentation updates (doc 10 strategy) | Phase 6 | runbooks, architecture, endpoint catalog notes |

## Phase Summary (Gantt-ish)
```
P0 ──► P1 ──► P2 ──► P3 ──► P4 ──► P5 ──► P6 ──► (done)
                                  └──► P7 (parallel from P5)
```

Critical path: SB-01 → SB-02 → SB-04(d,e) → SB-06e → SB-07f → SB-08b/c.