# 3. Implementation Plan — .NET → Spring Boot

## 3.1 Strategy
Layer-by-layer, bottom-up, mirroring the .NET dependency graph (`Data → Core → Services → Api`). Each layer is migrated, compiled, and unit-tested before the layer above it begins. Integration (MockMvc) tests are added as soon as controllers exist. A parity contract test (captured .NET HTTP responses) gates the finish.

## 3.2 Build Tooling
- **Maven** (chosen over Gradle for simpler tooling and broad AI familiarity). Java 21 (LTS), Spring Boot 3.3.x.
- If a Gradle preference exists, swap to `build.gradle.kts`; plan is tool-agnostic except for command names.

## 3.3 Layer Translation Order

```
1. Project skeleton + build file + application.yml + Flyway baseline
2. Domain entities (9) + JPA mappings
3. DTOs (14 + 2 auth records) as Java records
4. Repositories (Spring Data JPA + 2 JdbcTemplate queries)
5. Security: JWT encode/decode + Spring Security config
6. Services (5) with business logic parity
7. Controllers (8) with exact route/method/status parity
8. Exception advice to reproduce { message } envelopes
9. Tests: unit (services) + MockMvc (controllers) + contract (captured responses)
10. CI/CD adaptation + Makefile + runbooks + doc updates
```

## 3.4 Per-Layer Detail

### 3.4.1 Skeleton & Config (Task SB-01)
- `pom.xml`: spring-boot-starter-web, -security, -data-jpa, -validation, mssql-jdbc, flyway, springdoc-openapi, jjwt 0.12.x, lombok (optional, records preferred), junit5, mockito, spring-boot-starter-test.
- `application.yml`: datasource (`jdbc:sqlserver://localhost;databaseName=LegacyEcommerceDb;...`), JPA `ddl-auto: none`, Flyway `enabled: true`, `jwt.signing-key`, server port `5001`, Jackson `property-naming-strategy: LOWER_CAMEL_CASE`.
- `application-dev.yml`: dev overrides + CORS.

### 3.4.2 Domain (Task SB-02)
- 9 entities annotated `@Entity`, table/column names matching inferred schema exactly (`[Order]` → `@Table(name = "\"Order\"")`).
- `BigDecimal` for money; `LocalDateTime`/`Instant` for dates (serialize as ISO-8601 to match .NET).
- Relationships: lazy `@OneToMany` where the .NET code navigates (Product→Images, Product→Variants, Category→Products, Order→OrderItems). `@ManyToOne` Product→Category (read-only).

### 3.4.3 DTOs (Task SB-03)
- Java records mirroring each .NET DTO; Jackson camelCase serialization verified by test.
- `PagedResult<T>` as generic record/class.
- Auth request bodies: `LoginRequest(String email, String password)`, `RegisterRequest(String email, String password, String firstName, String lastName)`.

### 3.4.4 Repositories (Task SB-04)
- Spring Data JPA interfaces: `ProductRepository`, `CategoryRepository`, `CartItemRepository`, `AddressRepository`, `CustomerRepository`, `OrderRepository`, `ProductImageRepository`, `ProductVariantRepository`.
- Custom query methods (`@Query` JPQL/SQL) for: `GetActiveProducts` (IsActive=1 ORDER BY Name), `Search` (LIKE), `GetCartItemsByCustomer` (join Product+ProductVariant — use a projection interface or `JdbcTemplate` RowMapper for exact parity), `GetOrdersByCustomer` (batch items), `FindByIdempotencyKey`.
- `NamedParameterJdbcTemplate` fallback for the 2 join queries where JPA projection is awkward.

### 3.4.5 Security (Task SB-05)
- `JwtTokenService` (using `io.jsonwebtoken:jjwt`) replicating claim set, 1h expiry, HS256.
- `SecurityConfig` (`SecurityFilterChain` bean): stateless, CSRF disabled, public paths `/api/auth/login`, `/api/auth/register`, `/api/products/**`, `/api/categories/**`, `/api/health`; all `/api/cart/**`, `/api/checkout/**`, `/api/orders/**`, `/api/auth/me`, `/api/authtest` require JWT.
- Custom `JwtAuthConverter` maps `customer_id` claim into a principal the controllers can read.
- CORS: allow `http://localhost:5173` in dev.

