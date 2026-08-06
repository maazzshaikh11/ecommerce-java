# 1. Repository Understanding Document — .NET Backend

> Source of truth: `src/backend/` (7 projects in `Ecommerce.sln`). This document is derived entirely from the source code; no external architectural input was used.

## 1.1 Solution Topology

```
Ecommerce.sln
└── src/backend/
    ├── Ecommerce.Api              (net10.0, Web SDK)   ← entry point, controllers, YARP, DI
    ├── Ecommerce.Api.Core         (net10.0, lib)       ← models, DTOs, interfaces, security
    ├── Ecommerce.Api.Services     (net10.0, lib)       ← business logic
    ├── Ecommerce.Api.Data         (net10.0, lib)       ← ADO.NET repositories + DatabaseHelper
    ├── Ecommerce.Api.Migrations   (net10.0, lib)      ← placeholder (empty)
    ├── Ecommerce.Api.Core.Tests   (net10.0, test)      ← JWT tests
    └── Ecommerce.Api.Tests        (net10.0, test)      ← controller + service tests
```

Reference graph (project dependencies): `Api → Core, Services`; `Services → Core, Data`; `Data → Core`; `Tests → Api, Services, Core`. Data transitively reaches `Api` via `Services`.

## 1.2 Application Entry Point

`Ecommerce.Api/Program.cs` (78 lines) — minimal-host model:

- Reads `JwtSigningKey` from configuration; requires ≥32 bytes (HMAC-SHA256).
- Registers: Controllers, OpenAPI, YARP reverse proxy (config-driven), CORS (`VueDev`, origin `http://localhost:5173`).
- Auth: `AddAuthentication(JwtBearer)` + `AddAuthorization()`; validation params: issuer=off, audience=off, lifetime=on, signing key=on, **ClockSkew=Zero**.
- DI registrations (all **scoped**): `DatabaseHelper`, six repositories, five services, `JwtTokenService`.
- Pipeline (Dev): OpenAPI + CORS; (always): `UseHttpsRedirection` → `UseAuthentication` → `UseAuthorization` → `MapControllers` → `MapReverseProxy`.
- Default port `5001` (`launchSettings.json`).

## 1.3 Domain Model (Core/Models)

| Entity | Key fields | Relationships |
|---|---|---|
| `Customer` | Id, IdentityUserId?, Email, FirstName, LastName, PhoneNumber?, CreatedDate | → CartItem, Address, Order |
| `Category` | Id, Name, Description, ThumbnailUrl, ParentCategoryId? | self-ref tree, → Product |
| `Product` | Id, Name, Description, Price, CategoryId, ImageUrl, ThumbnailUrl, IsActive, CreatedDate | → ProductImage[], ProductVariant[] |
| `ProductImage` | Id, ProductId, ImageUrl, IsPrimary, DisplayOrder | → Product |
| `ProductVariant` | Id, ProductId, VariantName, VariantValue, PriceAdjustment, StockQuantity | → Product |
| `CartItem` | Id, SessionId?, ProductId, VariantId?, CustomerId?, Quantity, UnitPrice, CreatedDate | → Product, ProductVariant |
| `Address` | Id, CustomerId, AddressType, StreetAddress, City, StateProvince?, PostalCode?, Country, IsDefault | → Customer |
| `Order` | Id, CustomerId, OrderDate, OrderStatus, OrderNumber, SubTotal, ShippingTotal, TaxTotal, GrandTotal, ShippingAddressId?, BillingAddressId?, IdempotencyKey? | → OrderItem[] |
| `OrderItem` | Id, OrderId, ProductId, ProductName, Quantity, UnitPrice, LineTotal | → Order |

No EF Core, no migrations, no navigation properties persisted — pure POCOs mapped by hand.

## 1.4 DTOs (Core/DTOs)

Records (immutable): `AddCartItemRequest`, `AddressDto`, `CartItemDto`, `CartSummaryDto`, `OrderConfirmationDto`, `OrderDto`, `OrderItemDto`, `PaymentRequest`, `PlaceOrderRequest`, `ShippingOptionDto`.
Classes (mutable): `CategoryDto` (with `Children`), `PagedResult<T>`, `ProductDetailDto` (+ nested `ProductImageDto`, `ProductVariantDto`), `ProductListResponseDto`.
Ad-hoc records in `AuthController.cs`: `LoginRequest(Email, Password)`, `RegisterRequest(Email, Password, FirstName, LastName)`.

## 1.5 Data Access Layer (ADO.NET)

