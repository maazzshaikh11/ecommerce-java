# 11 — Progress Tracking

**How to track progress against the master task list and report task completion.**

---

## Prerequisites

- [05-task-breakdown.md](05-task-breakdown.md) — master task list with stable IDs

---

## Master Task List as Single Source of Truth

The master task list is the single source of truth for execution order. Progress is tracked against this list. No task is "done" until its acceptance criteria are verified.

---

## Progress Document

A progress document is maintained with:

- Last completed task ID
- Current phase
- Timestamp
- Status summary
- Test results table
- Deliverables checklist

### Example Format

```
## Current State

- Last Completed Task ID: T-125
- Current Phase: Phase 10 complete — ALL PHASES DONE
- Timestamp: 2026-08-05
- Status: Full migration complete. All traffic routed to modern stack.

## Final Verification

| Suite | Tests | Status |
|-------|-------|--------|
| Backend (mvn test) | 28 | 28 passed |
| Frontend (vitest) | 7 | 7 passed |
| E2E (playwright) | 5 | 5 passed |
| Build | — | SUCCESS |
```

---

## Task Completion Reporting

When a task completes, the agent reports:

```
TASK COMPLETION
Task ID: T-NNN
Status: COMPLETE
Files Created: [list]
Files Modified: [list]
Command: mvn test
Result: Tests run: 28, Failures: 0, Errors: 0
Deviations considered but NOT made: [list]
```

The "deviations not made" field is required — it surfaces unconscious bias toward "improving" code. See [01-requirement-understanding.md](01-requirement-understanding.md) for why this matters.

---

## Progress Metrics

At any point, progress is measured as:

```
Task Completion = (Tasks Passed) / (Total Tasks) × 100%
Phase Completion = (Tasks Passed in Phase N) / (Tasks in Phase N) × 100%
```