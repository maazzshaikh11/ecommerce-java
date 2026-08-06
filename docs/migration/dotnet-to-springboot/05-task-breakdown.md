# 5. Task Breakdown

Each task is small enough for a single AI agent session and carries a clear definition of done. IDs are stable and referenced by docs 6–8.

| ID | Task | Phase | Effort | Depends on | Files touched |
|---|---|---|---|---|---|
| SB-01 | Create Maven project skeleton, pom.xml, application.yml + dev profile, server port 5001, Jackson camelCase config, Flyway baseline V1, basic `EcommerceApplication` + `HealthController` | P1 | M | — | `pom.xml`, `src/main/resources/**`, `EcommerceApplication.java`, `HealthController.java` |
| SB-02 | Map 9 domain entities to JPA (`@Entity`, `@Table(name="\"Order\"")` for Order), BigDecimal money, LocalDateTime dates, relationships | P1 | M | SB-01 | `domain/*.java` |
| SB-03 | Create Java records for 14 DTOs + `LoginRequest`/`RegisterRequest`; generic `PagedResult<T>` | P1 | S | SB-01 | `dto/*.java` |
| SB-04a | `ProductRepository`, `ProductVariantRepository`, `ProductImageRepository` (JPA + custom `@Query` for active/search/withVariants) | P2 | M | SB-02 | `repository/Product*.java` |
| SB-04b | `CategoryRepository` (incl. `GetAllWithProducts` — load categories + active products, group in-memory) | P2 | M | SB-02 | `repository/Category*.java` |
| SB-04c | `CustomerRepository`, `AddressRepository` (findByEmail, findByCustomerId, add returns generated id) | P2 | S | SB-02 | `repository/Customer*,Address*.java` |
| SB-04d | `CartItemRepository` (CRUD + `GetCartItemsByCustomer` join query via `JdbcTemplate` RowMapper returning product/variant info) | P2 | M | SB-02 | `repository/CartItem*.java` |
| SB-04e | `OrderRepository`, `OrderItemRepository` (CRUD, `GetOrdersByCustomer` batch-fetch items, `FindByIdempotencyKey`); ensure unique index exists in Flyway | P2 | M | SB-02 | `repository/Order*.java`, `V1__baseline.sql` (index) |
| SB-04f | Repository integration tests with Testcontainers (SQL Server) + Flyway + seed data | P2 | L | SB-04a–e | `test/.../repository/*IT.java`, `test/resources/seed.sql` |
| SB-05a | `JwtTokenService` (jjwt): `generateToken(userId, customerId, email)` → claims sub/customer_id/email/jti, 1h, HS256; `validateToken` + unit tests | P3 | S | SB-03, SB-01 | `security/JwtTokenService.java`, tests |
| SB-05b | `SecurityConfig`: stateless, CSRF off, public/protected path map, JWT decoder via `oauth2-resource-server` or custom filter, `JwtAuthConverter` → principal with `customer_id`, CORS for 5173 | P3 | M | SB-05a | `config/SecurityConfig.java`, `security/JwtAuthConverter.java` |
| SB-05c | Security integration test: anonymous 401, valid 200, expired/malformed 401, `customer_id` extractable | P3 | S | SB-05b | `test/.../SecurityTests.java` |
| SB-06a | `ProductService` (pagination defaults 1/12, category-tree BFS, search, price filter, sort switch, detail, featured) + ported `ProductServiceTests` | P4 | M | SB-04a,b, SB-03 | `service/ProductService.java`, tests |
| SB-06b | `CategoryService` (in-memory tree, Children=null for leaves) + unit tests | P4 | S | SB-04b, SB-03 | `service/CategoryService.java`, tests |
| SB-06c | `CartService` (merge-on-add, ownership, delete-when-qty<1, summary) + ported `CartServiceTests` | P4 | M | SB-04d,a | `service/CartService.java`, tests |
| SB-06d | `OrderService` (history, detail→DTO) + unit tests | P4 | S | SB-04e | `service/OrderService.java`, tests |
| SB-06e | `CheckoutService` (`@Transactional`, idempotency pre-check + catch `DataIntegrityViolationException` → re-fetch, GST 0.18, shipping 99/199, order number `RF-yyyyMMdd-NNNN`, payment regex, `FormatAddress`=null, billing-address copy) + ported + new tests | P4 | L | SB-04a,c,d,e | `service/CheckoutService.java`, tests |
| SB-07a | `@RestControllerAdvice` mapping exceptions → `{message}` + status (400/401/404/500); include `InvalidOperationException` equivalent `BusinessException` | P5 | S | SB-03 | `exception/GlobalExceptionHandler.java`, `BusinessException.java` |
| SB-07b | `AuthController` (login/register/me, login password stub preserved) + `AuthTestController` + MockMvc tests | P5 | M | SB-07a, SB-05b, SB-04c | `controller/Auth*.java`, tests |
| SB-07c | `ProductsController` (GET with query params, GET/{id}, GET/featured) + ported `ProductsControllerTests` | P5 | M | SB-07a, SB-06a | `controller/ProductsController.java`, tests |
| SB-07d | `CategoriesController` (GET, GET/{id}) + MockMvc tests | P5 | S | SB-07a, SB-06b | `controller/CategoriesController.java`, tests |
| SB-07e | `CartController` (GET, GET/summary, POST/items with qty validation 1–999, PUT items/{id} **raw-int body**, DELETE items/{id}, DELETE) + ported `CartControllerTests` | P5 | M | SB-07a, SB-05b, SB-06c | `controller/CartController.java`, tests |
| SB-07f | `CheckoutController` (GET addresses, POST addresses hardcode AddressType=Shipping, GET review {}, GET shipping-options, POST validate-payment, POST orders with `Idempotency-Key` header → 201, GET orders/{id}/confirmation) + ported `CheckoutControllerTests` | P5 | L | SB-07a, SB-05b, SB-06e | `controller/CheckoutController.java`, tests |
| SB-07g | `OrdersController` (GET, GET/{id}) + MockMvc tests | P5 | S | SB-07a, SB-05b, SB-06d | `controller/OrdersController.java`, tests |
| SB-07h | `HealthController` (SELECT 1 via repo/JdbcTemplate) | P5 | XS | SB-07a | `controller/HealthController.java` |
| SB-08a | Capture representative .NET API responses (auth, products list/detail/featured, categories, cart, checkout addresses/shipping/orders/confirmation, orders) into `test/resources/contracts/*.json` | P6 | M | Running .NET app | test fixtures |
| SB-08b | Contract tests: `@SpringBootTest` + TestRestTemplate + Testcontainers SQL Server; assert Spring responses match captured .NET JSON (field names + types + values) | P6 | L | SB-08a, Phase 5 | `test/.../ContractTests.java` |
| SB-08c | Run Playwright E2E suite (`tests/e2e`) against Spring backend (point `VITE_API_BASE_URL` at Spring) | P6 | M | Phase 5 | E2E config + report |
| SB-08d | Parity spot tests: BigDecimal GST rounding vs .NET decimal; JWT claim set + 1h expiry; payment regex (good/bad cards); order number format | P6 | S | SB-06e, SB-05a | `test/.../ParityTests.java` |
| SB-09a | Maven multi-stage Dockerfile + update `docker-build.yml` | P7 | S | SB-01 | `Dockerfile`, `.github/workflows/docker-build.yml` |
| SB-09b | Update `ci.yml` + `pr-validation.yml` to run `mvn -B verify` (replace dotnet steps for backend) | P7 | S | Phase 5 | `.github/workflows/*.yml` |
| SB-09c | Add Makefile targets: `build-java`, `test-java`, `run-java`, `verify-java` | P7 | XS | SB-01 | `Makefile` |
| SB-09d | Documentation updates per doc-10 strategy (README, runbooks, architecture, endpoint catalog, ADR) | P7 | M | Phase 6 | `docs/**`, `README.md` |

**Legend:** XS <0.5d, S ~0.5d, M ~1d, L ~2d (AI-assisted wall-clock).