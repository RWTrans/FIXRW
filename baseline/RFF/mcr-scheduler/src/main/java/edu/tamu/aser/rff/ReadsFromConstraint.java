package edu.tamu.aser.rff;

import java.util.Objects;

/**
 * Reads-from constraint: w rf-> r  (positive) or w rf/-> r (negative)
 * Represents that read event r must (or must not) observe the value written by write event w.
 */
public class ReadsFromConstraint {
    private final AbstractEvent write;   // write event
    private final AbstractEvent read;    // read event
    private final boolean positive;      // true: w rf-> r, false: w rf/-> r
    
    public ReadsFromConstraint(AbstractEvent write, AbstractEvent read, boolean positive) {
        assert write.getOp() == AbstractEvent.Op.WRITE : "First argument must be a write event";
        assert read.getOp() == AbstractEvent.Op.READ : "Second argument must be a read event";
        assert write.getLocation().equals(read.getLocation()) : "Write and read must be on same location";
        this.write = write;
        this.read = read;
        this.positive = positive;
    }
    
    public static ReadsFromConstraint positive(AbstractEvent write, AbstractEvent read) {
        return new ReadsFromConstraint(write, read, true);
    }
    
    public static ReadsFromConstraint negative(AbstractEvent write, AbstractEvent read) {
        return new ReadsFromConstraint(write, read, false);
    }
    
    public AbstractEvent getWrite() { return write; }
    public AbstractEvent getRead() { return read; }
    public boolean isPositive() { return positive; }
    public boolean isNegative() { return !positive; }
    
    /**
     * Get the negated constraint: (w rf-> r) becomes (w rf/-> r) and vice versa
     */
    public ReadsFromConstraint negate() {
        return new ReadsFromConstraint(write, read, !positive);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ReadsFromConstraint)) return false;
        ReadsFromConstraint that = (ReadsFromConstraint) o;
        return positive == that.positive &&
               Objects.equals(write, that.write) &&
               Objects.equals(read, that.read);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(write, read, positive);
    }
    
    @Override
    public String toString() {
        return write + (positive ? " rf-> " : " rf/> ") + read;
    }
}
