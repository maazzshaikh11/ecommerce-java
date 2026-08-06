# 08 — Implementation

**The build-test-fix loop and the "never fix what isn't broken" rule.**

---

## Prerequisites

- [07-prompt-generation.md](07-prompt-generation.md) — prompt ready
- [06-agent-orchestration.md](06-agent-orchestration.md) — task assigned

---

## Build-Test-Fix Loop

Every implementation task follows this cycle:

1. **Read** — source code (legacy) and planning docs referenced in the prompt
2. **Write** — new code following conventions from the shared preamble
3. **Build** — `mvn compile`; if errors, read them, fix code, rebuild
4. **Test** — `mvn test`; if failures, read them, fix code, retest
5. **Report** — files created/modified, command output, pass/fail, deviations not made

This loop repeats until both build and tests pass. No task is considered complete until the test command returns green.

---

## "Never Fix What Isn't Broken"

During a parity migration, the agent is instructed not to refactor, optimize, or improve code. Tech debt is preserved explicitly. Improvements are identified as a separate project, not part of the migration.

This rule is enforced by:
1. The shared preamble listing hard parity rules (see [07-prompt-generation.md](07-prompt-generation.md))
2. The "report what you didn't do" requirement in the completion signal
3. Parity verification in the testing phase (see [09-build-and-verification.md](09-build-and-verification.md))

---

## Self-Correction During Implementation

When a build or test fails, the agent diagnoses and fixes in the same session:

| Failure Type | Response |
|-------------|----------|
| Compilation error | Read error message, fix syntax/type error, rebuild |
| Test assertion failure | Read expected vs actual, fix code to match expected behavior, retest |
| Runtime error (SQL, DI, config) | Read stack trace, identify root cause, fix mapping/config, rebuild, retest |
| Parity deviation caught by test | Do not patch the test. Fix the code to match the expected behavior. |

---

## Output

Each task produces:
- New or modified source files (as specified in the task definition)
- A build that compiles
- Tests that pass
- A completion report (files, command, result, deviations not made)

The output feeds into [09-build-and-verification.md](09-build-and-verification.md) for gate enforcement.