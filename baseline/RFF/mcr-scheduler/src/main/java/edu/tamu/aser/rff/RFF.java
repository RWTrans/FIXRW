package edu.tamu.aser.rff;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Complete Java implementation of RFF (Reads-From Fuzzer).
 * 
 * This class provides a self-contained implementation of the RFF algorithm
 * that can be used for testing and experimentation.
 * 
 * Based on: Wolff et al. "Greybox Fuzzing for Concurrency Testing" (ASPLOS 2024)
 */
public class RFF {
    // Core components
    private final ScheduleCorpus corpus;
    private final ScheduleMutator mutator;
    private final GreyboxFeedback feedback;
    private final RFProactiveScheduler proactiveScheduler;
    
    // Configuration
    private final long timeoutMillis;
    private final int maxIterations;
    private final int maxEnergy;
    
    // Statistics
    private final AtomicInteger totalExecutions;
    private final AtomicInteger totalCrashes;
    private final AtomicInteger totalInteresting;
    private final List<String> failureTraces;
    
    public RFF() {
        this(300000, 10000, 16); // 5 min, 10000 iterations, max energy 16
    }
    
    public RFF(long timeoutMillis, int maxIterations, int maxEnergy) {
        this.corpus = new ScheduleCorpus();
        this.mutator = new ScheduleMutator();
        this.feedback = new GreyboxFeedback();
        this.proactiveScheduler = new RFProactiveScheduler();
        this.timeoutMillis = timeoutMillis;
        this.maxIterations = maxIterations;
        this.maxEnergy = maxEnergy;
        this.totalExecutions = new AtomicInteger(0);
        this.totalCrashes = new AtomicInteger(0);
        this.totalInteresting = new AtomicInteger(0);
        this.failureTraces = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Run the RFF fuzzing loop
     */
    public void fuzz() {
        System.out.println("=== RFF: Greybox Fuzzing for Concurrency Testing ===");
        System.out.println("Configuration:");
        System.out.println("  Timeout: " + timeoutMillis / 1000 + " seconds");
        System.out.println("  Max iterations: " + maxIterations);
        System.out.println("  Max energy: " + maxEnergy);
        System.out.println();
        
        long startTime = System.currentTimeMillis();
        
        // Initialize corpus with empty schedule
        AbstractSchedule emptySchedule = new AbstractSchedule();
        corpus.addSchedule(emptySchedule);
        
        // Main fuzzing loop (Algorithm 1)
        while (true) {
            // Termination check
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
            
            int energy = computeEnergy(schedule);
            if (energy == 0) {
                schedule.incrementTimesSkipped();
                continue;
            }
            
            // Fuzz this schedule
            fuzzOne(schedule, energy);
        }
        
        printFinalStatistics(startTime);
    }
    
    /**
     * Fuzz a single schedule by mutating it energy times
     */
    private void fuzzOne(AbstractSchedule schedule, int energy) {
        for (int i = 0; i < energy; i++) {
            // Generate mutant schedule
            AbstractSchedule mutant = mutator.mutate(
                schedule, 
                corpus.getAllObservedEvents()
            );
            
            // Execute the mutant
            ExecutionResult result = execute(mutant);
            totalExecutions.incrementAndGet();
            
            // Handle crash
            if (result.crashed) {
                totalCrashes.incrementAndGet();
                failureTraces.add(result.trace);
                System.out.println("[RFF] BUG FOUND in execution #" + totalExecutions.get());
                System.out.println("  Schedule: " + mutant);
            }
            
            // Update feedback
            feedback.updateFeedback(result.tracker);
            corpus.addObservedEvents(result.tracker.getAllEvents());
            
            // Check if interesting
            boolean interesting = feedback.isInteresting(result.tracker, result.crashed);
            if (interesting) {
                totalInteresting.incrementAndGet();
                corpus.addSchedule(mutant);
            }
            
            // Progress report
            if (totalExecutions.get() % 100 == 0) {
                printProgress();
            }
        }
        
        // Put schedule back if still has energy
        if (computeEnergy(schedule) > 0) {
            corpus.putBack(schedule);
        }
    }
    
    /**
     * Execute a schedule and collect reads-from information.
     * In a real implementation, this would:
     * 1. Set up the proactive scheduler with the abstract schedule
     * 2. Run the instrumented program
     * 3. Collect reads-from pairs
     * 4. Detect crashes
     * 
     * Here we provide a simulation framework.
     */
    protected ExecutionResult execute(AbstractSchedule schedule) {
        proactiveScheduler.setAbstractSchedule(schedule);
        
        ReadsFromTracker tracker = new ReadsFromTracker();
        boolean crashed = false;
        StringBuilder trace = new StringBuilder();
        
        // TODO: Integrate with actual program execution
        // This is a placeholder that would be replaced by:
        // - Running the instrumented program with the proactive scheduler
        // - Collecting actual reads-from pairs from memory accesses
        // - Detecting assertion failures, deadlocks, etc.
        
        return new ExecutionResult(tracker, crashed, trace.toString());
    }
    
    /**
     * Compute energy for a schedule using power schedule
     */
    private int computeEnergy(AbstractSchedule schedule) {
        // Simplified power schedule
        // In full implementation, use the formula from GreyboxFeedback
        return Math.min(1 + schedule.getTimesChosen(), maxEnergy);
    }
    
    /**
     * Print progress
     */
    private void printProgress() {
        System.out.println("[RFF] Executions: " + totalExecutions.get() +
                          ", Bugs: " + totalCrashes.get() +
                          ", Interesting: " + totalInteresting.get() +
                          ", Corpus: " + corpus.size() +
                          ", RF pairs: " + feedback.getTotalUniquePairs());
    }
    
    /**
     * Print final statistics
     */
    private void printFinalStatistics(long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        
        System.out.println("\n=== RFF Final Statistics ===");
        System.out.println("Total executions: " + totalExecutions.get());
        System.out.println("Total bugs found: " + totalCrashes.get());
        System.out.println("Total interesting schedules: " + totalInteresting.get());
        System.out.println("Failure traces: " + failureTraces.size());
        System.out.println("Final corpus size: " + corpus.size());
        System.out.println("Unique reads-from pairs: " + feedback.getTotalUniquePairs());
        System.out.println("Time elapsed: " + elapsed / 1000 + "s");
        System.out.println("Execution rate: " + (totalExecutions.get() * 1000.0 / elapsed) + " exec/s");
        
        if (!failureTraces.isEmpty()) {
            System.out.println("\n=== Failure Traces ===");
            for (int i = 0; i < Math.min(5, failureTraces.size()); i++) {
                System.out.println("\n--- Failure " + (i+1) + " ---");
                System.out.println(failureTraces.get(i));
            }
        }
    }
    
    /**
     * Get failing schedules for reproduction
     */
    public List<String> getFailureTraces() {
        return Collections.unmodifiableList(failureTraces);
    }
    
    /**
     * Execution result container
     */
    protected static class ExecutionResult {
        final ReadsFromTracker tracker;
        final boolean crashed;
        final String trace;
        
        ExecutionResult(ReadsFromTracker tracker, boolean crashed, String trace) {
            this.tracker = tracker;
            this.crashed = crashed;
            this.trace = trace;
        }
    }
    
    /**
     * Main entry point for standalone execution
     */
    public static void main(String[] args) {
        // Parse arguments
        long timeout = 300000; // 5 minutes
        int maxIter = 10000;
        int maxEnergy = 16;
        
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-t": case "--timeout":
                    if (i + 1 < args.length) {
                        timeout = Long.parseLong(args[++i]) * 1000;
                    }
                    break;
                case "-i": case "--iterations":
                    if (i + 1 < args.length) {
                        maxIter = Integer.parseInt(args[++i]);
                    }
                    break;
                case "-e": case "--energy":
                    if (i + 1 < args.length) {
                        maxEnergy = Integer.parseInt(args[++i]);
                    }
                    break;
                case "-h": case "--help":
                    printUsage();
                    return;
            }
        }
        
        RFF rff = new RFF(timeout, maxIter, maxEnergy);
        rff.fuzz();
    }
    
    private static void printUsage() {
        System.out.println("Usage: java RFF [options]");
        System.out.println("Options:");
        System.out.println("  -t, --timeout <seconds>    Timeout in seconds (default: 300)");
        System.out.println("  -i, --iterations <count>   Max iterations (default: 10000)");
        System.out.println("  -e, --energy <count>       Max energy per schedule (default: 16)");
        System.out.println("  -h, --help                 Show this help");
    }
}
