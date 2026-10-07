package edu.tamu.aser.rff;

/**
 * Writer thread for RFFOrderTest - writes 42 to sharedValue
 */
public class WriterThread implements Runnable {
    @Override
    public void run() {
        // Add delay to allow Reader to potentially execute first
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Write 42 to shared value
        RFFOrderTest.sharedValue = 42;
    }
}
