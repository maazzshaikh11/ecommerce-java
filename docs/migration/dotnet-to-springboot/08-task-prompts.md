# 8. Prompt / Instruction per Task

Each task gets a self-contained prompt. All prompts share the preamble below; only the **Task-specific** section changes. Paste the preamble + the task block into a fresh agent session.

---

## 8.0 Shared Preamble (include in every task prompt)

```
You are migrating the backend of an eCommerce app from .NET 10 to Java 21 + Spring Boot 3.3.
Source of truth for behavior: the .NET code under /Users/huzaifashaikh/eCommerce-Migration/src/backend.
Read these planning docs first and treat them as authoritative:
  docs/migration/dotnet-to-springboot/01-repository-understanding.md
  docs/migration/dotnet-to-springboot/02-migration-summary.md
  docs/migration/dotnet-to-springboot/03-implementation-plan.md
Hard parity rules (do NOT deviate):
  - JSON field names MUST be camelCase (global Jackson LOWER_CAMEL_CASE).
  - Money MUST be java.math.BigDecimal, never double. GST=0.18, RoundingMode.HALF_EVEN.
  - JWT claims EXACTLY: sub, customer_id, email, jti; 1h expiry; HS256; no iss/aud; ClockSkew=0.
  - Endpoints, paths, HTTP methods, status codes, and response JSON shapes MUST match the .NET controllers byte-for-byte at the field level.
  - The login password-verification stub MUST be preserved (do not implement password hashing).
  - Category tree leaves MUST serialize Children as null, not [].
  - PUT /api/cart/items/{id} reads a RAW integer JSON body, not an object.
  - POST /api/checkout/orders requires the Idempotency-Key header; duplicates return the original OrderConfirmationDto.
  - [Order] is a reserved word in SQL Server — quote it in all SQL/JPA.
Write code in src/backend-java (Maven). Java 21 records preferred over Lombok. No comments unless asked.
Do NOT modify anything under src/backend (the .NET project) or src/frontend.
When done: run `mvn -q -pl <module> test` (or `mvn -q compile` if no tests yet) and report the exact command + result.
Return a concise summary: files created/changed, command run, pass/fail, and any parity deviations you were tempted to make but did NOT.
```

---

## 8.1 SB-01 — Skeleton, Config, Flyway, Health
```
Task SB-01 (Spring Architect):
Create src/backend-java as a Maven project with Spring Boot 3.3.x, Java 21.
pom.xml deps: spring-boot-starter-web, -security, -data-jpa, -validation, mssql-jdbc, flyway-core + flyway-sqlserver, springdoc-openapi-starter-webmvc-ui, io.jsonwebtoken:jjwt-api/impl/jackson 0.12.x, junit5/mockito/spring-boot-starter-test.
application.yml:
  - server.port: 5001
  - spring.datasource.url: jdbc:sqlserver://localhost;databaseName=LegacyEcommerceDb;encrypt=true;trustServerCertificate=true;integratedSecurity=true (also provide a non-integratedSecurity variant in a comment for non-Windows dev)
  - spring.jpa.hibernate.ddl-auto: none
  - spring.jpa.open-in-view: false
  - spring.flyway.enabled: true; locations: classpath:db/migration
  - spring.jackson.property-naming-strategy: LOWER_CAMEL_CASE
  - jwt.signing-key: bXlzZWNyZXRrZXltdXN0YmVleGFjdGx5MzJieXRlc2xvbmc=
application-dev.yml: CORS allow-origin http://localhost:5173.
Create EcommerceApplication.java.
Create V1__baseline.sql in resources/db/migration containing CREATE TABLE statements for: Product, Category, ProductImage, ProductVariant, CartItem, Address, Customer, [Order] (quoted), OrderItem — using the exact columns/types inferred in doc 01 §1.5. Include a unique index IX_Order_IdempotencyKey on [Order](IdempotencyKey) WHERE IdempotencyKey IS NOT NULL.
Add a minimal HealthController at /api/health that runs SELECT 1 (via JdbcTemplate) and returns {"status":"Healthy","db":1} (camelCase).
Verify: mvn -q compile; then mvn -q spring-boot:run should boot (do not need a live DB for compile).
Return: files, command, result.
```

