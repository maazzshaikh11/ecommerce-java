# Local Development Setup

## Prerequisites

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 21+ | Spring Boot backend, tests |
| Maven | 3.9+ | Java build, dependency resolution |
| Node.js | 22+ | Frontend SPA, E2E tests |
| npm | 10+ | Package management |
| Docker | — | SQL Server, containerized deployment |
| Git | 2.x | Version control |

## Step 1: Clone

```bash
git clone <repository-url>
cd ecommerce
```

## Step 2: Database (SQL Server via Docker)

```bash
# Start SQL Server container
docker run -d --name sqlserver \
  -e "ACCEPT_EULA=Y" \
  -e "MSSQL_SA_PASSWORD=YourStrong@Passw0rd" \
  -p 1433:1433 \
  mcr.microsoft.com/mssql/server:2022-latest

# Create the database
docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C \
  -d master -Q "CREATE DATABASE LegacyEcommerceDb"

# Apply the baseline schema
docker exec -i sqlserver /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "YourStrong@Passw0rd" -C \
  -d LegacyEcommerceDb -i src/backend/src/main/resources/db/migration/V1__baseline.sql
```

The connection string is in `src/backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost;databaseName=LegacyEcommerceDb;encrypt=true;trustServerCertificate=true;user=sa;password=YourStrong@Passw0rd
```

## Step 3: Backend

```bash
cd src/backend

# Build
mvn clean package -B

# Run the API (port 5001)
mvn spring-boot:run
```

Verify: `GET http://localhost:5001/api/health` returns `200 OK`.

## Step 4: Frontend

```bash
cd src/frontend/spa

# Install dependencies
npm install

# Start dev server (port 5174)
npm run dev
```

Verify: `http://localhost:5174` loads the SPA.

## Environment Variables

| Variable | Location | Default | Purpose |
|----------|----------|---------|---------|
| `jwt.signing-key` | `application.yml` | (dev key) | JWT signing key (≥32 bytes, base64) |
| `DB_HOST` | env | `localhost` | SQL Server hostname (Docker: `db`) |
| `DB_USER` | env | `sa` | SQL Server username |
| `DB_PASSWORD` | env | `YourStrong@Passw0rd` | SQL Server password |
| `VITE_API_BASE_URL` | `.env.development` | `http://localhost:5001/api` | API base URL for SPA |

> **Warning:** Never use the development JWT signing key in production. Generate a new key with at least 32 bytes of entropy.

## Verification

```bash
# Backend tests
cd src/backend
mvn test

# Frontend tests
cd src/frontend/spa
npm run build   # includes type check via vue-tsc
npx vitest run  # unit tests

# E2E tests
cd tests/e2e
npx playwright test
```

## Troubleshooting

### Database Connection Failed
- Ensure the Docker container is running: `docker ps | grep sqlserver`
- Check connection string in `application.yml` matches your SQL Server instance
- Verify port 1433 is not in use: `lsof -i :1433`

### Port Already in Use
- API default: 5001, SPA default: 5174
- Change port in `application.yml` (API) or `vite.config.ts` (SPA)

### JWT Validation Failed
- Ensure `jwt.signing-key` in `application.yml` is at least 32 bytes (base64-encoded)
- Check that the SPA's `api/client.ts` reads from `localStorage['jwt']`

## Related

- [Spring Boot Setup](spring-setup.md)
- [Architecture — Deployment Model](../architecture/deployment-model.md)
- [API Endpoint Catalog](../api/endpoint-catalog.md)
- [Runbook — Database Seeding](database-seeding.md)