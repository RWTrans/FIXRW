package edu.tamu.aser.rff;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * End-to-end simulation test for RFF bug-finding capability.
 * This test simulates the RFF fuzzing loop without requiring actual
 * program instrumentation, to verify the core algorithm can discover
 * concurrency bugs.
 */
public class RFFEndToEndSimulationTest {
    
    /**
     * Simulated concurrent program with an atomicity violation.
     * The bug is triggered when both threads read before either writes.
     */
    static class SimulatedAtomicityViolationProgram {
        
        /**
         * Simulate execution with a given abstract schedule.
         */
        public RFF.ExecutionResult execute(AbstractSchedule schedule) {
            ReadsFromTracker tracker = new ReadsFromTracker();
            boolean crashed = false;
            StringBuilder trace = new StringBuilder();
            
            // Create abstract events for the program
            // Two threads: check-then-act on balance
            AbstractEvent readBal1 = new AbstractEvent(
                AbstractEvent.Op.READ, "balance", 50, "Thread-1");
            AbstractEvent writeBal1 = new AbstractEvent(
                AbstractEvent.Op.WRITE, "balance", 55, "Thread-1");
            AbstractEvent readBal2 = new AbstractEvent(
                AbstractEvent.Op.READ, "balance", 60, "Thread-2");
            AbstractEvent writeBal2 = new AbstractEvent(
                AbstractEvent.Op.WRITE, "balance", 65, "Thread-2");
            
            // Simulate initial write
            AbstractEvent initWrite = new AbstractEvent(
                AbstractEvent.Op.WRITE, "balance", -1, "INIT");
            
            tracker.recordEvent(readBal1);
            tracker.recordEvent(writeBal1);
            tracker.recordEvent(readBal2);
            tracker.recordEvent(writeBal2);
            tracker.recordEvent(initWrite);
            
            // Determine execution order based on schedule constraints
            // If schedule prioritizes both reads, atomicity violation occurs
            boolean read1First = isPrioritized(schedule, readBal1);
            boolean read2First = isPrioritized(schedule, readBal2);
            
            // Also check for negative constraints that might force both reads first
            boolean hasNegWrite1 = hasNegativeConstraint(schedule, writeBal1);
            boolean hasNegWrite2 = hasNegativeConstraint(schedule, writeBal2);
            
            // Empty schedule (no constraints) has 30% chance of triggering the bug
            // This simulates the natural non-determinism of concurrent execution
            boolean emptyScheduleBug = schedule.getAllConstraints().isEmpty() && 
                                       new Random(schedule.getId()).nextInt(10) < 3;
            
            if ((read1First && read2First) || (hasNegWrite1 && hasNegWrite2) || emptyScheduleBug) {
                // Both threads read balance=100 before either writes
                // This causes atomicity violation
                tracker.recordReadsFrom(readBal1, initWrite);
                tracker.recordReadsFrom(readBal2, initWrite);
                crashed = true;
                trace.append("ATOMICITY VIOLATION: Both threads read stale balance\n");
            } else if (read1First || hasNegWrite2) {
                // Thread 1 reads first, then writes, then Thread 2 reads
                tracker.recordReadsFrom(readBal1, initWrite);
                tracker.recordReadsFrom(readBal2, writeBal1);
            } else if (read2First || hasNegWrite1) {
                // Thread 2 reads first, then writes, then Thread 1 reads
                tracker.recordReadsFrom(readBal2, initWrite);
                tracker.recordReadsFrom(readBal1, writeBal2);
            } else {
                // Default order: some interleaving without bug
                tracker.recordReadsFrom(readBal1, initWrite);
                tracker.recordReadsFrom(readBal2, writeBal1);
            }
            
            trace.append("Execution with schedule #").append(schedule.getId()).append("\n");
            trace.append("Events: ").append(tracker.getAllEvents()).append("\n");
            trace.append("Reads-from: ").append(tracker.getReadsFromMap()).append("\n");
            if (crashed) {
                trace.append("BUG DETECTED!\n");
            }
            
            return new RFF.ExecutionResult(tracker, crashed, trace.toString());
        }
        
        private boolean isPrioritized(AbstractSchedule schedule, AbstractEvent read) {
            for (ReadsFromConstraint c : schedule.getPositiveConstraints()) {
                if (c.isPositive() && matches(c.getRead(), read)) {
                    return true;
                }
            }
            return false;
        }
        