## 8.2 SB-02 — Domain Entities
```
Task SB-02 (JPA Mapper):
Create package com.ecommerce.domain with 9 @Entity classes matching doc 01 §1.3 and §1.5:
Address, CartItem, Category, Customer, Order, OrderItem, Product, ProductImage, ProductVariant.
- @Table(name="\"Order\"") for Order (reserved word); quote the table in all native SQL too.
- BigDecimal for all money fields (Price, UnitPrice, PriceAdjustment, LineTotal, SubTotal, ShippingTotal, TaxTotal, GrandTotal).
- LocalDateTime for CreatedDate/OrderDate; map with @Column(columnDefinition="datetime2") if needed.
- Relationships (lazy): Product 1→N ProductImage, Product 1→N ProductVariant, Category 1→N Product, Order 1→N OrderItem, Category self-ref via ParentCategoryId (bidirectional optional).
- Column names MUST match the .NET queries exactly (e.g., ThumbnailUrl, IsPrimary, ParentCategoryId, ShippingAddressId, BillingAddressId, IdempotencyKey, IdentityUserId, AddressType, StateProvince, PostalCode, IsDefault, VariantName, VariantValue, PriceAdjustment, StockQuantity).
- Boolean IsActive/IsPrimary/IsDefault map to BIT.
Verify: mvn -q compile.
Return: files, command, result.
```

## 8.3 SB-03 — DTO Records
```
Task SB-03 (DTO Transcriber):
Create package com.ecommerce.dto with Java records mirroring doc 01 §1.4 EXACTLY:
Records: AddCartItemRequest(int productId, Integer variantId, int quantity), AddressDto(int id, String streetAddress, String city, String stateProvince, String postalCode, String country, boolean isDefault), CartItemDto(int id, int productId, String productName, String variantName, String imageUrl, BigDecimal unitPrice, int quantity, BigDecimal lineTotal), CartSummaryDto(int itemCount, BigDecimal subTotal, BigDecimal total), OrderConfirmationDto(int orderId, String orderNumber, LocalDateTime orderDate, String orderStatus, BigDecimal subTotal, BigDecimal shippingTotal, BigDecimal taxTotal, BigDecimal grandTotal, String shippingAddressFormatted, List<OrderItemDto> items), OrderDto(int id, String orderNumber, LocalDateTime orderDate, String orderStatus, BigDecimal subTotal, BigDecimal shippingTotal, BigDecimal taxTotal, BigDecimal grandTotal), OrderItemDto(String productName, int quantity, BigDecimal unitPrice, BigDecimal lineTotal), PaymentRequest(String cardholderName, String cardNumber, String expirationDate, String cvv), PlaceOrderRequest(Integer addressId, String shippingMethod, String idempotencyKey), ShippingOptionDto(String method, String label, BigDecimal cost).
Classes (mutable): CategoryDto (id, name, description, thumbnailUrl, parentCategoryId, List<CategoryDto> children — nullable), PagedResult<T> (items, currentPage, pageSize, totalCount, totalPages), ProductDetailDto (+ nested ProductImageDto, ProductVariantDto), ProductListResponseDto.
Also in com.ecommerce.controller (or dto): LoginRequest(String email, String password), RegisterRequest(String email, String password, String firstName, String lastName).
Jackson will serialize record component names as-is; with global LOWER_CAMEL_CASE they become camelCase. Confirm by adding one tiny JacksonTest asserting a serialized CartItemDto has keys id, productId, productName, variantName, imageUrl, unitPrice, quantity, lineTotal.
Verify: mvn -q test -Dtest=JacksonTest.
Return: files, command, result.
```

## 8.4 SB-04a — Product/Variant/Image Repos
```
Task SB-04a (Repository Engineer — Catalog):
Create Spring Data JPA interfaces in com.ecommerce.repository: ProductRepository, ProductVariantRepository, ProductImageRepository.
Methods (JPQL @Query or method names) matching the .NET ProductRepository:
- ProductRepository: findById, findAll(Sort.by name), findProductsByCategoryAndIsActiveTrue(int, Sort), search(active, name OR description LIKE), findActiveProducts (IsActive=1 ORDER BY Name), findProductWithVariantsById (fetch images + variants — implement via a custom finder that returns Product with Images/Variants populated; lazy load is acceptable), findVariantById, updateVariant (use save).
- Add a custom @Query for search using LIKE %:term% on Name and Description, IsActive=1.
- Use @Modifying @Query for UpdateVariant if not using save().
Provide a JdbcTemplate-free implementation first; note any query that must fall back to native SQL (reserved words).
Verify: mvn -q compile (tests come in SB-04f).
Return: files, command, result, and list of queries using native SQL.
```

