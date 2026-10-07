# RFF Java Implementation - Status Report

## Implementation Complete

All core RFF components have been implemented and tested:

### Core Components (17 classes)
- `AbstractEvent` - Abstract events op(x)@l
- `ReadsFromConstraint` - Positive/negative reads-from constraints
- `AbstractSchedule` - Abstract schedules (constraint sets)
- `ScheduleMutator` - Four mutation operators (insert/swap/delete/negate)
- `PositiveConstraintStateMachine` - State machine for positive constraints
- `NegativeConstraintStateMachine` - State machine for negative constraints
- `ReadsFromTracker` - Reads-from relation tracking
- `GreyboxFeedback` - isInteresting + Power Schedule
- `ScheduleCorpus` - Schedule corpus with energy-based prioritization
- `RFProactiveScheduler` - Core proactive scheduler (291 lines)
- `RFFStrategy` - SURW SchedulingStrategy interface implementation
- `RFFDriver` - Algorithm 1 main loop
- `RFFRuntimeBridge` - SURW runtime bridge
- `RFF` - Self-contained complete implementation (285 lines)
- `RFFJUnitRunner` - JUnit4 Runner
- `RFFConfig` - Configuration class

### Test Results
- **RFFCoreTest**: 8 unit tests - ALL PASSED
- **RFFEndToEndSimulationTest**: 4 simulation tests - ALL PASSED
  - Verified RFF finds atomicity violations (26+ bugs in 1000 executions)
  - Verified feedback mechanism correctly identifies new/old reads-from pairs
  - Verified mutation operators work correctly
  - Verified power schedule energy computation

### Build Artifacts
- `rff-agent.jar` - Agent JAR with RFF classes and ASM 9.4
- `run_rff_test.sh` - Test runner script

## Compatibility Limitation

**The SURW instrumentor is incompatible with Java 25.**

SURW was designed for Java 8 (2017-2020) and uses:
- ASM 3.0 (supports Java 8 max)
- Internal ASM APIs (`ClassReader.header`, `ClassReader.getItem()`) that changed in newer ASM versions
- `sun.misc.Unsafe.monitorEnter/monitorExit` (removed in Java 19+)

The instrumentor fails with:
```
java.lang.IllegalArgumentException
    at org.objectweb.asm.ClassReader.<init>(ClassReader.java:170)
```

This occurs because:
1. Java 25 generates class files in a newer format
2. SURW's `ExtendedClassWriter` accesses ASM internal fields directly
3. These internal APIs changed between ASM 3.0 and ASM 9.4

## To Run End-to-End Tests

You need either:

### Option A: Downgrade to Java 8
```bash
# Install Java 8 and set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk
./run_rff_test.sh edu.tamu.aser.rff.SimpleRaceTest
```

### Option B: Modernize SURW Instrumentor (Recommended)
Update the instrumentor to use ASM 9.4 APIs:
1. Replace `ExtendedClassWriter` with standard `ClassWriter`
2. Use `ClassReader.getClassName()` instead of direct field access
3. Remove `sun.misc.Unsafe` dependencies
4. Update to modern Java APIs

## Files Modified
- `pom.xml` - Added Java 8 properties
- `mcr-test/pom.xml` - Updated to Java 8
- `mcr-scheduler/src/main/java/edu/tamu/aser/reex/Scheduler.java`:
  - Line 627: `unsafe.monitorEnter()` → `synchronized` block
  - Line 730: `unsafe.monitorExit()` → comment

## Files Created (All in `/mnt/f/vcf/baseline/SURW/mcr-scheduler/src/main/java/edu/tamu/aser/rff/`)
All 17 RFF core classes listed above.

## Verification
```bash
# Compile
cd /mnt/f/vcf/baseline/SURW && mvn compile -pl mcr-scheduler

# Run unit tests
mvn test -pl mcr-scheduler -Dtest="RFFCoreTest,RFFEndToEndSimulationTest"

# Result: 12 tests passed, 0 failures
```

## Next Steps
To complete end-to-end verification with actual instrumentation:
1. Set up Java 8 environment, OR
2. Modernize SURW instrumentor for Java 17+ (estimated 2-4 hours work)
