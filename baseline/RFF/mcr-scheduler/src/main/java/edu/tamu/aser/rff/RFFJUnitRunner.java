package edu.tamu.aser.rff;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * JUnit runner for RFF.
 * Similar to JUnit4MCRRunner but uses RFF strategy instead of MCR.
 * 
 * Usage: Add @RunWith(RFFJUnitRunner.class) to your test class
 */
public class RFFJUnitRunner extends org.junit.runners.BlockJUnit4ClassRunner {
    private final RFFStrategy strategy;
    private final RFFRuntimeBridge bridge;
    
    public RFFJUnitRunner(Class<?> clazz) throws org.junit.runners.model.InitializationError {
        super(clazz);
        this.strategy = new RFFStrategy();
        this.bridge = new RFFRuntimeBridge();
    }
    
    @Override
    protected void runChild(org.junit.runners.model.FrameworkMethod method, org.junit.runner.notification.RunNotifier notifier) {
        // Start RFF exploration
        bridge.startExploration();
        
        // Run the test multiple times with different schedules
        while (bridge.canExecuteMoreSchedules()) {
            bridge.startingScheduleExecution();
            
            try {
                super.runChild(method, notifier);
            } catch (Exception e) {
                // Mark as crashed
                bridge.onCrashDetected();
            }
            
            bridge.completedScheduleExecution();
        }
        
        // Print statistics
        bridge.printStatistics();
    }
}
