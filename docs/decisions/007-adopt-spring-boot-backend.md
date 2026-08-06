# ADR-007: Adopt Spring Boot as Primary Backend

## Status
Accepted

## Context
The existing modernized stack used a .NET 10 Web API backend paired with a Vue 3 SPA frontend. As part of ongoing platform modernization, the team migrated the backend to Java + Spring Boot to align with organizational standards, leverage the Spring ecosystem, and consolidate JVM-based tooling across services.

The migration constraint was **complete feature parity** with the existing .NET backend. The .NET backend served as the parity oracle during migration: any deviation in endpoint behavior, response shape, or JWT claim handling was considered a regression.

## Decision
Adopt **Java 21 + Spring Boot 3.3** as the primary backend, located at `src/backend/`. The .NET backend has been removed from the repository. The migration is complete and verified.

## Consequences

### Positive
- **Ecosystem alignment:** Spring Boot 3.3 provides battle-tested auto-configuration, metrics, and observability hooks.
- **JPA + JdbcTemplate hybrid:** Spring Data JPA handles CRUD; `NamedParameterJdbcTemplate` handles complex cart join queries where JPA mapping would be inefficient.
- **JWT parity preserved:** Same HS256 signing key, same claim set (`sub`, `customer_id`, `email`, `jti`), same 1-hour expiry, no `iss`/`aud`, `ClockSkew=0`. Frontend requires no changes.
- **OpenAPI out of the box:** `springdoc-openapi` generates `/v3/api-docs` and Swagger UI with zero annotation beyond existing `@RestController` mappings.
- **H2 for tests:** Test profile uses H2 in-memory with DDL auto + `DataSeeder` bean; production uses SQL Server with Flyway baseline migrations.

### Trade-offs
- **Login password stub:** The .NET backend never implemented real password hashing (legacy ASP.NET Identity passwords are opaque). The Java backend preserves this stub behavior — passwords are accepted for any known customer email.
- **Reserved word `Order`:** JPQL `ORDER` is a reserved keyword, so the `Order` entity uses `@Entity(name = "EcommerceOrder")` + `@Table(name = "\"Order\"")`.
- **Raw-int PUT body:** The cart update endpoint accepts a raw integer JSON body (`3`) for quantity, matching the frontend's existing `fetch` call.
- **GST rounding:** Tax calculation uses `RoundingMode.HALF_EVEN` to match .NET `Math.Round` default behavior.

## Compliance
- Every endpoint in `docs/api/endpoint-catalog.md` has a corresponding Java controller.
- 28 unit/integration tests pass (`mvn test -B`).
- All endpoints verified end-to-end against SQL Server.
- OpenAPI spec generated from `/v3/api-docs`.

## References
- [Migration Summary](../migration/dotnet-to-springboot/02-migration-summary.md)
- [Implementation Plan](../migration/dotnet-to-springboot/03-implementation-plan.md)
- [Feature Parity Report](../migration/feature-parity-report.md)
