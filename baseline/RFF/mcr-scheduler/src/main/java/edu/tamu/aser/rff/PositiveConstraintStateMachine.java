package edu.tamu.aser.rff;

import java.util.*;

/**
 * State machine for scheduling a positive reads-from constraint w rf-> r.
 * 
 * States:
 * - q1: Initial state, no relevant events observed
 * - q2: r enabled, w not yet executed (deprioritize r, prioritize w)
 * - q3: w executed (prioritize r, deprioritize other writes)
 * - q4: Constraint satisfied
 * - q5: r forced to execute before w (constraint violated, revert)
 */
public class PositiveConstraintStateMachine {
    enum State { q1_INITIAL, q2_R_ENABLED_W_NOT_EXEC, q3_W_EXECUTED, q4_SATISFIED, q5_VIOLATED }
    
    private State currentState;
    private final ReadsFromConstraint constraint;
    
    // Priority adjustments
    private boolean prioritizeRead = false;
    private boolean deprioritizeRead = false;
    private boolean prioritizeWrite = false;
    private boolean deprioritizeOtherWrites = false;
    
    public PositiveConstraintStateMachine(ReadsFromConstraint constraint) {
        assert constraint.isPositive() : "Must be a positive constraint";
        this.constraint = constraint;
        this.currentState = State.q1_INITIAL;
    }
    
    /**
     * Called when the read event r is enabled (ready to execute)
     */
    public void onReadEnabled() {
        if (currentState == State.q1_INITIAL) {
            currentState = State.q2_R_ENABLED_W_NOT_EXEC;
            // Deprioritize r to delay its execution
            deprioritizeRead = true;
            // Prioritize w to encourage it to execute first
            prioritizeWrite = true;
        }
    }
    
    /**
     * Called when the write event w is executed
     */
    public void onWriteExecuted() {
        if (currentState == State.q1_INITIAL || currentState == State.q2_R_ENABLED_W_NOT_EXEC) {
            currentState = State.q3_W_EXECUTED;
            // Now prioritize r to read from w
            prioritizeRead = true;
            deprioritizeRead = false;
            // Deprioritize other writes to same location to avoid overwriting
            deprioritizeOtherWrites = true;
            prioritizeWrite = false;
        }
    }
    
    /**
     * Called when the read event r is executed
     */
    public void onReadExecuted(AbstractEvent actualWrite) {
        if (currentState == State.q3_W_EXECUTED) {
            // Check if r actually read from w
            if (matches(actualWrite, constraint.getWrite())) {
                currentState = State.q4_SATISFIED;
            } else {
                currentState = State.q5_VIOLATED;
            }
            // Reset all priorities
            resetPriorities();
        } else if (currentState == State.q2_R_ENABLED_W_NOT_EXEC) {
            // r executed before w - constraint violated
            currentState = State.q5_VIOLATED;
            resetPriorities();
        }
    }
    
    /**
     * Called when another write w' to the same location is executed
     */
    public void onOtherWriteExecuted(AbstractEvent otherWrite) {
        if (currentState == State.q3_W_EXECUTED) {
            // Another write might overwrite w before r reads it
            // Keep deprioritizing other writes
            deprioritizeOtherWrites = true;
        }
    }
    
    /**
     * Check if constraint has been satisfied (existentially quantified)
     */
    public boolean isSatisfied() {
        return currentState == State.q4_SATISFIED;
    }
    
    /**
     * Check if constraint is violated and should be removed
     */
    public boolean isViolated() {
        return currentState == State.q5_VIOLATED;
    }
    
    // Priority getters for scheduler
    public boolean shouldPrioritizeRead() { return prioritizeRead; }
    public boolean shouldDeprioritizeRead() { return deprioritizeRead; }
    public boolean shouldPrioritizeWrite() { return prioritizeWrite; }
    public boolean shouldDeprioritizeOtherWrites() { return deprioritizeOtherWrites; }
    
    private void resetPriorities() {
        prioritizeRead = false;
        deprioritizeRead = false;
        prioritizeWrite = false;
        deprioritizeOtherWrites = false;
    }
    
    private boolean matches(AbstractEvent concrete, AbstractEvent abstractEvent) {
        return concrete.getOp() == abstractEvent.getOp() &&
               concrete.getLocation().equals(abstractEvent.getLocation()) &&
               concrete.getLine() == abstractEvent.getLine();
    }
    
    public ReadsFromConstraint getConstraint() { return constraint; }
    public State getCurrentState() { return currentState; }
}
