package edu.tamu.aser.rff;

import edu.tamu.aser.scheduling.strategy.SchedulingStrategy;
import edu.tamu.aser.scheduling.strategy.ChoiceType;
import edu.tamu.aser.scheduling.strategy.ThreadInfo;

import java.util.*;

/**
 * RFF scheduling strategy that implements greybox fuzzing for concurrency testing.
 * This replaces MCR's constraint-solving approach with a fuzzing-inspired
 * biased random search over abstract schedules.
 */
public class RFFStrategy extends SchedulingStrategy {
    // Core RFF components
    private final ScheduleCorpus corpus;
    private final ScheduleMutator mutator;
    private final GreyboxFeedback feedback;
    private final RFProactiveScheduler proactiveScheduler;
    
    // Current execution state
    private AbstractSchedule currentAbstractSchedule;
    private ReadsFromTracker currentTracker;
    private boolean currentExecutionCrashed;
    
    // Configuration
    private final int maxIterations;
    private final long timeoutMillis;
    
    // Statistics
    private int totalSchedulesExecuted = 0;
    private int totalBugsFound = 0;
    private long startTime;
    
    public RFFStrategy() {
        this(10000, 300000); // Default: 10000 iterations or 5 minutes
    }
    
    public RFFStrategy(int maxIterations, long timeoutMillis) {
        this.corpus = new ScheduleCorpus();
        this.mutator = new ScheduleMutator();
        this.feedback = new GreyboxFeedback();
        this.proactiveScheduler = new RFProactiveScheduler();
        this.maxIterations = maxIterations;
        this.timeoutMillis = timeoutMillis;
    }
    
    @Override
    public void startingExploration() {
        this.startTime = System.currentTimeMillis();
        
        // Initialize with empty schedule
        AbstractSchedule emptySchedule = new AbstractSchedule();
        corpus.addSchedule(emptySchedule);
    }
    
    @Override
    public void startingScheduleExecution() {
        // Pick next schedule from corpus
        currentAbstractSchedule = corpus.pickNext();
        
        if (currentAbstractSchedule == null) {
            // Corpus is empty, should not happen
            currentAbstractSchedule = new AbstractSchedule();
        }
        
        // Reset tracker for new execution
        currentTracker = new ReadsFromTracker();
        currentExecutionCrashed = false;
        
        // Set up proactive scheduler
        proactiveScheduler.setAbstractSchedule(currentAbstractSchedule);
        
        corpus.incrementExecutions();
        totalSchedulesExecuted++;
    }
    
    @Override
    public void completedScheduleExecution() {
        // Check if execution was interesting (BEFORE updating global state)
        boolean interesting = feedback.isInteresting(currentTracker, currentExecutionCrashed);
        
        // Update feedback with observed reads-from pairs
        feedback.updateFeedback(currentTracker);
        
        // Add observed events to corpus
        corpus.addObservedEvents(currentTracker.getAllEvents());
        
        if (currentExecutionCrashed) {
            corpus.incrementCrashes();
            totalBugsFound++;
        }
        
        if (interesting) {
            // Add mutated schedules to corpus
            int energy = feedback.computeEnergy(currentAbstractSchedule);
            
            for (int i = 0; i < energy; i++) {
                AbstractSchedule mutated = mutator.mutate(
                    currentAbstractSchedule, 
                    corpus.getAllObservedEvents()
                );
                corpus.addSchedule(mutated);
            }
        }
        
        // Put schedule back if it still has energy
        int energy = feedback.computeEnergy(currentAbstractSchedule);
        if (energy > 0) {
            corpus.putBack(currentAbstractSchedule);
        }
        
        // Print progress
        if (totalSchedulesExecuted % 100 == 0) {
            printProgress();
        }
    }
    
