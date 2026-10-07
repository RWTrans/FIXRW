package edu.tamu.aser.rff;

import java.util.*;

/**
 * Tracks reads-from relations observed during a concrete execution.
 * Maps each read event to the write event it observed its value from.
 */
public class ReadsFromTracker {
    // Map from read event to the write event it observed
    private final Map<AbstractEvent, AbstractEvent> readsFromMap;
    
    // Set of all unique reads-from pairs observed (for feedback)
    private final Set<ReadsFromPair> observedPairs;
    
    // All events observed in this execution
    private final Set<AbstractEvent> allEvents;
    
    public ReadsFromTracker() {
        this.readsFromMap = new HashMap<>();
        this.observedPairs = new HashSet<>();
        this.allEvents = new HashSet<>();
    }
    
    /**
     * Record that read event r observed the value written by write event w
     */
    public void recordReadsFrom(AbstractEvent read, AbstractEvent write) {
        readsFromMap.put(read, write);
        observedPairs.add(new ReadsFromPair(write, read));
        allEvents.add(read);
        allEvents.add(write);
    }
    
    /**
     * Record an event that was executed (but not a reads-from pair)
     */
    public void recordEvent(AbstractEvent event) {
        allEvents.add(event);
    }
    
    /**
     * Get the write event that a read event observed
     */
    public AbstractEvent getObservedWrite(AbstractEvent read) {
        return readsFromMap.get(read);
    }
    
    /**
     * Check if a specific reads-from pair was observed
     */
    public boolean observedPair(AbstractEvent write, AbstractEvent read) {
        return observedPairs.contains(new ReadsFromPair(write, read));
    }
    
    /**
     * Get all unique reads-from pairs observed
     */
    public Set<ReadsFromPair> getObservedPairs() {
        return Collections.unmodifiableSet(observedPairs);
    }
    
    /**
     * Get all events observed
     */
    public Set<AbstractEvent> getAllEvents() {
        return Collections.unmodifiableSet(allEvents);
    }
    
    /**
     * Get the reads-from map for constraint checking
     */
    public Map<AbstractEvent, AbstractEvent> getReadsFromMap() {
        return Collections.unmodifiableMap(readsFromMap);
    }
    
    /**
     * Check if any new reads-from pair was observed that is not in the given set
     */
    public boolean hasNewPair(Set<ReadsFromPair> knownPairs) {
        for (ReadsFromPair pair : observedPairs) {
            if (!knownPairs.contains(pair)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * A pair of (write, read) events representing a reads-from relation
     */
    public static class ReadsFromPair {
        private final AbstractEvent write;
        private final AbstractEvent read;
        
        public ReadsFromPair(AbstractEvent write, AbstractEvent read) {
            this.write = write;
            this.read = read;
        }
        
        public AbstractEvent getWrite() { return write; }
        public AbstractEvent getRead() { return read; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReadsFromPair)) return false;
            ReadsFromPair that = (ReadsFromPair) o;
            return Objects.equals(write, that.write) && Objects.equals(read, that.read);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(write, read);
        }
        
        @Override
        public String toString() {
            return write + " -> " + read;
        }
    }
}