## 8.5 SB-04b — Category Repository
```
Task SB-04b (Repository Engineer — Category):
Create CategoryRepository (Spring Data JPA). Methods matching .NET CategoryRepository:
- findById, findAll(Sort.by name), getAllWithProducts() — load all categories, then all active products, group products under their category in-memory (mirror CategoryRepository.GetAllWithProducts). Return List<Category>.
- Add/Update/Delete via save/delete.
Keep the in-memory grouping logic identical to .NET (no SQL CTE).
Verify: mvn -q compile.
Return: files, command, result.
```

## 8.6 SB-04c — Customer & Address Repos
```
Task SB-04c (Repository Engineer — Identity/Address):
Create CustomerRepository and AddressRepository (Spring Data JPA) matching .NET:
- CustomerRepository: findById, findByEmail, findByIdentityUserId, save (add), save (update), delete.
- AddressRepository: findById, findByCustomerId, save, delete.
Generated id must be populated after save (JPA does this automatically).
Verify: mvn -q compile.
Return: files, command, result.
```

## 8.7 SB-04d — CartItem Repository (JdbcTemplate join)
```
Task SB-04d (JdbcTemplate Specialist — Cart):
Create CartItemRepository (Spring Data JPA) for basic CRUD (findById, save, delete, clearCartByCustomerId via @Modifying @Query DELETE).
Additionally, implement getCartItemsByCustomer(int customerId) to mirror the .NET SQL:
  SELECT ci.*, p.Name as ProductName, p.ThumbnailUrl, p.ImageUrl, pv.VariantName, pv.VariantValue
  FROM CartItem ci
  LEFT JOIN Product p ON ci.ProductId = p.Id
  LEFT JOIN ProductVariant pv ON ci.VariantId = pv.Id
  WHERE ci.CustomerId = :customerId
Use NamedParameterJdbcTemplate with a RowMapper that returns a CartItem with its Product (id, name, thumbnailUrl, imageUrl) and optional ProductVariant (id, variantName, variantValue) populated. Null-safe: only set Variant when VariantName is not null.
Put the JdbcTemplate query in a CartItemRepositoryCustom impl wired via a fragment interface (Spring Data JPA custom-repository pattern).
Verify: mvn -q compile.
Return: files, command, result.
```

## 8.8 SB-04e — Order Repository + Idempotency Index
```
Task SB-04e (Repository Engineer — Orders):
Create OrderRepository and OrderItemRepository (Spring Data JPA). Match .NET OrderRepository:
- findById, findOrdersByCustomer(customerId) — fetch orders + batch-load their OrderItems in a second query, group by OrderId (mirror GetOrdersByCustomer). Order by OrderDate DESC.
- findOrderWithItemsById(id) — order + items.
- findByIdempotencyKey(key) — order + items; null/blank → null.
- save (add Order AND its OrderItems in one transaction — JPA cascade PERSIST on Order.orderItems).
- update, delete.
Confirm V1__baseline.sql (from SB-01) already has the unique index IX_Order_IdempotencyKey. If not, add V2__order_idempotency_index.sql with: CREATE UNIQUE INDEX IX_Order_IdempotencyKey ON [Order](IdempotencyKey) WHERE IdempotencyKey IS NOT NULL.
Table [Order] must be quoted everywhere.
Verify: mvn -q compile.
Return: files, command, result.
```

## 8.9 SB-04f — Repository Integration Tests
```
Task SB-04f (Integration Test Engineer):
Add Testcontainers (mssqlserver) + Flyway to the test scope. Create a seed.sql under test/resources that inserts a small representative dataset (≥2 categories with a child, ≥3 active products with images+variants, a customer, an address, a cart item, an order with items).
Write @SpringBootTest repository ITs: ProductRepositoryIT, CategoryRepositoryIT, CustomerRepositoryIT, AddressRepositoryIT, CartItemRepositoryIT, OrderRepositoryIT.
Each IT boots a SQL Server container, runs Flyway, loads seed.sql, then asserts:
- product search/filter/with-variants,
- category tree grouping,
- cart join query returns product+variant info,
- order batch item fetch,
- findByIdempotencyKey returns the seeded order.
Use @Testcontainers, @Container with MSSQLServerContainer, @DynamicPropertySource for datasource.
Verify: mvn -q test -Dtest='*IT' (may need Docker; if unavailable, report and mark @Disabled with reason).
Return: files, command, result, and whether Docker was available.
```

