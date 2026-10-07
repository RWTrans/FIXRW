package edu.tamu.aser.rff;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Proactive reads-from scheduler for RFF.
 * Implements the state machine-based scheduling algorithm that biases
 * thread selection towards satisfying abstract schedule constraints.
 * 
 * When no definitive decision can be made, falls back to Partial Order Sampling (POS).
 */
public class RFProactiveScheduler {
    // Current abstract schedule being pursued
    private AbstractSchedule currentSchedule;
    
    // State machines for each constraint
    private final Map<ReadsFromConstraint, PositiveConstraintStateMachine> positiveStateMachines;
    private final Map<ReadsFromConstraint, NegativeConstraintStateMachine> negativeStateMachines;
    
    // POS fallback: random scores for threads
    private final Map<String, Double> posScores;
    private final Random random;
    
    // Last write on each memory location (for tracking reads-from)
    private final Map<String, AbstractEvent> lastWriteOnLocation;
    
    // Enabled events per thread
    private final Map<String, Set<AbstractEvent>> enabledEvents;
    
    // Current thread being executed
    private String currentThread;
    
    public RFProactiveScheduler() {
        this(new Random());
    }
    
    public RFProactiveScheduler(Random random) {
        this.positiveStateMachines = new HashMap<>();
        this.negativeStateMachines = new HashMap<>();
        this.posScores = new HashMap<>();
        this.lastWriteOnLocation = new HashMap<>();
        this.enabledEvents = new ConcurrentHashMap<>();
        this.random = random;
        this.currentSchedule = null;
    }
    
    /**
     * Set the abstract schedule to pursue for the next execution
     */
    public void setAbstractSchedule(AbstractSchedule schedule) {
        this.currentSchedule = schedule;
        this.positiveStateMachines.clear();
        this.negativeStateMachines.clear();
        this.lastWriteOnLocation.clear();
        this.enabledEvents.clear();
        this.currentThread = null;
        
        // Initialize state machines for each constraint
        for (ReadsFromConstraint c : schedule.getPositiveConstraints()) {
            positiveStateMachines.put(c, new PositiveConstraintStateMachine(c));
        }
        for (ReadsFromConstraint c : schedule.getNegativeConstraints()) {
            negativeStateMachines.put(c, new NegativeConstraintStateMachine(c));
        }
    }
    
    /**
     * Record that an event is enabled (ready to execute)
     */
    public synchronized void onEventEnabled(AbstractEvent event, String threadName) {
        if (event == null || threadName == null) {
            return;
        }
        if (enabledEvents == null) {
            return;
        }
        try {
            Set<AbstractEvent> events = enabledEvents.get(threadName);
            if (events == null) {
                events = new HashSet<>();
                enabledEvents.put(threadName, events);
            }
            events.add(event);
        } catch (Exception e) {
            // Silently ignore
        }
        
        // Notify state machines
        if (event.getOp() == AbstractEvent.Op.READ) {
            for (PositiveConstraintStateMachine sm : positiveStateMachines.values()) {
                if (matches(event, sm.getConstraint().getRead())) {
                    sm.onReadEnabled();
                }
            }
            for (NegativeConstraintStateMachine sm : negativeStateMachines.values()) {
                if (matches(event, sm.getConstraint().getRead())) {
                    sm.onReadEnabled();
                }
            }
        }
    }
    
    /**
     * Record that an event has been executed
     */
    public void onEventExecuted(AbstractEvent event, String threadName) {
        // Remove from enabled
        Set<AbstractEvent> threadEvents = enabledEvents.get(threadName);
        if (threadEvents != null) {
            threadEvents.remove(event);
        }
        
        // Update last write tracking
        if (event.getOp() == AbstractEvent.Op.WRITE) {
            lastWriteOnLocation.put(event.getLocation(), event);
            
            // Notify state machines about write execution
            for (PositiveConstraintStateMachine sm : positiveStateMachines.values()) {
                if (matches(event, sm.getConstraint().getWrite())) {
                    sm.onWriteExecuted();
                } else if (event.getLocation().equals(sm.getConstraint().getWrite().getLocation())) {
                    // Another write to same location
                    sm.onOtherWriteExecuted(event);
                }
            }
            for (NegativeConstraintStateMachine sm : negativeStateMachines.values()) {
                if (event.getLocation().equals(sm.getConstraint().getWrite().getLocation())) {
                    sm.onWriteExecuted(event);
                }
            }
        }
        
        // Handle read execution
        if (event.getOp() == AbstractEvent.Op.READ) {
            AbstractEvent observedWrite = lastWriteOnLocation.get(event.getLocation());
            
            for (PositiveConstraintStateMachine sm : positiveStateMachines.values()) {
                if (matches(event, sm.getConstraint().getRead())) {
                    sm.onReadExecuted(observedWrite);
                }
            }
            for (NegativeConstraintStateMachine sm : negativeStateMachines.values()) {
                if (matches(event, sm.getConstraint().getRead())) {
                    sm.onReadExecuted(observedWrite);
                }
            }
        }
        
        this.currentThread = threadName;
    }
    
