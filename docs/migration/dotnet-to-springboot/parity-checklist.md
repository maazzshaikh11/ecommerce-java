# Spring Boot Parity Checklist

This checklist maps each known parity trap from the migration verification strategy to the test or code artifact that proves parity. Reviewers should tick each item before signing off on the Java backend.

## Authentication

| Trap | Test / Evidence | Status |
|------|----------------|--------|
| JWT claim set identical (`sub`, `customer_id`, `email`, `jti`) | `JwtTokenServiceTest.generateAndValidateToken_shouldSucceed` | |
| HS256 signing, same key, 1h expiry | `JwtTokenServiceTest.validateToken_withTamperedToken_shouldReturnFalse` | |
| No `iss`/`aud`, ClockSkew=0 | Code review: `JwtAuthFilter.java` line 43 | |
| Login accepts any password for known email | `EcommerceApplicationIntegrationTest.login_withValidCredentials_shouldReturnToken` | |
| `/api/auth/me` returns customer profile | Controller test + manual cURL | |

## Product Catalog

| Trap | Test / Evidence | Status |
|------|----------------|--------|
| Featured products sorted by name | `EcommerceApplicationIntegrationTest.featuredProducts_shouldReturnList` | |
| Product detail loads images + variants (no `MultipleBagFetchException`) | `ProductService.getProductDetail()` + `ProductService.getProductImages()` / `getProductVariants()` | |
| Category tree with nested children | `EcommerceApplicationIntegrationTest.getCategories_shouldReturnList` | |
| Search with keyword + price filter + sort | Manual: `GET /api/products?searchQuery=mouse&minPrice=100&maxPrice=1000&sortBy=price` | |

## Cart

| Trap | Test / Evidence | Status |
|------|----------------|--------|
| Raw integer PUT body (`3`) for quantity update | `CartControllerTest.updateCartItem_shouldReturnUpdatedSummary` | |
| Add item validates quantity range (1-999) | `CartController.addItem` @ lines 40-42 | |
| Cart summary includes itemCount + total | `CartControllerTest.getCartSummary_shouldReturnSummary` | |
| Cart SQL uses H2 snake_case column names | `CartItemRepositoryImpl.java` SQL string | |

## Checkout & Orders

| Trap | Test / Evidence | Status |
|------|----------------|--------|
| Idempotent order placement (`IdempotencyKey`) | `CheckoutService.placeOrder` + `OrderRepository.findByIdempotencyKey` | |
| GST calculated as `(subTotal + shipping) * 0.18`, HALF_EVEN rounding | `CheckoutService.java` lines 86-98 | |
| Shipping: Standard ₹99, Express ₹199 | `CheckoutServicePureTest.getShippingOptions_shouldReturnTwoOptions` | |
| Address validation (all fields non-blank) | `CheckoutServicePureTest.validateAddress_withValidInput_shouldReturnTrue` | |
| Payment card validation (Luhn-like 16-digit, MM/YY, 3-4 CVV) | `CheckoutServicePureTest.validatePayment_withValidCard_shouldReturnTrue` | |
| `Order` entity avoids JPQL reserved word | `@Entity(name = "EcommerceOrder")` + `@Table(name = "\"Order\"")` | |
| `OrderItem` bidirectional `@ManyToOne` with cascade | `Order.java` line 51, `OrderItem.java` lines 15-17 | |
| Order confirmation includes items list | `OrderService.buildConfirmation` + `OrderRepository.findOrderWithItems` | |

## Infrastructure

| Trap | Test / Evidence | Status |
|------|----------------|--------|
| Port 5001 | `application.yml` + Dockerfile `EXPOSE 5001` | |
| `/api/health` returns `{"status":"Healthy","db":1}` | `EcommerceApplicationIntegrationTest.healthEndpoint_shouldReturnHealthy` | |
| CORS allows `http://localhost:5173` | `SecurityConfig.corsConfigurationSource()` | |
| Dev profile: H2 in-memory, Flyway disabled | `application.yml` + `application-dev.yml` | |
| Prod profile: SQL Server, Flyway baseline | `application-prod.yml` | |
| `spring.jpa.open-in-view=false` | `application.yml` line 22 | |
| Jackson `LOWER_CAMEL_CASE` + `non_null` | `application.yml` lines 9-10 | |

## Known Deviations (Documented)

| Deviation | Reason | Ticket |
|-----------|--------|--------|
| Password verification is a stub | Preserved from .NET backend; ASP.NET Identity hash algorithm unknown | T-legacy-auth |
| `FormatAddress` returns `null` | Preserved from .NET; frontend handles null gracefully | T-legacy-address |

## Sign-off

Reviewer: _________________  Date: _________________

All items ticked?  Yes / No
If No, list blockers:
