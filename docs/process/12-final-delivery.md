# 12 — Final Delivery

**Final validation, repository cleanup, and sign-off criteria.**

---

## Prerequisites

- [09-build-and-verification.md](09-build-and-verification.md) — all verification rings pass
- All phases complete and gates passed

---

## Final Validation

### Step 1: Automated Tests

All tests must pass:

| Check | Command | Expected |
|-------|---------|----------|
| Backend builds | `mvn clean package -B` | BUILD SUCCESS |
| Backend tests | `mvn test` | 28/28 pass, 0 failures |
| Frontend builds | `npm run build` | Success |

### Step 2: End-to-End Smoke Test

Start the app against the real target database. Hit every endpoint with `curl`. Verify status codes and JSON response shapes.

| Method | Endpoint | Expected Status | Auth |
|--------|----------|-----------------|------|
| GET | `/api/health` | 200 | No |
| GET | `/api/products` | 200 | No |
| GET | `/api/products/featured` | 200 | No |
| GET | `/api/products/{id}` | 200 | No |
| GET | `/api/categories` | 200 | No |
| POST | `/api/auth/login` | 200 | No |
| GET | `/api/cart` | 200 | JWT |
| GET | `/api/cart/summary` | 200 | JWT |
| POST | `/api/checkout/orders` | 201 | JWT |
| GET | `/api/checkout/shipping-options` | 200 | JWT |
| GET | `/api/checkout/addresses` | 200 | JWT |
| GET | `/api/checkout/orders/{id}/confirmation` | 200 | JWT |

### Step 3: Repository Cleanup

Remove all migration artifacts so the repository reads as a native project:

| Action | Verification |
|--------|-------------|
| Remove legacy source directories | `find . -name "*.cs"` returns nothing |
| Remove legacy root files (solution files, build props, SDK pins) | `ls` confirms removed |
| Remove placeholder directories | `ls src/` shows only active projects |
| Rename migration directory to final name | Target path exists |
| Update Makefile (remove legacy targets) | `make build` works |
| Update CI workflows (remove legacy jobs) | Workflows reference only active stack |
| Update Docker compose (correct build contexts) | Compose references correct paths |
| Update README (rewritten as native project) | No "migration" language in active sections |
| Update dependabot (remove legacy package ecosystem, add new) | Correct ecosystems listed |
| Update .gitignore/.editorconfig/.gitattributes | Legacy sections removed, new sections added |
| Build still passes | `mvn clean package -B` → BUILD SUCCESS |
| Tests still pass | `mvn test` → all pass |
| Frontend still builds | `npm run build` → success |

---

## Sign-Off Criteria

All of the following must be true before the project is considered delivered:

- [ ] All automated tests pass (backend + frontend)
- [ ] E2E smoke test against real database passes for all endpoints
- [ ] Repository contains no legacy source files
- [ ] Repository contains no legacy build configuration
- [ ] README describes the project as a native project (not a migration)
- [ ] CI workflows build and test only the active stack
- [ ] Docker compose references correct paths
- [ ] Build succeeds after cleanup
- [ ] Tests pass after cleanup
- [ ] Frontend builds after cleanup

---

## Post-Delivery

- Mark migration planning documents as historical archive
- Update ADRs to reflect final status
- Ensure runbooks reflect final paths and configuration
- See [10-documentation.md](10-documentation.md) for documentation cleanup details