## 8.10 SB-05a — JwtTokenService
```
Task SB-05a (Security Engineer — JWT):
Create com.ecommerce.security.JwtTokenService using io.jsonwebtoken (jjwt 0.12.x):
- generateToken(int userId, int customerId, String email): claims sub=userId, customer_id=customerId, email=email, jti=UUID, expiration = now+1h, sign HS256 with the key from jwt.signing-key (Base64-decode the configured value; if raw bytes <32 throw). Return compact token.
- validateToken(String): return false on any exception, true otherwise. Validate signature + expiry only (no iss/aud).
- @Component; inject key via @Value or a config bean.
Port the 4 tests from Ecommerce.Api.Core.Tests/JwtTokenServiceTests.cs to JUnit 5: generate returns non-null, claims present (sub, customer_id, email, exp), validate true for valid, false for garbage.
Verify: mvn -q test -Dtest=JwtTokenServiceTests.
Return: files, command, result.
```

## 8.11 SB-05b — SecurityConfig + Principal
```
Task SB-05b (Security Engineer — Spring Security):
Create com.ecommerce.config.SecurityConfig with a SecurityFilterChain bean:
- Stateless session; CSRF disabled; CORS for http://localhost:5173 (dev).
- Public: POST /api/auth/login, POST /api/auth/register, GET /api/products/**, GET /api/categories/**, GET /api/health.
- Authenticated: everything else under /api/** (cart, checkout, orders, /api/auth/me, /api/authtest).
- JWT validation: use spring-boot-starter-oauth2-resource-server with a symmetric-key JwtDecoder (Nimbus) reading jwt.signing-key, OR a custom OncePerRequestFilter that calls JwtTokenService.validateToken. Either is acceptable; pick the resource-server approach.
- Create JwtAuthConverter that converts the JWT into an Authentication whose principal carries customerId (the customer_id claim) and email.
- ClockSkew = 0 (configure Nimbus JwtDecoder with a 0-leeway OAuth2ResourceServerProperties or custom decoder).
Add a helper bean/method to retrieve the current customerId for controllers (e.g., SecurityUtils.currentCustomerId() reading SecurityContextHolder).
Verify: mvn -q compile.
Return: files, command, result, and which JWT approach was chosen.
```

## 8.12 SB-05c — Security Integration Test
```
Task SB-05c (Security Test Engineer):
Write SecurityTests (@SpringBootTest + MockMvc):
- GET /api/products (no token) → 200.
- GET /api/cart (no token) → 401.
- POST /api/auth/login with a seeded customer (via test setup or mocked) → 200 + token.
- GET /api/cart with valid token → 200.
- GET /api/cart with tampered token → 401.
- GET /api/cart with expired token (generate a token exp = now-1h using the same key) → 401.
- Assert the principal's customer_id equals the seeded customer id (expose via a test endpoint or via /api/auth/me).
Verify: mvn -q test -Dtest=SecurityTests.
Return: files, command, result.
```

## 8.13 SB-06a — ProductService
```
Task SB-06a (Domain Logic — Catalog):
Create com.ecommerce.service.ProductService (and interface IProductService mirroring .NET IProductService). Port ProductService.cs exactly:
- getProducts(categoryId, keyword, minPrice, maxPrice, sortBy, page, pageSize): default page=1, pageSize=12; clamp <1. If categoryId present → getProductsByCategoryTree(categoryId); else activeProducts. Keyword filter on name/description case-insensitive. Price min/max filter. Sort switch: price_asc, price_desc, name, default Id DESC. Paginate. Map to ProductListResponseDto (categoryName = parent category name when categoryId filter present, else null). Return PagedResult.
- getProductsByCategoryTree(rootCategoryId): in-memory BFS across all categories collecting ids; then filter active products by those categoryIds. Keep the .NET comment about a future CTE but DO NOT implement a CTE.
- getProductDetail(id): load product with images+variants; map to ProductDetailDto; categoryName from category lookup; null if not found.
- getFeaturedProducts(count=6): first N active products → ProductListResponseDto (no categoryName).
Port ProductServiceTests.cs to JUnit 5 + Mockito (mock IProductRepository, ICategoryRepository).
Verify: mvn -q test -Dtest=ProductServiceTests.
Return: files, command, result.
```

## 8.14 SB-06b — CategoryService
```
Task SB-06b (Domain Logic — Category):
Create com.ecommerce.service.CategoryService (interface ICategoryService). Port CategoryService.cs:
- getCategoryTree(): load all (GetAllWithProducts), roots = parentCategoryId==null, build recursively; Children = empty-list→null (serialize as null) for leaves. Return List<CategoryDto>.
- getById(id): null if missing.
Add JUnit5 + Mockito tests.
Verify: mvn -q test -Dtest=CategoryServiceTests.
Return: files, command, result.
```

