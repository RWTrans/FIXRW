package edu.tamu.aser.selective;

/**
 * Simple test to verify TraceBasedSelectiveConfig logic
 */
public class TraceBasedSelectiveConfigTest {
    public static void main(String[] args) {
        System.out.println("=== TraceBasedSelectiveConfig Logic Test ===\n");
        
        // Test 1: Check mode
        System.out.println("Test 1: Check current mode");
        System.out.println("  isEnabled() = " + TraceBasedSelectiveConfig.isEnabled());
        System.out.println("  isTraceCollectionMode() = " + TraceBasedSelectiveConfig.isTraceCollectionMode());
        System.out.println("  isSchedulingMode() = " + TraceBasedSelectiveConfig.isSchedulingMode());
        
        if (!TraceBasedSelectiveConfig.isEnabled()) {
            System.out.println("  -> Selective mode DISABLED (no config file)");
            return;
        }
        
        if (TraceBasedSelectiveConfig.isTraceCollectionMode()) {
            System.out.println("  -> Trace collection mode (first run)");
            
            // Simulate some events
            System.out.println("\nTest 2: Simulating field access events");
            TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "counter", 25, true);
            TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "counter", 26, false);
            TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "value", 30, true);
            TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/WriterThread", "data", 15, false);
            
            // Trigger end of execution
            System.out.println("\nTest 3: Triggering execution end");
            TraceBasedSelectiveConfig.onExecutionEnd();
            
        } else if (TraceBasedSelectiveConfig.isSchedulingMode()) {
            System.out.println("  -> Scheduling mode (second run)");
            System.out.println("  Interested variables:");
            
            // Test: same variable should be interested
            System.out.println("\nTest 2: Checking variable matching");
            boolean r1 = TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "counter", 25, true);
            System.out.println("  counter (configured line): " + r1 + " (should be true)");
            
            boolean r2 = TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "counter", 100, true);
            System.out.println("  counter (different line): " + r2 + " (should be true - same variable)");
            
            boolean r3 = TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "otherField", 25, true);
            System.out.println("  otherField (same line): " + r3 + " (should be false - different variable)");
            
            boolean r4 = TraceBasedSelectiveConfig.isInterested("edu/tamu/aser/rff/WriterThread", "counter", 25, true);
            System.out.println("  WriterThread.counter: " + r4 + " (should be false - different class)");
        }
    }
}
