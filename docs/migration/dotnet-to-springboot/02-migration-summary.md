# 2. Migration Summary — .NET 10 → Java Spring Boot

## 2.1 Goal
Produce a Java Spring Boot backend that is **behaviorally indistinguishable** from the existing .NET 10 `Ecommerce.Api`. Same endpoints, same DTOs (JSON shapes byte-identical), same auth, same DB schema, same business rules. The Vue SPA must work unchanged against the new backend.

## 2.2 Scope

In scope (backend only):
- All 8 controllers (≈22 endpoints) → Spring `@RestController` equivalents.
- 9 domain models → JPA `@Entity` (or plain POJOs if we keep raw-JDBC parity; see decision §2.5).
- 14 DTOs + 2 ad-hoc auth records → Java records.
- 5 services → `@Service` beans.
- 6 repositories → Spring Data JPA repositories OR `JdbcTemplate`/`NamedParameterJdbcTemplate` repos.
- JWT (HS256) auth → `spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server` (or `jjwt`).
- `appsettings.json` → `application.yml` + `application-dev.yml`.
- xUnit tests → JUnit 5 + Mockito.

Out of scope (explicitly preserved as-is, not migrated):
- Vue SPA frontend (`src/frontend/spa`) — consumes the API unchanged.
- YARP reverse proxy — replaced by Spring static resources or a front proxy in dev; documented but not blocking.
- Playwright E2E (`tests/e2e`) — reused against the new backend as the integration contract.
- CI/CD workflows — adapted to build Maven/Gradle instead of `dotnet`, kept otherwise.

## 2.3 Technology Mapping

| .NET concept | Spring Boot equivalent |
|---|---|
| `Ecommerce.Api` (Web SDK) | `spring-boot-starter-web` |
| `Program.cs` minimal host | `@SpringBootApplication` + `application.yml` |
| `AddControllers` | `@RestController` + component scan |
| `[ApiController]`/`[Route]`/`[HttpGet]` | `@RestController`/`@RequestMapping`/`@GetMapping` |
| `[Authorize]` (JWT Bearer) | Spring Security + `oauth2-resource-server` (JWT) |
| `User.FindFirst("customer_id")` | `@AuthenticationPrincipal` Jwt or custom `Authentication.getClaims()` |
| DI `AddScoped<IFoo, Foo>` | `@Component`/`@Service`/`@Repository` (singleton by default; repos are stateless so scope parity is safe) |
| `IConfiguration["JwtSigningKey"]` | `@Value("${jwt.signing-key}")` / `@ConfigurationProperties` |
| ADO.NET `DatabaseHelper` + raw SQL | `NamedParameterJdbcTemplate` (closest parity) **or** Spring Data JPA |
| `SqlTransaction` / `CurrentTransaction` | `@Transactional` (declarative) |
| `SCOPE_IDENTITY()` | `GeneratedKeyHolder` |
| `DataRow` extensions | `RowMapper<T>` |
| xUnit + Moq | JUnit 5 + Mockito + MockMvc |
| `Microsoft.Data.SqlClient` | `mssql-jdbc` (Microsoft SQL Server JDBC driver) |
| OpenAPI (`AddOpenApi`) | `springdoc-openapi-starter-webmvc-ui` |
| YARP | (dropped) Spring serves `/` in dev, or leave SPA dev server on 5173 |
| `appsettings.json` | `application.yml` (+ profiles) |

## 2.4 Key Parity Constraints (must not drift)

1. **JSON field names** use camelCase in .NET (System.Text.Json default for `record`/properties named in PascalCase → serialized camelCase). Spring's Jackson defaults to the Java field name. **Action:** configure Jackson `PropertyNamingStrategy.LOWER_CAMEL_CASE` globally and name Java records' components in a way that serializes identically (e.g., `ProductName` → `productName`). Verify with a contract test against the SPA's `types/index.ts`.
2. **JWT claims** exactly: `sub`, `customer_id`, `email`, `jti`; 1h expiry; HS256; no iss/aud; ClockSkew=0. The SPA reads `customer_id` from the decoded token (see `useAuthStore.ts`).
3. **Idempotency** on `POST /api/checkout/orders` via `Idempotency-Key` header; duplicate returns the original `OrderConfirmationDto`, not an error. Race handled by DB unique index + catch-duplicate path.
4. **Money** uses `decimal` in C# (`java.math.BigDecimal` in Java) — never `double`. GST = `0.18`, rounding follows .NET default (banker's rounding via `decimal` arithmetic); Spring must match to the paise.
5. **Default ports:** API `5001`, SPA `5002`. Keep `5001` or remap cleanly; SPA's `VITE_API_BASE_URL` may need updating — coordinate.
6. **Sort default** is `Id DESC` when `sortBy` is null/unknown. Preserve exact switch behavior.
7. **Category tree** built in-memory with `Children=null` for leaves (not empty array) — the SPA keys off this.
8. **`GET /api/checkout/review`** returns empty object `{}` — preserve.
9. **`AuthController.Login` skips password verification** — preserve the stub explicitly (tag as tech debt; do not "fix" during migration).
10. **`POST /api/cart/items`** returns `{ success, itemCount, cartTotal }` (computed from summary), not the created item — despite the endpoint-catalog doc claiming `CartItemDto`.
11. **`PUT /api/cart/items/{id}`** body is a **bare integer** (`[FromBody] int quantity`), not a JSON object — must configure the Spring endpoint to read a raw number body.
12. **`AddressDto`** on `POST /api/checkout/addresses` ignores the `Id` and `AddressType` fields from the client and hardcodes `AddressType="Shipping"`.

## 2.5 Data Access Decision

**Recommendation:** Use **Spring Data JPA** for entities with simple CRUD (Customer, Category, Product, ProductImage, ProductVariant, Address, CartItem, OrderItem) **and** `@Version`/custom queries where the .NET code uses joins; keep `NamedParameterJdbcTemplate` only where raw SQL parity is safer (e.g., `OrderRepository.GetOrdersByCustomer` batch item fetch, `CartItemRepository.GetCartItemsByCustomer` join). Rationale: JPA reduces boilerplate and handles `SCOPE_IDENTITY` via `@GeneratedValue`; JdbcTemplate is reserved for the 2-3 multi-table queries. **Either choice is acceptable as long as the SQL emitted against SQL Server is functionally equivalent.** This plan defaults to JPA + a thin JdbcTemplate fallback.

## 2.6 Database
- Same SQL Server instance (`LegacyEcommerceDb`), same schema. The Java app connects via `mssql-jdbc`. Connection string moves to `application.yml`.
- No schema changes are required for parity. A Flyway/Liquibase baseline script is generated from the inferred schema (doc 01 §1.5) so the Java project is self-bootstrapping and so future Spring-led migrations are tracked.

## 2.7 Deliverable Structure

```
src/backend-java/
├── pom.xml                       (or settings.gradle + build.gradle)
├── src/main/java/com/ecommerce/
│   ├── EcommerceApplication.java
│   ├── config/                   (SecurityConfig, JacksonConfig, CorsConfig, OpenApiConfig)
│   ├── domain/                   (JPA entities)
│   ├── dto/                      (Java records)
│   ├── repository/               (Spring Data JPA interfaces + JdbcTemplate impls)
│   ├── service/                  (@Service impls + interfaces)
│   ├── security/                 (JwtTokenService, JwtAuthConverter)
│   ├── controller/              (@RestController)
│   └── exception/               (@RestControllerAdvice, parity error envelopes)
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── db/migration/V1__baseline.sql   (Flyway)
└── src/test/java/com/ecommerce/  (JUnit 5 + Mockito + MockMvc)
```