## 8.15 SB-06c — CartService
```
Task SB-06c (Domain Logic — Cart):
Create com.ecommerce.service.CartService (interface ICartService). Port CartService.cs:
- getCartItems(customerId): map to CartItemDto; product name fallback "Unknown Product"; imageUrl fallback thumbnailUrl||imageUrl; lineTotal = unitPrice*quantity.
- getCartSummary(customerId): itemCount = sum qty; subTotal = sum unitPrice*qty; total = subTotal (equal).
- addToCart(customerId, productId, quantity, variantId): if qty<1 → 1; merge into existing line (same productId+variantId) by incrementing; else fetch product (throw IllegalArgumentException if null) and insert with unitPrice=product.price, createdDate=now.
- updateQuantity(customerId, cartItemId, quantity): null/owned check; if qty<1 delete; else set qty.
- removeFromCart(customerId, cartItemId): null/owned check; delete.
- clearCart(customerId).
Port CartServiceTests.cs to JUnit5+Mockito.
Verify: mvn -q test -Dtest=CartServiceTests.
Return: files, command, result.
```

## 8.16 SB-06d — OrderService
```
Task SB-06d (Domain Logic — Orders):
Create com.ecommerce.service.OrderService (interface IOrderService). Port OrderService.cs:
- getOrderById(orderId): order with items → OrderConfirmationDto (shippingAddressFormatted=null) or null.
- getOrderHistory(customerId): orders → OrderDto list.
Add JUnit5+Mockito tests.
Verify: mvn -q test -Dtest=OrderServiceTests.
Return: files, command, result.
```

## 8.17 SB-06e — CheckoutService
```
Task SB-06e (Checkout/Transaction Specialist):
Create com.ecommerce.service.CheckoutService (interface ICheckoutService). Port CheckoutService.cs EXACTLY. This is the highest-risk task.
- @Transactional on placeOrder.
- Validate address method (non-blank checks).
- getCustomerAddresses(customerId) → AddressDto list.
- getOrderConfirmation(orderId) → OrderConfirmationDto or null (uses BuildConfirmation).
- placeOrder(customerId, addressId, shippingMethod, idempotencyKey):
    1. If idempotencyKey blank → IllegalArgumentException.
    2. findByIdempotencyKey → if present return BuildConfirmation (idempotent replay).
    3. Within the transaction:
       a. getCartItemsByCustomer(customerId); if empty → throw a BusinessException("Cart is empty.") (maps to 400).
       b. For each item with variantId: getVariantById, decrement stock (min 0), save.
       c. Resolve shippingAddr = addressRepository.findById(addressId).orElseThrow("Address not found.") or if addressId null → "Address is required."
       d. Create billingAddr copy (AddressType="Billing", IsDefault=false) and save.
       e. subTotal = sum(unitPrice*qty); shippingTotal = express→199, standard→199? NO: standard→99, default→99; taxTotal = (subTotal+shipping)*0.18 with BigDecimal HALF_EVEN; grandTotal = sum.
       f. order = new Order(customerId, now, "Paid", orderNumber=RF-yyyyMMdd-NNNN where NNNN=ThreadLocalRandom.nextInt(1000,9000), subTotal, shipping, tax, grand, shippingAddr.id, billingAddr.id, idempotencyKey, orderItems mapped).
       g. save(order) (cascade persists items).
       h. clearCartByCustomerId(customerId).
       i. return BuildConfirmation(order).
    4. Catch DataIntegrityViolationException → re-find by idempotencyKey → BuildConfirmation(duplicate) or rethrow.
- BuildConfirmation: OrderConfirmationDto(order.id, orderNumber, orderDate, "Paid", subTotal, shipping, tax, grand, FormatAddress(...)=null, items mapped to OrderItemDto).
- getShippingOptions(): [Standard ₹99 label "Standard Shipping (5-7 business days)", Express ₹199 label "Express Shipping (2-3 business days)"].
- validatePayment(PaymentRequest): null→false; non-blank cardholderName; cardNumber regex ^\d{16}$; expirationDate ^(0[1-9]|1[0-2])/\d{2}$; cvv ^\d{3,4}$.
Add ported + new tests: idempotent replay returns same confirmation, empty cart throws, express shipping=199, GST math parity (assert a known subTotal+shipping → expected tax/grand with HALF_EVEN), payment good/bad cards, order number format regex.
Verify: mvn -q test -Dtest=CheckoutServiceTests.
Return: files, command, result, and explicitly confirm GST rounding mode + the standard=99/express=199/default=99 switch.
```