    @Override
    public boolean canExecuteMoreSchedules() {
        // Check timeout
        if (System.currentTimeMillis() - startTime > timeoutMillis) {
            return false;
        }
        
        // Check max iterations
        if (totalSchedulesExecuted >= maxIterations) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public Object choose(SortedSet<? extends Object> objectChoices, ChoiceType choiceType) {
        if (objectChoices.isEmpty()) {
            return null;
        }
        
        if (objectChoices.size() == 1) {
            return objectChoices.first();
        }
        
        // Convert ThreadInfo choices to thread names
        Set<String> threadNames = new HashSet<>();
        for (Object choice : objectChoices) {
            if (choice instanceof ThreadInfo) {
                ThreadInfo ti = (ThreadInfo) choice;
                threadNames.add(ti.getThread().getName());
            }
        }
        
        // Use proactive scheduler to choose
        String chosenThread = proactiveScheduler.chooseNextThread(threadNames);
        
        // Find the corresponding ThreadInfo
        for (Object choice : objectChoices) {
            if (choice instanceof ThreadInfo) {
                ThreadInfo ti = (ThreadInfo) choice;
                if (ti.getThread().getName().equals(chosenThread)) {
                    return ti;
                }
            }
        }
        
        // Fallback: choose first
        return objectChoices.first();
    }
    
    @Override
    public List<Integer> getChoicesMadeDuringThisSchedule() {
        // RFF doesn't track choices like MCR does
        // Return empty list for compatibility
        return new ArrayList<>();
    }
    
    /**
     * Record a reads-from pair observed during execution
     */
    public void recordReadsFrom(String readLoc, int readLine, String writeLoc, int writeLine,
                                String readThread, String writeThread) {
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, readLoc, readLine, readThread);
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, writeLoc, writeLine, writeThread);
        currentTracker.recordReadsFrom(read, write);
    }
    
    /**
     * Record that an event was enabled
     */
    public void recordEventEnabled(String loc, int line, String threadName, boolean isRead) {
        if (proactiveScheduler == null) {
            return;
        }
        AbstractEvent.Op op = isRead ? AbstractEvent.Op.READ : AbstractEvent.Op.WRITE;
        AbstractEvent event = new AbstractEvent(op, loc, line, threadName);
        proactiveScheduler.onEventEnabled(event, threadName);
    }
    
    /**
     * Record that an event was executed
     */
    public void recordEventExecuted(String loc, int line, String threadName, boolean isRead) {
        AbstractEvent.Op op = isRead ? AbstractEvent.Op.READ : AbstractEvent.Op.WRITE;
        AbstractEvent event = new AbstractEvent(op, loc, line, threadName);
        proactiveScheduler.onEventExecuted(event, threadName);
        currentTracker.recordEvent(event);
        
        // Track reads-from relations: when a READ occurs, find the corresponding WRITE
        if (isRead) {
            AbstractEvent observedWrite = findLatestWrite(loc, threadName);
            if (observedWrite != null) {
                currentTracker.recordReadsFrom(event, observedWrite);
            }
        }
    }
    
    /**
     * Find the latest write to the same location from a different thread
     */
    private AbstractEvent findLatestWrite(String loc, String readThreadName) {
        AbstractEvent latestWrite = null;
        for (AbstractEvent e : currentTracker.getAllEvents()) {
            if (e.getOp() == AbstractEvent.Op.WRITE && e.getLocation().equals(loc)) {
                // In a real implementation, we'd check if this write happened-before the read
                // For now, just find any write to the same location
                latestWrite = e;
            }
        }
        return latestWrite;
    }
    
    /**
     * Mark current execution as crashed
     */
    public void markCrashed() {
        this.currentExecutionCrashed = true;
    }
    
    public int getTotalBugsFound() {
        return totalBugsFound;
    }
    
    /**
     * Print progress statistics
     */
    private void printProgress() {
        System.out.println("[RFF] Progress: " + totalSchedulesExecuted + " schedules, " +
                          totalBugsFound + " bugs, corpus size: " + corpus.size() +
                          ", unique RF pairs: " + feedback.getTotalUniquePairs());
    }
    
    /**
     * Get final statistics
     */
    public void printStatistics() {
        System.out.println("\n[RFF] Final Statistics:");
        System.out.println("  Total schedules executed: " + totalSchedulesExecuted);
        System.out.println("  Total bugs found: " + totalBugsFound);
        System.out.println("  Corpus size: " + corpus.size());
        System.out.println("  Unique reads-from pairs: " + feedback.getTotalUniquePairs());
        System.out.println("  Total combinations: " + feedback.getTotalCombinations());
        System.out.println("  Time elapsed: " + (System.currentTimeMillis() - startTime) / 1000 + "s");
    }
}
