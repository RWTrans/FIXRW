package edu.tamu.aser.rff;

import java.util.*;

/**
 * State machine for scheduling a negative reads-from constraint w rf/-> r.
 * 
 * States:
 * - q1: Initial state, last write on location is not w
 * - q2: Last write is w, r not yet enabled (deprioritize r, prioritize other writes)
 * - q3: r enabled while last write is not w (prioritize r)
 * - q4: Constraint satisfied (r executed without reading from w)
 * - q5: Constraint violated (r read from w)
 * - REJECT: Constraint unavoidably violated
 */
public class NegativeConstraintStateMachine {
    enum State { q1_LAST_NOT_W, q2_LAST_IS_W, q3_R_ENABLED_SAFE, q4_SATISFIED, q5_VIOLATED, REJECT }
    
    private State currentState;
    private final ReadsFromConstraint constraint;
    
    // Priority adjustments
    private boolean prioritizeRead = false;
    private boolean deprioritizeRead = false;
    private boolean prioritizeWrite = false;      // prioritize w (to set up violation scenario)
    private boolean deprioritizeWrite = false;    // deprioritize w (to avoid violation)
    private boolean prioritizeOtherWrites = false;
    
    public NegativeConstraintStateMachine(ReadsFromConstraint constraint) {
        assert constraint.isNegative() : "Must be a negative constraint";
        this.constraint = constraint;
        this.currentState = State.q1_LAST_NOT_W;
    }
    
    /**
     * Called when a write to the same location is executed
     */
    public void onWriteExecuted(AbstractEvent write) {
        boolean isTargetWrite = matches(write, constraint.getWrite());
        
        if (isTargetWrite) {
            currentState = State.q2_LAST_IS_W;
            // Deprioritize r to avoid reading from w
            deprioritizeRead = true;
            prioritizeRead = false;
            // Prioritize other writes to overwrite w
            prioritizeOtherWrites = true;
            deprioritizeWrite = false;
        } else {
            // Another write w' executed
            if (currentState == State.q2_LAST_IS_W) {
                // w' might overwrite w, back to safe state
                currentState = State.q1_LAST_NOT_W;
                prioritizeRead = true;
                deprioritizeRead = false;
                prioritizeOtherWrites = false;
            }
        }
    }
    
    /**
     * Called when the read event r is enabled
     */
    public void onReadEnabled() {
        if (currentState == State.q1_LAST_NOT_W) {
            currentState = State.q3_R_ENABLED_SAFE;
            // Safe to execute r now (last write is not w)
            prioritizeRead = true;
        } else if (currentState == State.q2_LAST_IS_W) {
            // r enabled while w is last write - deprioritize r
            deprioritizeRead = true;
            prioritizeRead = false;
        }
    }
    
    /**
     * Called when the read event r is executed
     */
    public void onReadExecuted(AbstractEvent actualWrite) {
        boolean readFromW = matches(actualWrite, constraint.getWrite());
        
        if (readFromW) {
            currentState = State.q5_VIOLATED;
            resetPriorities();
        } else {
            currentState = State.q4_SATISFIED;
            resetPriorities();
        }
    }
    
    /**
     * Called when no other thread is runnable and r must execute
     */
    public void onForcedExecution() {
        if (currentState == State.q2_LAST_IS_W) {
            // r is forced to execute while w is the last write
            // This will violate the constraint
            currentState = State.REJECT;
        }
    }
    
    public boolean isSatisfied() {
        return currentState == State.q4_SATISFIED;
    }
    
    public boolean isViolated() {
        return currentState == State.q5_VIOLATED || currentState == State.REJECT;
    }
    
    // Priority getters
    public boolean shouldPrioritizeRead() { return prioritizeRead; }
    public boolean shouldDeprioritizeRead() { return deprioritizeRead; }
    public boolean shouldPrioritizeWrite() { return prioritizeWrite; }
    public boolean shouldDeprioritizeWrite() { return deprioritizeWrite; }
    public boolean shouldPrioritizeOtherWrites() { return prioritizeOtherWrites; }
    
    private void resetPriorities() {
        prioritizeRead = false;
        deprioritizeRead = false;
        prioritizeWrite = false;
        deprioritizeWrite = false;
        prioritizeOtherWrites = false;
    }
    
    private boolean matches(AbstractEvent concrete, AbstractEvent abstractEvent) {
        return concrete.getOp() == abstractEvent.getOp() &&
               concrete.getLocation().equals(abstractEvent.getLocation()) &&
               concrete.getLine() == abstractEvent.getLine();
    }
    
    public ReadsFromConstraint getConstraint() { return constraint; }
    public State getCurrentState() { return currentState; }
}