## 8.18 SB-07a — Exception Advice
```
Task SB-07a (Error Envelope Engineer):
Create com.ecommerce.exception.BusinessException (extends RuntimeException; maps to 400).
Create com.ecommerce.exception.GlobalExceptionHandler (@RestControllerAdvice) returning {"message": "<msg>"} (camelCase key) with:
- BusinessException / IllegalArgumentException → 400
- AccessDeniedException / AuthenticationException → 401
- EntityNotFoundException (jakarta) → 404
- DataIntegrityViolationException (non-idempotency) → 400
- Exception → 500 with generic "An error occurred." (do NOT leak stack traces)
Match the .NET controller error shapes exactly: { message: "..." }.
Add a MockMvc test hitting a controller that throws each type.
Verify: mvn -q test -Dtest=GlobalExceptionHandlerTests.
Return: files, command, result.
```

## 8.19 SB-07b — Auth Controllers
```
Task SB-07b (API Engineer — Auth):
Create com.ecommerce.controller.AuthController (@RestController @RequestMapping("/api/auth")) and AuthTestController.
- POST /login (LoginRequest): find customer by email; if null → 401 {"message":"Invalid credentials."}. (Preserve the stub: do NOT verify password.) Generate token via JwtTokenService.generateToken(customer.id, customer.id, customer.email). Return 200 {"token": "..."}.
- POST /register (RegisterRequest): if email exists → 400 {"message":"Email already registered."}. Else create Customer(email, firstName, lastName, createdDate=now), save, generate token, return 200 {"token":"..."}.
- GET /me (authenticated): read customerId from SecurityUtils; find customer; 401 if token invalid, 404 if not found; return 200 {id, email, firstName, lastName, createdDate}.
- AuthTestController: GET /api/authtest (authenticated) → {"message":"Authenticated"}.
Port relevant controller tests to MockMvc (mock ICustomerRepository + JwtTokenService).
Verify: mvn -q test -Dtest=AuthControllerTests.
Return: files, command, result.
```

## 8.20 SB-07c — ProductsController
```
Task SB-07c (API Engineer — Products):
Create com.ecommerce.controller.ProductsController (@RestController @RequestMapping("/api/products")). Port ProductsController.cs:
- GET (query: Integer categoryId, String searchQuery, BigDecimal minPrice, BigDecimal maxPrice, String sortBy, int page=1, int pageSize=12) → 200 PagedResult<ProductListResponseDto>.
- GET /{id} → 200 ProductDetailDto or 404.
- GET /featured (int count=6) → 200 List<ProductListResponseDto>.
Use @RequestParam(required=false) for the optional ones; primitives default via @RequestParam(defaultValue="1").
Port ProductsControllerTests.cs to MockMvc (mock IProductService).
Verify: mvn -q test -Dtest=ProductsControllerTests.
Return: files, command, result.
```

## 8.21 SB-07d — CategoriesController
```
Task SB-07d (API Engineer — Categories):
Create com.ecommerce.controller.CategoriesController (@RequestMapping("/api/categories")): GET → 200 tree; GET /{id} → 200 or 404.
Add MockMvc tests.
Verify: mvn -q test -Dtest=CategoriesControllerTests.
Return: files, command, result.
```

## 8.22 SB-07e — CartController (raw-int body)
```
Task SB-07e (API Engineer — Cart):
Create com.ecommerce.controller.CartController (@RestController @RequestMapping("/api/cart"), class-level @PreAuthorize or rely on SecurityConfig authenticated rule). Port CartController.cs:
- GET → cart items list.
- GET /summary → CartSummaryDto.
- POST /items (AddCartItemRequest): if quantity<1 || >999 → 400 {"message":"Quantity must be between 1 and 999."}; call service.addToCart; return {"success":true,"itemCount":summary.itemCount,"cartTotal":summary.total}.
- PUT /items/{id} with a RAW INTEGER body: use @RequestBody Integer quantity (Spring binds a bare JSON number to Integer). If quantity<0 || >999 → 400 {"message":"Quantity must be between 0 and 999."}; call updateQuantity; return {"success":true,"itemCount":...,"cartTotal":...}.
- DELETE /items/{id} → service.removeFromCart; return {"success":true,"itemCount":...,"cartTotal":...}.
- DELETE → service.clearCart; return {"success":true}.
customerId from SecurityUtils.currentCustomerId().
Port CartControllerTests.cs to MockMvc. Add an explicit test that PUT sends body `3` (not {"quantity":3}) and succeeds.
Verify: mvn -q test -Dtest=CartControllerTests.
Return: files, command, result, and confirm the raw-int body handling.
```

