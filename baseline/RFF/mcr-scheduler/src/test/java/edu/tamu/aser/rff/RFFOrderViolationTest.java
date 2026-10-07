package edu.tamu.aser.rff;

import edu.tamu.aser.reex.Scheduler;

/**
 * Test that demonstrates RFF's ability to find order violations.
 * 
 * Bug: If readerThread runs before writerThread, it will see stale value.
 * RFF should find the schedule where reader reads before writer writes.
 */
public class RFFOrderViolationTest {
    private static int sharedValue = 0;
    private static volatile boolean ready = false;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting RFFOrderViolationTest...");
        
        Scheduler.startingExploration("RFFOrderViolationTest");
        
        Thread explorationThread = new Thread(() -> {
            int executions = 0;
            int bugsFound = 0;
            
            while (Scheduler.canExecuteMoreSchedules() && executions < 20) {
                Scheduler.startingScheduleExecution();
                
                try {
                    // Reset
                    sharedValue = 0;
                    ready = false;
                    
                    // Writer thread
                    Thread writer = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sharedValue = 42;  // Write
                            ready = true;       // Signal
                        }
                    }, "Writer");
                    
                    // Reader thread
                    Thread reader = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (!ready) {
                                // Spin wait
                                Thread.yield();
                            }
                            int value = sharedValue;  // Read
                            if (value != 42) {
                                throw new RuntimeException("Order violation! Read " + value + " instead of 42");
                            }
                        }
                    }, "Reader");
                    
                    reader.start();
                    writer.start();
                    reader.join();
                    writer.join();
                    
                    executions++;
                    System.out.println("Execution " + executions + ": OK");
                    
                } catch (Exception e) {
                    executions++;
                    bugsFound++;
                    System.out.println("BUG in execution " + executions + ": " + e.getMessage());
                    Scheduler.failureDetected(e.getMessage());
                }
                
                Scheduler.completedScheduleExecution();
            }
            
            System.out.println("\n=== Results ===");
            System.out.println("Executions: " + executions);
            System.out.println("Bugs found: " + bugsFound);
        }, "ExplorationThread");
        
        explorationThread.start();
        explorationThread.join();
        
        Scheduler.completedExploration();
        System.out.println("Done.");
    }
}
