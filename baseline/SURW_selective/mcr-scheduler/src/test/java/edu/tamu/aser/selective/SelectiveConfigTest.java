package edu.tamu.aser.selective;

/**
 * Test to verify SelectiveConfig logic
 * Run with: -Dselective.config=selective_config.txt
 */
public class SelectiveConfigTest {
    public static void main(String[] args) {
        System.out.println("=== SelectiveConfig Logic Test ===\n");
        System.out.println("Config file: " + System.getProperty("selective.config", "selective_config.txt (default)"));
        System.out.println();
        
        // Test 1: Check if enabled
        System.out.println("Test 1: Check if selective mode is enabled");
        System.out.println("  isEnabled() = " + SelectiveConfig.isEnabled());
        if (!SelectiveConfig.isEnabled()) {
            System.out.println("  -> Selective mode DISABLED, all events are interested");
            System.out.println("  -> This is the DEFAULT behavior (same as original SURW)");
            return;
        }
        System.out.println("  -> Selective mode ENABLED");
        System.out.println();
        
        // Test 2: Simulating variable extraction
        System.out.println("Test 2: Simulating variable extraction");
        System.out.println("  Config: edu.tamu.aser.rff.CounterThread:25");
        System.out.println();
        
        // Simulate: Event at configured location
        System.out.println("  Step 1: Event at CounterThread:25 accessing 'counter'");
        boolean r1 = SelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "counter", 25);
        System.out.println("    isInterested = " + r1 + " (should be true - configured location)");
        System.out.println("    Status: " + SelectiveConfig.getStatus());
        System.out.println();
        
        // Simulate: Same variable at DIFFERENT location
        System.out.println("  Step 2: Same variable 'counter' at DIFFERENT line (line 42)");
        boolean r2 = SelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "counter", 42);
        System.out.println("    isInterested = " + r2 + " (should be true - same variable)");
        System.out.println();
        
        // Simulate: Same variable in DIFFERENT method
        System.out.println("  Step 3: Same variable 'counter' in different method (line 100)");
        boolean r3 = SelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "counter", 100);
        System.out.println("    isInterested = " + r3 + " (should be true - same variable)");
        System.out.println();
        
        // Simulate: DIFFERENT variable at same configured location
        System.out.println("  Step 4: DIFFERENT variable 'otherField' at same configured line 25");
        boolean r4 = SelectiveConfig.isInterested("edu/tamu/aser/rff/CounterThread", "otherField", 25);
        System.out.println("    isInterested = " + r4 + " (true if line 25 accesses otherField too)");
        System.out.println();
        
        // Simulate: Same variable name in DIFFERENT class
        System.out.println("  Step 5: Same field name 'counter' in DIFFERENT class");
        boolean r5 = SelectiveConfig.isInterested("edu/tamu/aser/rff/OtherClass", "counter", 25);
        System.out.println("    isInterested = " + r5 + " (should be false - different class)");
        System.out.println();
        
        System.out.println("=== Final Status ===");
        System.out.println(SelectiveConfig.getStatus());
        SelectiveConfig.printExtractedVariables();
    }
}