- `DatabaseHelper` (`Data/DatabaseHelper.cs`): scoped, owns a single lazy `SqlConnection`; exposes `ExecuteQuery/ExecuteScalar/ExecuteNonQuery` (with and without explicit `SqlTransaction`), `BeginTransaction()`, `CurrentTransaction` (mutable, used by `CheckoutService` for cross-repo transaction). `IDisposable`.
- `DataRowExtensions`: typed accessors (`GetString`, `GetInt`, `GetIntNullable`, `GetDecimal`, `GetBool`, `GetDateTime`) with null-safe defaults.
- Six repositories, all raw SQL with `SqlParameter` (`@Name` placeholders). No ORM, no LINQ-to-SQL. `SCOPE_IDENTITY()` used to retrieve inserted identities. `[Order]` is bracketed (reserved word).

### Implied SQL Server schema (reverse-engineered from queries)

Tables/columns inferred from SELECT/INSERT/UPDATE statements:

- **Product** (Id PK, Name, Description, Price, CategoryId, ImageUrl, ThumbnailUrl, IsActive bit, CreatedDate)
- **Category** (Id PK, Name, Description, ThumbnailUrl, ParentCategoryId NULLable)
- **ProductImage** (Id PK, ProductId, ImageUrl, IsPrimary bit, DisplayOrder)
- **ProductVariant** (Id PK, ProductId, VariantName, VariantValue, PriceAdjustment, StockQuantity)
- **CartItem** (Id PK, SessionId NULL, ProductId, VariantId NULL, CustomerId NULL, Quantity, UnitPrice, CreatedDate)
- **Address** (Id PK, CustomerId, AddressType, StreetAddress, City, StateProvince NULL, PostalCode NULL, Country, IsDefault bit)
- **Customer** (Id PK, IdentityUserId NULL, Email, FirstName, LastName, PhoneNumber NULL, CreatedDate)
- **[Order]** (Id PK, CustomerId, OrderDate, OrderStatus, OrderNumber, SubTotal, ShippingTotal, TaxTotal, GrandTotal, ShippingAddressId NULL, BillingAddressId NULL, IdempotencyKey NULL — referenced by `IsDuplicateKeyException` expecting an index `IX_Order_IdempotencyKey`)
- **OrderItem** (Id PK, OrderId, ProductId, ProductName, Quantity, UnitPrice, LineTotal)

DB: `LegacyEcommerceDb` on `(localdb)\MSSQLLocalDB`, `Trusted_Connection=True`, `TrustServerCertificate=True`.

## 1.6 Business Logic (Services)

| Service | Key behaviors |
|---|---|
| `ProductService` | Paginated listing (default 12), category-tree filter via in-memory BFS walk, keyword search (name/description, case-insensitive), price min/max filter, sort (`price_asc`/`price_desc`/`name`/default `Id DESC`); detail loads images+variants; featured = first N active. |
| `CategoryService` | Builds hierarchical tree in-memory from `GetAllWithProducts`; `Children=null` for leaves. |
| `CartService` | Per-customer cart; add merges into existing line (same productId+variantId) by incrementing qty; update qty<1 deletes; ownership check on update/remove. |
| `CheckoutService` | **Idempotent** order placement keyed by `IdempotencyKey` (DB unique index + pre-check + `SqlException` 2601/2627 race handling); deducts variant stock; creates billing address copy; GST 18% on (subtotal+shipping); shipping Standard ₹99 / Express ₹199; order number `RF-yyyyMMdd-NNNN`; payment validation via regex (16-digit card, MM/YY, 3-4 CVV). Transaction spans repos via `_db.CurrentTransaction`. |
| `OrderService` | Order history (no items) + single order with items → DTOs. |

## 1.7 API Surface (Controllers)

| Controller | Route | Auth | Endpoints |
|---|---|---|---|
| `AuthController` | `api/auth` | mixed | `POST login`, `POST register`, `GET me` (Bearer) |
| `AuthTestController` | `api/authtest` | Bearer | `GET` |
| `ProductsController` | `api/products` | None | `GET` (query: categoryId, searchQuery, minPrice, maxPrice, sortBy, page=1, pageSize=12), `GET {id}`, `GET featured?count=6` |
| `CategoriesController` | `api/categories` | None | `GET`, `GET {id}` |
| `CartController` | `api/cart` | **Bearer** (class) | `GET`, `GET summary`, `POST items`, `PUT items/{id}`, `DELETE items/{id}`, `DELETE` |
| `CheckoutController` | `api/checkout` | **Bearer** (class) | `GET addresses`, `POST addresses`, `GET review`, `GET shipping-options`, `POST validate-payment`, `POST orders` (needs `Idempotency-Key` header), `GET orders/{id}/confirmation` |
| `OrdersController` | `api/orders` | **Bearer** (class) | `GET`, `GET {id}` |
| `HealthController` | `api/health` | None | `GET` |

