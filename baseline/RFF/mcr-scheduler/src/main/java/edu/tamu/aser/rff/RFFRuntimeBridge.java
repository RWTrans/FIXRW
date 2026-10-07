package edu.tamu.aser.rff;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integration layer between RFF and SURW's runtime instrumentation.
 * Bridges RVRunTime event callbacks with RFF's scheduling strategy.
 */
public class RFFRuntimeBridge {
    // RFF components
    private final RFFStrategy strategy;
    private final RFProactiveScheduler proactiveScheduler;
    private final ReadsFromTracker currentTracker;
    
    // Thread management
    private final Map<Thread, String> threadNames;
    private final AtomicBoolean explorationActive;
    private final AtomicInteger scheduleCount;
    
    // Event queues for communication between instrumented code and scheduler
    private final BlockingQueue<EventNotification> eventQueue;
    
    public RFFRuntimeBridge() {
        this.strategy = new RFFStrategy();
        this.proactiveScheduler = new RFProactiveScheduler();
        this.currentTracker = new ReadsFromTracker();
        this.threadNames = new ConcurrentHashMap<>();
        this.explorationActive = new AtomicBoolean(false);
        this.scheduleCount = new AtomicInteger(0);
        this.eventQueue = new LinkedBlockingQueue<>();
    }
    
    /**
     * Initialize RFF exploration
     */
    public void startExploration() {
        System.out.println("[RFF-Bridge] Starting exploration");
        strategy.startingExploration();
        explorationActive.set(true);
        
        // Start scheduler thread
        Thread schedulerThread = new Thread(this::schedulerLoop, "RFF-Scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }
    
    /**
     * Called when a new schedule execution starts
     */
    public void startingScheduleExecution() {
        scheduleCount.incrementAndGet();
        strategy.startingScheduleExecution();
    }
    
    /**
     * Called when current schedule execution completes
     */
    public void completedScheduleExecution() {
        strategy.completedScheduleExecution();
    }
    
    /**
     * Check if more schedules can be executed
     */
    public boolean canExecuteMoreSchedules() {
        return strategy.canExecuteMoreSchedules();
    }
    
    /**
     * Called by instrumented code when a memory access is about to occur
     */
    public void onMemoryAccess(String location, int line, boolean isRead, Object value) {
        if (!explorationActive.get()) {
            return;
        }
        
        String threadName = getCurrentThreadName();
        EventNotification event = new EventNotification(
            location, line, isRead, value, threadName
        );
        
        eventQueue.offer(event);
    }
    
    /**
     * Called by instrumented code when a thread is created
     */
    public void onThreadCreate(Thread thread) {
        if (!explorationActive.get()) {
            return;
        }
        
        String name = "Thread-" + thread.getId();
        threadNames.put(thread, name);
    }
    
    /**
     * Called by instrumented code when a thread starts
     */
    public void onThreadStart(Thread thread) {
        if (!explorationActive.get()) {
            return;
        }
        
        // Thread is now runnable
    }
    
    /**
     * Called by instrumented code when a thread terminates
     */
    public void onThreadEnd(Thread thread) {
        if (!explorationActive.get()) {
            return;
        }
        
        // Thread is no longer runnable
    }
    
    /**
     * Called when a crash/bug is detected
     */
    public void onCrashDetected() {
        strategy.markCrashed();
    }
    
    /**
     * Scheduler thread: processes events and makes scheduling decisions
     */
    private void schedulerLoop() {
        while (explorationActive.get()) {
            try {
                EventNotification event = eventQueue.poll(100, TimeUnit.MILLISECONDS);
                if (event == null) {
                    continue;
                }
                
                // Record event in tracker
                AbstractEvent.Op op = event.isRead ? AbstractEvent.Op.READ : AbstractEvent.Op.WRITE;
                AbstractEvent abstractEvent = new AbstractEvent(
                    op, event.location, event.line, event.threadName
                );
                currentTracker.recordEvent(abstractEvent);
                
                // If this is a read, determine which write it observed
                if (event.isRead) {
                    // In real implementation, we'd track the actual write that produced this value
                    // For now, we'll need to infer from the proactive scheduler's last write tracking
                }
                
                // Notify proactive scheduler
                proactiveScheduler.onEventExecuted(abstractEvent, event.threadName);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    /**
     * Get current thread name (or assign one if unknown)
     */
    private String getCurrentThreadName() {
        Thread current = Thread.currentThread();
        return threadNames.computeIfAbsent(current, t -> "Thread-" + t.getId());
    }
    
    /**
     * Get statistics
     */
    public void printStatistics() {
        strategy.printStatistics();
    }
    
    /**
     * Event notification from instrumented code
     */
    private static class EventNotification {
        final String location;
        final int line;
        final boolean isRead;
        final Object value;
        final String threadName;
        
        EventNotification(String location, int line, boolean isRead, 
                         Object value, String threadName) {
            this.location = location;
            this.line = line;
            this.isRead = isRead;
            this.value = value;
            this.threadName = threadName;
        }
    }
}
