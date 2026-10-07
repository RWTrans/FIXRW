package edu.tamu.aser.rff;

/**
 * Reader thread for RFFOrderTest - reads sharedValue and expects 42
 */
public class ReaderThread implements Runnable {
    @Override
    public void run() {
        // Small delay to increase chance of reading before write
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        try {
            int value = RFFOrderTest.sharedValue;
            if (value != 42) {
                // Found order violation - reader saw initial value
                System.out.println("[BUG DETECTED] Read " + value + " instead of 42");
                // Mark this execution as crashed
                try {
                    Class<?> rffStrategyClass = Class.forName("edu.tamu.aser.rff.RFFStrategy");
                    java.lang.reflect.Method markCrashed = rffStrategyClass.getMethod("markCrashed");
                    Object strategy = edu.tamu.aser.reex.Scheduler.getSchedulingStrategy();
                    if (strategy != null && rffStrategyClass.isInstance(strategy)) {
                        markCrashed.invoke(strategy);
                    }
                } catch (Exception ex) {
                    // Ignore
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
