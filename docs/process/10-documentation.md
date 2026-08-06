# 10 — Documentation

**Documentation principles, what to produce and when, and final cleanup.**

---

## Prerequisites

- Applies throughout the project, concentrated at the end

---

## Principles

1. **Truth lives in code; docs explain.** No doc claims behavior the code doesn't exhibit. Tests are the parity oracle; docs cite them.
2. **Single source per fact.** Each fact (port, endpoint, env var, run command) is stated in one authoritative doc; others link to it.
3. **Updated in lockstep with code.** Every task that changes a documented behavior updates the relevant doc in the same session.
4. **Planning docs are inputs, not outputs.** Planning artifacts are produced before implementation and serve as context for every implementation task.

---

## Documents Produced

| Document | Type | When |
|----------|------|------|
| Repository Understanding | Planning | Before code |
| Migration Summary | Planning | Before code |
| Implementation Plan | Planning | Before code |
| Migration Phases | Planning | Before code |
| Task Breakdown | Planning | Before code |
| Task Dependencies | Planning | Before code |
| AI Role Assignment | Planning | Before code |
| Task Prompts | Planning | Before code |
| Verification Strategy | Planning | Before code |
| Documentation Strategy | Planning | Before code |
| ADRs | Living | Per decision |
| Parity Checklist | Living | Post-implementation |
| Migration Report | Living | Post-implementation |
| Runbooks | Living | Post-implementation |
| Progress Tracker | Living | Per task |
| README | Living | Updated per wave |

---

## Final Documentation Cleanup

After the migration is complete and verified, all documentation is updated to reflect the final state:

- README rewritten as a native project (no migration language)
- Runbooks updated with correct paths
- ADRs updated to reflect final status (e.g., "Secondary Backend" → "Primary Backend")
- Migration docs marked as historical archive
- Legacy-specific runbook content removed

---

## Cross-References

- Planning artifacts are defined in [04-planning.md](04-planning.md)
- Progress tracking is described in [11-progress-tracking.md](11-progress-tracking.md)
- Final delivery cleanup is in [12-final-delivery.md](12-final-delivery.md)