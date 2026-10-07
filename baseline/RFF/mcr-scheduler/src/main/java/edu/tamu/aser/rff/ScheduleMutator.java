package edu.tamu.aser.rff;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mutation operators for abstract schedules, as defined in RFF paper Section 3.
 * 
 * Four mutation operators:
 * - insert(α, C) = α ∪ {C}
 * - swap(α, C₁, C₂) = (α \ {C₁}) ∪ {C₂}
 * - delete(α, C) = α \ {C}
 * - negate(α, C) = swap(α, C, ¬C)
 */
public class ScheduleMutator {
    private final Random random;
    
    public ScheduleMutator() {
        this(new Random());
    }
    
    public ScheduleMutator(Random random) {
        this.random = random;
    }
    
    /**
     * Mutate an abstract schedule by applying one random mutation operator
     */
    public AbstractSchedule mutate(AbstractSchedule schedule, Set<AbstractEvent> allObservedEvents) {
        int operator = random.nextInt(4);
        
        switch (operator) {
            case 0: return insert(schedule, allObservedEvents);
            case 1: return swap(schedule, allObservedEvents);
            case 2: return delete(schedule);
            case 3: return negate(schedule);
            default: return insert(schedule, allObservedEvents);
        }
    }
    
    /**
     * insert(α, C) = α ∪ {C}
     * Add a new random constraint formed from observed events
     */
    public AbstractSchedule insert(AbstractSchedule schedule, Set<AbstractEvent> allObservedEvents) {
        AbstractSchedule mutated = copySchedule(schedule);
        
        Optional<ReadsFromConstraint> newConstraint = generateRandomConstraint(allObservedEvents, true);
        if (newConstraint.isPresent()) {
            mutated.addConstraint(newConstraint.get());
        }
        
        return mutated;
    }
    
    /**
     * swap(α, C₁, C₂) = (α \ {C₁}) ∪ {C₂}
     * Replace one constraint with another
     */
    public AbstractSchedule swap(AbstractSchedule schedule, Set<AbstractEvent> allObservedEvents) {
        AbstractSchedule mutated = copySchedule(schedule);
        
        Set<ReadsFromConstraint> allConstraints = schedule.getAllConstraints();
        if (allConstraints.isEmpty()) {
            // Nothing to swap, fall back to insert
            return insert(schedule, allObservedEvents);
        }
        
        // Remove a random constraint
        ReadsFromConstraint toRemove = selectRandom(allConstraints);
        mutated.removeConstraint(toRemove);
        
        // Add a new random constraint
        Optional<ReadsFromConstraint> newConstraint = generateRandomConstraint(allObservedEvents, toRemove.isPositive());
        if (newConstraint.isPresent()) {
            mutated.addConstraint(newConstraint.get());
        }
        
        return mutated;
    }
    
    /**
     * delete(α, C) = α \ {C}
     * Remove a random constraint
     */
    public AbstractSchedule delete(AbstractSchedule schedule) {
        AbstractSchedule mutated = copySchedule(schedule);
        
        Set<ReadsFromConstraint> allConstraints = schedule.getAllConstraints();
        if (!allConstraints.isEmpty()) {
            ReadsFromConstraint toRemove = selectRandom(allConstraints);
            mutated.removeConstraint(toRemove);
        }
        
        return mutated;
    }
    
    /**
     * negate(α, C) = swap(α, C, ¬C)
     * Flip a constraint from positive to negative or vice versa
     */
    public AbstractSchedule negate(AbstractSchedule schedule) {
        AbstractSchedule mutated = copySchedule(schedule);
        
        Set<ReadsFromConstraint> allConstraints = schedule.getAllConstraints();
        if (allConstraints.isEmpty()) {
            return mutated;
        }
        
        ReadsFromConstraint toNegate = selectRandom(allConstraints);
        mutated.removeConstraint(toNegate);
        mutated.addConstraint(toNegate.negate());
        
        return mutated;
    }
    
    /**
     * Generate a random reads-from constraint from observed events
     */
    private Optional<ReadsFromConstraint> generateRandomConstraint(
            Set<AbstractEvent> events, boolean positive) {
        
        // Get all write events
        List<AbstractEvent> writes = events.stream()
            .filter(e -> e.getOp() == AbstractEvent.Op.WRITE)
            .collect(Collectors.toList());
        
        // Get all read events
        List<AbstractEvent> reads = events.stream()
            .filter(e -> e.getOp() == AbstractEvent.Op.READ)
            .collect(Collectors.toList());
        
        if (writes.isEmpty() || reads.isEmpty()) {
            return Optional.empty();
        }
        
        // Find a matching write-read pair on the same location
        List<AbstractEvent> candidateWrites = new ArrayList<>();
        List<AbstractEvent> candidateReads = new ArrayList<>();
        
        AbstractEvent read = selectRandom(reads);
        
        // Find writes on the same location
        for (AbstractEvent write : writes) {
            if (write.getLocation().equals(read.getLocation())) {
                candidateWrites.add(write);
            }
        }
        
        if (candidateWrites.isEmpty()) {
            return Optional.empty();
        }
        
        AbstractEvent write = selectRandom(candidateWrites);
        return Optional.of(new ReadsFromConstraint(write, read, positive));
    }
    
    private <T> T selectRandom(Collection<T> collection) {
        int index = random.nextInt(collection.size());
        int i = 0;
        for (T item : collection) {
            if (i == index) return item;
            i++;
        }
        return null;
    }
    
    private AbstractSchedule copySchedule(AbstractSchedule original) {
        return new AbstractSchedule(
            original.getPositiveConstraints(),
            original.getNegativeConstraints(),
            original.getObservedEvents()
        );
    }
}
