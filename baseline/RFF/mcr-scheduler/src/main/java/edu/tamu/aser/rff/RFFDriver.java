package edu.tamu.aser.rff;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main driver for RFF greybox fuzzing.
 * Implements Algorithm 1 from the RFF paper:
 * 
 * Input: Initial corpus of schedules S_init
 * 1: S ← S_init, S_fail ← ∅
 * 2: if S = ∅ then S ← {ε}  (empty schedule)
 * 3: repeat
 * 4:   (σ, η_σ) ← PickNextAndAssignEnergy(S)
 * 5:   for i ∈ {1, ..., η_σ} do
 * 6:     σ_mut ← mutateSchedule(σ, S)
 * 7:     if σ_mut crashes then S_fail ← S_fail ∪ {σ_mut}
 * 8:     if isInteresting(σ_mut, S) then
 * 9:       S ← S ∪ {σ_mut}
 * 10: until timeout
 * 11: return S_fail
 */
public class RFFDriver {
    private final ScheduleCorpus corpus;
    private final ScheduleMutator mutator;
    private final GreyboxFeedback feedback;
    private final RFProactiveScheduler proactiveScheduler;
    
    // Configuration
    private final long timeoutMillis;
    private final int maxIterations;
    
    // Statistics
    private final AtomicInteger totalExecutions;
    private final AtomicInteger totalCrashes;
    private final List<AbstractSchedule> failingSchedules;
    
    public RFFDriver() {
        this(300000, 10000); // 5 minutes, 10000 iterations
    }
    
    public RFFDriver(long timeoutMillis, int maxIterations) {
        this.corpus = new ScheduleCorpus();
        this.mutator = new ScheduleMutator();
        this.feedback = new GreyboxFeedback();
        this.proactiveScheduler = new RFProactiveScheduler();
        this.timeoutMillis = timeoutMillis;
        this.maxIterations = maxIterations;
        this.totalExecutions = new AtomicInteger(0);
        this.totalCrashes = new AtomicInteger(0);
        this.failingSchedules = new ArrayList<>();
    }
    
    /**
     * Run the greybox fuzzing loop
     */
    public void run() {
        System.out.println("=== RFF: Greybox Fuzzing for Concurrency Testing ===");
        System.out.println("Timeout: " + timeoutMillis / 1000 + "s, Max iterations: " + maxIterations);
        
        long startTime = System.currentTimeMillis();
        
        // Initialize with empty schedule
        AbstractSchedule emptySchedule = new AbstractSchedule();
        corpus.addSchedule(emptySchedule);
        
        // Main fuzzing loop
        while (true) {
            // Check termination conditions
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                System.out.println("[RFF] Timeout reached");
                break;
            }
            if (totalExecutions.get() >= maxIterations) {
                System.out.println("[RFF] Max iterations reached");
                break;
            }
            if (corpus.isEmpty()) {
                System.out.println("[RFF] Corpus exhausted");
                break;
            }
            
            // Pick next schedule and assign energy
            AbstractSchedule schedule = corpus.pickNext();
            if (schedule == null) {
                continue;
            }
            
            int energy = feedback.computeEnergy(schedule);
            if (energy == 0) {
                // Skip over-explored schedules
                schedule.incrementTimesSkipped();
                continue;
            }
            
            // Fuzz this schedule
            fuzzSchedule(schedule, energy);
        }
        
        printStatistics(startTime);
    }
    
    /**
     * Fuzz a single schedule by mutating it energy times
     */
    private void fuzzSchedule(AbstractSchedule schedule, int energy) {
        for (int i = 0; i < energy; i++) {
            // Mutate the schedule
            AbstractSchedule mutated = mutator.mutate(
                schedule, 
                corpus.getAllObservedEvents()
            );
            
            // Execute the mutated schedule (simulated here)
            ExecutionResult result = executeSchedule(mutated);
            
            totalExecutions.incrementAndGet();
            
            // Check for crash
            if (result.crashed) {
                totalCrashes.incrementAndGet();
                failingSchedules.add(mutated);
                System.out.println("[RFF] Bug found in execution #" + totalExecutions.get());
            }
            
            // Update feedback
            feedback.updateFeedback(result.tracker);
            corpus.addObservedEvents(result.tracker.getAllEvents());
            
            // Check if interesting
            boolean interesting = feedback.isInteresting(result.tracker, result.crashed);
            if (interesting) {
                corpus.addSchedule(mutated);
            }
            
            // Print progress periodically
            if (totalExecutions.get() % 100 == 0) {
                printProgress();
            }
        }
        
        // Put schedule back if it still has energy
        if (feedback.computeEnergy(schedule) > 0) {
            corpus.putBack(schedule);
        }
    }
    
    /**
     * Execute a schedule and return the result.
     * In the real implementation, this would run the instrumented program.
     * Here we simulate with a placeholder.
     */
    private ExecutionResult executeSchedule(AbstractSchedule schedule) {
        // Set up proactive scheduler
        proactiveScheduler.setAbstractSchedule(schedule);
        
        // Simulate execution (in real implementation, this would:
        // 1. Run the instrumented program
        // 2. Collect reads-from pairs via RVRunTime hooks
        // 3. Detect crashes
        
        ReadsFromTracker tracker = new ReadsFromTracker();
        boolean crashed = false;
        
        // TODO: Integrate with actual program execution
        // For now, return empty result
        
        return new ExecutionResult(tracker, crashed);
    }
    
    /**
     * Print progress
     */
    private void printProgress() {
        System.out.println("[RFF] Executions: " + totalExecutions.get() + 
                          ", Bugs: " + totalCrashes.get() + 
                          ", Corpus: " + corpus.size() +
                          ", RF pairs: " + feedback.getTotalUniquePairs());
    }
    
    /**
     * Print final statistics
     */
    private void printStatistics(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        
        System.out.println("\n=== RFF Final Statistics ===");
        System.out.println("Total executions: " + totalExecutions.get());
        System.out.println("Total bugs found: " + totalCrashes.get());
        System.out.println("Failing schedules: " + failingSchedules.size());
        System.out.println("Corpus size: " + corpus.size());
        System.out.println("Unique reads-from pairs: " + feedback.getTotalUniquePairs());
        System.out.println("Total combinations: " + feedback.getTotalCombinations());
        System.out.println("Time elapsed: " + elapsed / 1000 + "s");
        System.out.println("Executions/second: " + (totalExecutions.get() * 1000.0 / elapsed));
    }
    
    /**
     * Get failing schedules
     */
    public List<AbstractSchedule> getFailingSchedules() {
        return Collections.unmodifiableList(failingSchedules);
    }
    
    /**
     * Result of executing a single schedule
     */
    private static class ExecutionResult {
        final ReadsFromTracker tracker;
        final boolean crashed;
        
        ExecutionResult(ReadsFromTracker tracker, boolean crashed) {
            this.tracker = tracker;
            this.crashed = crashed;
        }
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        RFFDriver driver = new RFFDriver();
        driver.run();
    }
}