### 3.4.6 Services (Task SB-06)
- `ProductService`: same pagination math, same in-memory category BFS, same sort switch, same `GetProductsByCategoryTree`. Default page 1, pageSize 12; clamp <1.
- `CategoryService`: in-memory tree with `Children=null` for leaves.
- `CartService`: same merge-on-add, ownership checks, delete-when-qty<1.
- `CheckoutService`: `@Transactional`; idempotency pre-check + catch `DataIntegrityViolationException` (unique index) → re-fetch; GST 0.18; shipping map; order number format `RF-yyyyMMdd-NNNN` (use `ThreadLocalRandom` 1000–9999, matching `Random.Shared` range); payment regex validation; `FormatAddress` returns null.
- `OrderService`: history + detail → DTOs.

### 3.4.7 Controllers (Task SB-07)
- One `@RestController` per .NET controller, same `@RequestMapping("/api/...")`, same method mappings.
- `customerId` extracted from `SecurityContextHolder` or `@AuthenticationPrincipal` (custom principal carrying `customer_id`).
- **`PUT /api/cart/items/{id}`** body = raw integer → use `@RequestBody Integer quantity` (Spring binds raw JSON number to `Integer`).
- `POST /api/checkout/orders` reads `Idempotency-Key` via `@RequestHeader`.
- Status codes: `201` on order, `404` on not-found, `400` on validation, `401` on bad/missing token, `500` on health failure.

### 3.4.8 Exception Advice (Task SB-08)
- `@RestControllerAdvice` translating exceptions to `{ "message": "..." }` JSON with correct HTTP status:
  - `IllegalArgumentException` → 400
  - `InvalidOperationException` equivalent (custom `BusinessException`) → 400
  - `EntityNotFoundException`/resource not found → 404
  - `AccessDeniedException` → 401
  - `DataIntegrityViolationException` (non-idempotency) → 400
  - Generic → 500 with sanitized message

### 3.4.9 Tests (Task SB-09)
- Unit tests for each service (Mockito mocks on repository interfaces) — port the 5 .NET test classes 1:1.
- MockMvc tests for each controller — port controller test classes.
- `JwtTokenServiceTests` port.
- **Contract tests** (new): capture representative responses from the running .NET API (auth, products, cart, checkout, orders) into `src/test/resources/contracts/*.json`; in Spring tests, hit the same endpoints with TestRestTemplate + H2/Testcontainers SQL Server and assert JSON shape equivalence (field names + types).

### 3.4.10 CI/CD & Docs (Task SB-10)
- Adapt `.github/workflows/ci.yml`: `mvn -B verify`; cache `~/.m2`.
- `docker-build.yml`: Maven multi-stage Dockerfile.
- `Makefile`: add `build-java`, `test-java`, `run-java` targets.
- Update runbooks + endpoint catalog (no API changes, just note backend stack).

## 3.5 Risk Register

| Risk | Mitigation |
|---|---|
| JSON casing drift breaks SPA | Global Jackson `LOWER_CAMEL_CASE` + contract test against `src/frontend/spa/src/types/index.ts`. |
| Money rounding mismatch (decimal vs BigDecimal) | Use `BigDecimal` with `RoundingMode.HALF_EVEN` (matches .NET `decimal` default banker's rounding) — verify in a dedicated unit test replicating the GST calc. |
| SQL Server reserved word `Order` | `@Table(name = "\"Order\"")` everywhere; raw SQL bracketed. |
| Idempotency race | Replicate DB unique index `IX_Order_IdempotencyKey` in Flyway; `@Transactional` + catch `DataIntegrityViolationException` → re-fetch. |
| `DatabaseHelper.CurrentTransaction` thread-state pattern | Replaced by declarative `@Transactional` — confirm no service reads `CurrentTransaction` directly (only `CheckoutService` does). |
| YARP removal in dev | SPA dev server still on 5173; either point SPA `VITE_API_BASE_URL` to `:5001` or add Spring CORS + proxy. Document; not blocking. |
| Password verification stub | Preserve stub behavior explicitly; document as carried-forward tech debt. |