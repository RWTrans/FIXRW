package edu.tamu.aser.rff;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test counter race condition detection with RFF.
 */
@RunWith(RFFJUnitRunner.class)
public class RFFCounterRaceTest {
    private int counter = 0;

    @Test
    public void testCounter() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                counter++;
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                counter++;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        if (counter != 2000) {
            throw new AssertionError("Race condition: counter=" + counter);
        }
    }
}
