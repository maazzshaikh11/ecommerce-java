# 6. Task Dependencies

## 6.1 Dependency Matrix (task → prerequisites)

```
SB-01  : —
SB-02  : SB-01
SB-03  : SB-01
SB-04a : SB-02
SB-04b : SB-02
SB-04c : SB-02
SB-04d : SB-02
SB-04e : SB-02
SB-04f : SB-04a, SB-04b, SB-04c, SB-04d, SB-04e
SB-05a : SB-03, SB-01
SB-05b : SB-05a
SB-05c : SB-05b, SB-01
SB-06a : SB-04a, SB-04b, SB-03
SB-06b : SB-04b, SB-03
SB-06c : SB-04d, SB-04a
SB-06d : SB-04e
SB-06e : SB-04a, SB-04c, SB-04d, SB-04e
SB-07a : SB-03
SB-07b : SB-07a, SB-05b, SB-04c
SB-07c : SB-07a, SB-06a
SB-07d : SB-07a, SB-06b
SB-07e : SB-07a, SB-05b, SB-06c
SB-07f : SB-07a, SB-05b, SB-06e
SB-07g : SB-07a, SB-05b, SB-06d
SB-07h : SB-07a
SB-08a : (running .NET app, independent)
SB-08b : SB-08a, SB-07b, SB-07c, SB-07d, SB-07e, SB-07f, SB-07g, SB-07h
SB-08c : SB-07b, SB-07c, SB-07d, SB-07e, SB-07f, SB-07g, SB-07h
SB-08d : SB-06e, SB-05a
SB-09a : SB-01
SB-09b : SB-07b, SB-07c, SB-07d, SB-07e, SB-07f, SB-07g, SB-07h
SB-09c : SB-01
SB-09d : SB-08b, SB-08c
```

## 6.2 Parallelization Opportunities (per phase)

- **P1:** SB-02 ∥ SB-03 (after SB-01).
- **P2:** SB-04a ∥ SB-04b ∥ SB-04c ∥ SB-04d ∥ SB-04e (after SB-02). Then SB-04f joins all five.
- **P3:** serial (SB-05a → SB-05b → SB-05c) — small chain; do in one session.
- **P4:** SB-06a ∥ SB-06b ∥ SB-06c ∥ SB-06d (after their repos). **SB-06e waits for ALL of SB-04a,c,d,e.**
- **P5:** SB-07a first; then SB-07b ∥ SB-07c ∥ SB-07d ∥ SB-07e ∥ SB-07f ∥ SB-07g ∥ SB-07h.
- **P6:** SB-08a can run anytime the .NET app runs; SB-08b/c need Phase 5; SB-08d needs SB-06e + SB-05a.
- **P7:** SB-09a ∥ SB-09c after SB-01; SB-09b after Phase 5; SB-09d after Phase 6.

## 6.3 Critical Path

```
SB-01 → SB-02 → SB-04e → SB-06e → SB-07f → SB-08b → SB-09d
```
(Also SB-04d on the SB-06e branch, and SB-04c on the SB-06e branch.)

## 6.4 Dependency Graph (textual)

```
                       ┌─► SB-02 ─┬─► SB-04a ─┐
   SB-01 ─┬─► SB-03    │          ├─► SB-04b  │
         │             │          ├─► SB-04c ─┤
         │             │          ├─► SB-04d ─┤
         │             │          └─► SB-04e ─┤──► SB-04f
         │             └─► SB-05a ─► SB-05b ──► SB-05c
         │
         ├─► SB-03 ──► SB-07a
         │
         │   SB-06a ◄─ SB-04a,SB-04b,SB-03
         │   SB-06b ◄─ SB-04b,SB-03
         │   SB-06c ◄─ SB-04d,SB-04a
         │   SB-06d ◄─ SB-04e
         │   SB-06e ◄─ SB-04a,SB-04c,SB-04d,SB-04e
         │
         │   SB-07b ◄─ SB-07a,SB-05b,SB-04c
         │   SB-07c ◄─ SB-07a,SB-06a
         │   SB-07d ◄─ SB-07a,SB-06b
         │   SB-07e ◄─ SB-07a,SB-05b,SB-06c
         │   SB-07f ◄─ SB-07a,SB-05b,SB-06e
         │   SB-07g ◄─ SB-07a,SB-05b,SB-06d
         │   SB-07h ◄─ SB-07a
         │
         └─► SB-08a ─► SB-08b ◄─ (Phase 5)
                       SB-08c ◄─ (Phase 5)
                       SB-08d ◄─ SB-06e,SB-05a
                       SB-09a ◄─ SB-01
                       SB-09b ◄─ (Phase 5)
                       SB-09c ◄─ SB-01
                       SB-09d ◄─ SB-08b,SB-08c
```