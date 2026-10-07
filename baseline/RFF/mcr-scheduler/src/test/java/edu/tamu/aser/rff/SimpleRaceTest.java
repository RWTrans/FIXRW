package edu.tamu.aser.rff;

import edu.tamu.aser.reex.Scheduler;

/**
 * Simple concurrent test program for RFF end-to-end verification.
 * Mimics JUnit4MCRRunner: uses separate exploration thread.
 */
public class SimpleRaceTest {
    private static int sharedCounter = 0;
    private static final int ITERATIONS = 10000;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting SimpleRaceTest with RFF...");
        
        // Start exploration
        Scheduler.startingExploration("SimpleRaceTest");
        
        // Create exploration thread (mimics JUnit4MCRRunner)
        Thread explorationThread = new Thread(() -> {
            int executions = 0;
            int bugsFound = 0;
            
            while (Scheduler.canExecuteMoreSchedules() && executions < 10) {
                Scheduler.startingScheduleExecution();
                
                try {
                    // Reset state for new execution
                    sharedCounter = 0;
                    
                    // Run the concurrent test
                    runOnce();
                    
                    executions++;
                    
                    System.out.println("Execution " + executions + ": counter=" + sharedCounter);
                    
                    // Check for bug - use assertion so RFF can detect it
                    if (sharedCounter != 2 * ITERATIONS) {
                        bugsFound++;
                        String msg = "Race condition! Expected " + (2 * ITERATIONS) + " but got " + sharedCounter;
                        System.out.println("BUG DETECTED in execution " + executions + ": " + msg);
                        // Throw exception so RFF's failure detection catches it
                        throw new RuntimeException(msg);
                    }
                } catch (Exception e) {
                    System.out.println("Exception in execution " + executions + ": " + e.getMessage());
                    Scheduler.failureDetected(e.getMessage());
                }
                
                Scheduler.completedScheduleExecution();
            }
            
            System.out.println("\n=== SimpleRaceTest Results ===");
            System.out.println("Total executions: " + executions);
            System.out.println("Total bugs found: " + bugsFound);
            
            if (bugsFound > 0) {
                System.out.println("RFF successfully detected race conditions!");
            } else {
                System.out.println("No bugs detected.");
            }
        }, "ExplorationThread");
        
        explorationThread.start();
        explorationThread.join();
        
        Scheduler.completedExploration();
        System.out.println("Exploration completed.");
    }
    
    private static void runOnce() throws InterruptedException {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < ITERATIONS; i++) {
                    sharedCounter++;
                }
            }
        }, "Writer-1");
        
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < ITERATIONS; i++) {
                    sharedCounter++;
                }
            }
        }, "Writer-2");
        
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
