.PHONY: help \
	restore init \
	build build-backend build-frontend \
	test test-backend test-frontend test-e2e \
	lint verify \
	docker-up docker-down \
	clean

MVN := mvn
NODE := npm

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ------------------------
# Restore Dependencies
# ------------------------

restore: ## Restore backend and frontend dependencies
	cd src/backend && $(MVN) dependency:resolve -B
	cd src/frontend/spa && $(NODE) ci

# ------------------------
# Build
# ------------------------

build: build-backend build-frontend ## Build entire project

build-backend: ## Build Java Spring Boot backend
	cd src/backend && $(MVN) clean package -DskipTests -B

build-frontend: ## Build Vue frontend
	cd src/frontend/spa && $(NODE) run build

# ------------------------
# Testing
# ------------------------

test: test-backend test-frontend ## Run backend and frontend tests

test-backend: ## Run Java Spring Boot tests
	cd src/backend && $(MVN) test -B

test-frontend: ## Run Vue unit tests
	cd src/frontend/spa && $(NODE) run test:unit -- --run

test-e2e: ## Run Playwright end-to-end tests
	cd tests/e2e && npx playwright test

# ------------------------
# Quality
# ------------------------

lint: ## Type check frontend
	cd src/frontend/spa && npx vue-tsc --noEmit

verify: ## Run full verification (same as CI)
	cd src/backend && $(MVN) clean package -B
	cd src/frontend/spa && $(NODE) run build
	cd src/frontend/spa && npx vitest run
	cd tests/e2e && npx playwright test

# ------------------------
# Docker
# ------------------------

docker-up: ## Start application stack
	docker compose -f docker/compose/docker-compose.yml up -d

docker-down: ## Stop application stack
	docker compose -f docker/compose/docker-compose.yml down

# ------------------------
# Cleanup
# ------------------------

clean: ## Remove build artifacts
	cd src/backend && $(MVN) clean -B
	cd src/frontend/spa && rm -rf dist

init: restore ## Initialize the project
	@echo "Project initialized successfully."