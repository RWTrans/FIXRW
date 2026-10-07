package edu.tamu.aser.rff;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Abstract schedule: a set of positive and negative reads-from constraints.
 * α = α⁺ ⊎ α⁻
 * 
 * This represents an equivalence class of concrete schedules that all satisfy
 * the same reads-from constraints. The proactive scheduler will attempt to
 * find a concrete execution that instantiates this abstract schedule.
 */
public class AbstractSchedule {
    private final Set<ReadsFromConstraint> positiveConstraints;
    private final Set<ReadsFromConstraint> negativeConstraints;
    
    // Set of all events observed (used for mutation)
    private final Set<AbstractEvent> observedEvents;
    
    // Unique ID for corpus management
    private final int id;
    private static int nextId = 0;
    
    // For power schedule: track how many times this schedule has been chosen
    private int timesChosen = 0;
    private int timesSkipped = 0;
    
    public AbstractSchedule() {
        this(new HashSet<>(), new HashSet<>(), new HashSet<>());
    }
    
    public AbstractSchedule(Set<ReadsFromConstraint> positive, 
                           Set<ReadsFromConstraint> negative,
                           Set<AbstractEvent> events) {
        this.positiveConstraints = new HashSet<>(positive);
        this.negativeConstraints = new HashSet<>(negative);
        this.observedEvents = new HashSet<>(events);
        this.id = nextId++;
    }
    
    public Set<ReadsFromConstraint> getPositiveConstraints() { 
        return Collections.unmodifiableSet(positiveConstraints); 
    }
    
    public Set<ReadsFromConstraint> getNegativeConstraints() { 
        return Collections.unmodifiableSet(negativeConstraints); 
    }
    
    public Set<ReadsFromConstraint> getAllConstraints() {
        Set<ReadsFromConstraint> all = new HashSet<>(positiveConstraints);
        all.addAll(negativeConstraints);
        return all;
    }
    
    public Set<AbstractEvent> getObservedEvents() {
        return Collections.unmodifiableSet(observedEvents);
    }
    
    public int getId() { return id; }
    
    public int getTimesChosen() { return timesChosen; }
    public void incrementTimesChosen() { timesChosen++; }
    
    public int getTimesSkipped() { return timesSkipped; }
    public void incrementTimesSkipped() { timesSkipped++; }
    public void resetTimesSkipped() { timesSkipped = 0; }
    
    /**
     * Check if a concrete schedule's reads-from relation satisfies this abstract schedule
     */
    public boolean isSatisfiedBy(Map<AbstractEvent, AbstractEvent> concreteRF) {
        // Check positive constraints: each must be satisfied
        for (ReadsFromConstraint c : positiveConstraints) {
            AbstractEvent w = c.getWrite();
            AbstractEvent r = c.getRead();
            
            // Find if there's a matching (w, r) pair in concreteRF
            boolean satisfied = concreteRF.entrySet().stream()
                .anyMatch(e -> matches(e.getKey(), r) && matches(e.getValue(), w));
            
            if (!satisfied) return false;
        }
        
        // Check negative constraints: none must be violated
        for (ReadsFromConstraint c : negativeConstraints) {
            AbstractEvent w = c.getWrite();
            AbstractEvent r = c.getRead();
            
            // Find if there's a matching (w, r) pair in concreteRF
            boolean violated = concreteRF.entrySet().stream()
                .anyMatch(e -> matches(e.getKey(), r) && matches(e.getValue(), w));
            
            if (violated) return false;
        }
        
        return true;
    }
    
    /**
     * Check if a concrete event matches an abstract event (same op, location, line)
     */
    private boolean matches(AbstractEvent concrete, AbstractEvent abstractEvent) {
        return concrete.getOp() == abstractEvent.getOp() &&
               concrete.getLocation().equals(abstractEvent.getLocation()) &&
               concrete.getLine() == abstractEvent.getLine();
    }
    
    /**
     * Add a new constraint to this schedule
     */
    public void addConstraint(ReadsFromConstraint constraint) {
        if (constraint.isPositive()) {
            positiveConstraints.add(constraint);
        } else {
            negativeConstraints.add(constraint);
        }
    }
    
    /**
     * Remove a constraint from this schedule
     */
    public void removeConstraint(ReadsFromConstraint constraint) {
        positiveConstraints.remove(constraint);
        negativeConstraints.remove(constraint);
    }
    
    /**
     * Add an observed event
     */
    public void addObservedEvent(AbstractEvent event) {
        observedEvents.add(event);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractSchedule)) return false;
        AbstractSchedule that = (AbstractSchedule) o;
        return Objects.equals(positiveConstraints, that.positiveConstraints) &&
               Objects.equals(negativeConstraints, that.negativeConstraints);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(positiveConstraints, negativeConstraints);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("AbstractSchedule#").append(id).append("{\n");
        sb.append("  Positive: ").append(positiveConstraints.stream()
            .map(Objects::toString).collect(Collectors.joining(", "))).append("\n");
        sb.append("  Negative: ").append(negativeConstraints.stream()
            .map(Objects::toString).collect(Collectors.joining(", "))).append("\n");
        sb.append("  Events: ").append(observedEvents.size()).append(" observed\n");
        sb.append("}");
        return sb.toString();
    }
}
