package edu.tamu.aser.rff;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test data race detection with RFF.
 */
@RunWith(RFFJUnitRunner.class)
public class RFFDataRaceTest {
    private int sharedValue = 0;

    @Test
    public void testDataRace() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            sharedValue = 1;
        });

        Thread t2 = new Thread(() -> {
            int val = sharedValue;
            if (val != 0 && val != 1) {
                throw new AssertionError("Invalid value: " + val);
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