Customer identity extracted from JWT claim `customer_id` (parsed to int) via a private `GetCustomerId()` helper in each protected controller. Response shapes: `{ token }` on auth; `{ message }` on errors; `{ success, itemCount, cartTotal }` on cart mutations; `201` with `OrderConfirmationDto` on order placement.

## 1.8 Security

- JWT HS256, symmetric key from config (`JwtSigningKey`, base64-ish string, ≥32 bytes).
- Claims: `sub`=userId, `customer_id`=customerId, `email`, `jti`=GUID. 1-hour expiry. No issuer/audience.
- **Login deliberately skips password verification** (legacy Identity hashes) — flagged as `// ponytail` tech debt.
- Register creates a `Customer` row (no password stored).
- `JwtTokenService` has both `GenerateToken` and `ValidateToken` (the latter is self-validation, not wired into the pipeline — the framework's `AddJwtBearer` does validation).

## 1.9 Cross-Cutting / Infra

- **YARP** reverse proxy routes SPA paths (`/login`, `/products`, `/cart`, `/checkout`, `/orders`, `/`, `{**catch-all}`) to `http://localhost:5002` (Vue dev). `/api/*` handled by API.
- CORS only in Dev (`VueDev` policy).
- No logging/telemetry/tracing configuration beyond defaults. No health checks framework (hand-rolled `HealthController`).
- No caching, no queues, no external HTTP clients, no background services.

## 1.10 Testing

- **xUnit + Moq**, ~unit-level only. No integration/E2E on backend.
- `Ecommerce.Api.Core.Tests`: `JwtTokenServiceTests` (4 tests) — token gen, claims, validation.
- `Ecommerce.Api.Tests`: `CartServiceTests` (7), `CartControllerTests` (7), `CheckoutControllerTests` (3), `ProductsControllerTests` (3), `ProductServiceTests` (5).
- Tests mock repos/services via interfaces and assert status codes + DTO shapes.

## 1.11 Build & Tooling

- Central package management (`Directory.Packages.props`): `Microsoft.AspNetCore.OpenApi 10.0.10`, `Yarp.ReverseProxy 2.3.0`, `Microsoft.Data.SqlClient 5.2.2`, `Microsoft.AspNetCore.Authentication.JwtBearer 10.0.10`, `Microsoft.IdentityModel.Tokens 8.3.0`, `System.IdentityModel.Tokens.Jwt 8.3.0`, xUnit/Moq.
- `global.json` pins SDK `10.0.302`, `rollForward: latestFeature`.
- `Makefile`: `build`, `test`, `test-e2e`, `lint`, `format`, `verify`, `docker-up/down`, `clean`.
- CI workflows under `.github/workflows/`: `ci.yml`, `pr-validation.yml`, `e2e.yml`, `docker-build.yml`, `deploy-staging.yml`, `deploy-production.yml`, `security-scan.yml`.
- Frontend (out of scope for backend migration): Vue 3 SPA at `src/frontend/spa`, talks to `/api/*` via `src/api/client.ts` (JWT in `localStorage['jwt']`).

## 1.12 Notable Gaps / Tech Debt (preserve parity)

1. No DB migration scripts exist — schema is implicit. Must be reconstructed from repository SQL.
2. `Ecommerce.Api.Migrations` project is an empty placeholder.
3. Login password verification intentionally stubbed.
4. `CheckoutService` couples to `DatabaseHelper.CurrentTransaction` (thread-unsafe scoped state) — Spring equivalent must use Spring's `@Transactional` properly.
5. `ProductService.GetProductsByCategoryTree` does an in-memory BFS — keep behavior identical in Spring (or push to SQL CTE later; **not** during parity migration).
6. `FormatAddress` returns null (deferred) — preserve as-is.
7. `AddAddress` in `CheckoutController` resolves `IAddressRepository` from `HttpContext.RequestServices` (service-locator anti-pattern) — Spring should inject it cleanly but keep the endpoint contract.
8. YARP SPA proxying — in Spring Boot this is either dropped (SPA served by separate dev server) or replaced by Spring static-resource serving / a front proxy. **Out of backend migration scope** but must be documented.