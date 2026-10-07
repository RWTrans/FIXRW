package edu.tamu.aser.rff;

import edu.tamu.aser.reex.Scheduler;

/**
 * Real concurrent test where two threads race on a shared variable.
 * RFF should find schedules where the interleaving causes a bug.
 */
public class RFFRaceTest {
    private static int counter = 0;
    private static final int ITERATIONS = 100;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting RFFRaceTest...");
        
        Scheduler.startingExploration("RFFRaceTest");
        
        Thread explorationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                int executions = 0;
                int bugsFound = 0;
                
                while (Scheduler.canExecuteMoreSchedules() && executions < 10) {
                    Scheduler.startingScheduleExecution();
                    
                    try {
                        CounterThread.counter = 0;
                        
                        Thread t1 = new Thread(new CounterThread(), "T1");
                        Thread t2 = new Thread(new CounterThread(), "T2");
                        
                        t1.start();
                        t2.start();
                        t1.join();
                        t2.join();
                        
                        executions++;
                        
                        if (CounterThread.counter != 2 * ITERATIONS) {
                            bugsFound++;
                            System.out.println("BUG: counter=" + CounterThread.counter + " (expected " + (2 * ITERATIONS) + ")");
                            if (Scheduler.getSchedulingStrategy() instanceof RFFStrategy) {
                                ((RFFStrategy) Scheduler.getSchedulingStrategy()).markCrashed();
                            }
                        } else {
                            System.out.println("Execution " + executions + ": OK (counter=" + CounterThread.counter + ")");
                        }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    Scheduler.completedScheduleExecution();
                }
                
                System.out.println("\n=== Results ===");
                System.out.println("Executions: " + executions);
                System.out.println("Bugs found: " + bugsFound);
                
                // Print RFF statistics
                if (Scheduler.getSchedulingStrategy() instanceof RFFStrategy) {
                    ((RFFStrategy) Scheduler.getSchedulingStrategy()).printStatistics();
                }
                
                System.exit(0);
            }
        }, "ExplorationThread");
        
        explorationThread.start();
        explorationThread.join();
        
        Scheduler.completedExploration();
        System.out.println("Done.");
    }
}
