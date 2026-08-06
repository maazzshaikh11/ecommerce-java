# Spring Boot Backend Runbook

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| JDK | 21+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Docker | — | `docker -v` |
| Git | 2.x | `git --version` |

## Quick Start (Default Profile — SQL Server)

Start the SQL Server Docker container:

```bash
docker run -d --name sqlserver \
  -e "ACCEPT_EULA=Y" \
  -e "MSSQL_SA_PASSWORD=YourStrong@Passw0rd" \
  -p 1433:1433 \
  mcr.microsoft.com/mssql/server:2022-latest
```

Create the database and schema (run once):

```bash
docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C \
  -d master -Q "CREATE DATABASE LegacyEcommerceDb"

docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C \
  -d LegacyEcommerceDb -i src/backend/src/main/resources/db/migration/V1__baseline.sql
```

Run the application:

```bash
cd src/backend

# Build + test
mvn clean package -B

# Run with default profile (SQL Server, no Flyway)
mvn spring-boot:run

# API available at http://localhost:5001
```

The default profile:
- Connects to SQL Server `LegacyEcommerceDb` on localhost:1433
- Disables Flyway and `sql.init`
- Does **not** run `DataSeeder` (use test profile for seeded data)
- Sets `PhysicalNamingStrategyStandardImpl` to preserve PascalCase column names

### SQL Server Connection

```yaml
# application.yml snippet
spring:
  datasource:
    url: jdbc:sqlserver://localhost;databaseName=LegacyEcommerceDb;encrypt=true;trustServerCertificate=true;user=sa;password=YourStrong@Passw0rd
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    hibernate:
      ddl-auto: none
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
```

## Test Profile (H2 In-Memory)

```bash
# Run tests with H2 in-memory DB (seed data auto-inserted)
mvn test
```

The test profile (`application-test.yml`):
- Uses H2 in-memory database (`LegacyEcommerceDb`)
- Runs `DataSeeder` on startup
- `ddl-auto: create-drop`
- Disables Flyway

## Production Profile (SQL Server + Flyway)

```bash
# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export JWT_SIGNING_KEY="your-base64-encoded-32-byte-key"

# Run JAR
java -jar target/ecommerce-api-1.0.0.jar
```

The prod profile (`application-prod.yml`):
- Connects to SQL Server via `jdbc:sqlserver://...`
- Enables Flyway for migration management
- Uses `V1__baseline.sql` for schema creation
- No seed data

## Testing

```bash
# Run all tests
mvn test -B

# Run a specific test class
mvn test -Dtest=JwtTokenServiceTest
```

## Docker

```bash
# Build image
cd src/backend
docker build -t ecommerce-api:latest .

# Run container
docker run -p 5001:5001 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e JWT_SIGNING_KEY=... \
  -e DB_HOST=... \
  -e DB_USER=sa \
  -e DB_PASSWORD=... \
  ecommerce-api:latest
```

Or use the compose stack:

```bash
docker compose -f docker/compose/docker-compose.yml up -d
```

## Common Issues

### `MultipleBagFetchException`
**Symptom:** Hibernate throws when fetching product with images + variants.
**Fix:** `ProductService.getProductDetail()` loads images and variants via separate repository queries instead of a single `JOIN FETCH`. See `ProductRepository.findImagesByProductId` and `findVariantsByProductId`.

### `ORDER` reserved word in H2/Hibernate
**Symptom:** JPQL parse error on `FROM Order`.
**Fix:** Entity uses `@Entity(name = "EcommerceOrder")` + `@Table(name = "\"Order\"")`. Repository queries reference `EcommerceOrder`.

### Cart SQL column case mismatch
**Symptom:** SQL Server query fails with "Invalid column name".
**Fix:** `CartItemRepositoryImpl` uses PascalCase column names with aliases (`ci.SessionId AS session_id`) to match SQL Server schema while keeping RowMapper compatibility. The `PhysicalNamingStrategyStandardImpl` ensures Hibernate respects explicit `@Column(name = "...")` annotations.

### JWT validation fails with ClockSkew
**Symptom:** Tokens rejected immediately after generation.
**Fix:** `JwtAuthFilter` sets `clockSkewSeconds(0)` to ensure tokens are valid only within their exact validity window.

## Debugging

```bash
# Enable debug logging
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_WEB=DEBUG
export LOGGING_LEVEL_ORG_SPRINGFRAMEWORK_SECURITY=DEBUG

# Or add to application.yml
logging:
  level:
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## API Documentation

- OpenAPI JSON: `http://localhost:5001/v3/api-docs`
- Swagger UI: `http://localhost:5001/swagger-ui.html`
- Health: `http://localhost:5001/api/health`