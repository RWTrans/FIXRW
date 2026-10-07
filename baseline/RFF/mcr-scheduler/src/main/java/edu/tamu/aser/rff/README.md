# RFF (Reads-From Fuzzer) - Java Implementation

Based on: Wolff et al. "Greybox Fuzzing for Concurrency Testing" (ASPLOS 2024)

## Overview

This is a Java implementation of RFF, a greybox fuzzing approach for concurrency testing. It replaces MCR's constraint-solving approach with a fuzzing-inspired biased random search over abstract schedules.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                         RFF Fuzzer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   Schedule   │  │   Schedule   │  │  GreyboxFeedback │  │
│  │   Corpus     │  │   Mutator    │  │  (isInteresting  │  │
│  │              │  │(insert/swap/ │  │   + PowerSched)  │  │
│  │              │  │delete/negate)│  │                  │  │
│  └──────┬───────┘  └──────┬───────┘  └────────┬─────────┘  │
│         │                 │                    │            │
│         └─────────────────┼────────────────────┘            │
│                           │                                 │
│                  ┌────────▼────────┐                        │
│                  │ RFProactiveScheduler                      │
│                  │ (State machines for                       │
│                  │  reads-from constraints)                   │
│                  └────────┬────────┘                        │
│                           │                                 │
│                  ┌────────▼────────┐                        │
│                  │  Instrumented   │                        │
│                  │  Program (PUT)  │                        │
│                  └─────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. AbstractEvent
Represents a memory operation: `op(x)@l`
- `op`: READ or WRITE
- `x`: memory location
- `l`: source line number

### 2. ReadsFromConstraint
Represents `w rf-> r` (positive) or `w rf/-> r` (negative)

### 3. AbstractSchedule
A set of positive and negative reads-from constraints: `α = α⁺ ⊎ α⁻`

### 4. ScheduleMutator
Four mutation operators:
- `insert(α, C)`: Add new constraint
- `swap(α, C₁, C₂)`: Replace constraint
- `delete(α, C)`: Remove constraint
- `negate(α, C)`: Flip positive/negative

### 5. RFProactiveScheduler
State machine-based scheduler that biases thread selection towards satisfying abstract schedule constraints.

### 6. GreyboxFeedback
- `isInteresting()`: Checks for new reads-from pairs or crashes
- `computeEnergy()`: Power schedule for energy assignment

## Usage

### As a JUnit Runner

```java
@RunWith(RFFJUnitRunner.class)
public class MyConcurrencyTest {
    @Test
    public void testConcurrentAccess() {
        // Your test code
    }
}
```

### Standalone

```java
RFF rff = new RFF()
    .setTimeoutMillis(300000)
    .setMaxIterations(10000);
rff.fuzz();
```

## Algorithm (from paper)

```
Algorithm 1: Greybox Concurrency Fuzzing
Input: Initial corpus of schedules S_init
1: S ← S_init, S_fail ← ∅
2: if S = ∅ then S ← {ε}
3: repeat
4:   (σ, η_σ) ← PickNextAndAssignEnergy(S)
5:   for i ∈ {1, ..., η_σ} do
6:     σ_mut ← mutateSchedule(σ, S)
7:     if σ_mut crashes then S_fail ← S_fail ∪ {σ_mut}
8:     if isInteresting(σ_mut, S) then
9:       S ← S ∪ {σ_mut}
10: until timeout
11: return S_fail
```

## Key Differences from MCR

| Aspect | MCR | RFF |
|--------|-----|-----|
| Search strategy | Systematic (constraint solving) | Greybox fuzzing (biased random) |
| Schedule representation | Concrete prefix | Abstract reads-from constraints |
| Next schedule generation | Z3 SMT solver | Mutation operators |
| Feedback | None | Reads-from pairs + power schedule |
| Exploration | Exhaustive (bounded) | Adaptive, non-enumerative |

## References

- Wolff et al. "Greybox Fuzzing for Concurrency Testing", ASPLOS 2024
- Huang et al. "Maximal Causality Reduction for TSO and PSO", OOPSLA 2016
