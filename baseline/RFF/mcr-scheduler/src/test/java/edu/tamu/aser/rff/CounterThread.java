package edu.tamu.aser.rff;

/**
 * Standalone thread class for RFFRaceTest (avoids synthetic access methods)
 */
public class CounterThread implements Runnable {
    public static int counter = 0;
    public static final int ITERATIONS = 100;
    
    @Override
    public void run() {
        for (int i = 0; i < ITERATIONS; i++) {
            increment();
        }
    }
    
    private void increment() {
        counter++;
    }
}