    /**
     * Choose the next thread to execute using proactive scheduling + POS fallback.
     * 
     * Algorithm:
     * 1. Filter to runnable threads
     * 2. Use state machine priorities to bias selection
     * 3. Fall back to POS when no definitive decision
     */
    public String chooseNextThread(Set<String> runnableThreads) {
        if (runnableThreads.isEmpty()) {
            return null; // Deadlock
        }
        
        if (runnableThreads.size() == 1) {
            return runnableThreads.iterator().next();
        }
        
        // Compute priority score for each thread
        Map<String, Integer> threadScores = new HashMap<>();
        for (String thread : runnableThreads) {
            threadScores.put(thread, computeThreadScore(thread));
        }
        
        // Find threads with highest score
        int maxScore = threadScores.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        List<String> bestThreads = threadScores.entrySet().stream()
            .filter(e -> e.getValue() == maxScore)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        // If multiple threads have same score, use POS
        if (bestThreads.size() > 1) {
            return chooseByPOS(bestThreads);
        }
        
        return bestThreads.get(0);
    }
    
    /**
     * Compute priority score for a thread based on state machine priorities
     */
    private int computeThreadScore(String threadName) {
        int score = 0;
        Set<AbstractEvent> threadEvents = enabledEvents.getOrDefault(threadName, Collections.emptySet());
        
        // Check positive constraint state machines
        for (PositiveConstraintStateMachine sm : positiveStateMachines.values()) {
            for (AbstractEvent event : threadEvents) {
                // Prioritize events that should be executed
                if (sm.shouldPrioritizeRead() && matches(event, sm.getConstraint().getRead())) {
                    score += 10;
                }
                if (sm.shouldPrioritizeWrite() && matches(event, sm.getConstraint().getWrite())) {
                    score += 10;
                }
                
                // Deprioritize events that should be delayed
                if (sm.shouldDeprioritizeRead() && matches(event, sm.getConstraint().getRead())) {
                    score -= 10;
                }
                if (sm.shouldDeprioritizeOtherWrites() && 
                    event.getOp() == AbstractEvent.Op.WRITE &&
                    event.getLocation().equals(sm.getConstraint().getWrite().getLocation()) &&
                    !matches(event, sm.getConstraint().getWrite())) {
                    score -= 5;
                }
            }
        }
        
        // Check negative constraint state machines
        for (NegativeConstraintStateMachine sm : negativeStateMachines.values()) {
            for (AbstractEvent event : threadEvents) {
                if (sm.shouldPrioritizeRead() && matches(event, sm.getConstraint().getRead())) {
                    score += 10;
                }
                if (sm.shouldDeprioritizeRead() && matches(event, sm.getConstraint().getRead())) {
                    score -= 10;
                }
                if (sm.shouldPrioritizeOtherWrites() && 
                    event.getOp() == AbstractEvent.Op.WRITE &&
                    event.getLocation().equals(sm.getConstraint().getWrite().getLocation())) {
                    score += 5;
                }
            }
        }
        
        return score;
    }
    
    /**
     * Partial Order Sampling (POS) fallback for tie-breaking
     * Uses random selection to explore different thread interleavings
     */
    private String chooseByPOS(List<String> threads) {
        // For small thread counts, use truly random selection to ensure exploration
        if (threads.size() <= 2) {
            return threads.get(random.nextInt(threads.size()));
        }
        
        // Assign random scores if not already assigned
        for (String thread : threads) {
            posScores.putIfAbsent(thread, random.nextDouble());
        }
        
        // Choose thread with highest POS score
        return threads.stream()
            .max(Comparator.comparingDouble(t -> posScores.getOrDefault(t, 0.0)))
            .orElse(threads.get(0));
    }
    
    /**
     * Check if a constraint matches an event (same op, location, line)
     */
    private boolean matches(AbstractEvent event, AbstractEvent abstractEvent) {
        if (event == null || abstractEvent == null) return false;
        if (event.getOp() == null || abstractEvent.getOp() == null) return false;
        if (event.getLocation() == null || abstractEvent.getLocation() == null) return false;
        return event.getOp() == abstractEvent.getOp() &&
               event.getLocation().equals(abstractEvent.getLocation()) &&
               event.getLine() == abstractEvent.getLine();
    }
    
    /**
     * Check if all positive constraints have been satisfied
     */
    public boolean allPositiveConstraintsSatisfied() {
        return positiveStateMachines.values().stream()
            .allMatch(PositiveConstraintStateMachine::isSatisfied);
    }
    
    /**
     * Get violated constraints (for debugging)
     */
    public List<ReadsFromConstraint> getViolatedConstraints() {
        List<ReadsFromConstraint> violated = new ArrayList<>();
        
        for (Map.Entry<ReadsFromConstraint, PositiveConstraintStateMachine> e : positiveStateMachines.entrySet()) {
            if (e.getValue().isViolated()) {
                violated.add(e.getKey());
            }
        }
        for (Map.Entry<ReadsFromConstraint, NegativeConstraintStateMachine> e : negativeStateMachines.entrySet()) {
            if (e.getValue().isViolated()) {
                violated.add(e.getKey());
            }
        }
        
        return violated;
    }
    
    /**
     * Reset for new execution
     */
    public void reset() {
        this.currentSchedule = null;
        this.positiveStateMachines.clear();
        this.negativeStateMachines.clear();
        this.lastWriteOnLocation.clear();
        this.enabledEvents.clear();
        this.currentThread = null;
        this.posScores.clear();
    }
}