## 8.23 SB-07f — CheckoutController
```
Task SB-07f (API Engineer — Checkout):
Create com.ecommerce.controller.CheckoutController (@RequestMapping("/api/checkout")). Port CheckoutController.cs:
- GET /addresses → AddressDto list.
- POST /addresses (AddressDto): hardcode AddressType="Shipping"; inject IAddressRepository (NOT service-locator); save; return {"id": address.id}.
- GET /review → {} (empty object).
- GET /shipping-options → ShippingOptionDto list.
- POST /validate-payment (PaymentRequest) → {"valid": boolean}.
- POST /orders (PlaceOrderRequest): require header "Idempotency-Key" (blank → 400 {"message":"Idempotency-Key header is required."}); call checkoutService.placeOrder(customerId, addressId, shippingMethod ?? "Standard", idempotencyKey); return 201 OrderConfirmationDto; catch BusinessException → 400 {"message":ex.message}.
- GET /orders/{id}/confirmation → OrderConfirmationDto or 404.
customerId from SecurityUtils.
Port CheckoutControllerTests.cs to MockMvc (mock ICheckoutService). Confirm 201 status and missing-header 400.
Verify: mvn -q test -Dtest=CheckoutControllerTests.
Return: files, command, result.
```

## 8.24 SB-07g — OrdersController
```
Task SB-07g (API Engineer — Orders):
Create com.ecommerce.controller.OrdersController (@RequestMapping("/api/orders")). Port OrdersController.cs:
- GET → OrderDto list (history).
- GET /{id} → OrderConfirmationDto or 404.
customerId from SecurityUtils.
Add MockMvc tests.
Verify: mvn -q test -Dtest=OrdersControllerTests.
Return: files, command, result.
```

## 8.25 SB-07h — HealthController
```
Task SB-07h (API Engineer — Health):
Create com.ecommerce.controller.HealthController (@RequestMapping("/api/health")): run SELECT 1 via JdbcTemplate.queryForObject; return {"status":"Healthy","db":1}; on exception return 500 {"status":"Unhealthy","error":ex.message}.
Add MockMvc test (mock JdbcTemplate) for both happy + failure.
Verify: mvn -q test -Dtest=HealthControllerTests.
Return: files, command, result.
```

## 8.26 SB-08a — Capture .NET Contracts
```
Task SB-08a (Contract Capture Technician):
Start the .NET backend (dotnet run --project src/backend/Ecommerce.Api/Ecommerce.Api.csproj) against the existing LegacyEcommerceDb. For each of these endpoints, capture a representative JSON response into src/backend-java/src/test/resources/contracts/<name>.json:
- POST /api/auth/login (use a known seeded customer email) → token then call authenticated endpoints
- GET /api/auth/me
- GET /api/products (default), GET /api/products?categoryId=&sortBy=price_asc, GET /api/products/{id}, GET /api/products/featured
- GET /api/categories, GET /api/categories/{id}
- GET /api/cart, GET /api/cart/summary
- POST /api/cart/items, PUT /api/cart/items/{id} (raw int), DELETE /api/cart/items/{id}
- GET /api/checkout/addresses, POST /api/checkout/addresses, GET /api/checkout/shipping-options, POST /api/checkout/validate-payment, POST /api/checkout/orders (with Idempotency-Key), GET /api/checkout/orders/{id}/confirmation
- GET /api/orders, GET /api/orders/{id}
- GET /api/health
Also capture the status code per file in a sibling .meta.json.
If the .NET app cannot be started (no SQL Server), instead synthesize the contracts from the .NET controller + DTO source code by hand, clearly marking each file as "synthesized".
Return: list of captured files + any that were synthesized.
```

