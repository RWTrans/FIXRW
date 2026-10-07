package edu.tamu.aser.rff;

import java.util.*;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Corpus of abstract schedules for greybox fuzzing.
 * Manages the working set of schedules and implements the fuzzing loop.
 */
public class ScheduleCorpus {
    // Priority queue based on energy (schedules with higher energy are chosen first)
    private final PriorityBlockingQueue<ScheduleEntry> corpus;
    
    // All schedules in the corpus (for deduplication)
    private final Set<AbstractSchedule> scheduleSet;
    
    // Set of all observed events across all executions (for mutation)
    private final Set<AbstractEvent> allObservedEvents;
    
    // Statistics
    private int totalExecutions = 0;
    private int totalCrashes = 0;
    
    public ScheduleCorpus() {
        this.corpus = new PriorityBlockingQueue<>(11, new ScheduleEntryComparator());
        this.scheduleSet = new HashSet<>();
        this.allObservedEvents = new HashSet<>();
    }
    
    /**
     * Add a schedule to the corpus if it's not already present
     */
    public boolean addSchedule(AbstractSchedule schedule) {
        if (scheduleSet.contains(schedule)) {
            return false;
        }
        
        scheduleSet.add(schedule);
        corpus.offer(new ScheduleEntry(schedule));
        return true;
    }
    
    /**
     * Pick the next schedule to fuzz (highest energy first)
     */
    public AbstractSchedule pickNext() {
        ScheduleEntry entry = corpus.poll();
        if (entry == null) {
            return null;
        }
        
        AbstractSchedule schedule = entry.getSchedule();
        schedule.incrementTimesChosen();
        return schedule;
    }
    
    /**
     * Put a schedule back into the corpus (after fuzzing)
     */
    public void putBack(AbstractSchedule schedule) {
        corpus.offer(new ScheduleEntry(schedule));
    }
    
    /**
     * Check if corpus is empty
     */
    public boolean isEmpty() {
        return corpus.isEmpty();
    }
    
    /**
     * Get the size of the corpus
     */
    public int size() {
        return corpus.size();
    }
    
    /**
     * Add observed events to the global event set
     */
    public void addObservedEvents(Set<AbstractEvent> events) {
        allObservedEvents.addAll(events);
    }
    
    /**
     * Get all observed events (for mutation)
     */
    public Set<AbstractEvent> getAllObservedEvents() {
        return Collections.unmodifiableSet(allObservedEvents);
    }
    
    /**
     * Increment execution count
     */
    public void incrementExecutions() {
        totalExecutions++;
    }
    
    /**
     * Increment crash count
     */
    public void incrementCrashes() {
        totalCrashes++;
    }
    
    /**
     * Get statistics
     */
    public int getTotalExecutions() { return totalExecutions; }
    public int getTotalCrashes() { return totalCrashes; }
    
    /**
     * Entry in the corpus with energy-based priority
     */
    private static class ScheduleEntry {
        private final AbstractSchedule schedule;
        
        public ScheduleEntry(AbstractSchedule schedule) {
            this.schedule = schedule;
        }
        
        public AbstractSchedule getSchedule() {
            return schedule;
        }
    }
    
    /**
     * Comparator for prioritizing schedules by energy
     * Schedules with more times chosen (higher energy) come first
     */
    private static class ScheduleEntryComparator implements Comparator<ScheduleEntry> {
        @Override
        public int compare(ScheduleEntry e1, ScheduleEntry e2) {
            // Higher timesChosen = higher priority (for exponential energy growth)
            return Integer.compare(e2.getSchedule().getTimesChosen(), 
                                   e1.getSchedule().getTimesChosen());
        }
    }
}
