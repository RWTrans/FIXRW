package edu.tamu.aser.rff;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Greybox feedback mechanism for RFF.
 * 
 * Tracks which reads-from combinations have been observed and provides
 * energy values for power scheduling.
 */
public class GreyboxFeedback {
    
    private final Set<ReadsFromTracker.ReadsFromPair> globalObservedPairs;
    private final Map<Set<ReadsFromTracker.ReadsFromPair>, Integer> combinationFrequency;
    
    // Energy computation parameters
    private static final double ENERGY_GAMMA = 1.0;  // Performance score
    private static final double ENERGY_BETA = 2.0;   // Energy divisor
    private static final int MAX_ENERGY = 16;        // Maximum energy cap
    
    public GreyboxFeedback() {
        this.globalObservedPairs = new HashSet<>();
        this.combinationFrequency = new HashMap<>();
    }
    
    /**
     * Check if a schedule execution is interesting:
     * - Crashed (bug found)
     * - Contains new reads-from pairs
     */
    public synchronized boolean isInteresting(ReadsFromTracker tracker, boolean crashed) {
        if (crashed) return true;
        return tracker.hasNewPair(globalObservedPairs);
    }
    
    /**
     * Update global state after observing a schedule execution
     */
    public synchronized void updateFeedback(ReadsFromTracker tracker) {
        Set<ReadsFromTracker.ReadsFromPair> pairs = new HashSet<>(tracker.getObservedPairs());
        
        // Add to global observed pairs
        globalObservedPairs.addAll(pairs);
        
        // Update combination frequency
        combinationFrequency.merge(new HashSet<>(pairs), 1, Integer::sum);
    }
    
    /**
     * Compute energy for a schedule using the cut-off exponential power schedule.
     * 
     * p(α) = 0                          if f(α) > μ
     * p(α) = min(γ(α)/β * 2^s(α), M)    otherwise
     */
    public synchronized int computeEnergy(AbstractSchedule schedule) {
        Set<ReadsFromTracker.ReadsFromPair> combination = getCombinationForSchedule(schedule);
        
        // Get frequency of this combination
        int frequency = combinationFrequency.getOrDefault(combination, 0);
        
        // Compute average frequency μ
        double mu = combinationFrequency.values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(1.0);
        
        // Cut-off: if frequency > μ, no energy (over-explored)
        if (frequency > mu) {
            return 0;
        }
        
        // Compute energy: min(γ/β * 2^s, M), with minimum of 1
        int timesChosen = schedule.getTimesChosen();
        double energy = (ENERGY_GAMMA / ENERGY_BETA) * Math.pow(2, timesChosen);
        int result = (int) Math.min(energy, MAX_ENERGY);
        return Math.max(result, 1);
    }
    
    /**
     * Get the reads-from combination for a schedule
     */
    private Set<ReadsFromTracker.ReadsFromPair> getCombinationForSchedule(AbstractSchedule schedule) {
        // For simplicity, return empty set (constraints are ReadsFromConstraint, not ReadsFromPair)
        // In practice, this would map the schedule to its observed reads-from pairs
        return new HashSet<>();
    }
    
    /**
     * Get total number of unique combinations observed
     */
    public synchronized int getTotalCombinations() {
        return combinationFrequency.size();
    }
    
    /**
     * Get total number of unique reads-from pairs observed
     */
    public synchronized int getTotalUniquePairs() {
        return globalObservedPairs.size();
    }
}