## 8.27 SB-08b — Contract Tests
```
Task SB-08b (Parity Test Engineer):
Create com.ecommerce.ContractTests (@SpringBootTest + TestRestTemplate + Testcontainers SQL Server + Flyway + seed.sql from SB-04f).
For each contract file from SB-08a, replay the request against the Spring backend and assert:
- HTTP status code matches the .meta.json.
- JSON field NAMES match exactly (camelCase).
- JSON field TYPES match (number vs string vs boolean; numbers within tolerance 0 for money, exact for ints).
- For list endpoints, array length matches the seed.
Use JSONassert in LENIENT mode for structure first, then STRICT for per-field type checks.
Cover at least: login, me, products (all variants), categories, cart full lifecycle, checkout full flow incl. idempotent replay, orders, health.
Verify: mvn -q test -Dtest=ContractTests (requires Docker).
Return: files, command, result, and a per-endpoint pass/fail table.
```

## 8.28 SB-08c — Playwright E2E Against Spring
```
Task SB-08c (E2E Runner):
Point the Vue SPA at the Spring backend (set VITE_API_BASE_URL=http://localhost:5001/api and ensure Spring CORS allows the SPA origin). Start Spring backend + SPA dev server.
Run: cd tests/e2e && npx playwright test.
Fix ONLY configuration issues (base URL, CORS, env) — do NOT change E2E test assertions. If an E2E test fails due to a Spring parity bug, report it (do NOT patch in this task; file it back to the relevant SB-07/06 task).
Verify: npx playwright test passes (or report failures with root-cause classification).
Return: pass/fail, list of failing tests + classification (config vs parity bug).
```

## 8.29 SB-08d — Parity Spot Tests
```
Task SB-08d (Numerics/Security Auditor):
Write ParityTests covering:
- GST: for subTotal=1999, shipping=99 → taxTotal = (1999+99)*0.18 = 377.64 (BigDecimal HALF_EVEN); grandTotal = 2475.64. Compare to the .NET result captured in SB-08a's checkout order contract.
- Order number: matches regex ^RF-\d{8}-\d{4}$ and the numeric suffix is in [1000,9999).
- JWT: generated token decoded yields sub/customer_id/email/jti and exp = iat+3600s; validateToken false for expired and tampered.
- Payment regex: valid 4111111111111111 / 12/25 / 123 → true; short card → false; bad expiry 13/25 → false; cvv 12 → false.
Verify: mvn -q test -Dtest=ParityTests.
Return: files, command, result.
```

## 8.30 SB-09a — Docker
```
Task SB-09a (DevOps — Docker):
Create src/backend-java/Dockerfile: Maven multi-stage (builder stage runs mvn -B package; runtime stage uses eclipse-temurin:21-jre). Expose 5001.
Update .github/workflows/docker-build.yml to also build the Java image (keep the existing .NET image build if present; do not remove it).
Verify: docker build -t ecommerce-java src/backend-java (if Docker available; else report).
Return: files, command, result.
```

## 8.31 SB-09b — CI Workflows
```
Task SB-09b (DevOps — CI):
Update .github/workflows/ci.yml and pr-validation.yml:
- Add a Java job (or replace the backend job) that runs: actions/setup-java@v4 (temurin 21), cache maven, mvn -B verify.
- Keep frontend jobs unchanged.
- Do NOT delete .NET jobs yet (parallel verification); add the Java job alongside.
Verify: workflow YAML valid (actionlint if available).
Return: files changed, result.
```

## 8.32 SB-09c — Makefile
```
Task SB-09c (Build Engineer — Makefile):
Add to Makefile:
- build-java: cd src/backend-java && mvn -B -q package -DskipTests
- test-java: cd src/backend-java && mvn -B -q test
- run-java: cd src/backend-java && mvn -q spring-boot:run
- verify-java: cd src/backend-java && mvn -B -q verify
Wire `build: build-backend build-java`, `test: test-backend test-java`.
Verify: make build-java.
Return: files, command, result.
```

## 8.33 SB-09d — Documentation
```
Task SB-09d (Technical Writer):
Update docs per docs/migration/dotnet-to-springboot/10-documentation-strategy.md:
- README.md: add Java backend section (prereqs Java 21 + Maven, run commands), keep .NET section as legacy reference.
- docs/architecture: add target-state-spring.md; update monorepo-design.md to mention src/backend-java.
- docs/runbooks/local-setup.md: add Java setup steps.
- docs/api/endpoint-catalog.md: add a note that endpoints are now served by Spring Boot (no API changes).
- docs/decisions: add ADR "Adopt Spring Boot backend (parity migration)".
- docs/migration: add final report dotnet-to-springboot/MIGRATION_REPORT.md summarizing outcome.
Do NOT claim parity the docs don't already assert; reference ContractTests + E2E results.
Return: files changed.
```