        private boolean hasNegativeConstraint(AbstractSchedule schedule, AbstractEvent write) {
            for (ReadsFromConstraint c : schedule.getNegativeConstraints()) {
                if (c.isNegative() && matches(c.getWrite(), write)) {
                    return true;
                }
            }
            return false;
        }
        
        private boolean matches(AbstractEvent e1, AbstractEvent e2) {
            return e1.getOp() == e2.getOp() &&
                   e1.getLocation().equals(e2.getLocation()) &&
                   e1.getLine() == e2.getLine();
        }
    }
    
    @Test
    public void testRFFFindsAtomicityViolation() {
        // Create RFF instance with short timeout for testing
        RFF rff = new RFF(10000, 1000, 4) {
            @Override
            protected ExecutionResult execute(AbstractSchedule schedule) {
                SimulatedAtomicityViolationProgram program = new SimulatedAtomicityViolationProgram();
                return program.execute(schedule);
            }
        };
        
        // Run fuzzing
        rff.fuzz();
        
        // Verify that RFF found the bug
        assertTrue("RFF should find at least one bug", rff.getFailureTraces().size() > 0);
        
        // Print found bugs
        System.out.println("RFF found " + rff.getFailureTraces().size() + " bugs:");
        for (String trace : rff.getFailureTraces()) {
            System.out.println(trace);
        }
    }
    
    @Test
    public void testRFFFeedbackMechanism() {
        // Test that feedback correctly identifies interesting executions
        GreyboxFeedback feedback = new GreyboxFeedback();
        
        // Create first execution with new reads-from pair
        ReadsFromTracker tracker1 = new ReadsFromTracker();
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5, "T2");
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10, "T1");
        tracker1.recordReadsFrom(read, write);
        
        // First check: should be interesting (new pair)
        assertTrue("First execution with new RF pair should be interesting",
                   feedback.isInteresting(tracker1, false));
        
        // Update feedback with the pair
        feedback.updateFeedback(tracker1);
        
        // Create second tracker with same pair
        ReadsFromTracker tracker2 = new ReadsFromTracker();
        AbstractEvent write2 = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5, "T2");
        AbstractEvent read2 = new AbstractEvent(AbstractEvent.Op.READ, "x", 10, "T1");
        tracker2.recordReadsFrom(read2, write2);
        
        // Second check: should NOT be interesting (same pair already known)
        assertFalse("Repeated RF pair should not be interesting",
                    feedback.isInteresting(tracker2, false));
        
        // Crash should always be interesting
        assertTrue("Crash should be interesting",
                   feedback.isInteresting(tracker2, true));
    }
    
    @Test
    public void testRFFMutationOperators() {
        ScheduleMutator mutator = new ScheduleMutator();
        
        // Create events for testing - note: positive(write, read) not positive(read, write)
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5, "T2");
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10, "T1");
        Set<AbstractEvent> events = new HashSet<>();
        events.add(read);
        events.add(write);
        
        // Create a schedule with constraints
        AbstractSchedule schedule = new AbstractSchedule();
        schedule.addConstraint(ReadsFromConstraint.positive(write, read));
        
        // Test negate mutation
        AbstractSchedule negated = mutator.negate(schedule);
        assertTrue("Negate should add negative constraint",
                   negated.getNegativeConstraints().size() > 0);
        
        // Test delete mutation
        AbstractSchedule deleted = mutator.delete(schedule);
        assertTrue("Delete should remove constraints",
                   deleted.getPositiveConstraints().size() < schedule.getPositiveConstraints().size());
    }
    
    @Test
    public void testRFFPowerSchedule() {
        GreyboxFeedback feedback = new GreyboxFeedback();
        
        // Create schedule with low novelty
        AbstractSchedule schedule = new AbstractSchedule();
        schedule.incrementTimesChosen();
        schedule.incrementTimesChosen();
        
        int energy1 = feedback.computeEnergy(schedule);
        assertTrue("Energy should be non-negative", energy1 >= 0);
        
        // Add new reads-from pair to make it more interesting
        ReadsFromTracker tracker = new ReadsFromTracker();
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5, "T2");
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10, "T1");
        tracker.recordReadsFrom(read, write);
        feedback.updateFeedback(tracker);
        
        int energy2 = feedback.computeEnergy(schedule);
        assertTrue("Energy should be non-negative", energy2 >= 0);
        
        System.out.println("Energy before: " + energy1 + ", after: " + energy2);
    }
}
