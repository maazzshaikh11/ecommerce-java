# 9. Verification Strategy

Three concentric rings of verification. A phase gate (G1–G6) only passes when its ring(s) are green. No "looks right" — everything is automated.

## 9.1 Ring A — Compile & Unit (per task)
Every task ends with a build/test command in its prompt. The task is not done until:
- `mvn -q compile` succeeds (no warnings treated as errors for new code).
- The task's named test class passes (`mvn -q test -Dtest=...`).
- The agent reports the exact command + exit status in its summary.

This ring catches: syntax, type, DI wiring, and ported-logic regressions.

## 9.2 Ring B — Integration (per phase gate)
| Gate | Tests | Environment |
|---|---|---|
| G1 (P1 done) | `mvn -q compile`; `mvn -q spring-boot:run` boots; `GET /api/health` returns 200 `{"status":"Healthy","db":1}` | Local SQL Server OR Testcontainers |
| G2 (P2 done) | `mvn -q test -Dtest='*IT'` — repository integration tests against Testcontainers SQL Server + Flyway + seed.sql | Testcontainers (Docker) |
| G3 (P3 done) | `mvn -q test -Dtest=SecurityTests` — public vs protected, token valid/expired/tampered | MockMvc |
| G4 (P4 done) | `mvn -q test -Dtest='*ServiceTests'` — all ported service unit tests + new CheckoutServiceTests | Mockito |
| G5 (P5 done) | `mvn -q test -Dtest='*ControllerTests'` — all MockMvc controller tests (status codes, JSON shapes) | MockMvc |
| G6 (P6 done) | `mvn -q test -Dtest='ContractTests,ParityTests'` + `npx playwright test` against Spring | Testcontainers + SPA running |

## 9.3 Ring C — Parity (final gate, behavioral equivalence)
The strongest evidence that Spring behaves like .NET. Built deliberately to be diff-based, not spec-based.

### 9.3.1 Contract tests (SB-08b)
- Captured .NET responses (`contracts/*.json` + `.meta.json`) are the oracle.
- Spring responses are compared with JSONassert: LENIENT for structure, then STRICT type checks per field.
- Money compared with `BigDecimal` tolerance 0 (exact to the paise).
- Date/times compared as ISO-8601 strings up to seconds (timezone normalized to UTC; .NET defaults to UTC).
- Array ordering compared only where the .NET response is explicitly ordered (products default `Id DESC`, orders `OrderDate DESC`).

### 9.3.2 Playwright E2E (SB-08c)
- The existing `tests/e2e` suite is run unchanged against the Spring backend (SPA pointed at `:5001`).
- A green run means the SPA — the real consumer of the API — cannot tell the backends apart for the covered user journeys (login, browse, cart, checkout, orders).

### 9.3.3 Parity spot tests (SB-08d)
- GST BigDecimal math vs .NET decimal (one concrete example: subTotal 1999, shipping 99 → tax 377.64, grand 2475.64).
- Order-number format + range.
- JWT claim set + expiry (iat→exp = 3600s).
- Payment regex (positive + negative cases).

## 9.4 Specific parity traps with explicit tests

| Trap | Test |
|---|---|
| JSON camelCase | `JacksonTest` (SB-03) + every ContractTest field-name check |
| `PUT /api/cart/items/{id}` raw integer body | `CartControllerTests` explicit "body `3`" test (SB-07e) |
| `POST /api/cart/items` returns `{success,itemCount,cartTotal}`, not a CartItemDto | ContractTest |
| Category leaves serialize `Children: null` | `CategoryServiceTests` + ContractTest |
| `GET /api/checkout/review` → `{}` | ContractTest |
| Idempotent `POST /api/checkout/orders` returns original order on replay | `CheckoutServiceTests` + ContractTest |
| Login skips password (stub) | `AuthControllerTests` asserts 200 with any password for known email |
| GST rounding (HALF_EVEN vs .NET decimal) | `ParityTests` |
| `[Order]` reserved word in SQL | `OrderRepositoryIT` (boots + queries the real table) |
| 401 vs 403: .NET returns 401 for unauthenticated | `SecurityTests` asserts 401 (not 403) on protected endpoints |
| `Idempotency-Key` header missing → 400 (not 401) | `CheckoutControllerTests` |

## 9.5 401 vs 403 — important
.NET `[Authorize]` with no token returns **401 Unauthorized**. Spring Security's default for a request reaching a protected resource without authentication is also 401 (via `AuthenticationEntryPoint`), but for a request *with* an invalid token that fails the filter, Spring may return 403 if the `AccessDeniedHandler` fires. **Tune** `SecurityConfig` so unauthenticated → 401 (default `Http403ForbiddenEntryPoint` must NOT be used; use `HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)` or the default JWT entry point). Test explicitly in `SecurityTests`.

## 9.6 Money & rounding
- All money is `BigDecimal`, constructed via `new BigDecimal("99")` (not `99.0`), arithmetic with `.setScale` only at output if needed.
- GST: `subTotal.add(shippingTotal).multiply(GST_RATE)` where `GST_RATE = new BigDecimal("0.18")`; do not pre-scale intermediate results; compare final `taxTotal` to the .NET value with `compareTo` == 0.
- .NET `decimal` uses banker's rounding (ToEven / `MidpointRounding.ToEven`) when rounding is required; the .NET code here does no explicit rounding (raw arithmetic), so Spring must also avoid rounding unless the DB column scale forces it. Match column scales (`decimal(18,2)`) to avoid implicit rounding differences.

## 9.7 Non-determinism
- Order number uses random in [1000, 10000). Tests assert format + range, not a specific value.
- `jti` is a random UUID — tests assert presence, not value.
- CreatedDate/OrderDate use `now()` — contract tests compare up to second precision or ignore the field.

## 9.8 Continuous verification cadence
- Every PR runs Ring A + B (CI job).
- Nightly (or pre-merge to main) runs Ring C (ContractTests + E2E) — they need Docker + SPA so are heavier.
- A failed parity test is a release blocker; a failed non-parity test is a task-level blocker only.

## 9.9 Rollback criterion
If, after migration, the SPA or E2E reveals a behavioral difference not caught by tests:
1. Do NOT patch the test to match Spring.
2. File the discrepancy back to the owning SB-0x task.
3. Re-run that task's prompt with the discrepancy appended as an additional requirement.
4. Re-run Rings B + C for the affected phase + downstream.