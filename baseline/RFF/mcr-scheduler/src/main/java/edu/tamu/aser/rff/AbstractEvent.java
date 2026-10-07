package edu.tamu.aser.rff;

import java.util.Objects;

/**
 * Abstract event: op(x)@l
 * Represents a memory operation (read/write) on location x at source line l.
 * Used in abstract schedules to define reads-from constraints.
 */
public class AbstractEvent {
    public enum Op { READ, WRITE }
    
    private final Op op;
    private final String location;   // memory location / variable name
    private final int line;          // source code line number
    private final String threadName; // optional: thread identifier
    
    public AbstractEvent(Op op, String location, int line) {
        this(op, location, line, null);
    }
    
    public AbstractEvent(Op op, String location, int line, String threadName) {
        this.op = op;
        this.location = location;
        this.line = line;
        this.threadName = threadName;
    }
    
    public Op getOp() { return op; }
    public String getLocation() { return location; }
    public int getLine() { return line; }
    public String getThreadName() { return threadName; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEvent)) return false;
        AbstractEvent that = (AbstractEvent) o;
        return line == that.line &&
               op == that.op &&
               Objects.equals(location, that.location) &&
               Objects.equals(threadName, that.threadName);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(op, location, line, threadName);
    }
    
    @Override
    public String toString() {
        String opStr = (op == Op.READ) ? "r" : "w";
        return opStr + "(" + location + ")@" + line +
               (threadName != null ? "[" + threadName + "]" : "");
    }
}
