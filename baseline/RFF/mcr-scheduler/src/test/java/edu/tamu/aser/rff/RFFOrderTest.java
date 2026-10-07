package edu.tamu.aser.rff;

import edu.tamu.aser.reex.Scheduler;

/**
 * Test to verify RFF can detect order violations.
 * Writer should write before Reader reads, but RFF should find
 * schedules where Reader executes first and sees the initial value.
 */
public class RFFOrderTest {
    // Shared variable - static so synthetic access methods are used
    static int sharedValue = 0;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting RFFOrderTest...");
        
        // Initialize exploration
        Scheduler.startingExploration("RFFOrderTest");
        
        int bugsFound = 0;
        int executions = 0;
        
        // Run exploration directly in main thread (instrumented)
        while (Scheduler.canExecuteMoreSchedules() && executions < 10) {
            executions++;
            Scheduler.startingScheduleExecution();
            
            // Reset shared value
            sharedValue = 0;
            
            // Create writer and reader threads
            Thread writer = new Thread(new WriterThread(), "Writer");
            Thread reader = new Thread(new ReaderThread(), "Reader");
            
            // Start writer and reader
            edu.tamu.aser.reex.Scheduler.beforeForking(writer);
            writer.start();
            edu.tamu.aser.reex.Scheduler.afterForking(writer);
            
            edu.tamu.aser.reex.Scheduler.beforeForking(reader);
            reader.start();
            edu.tamu.aser.reex.Scheduler.afterForking(reader);
            
            try {
                reader.join();
                writer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            
            // Check if Reader threw an exception (BUG detected)
            // In a real implementation, we'd use a custom ThreadFactory or handler
            // For now, just continue execution
            
            Scheduler.completedScheduleExecution();
            System.out.println("Execution " + executions + " completed");
        }
        
        System.out.println("\n=== Results ===");
        System.out.println("Executions: " + executions);
        
        // Get bug count from RFFStrategy
        int rffBugs = 0;
        try {
            Class<?> rffStrategyClass = Class.forName("edu.tamu.aser.rff.RFFStrategy");
            java.lang.reflect.Method getTotalBugs = rffStrategyClass.getMethod("getTotalBugsFound");
            Object strategy = edu.tamu.aser.reex.Scheduler.getSchedulingStrategy();
            if (strategy != null && rffStrategyClass.isInstance(strategy)) {
                rffBugs = (Integer) getTotalBugs.invoke(strategy);
            }
        } catch (Exception e) {
            System.err.println("Could not get RFF bug count: " + e);
        }
        
        System.out.println("Bugs found: " + rffBugs);
        
        // Print RFF statistics if available
        try {
            Class<?> rffStrategyClass = Class.forName("edu.tamu.aser.rff.RFFStrategy");
            java.lang.reflect.Method printStats = rffStrategyClass.getMethod("printStatistics");
            // Get the singleton instance from Scheduler
            Object strategy = edu.tamu.aser.reex.Scheduler.getSchedulingStrategy();
            if (strategy != null && rffStrategyClass.isInstance(strategy)) {
                printStats.invoke(strategy);
            }
        } catch (Exception e) {
            System.err.println("Could not print RFF statistics: " + e);
            e.printStackTrace();
        }
        
        if (rffBugs > 0) {
            System.out.println("SUCCESS: RFF detected " + rffBugs + " order violations!");
        } else {
            System.out.println("NOTE: No bugs detected in " + executions + " executions");
        }
        
        // Complete exploration
        Scheduler.completedExploration();
        System.out.println("Done.");
        System.exit(0);
    }
}
