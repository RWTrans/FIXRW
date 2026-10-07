package edu.tamu.aser.rff;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test atomicity violation detection with RFF.
 */
@RunWith(RFFJUnitRunner.class)
public class RFFAtomicityViolationTest {
    private int balance = 100;

    @Test
    public void testAtomicityViolation() throws InterruptedException {
        Thread t1 = new Thread(() -> {
            if (balance >= 50) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                balance -= 50;
            }
        });

        Thread t2 = new Thread(() -> {
            if (balance >= 50) {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                balance -= 50;
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        if (balance < 0) {
            throw new AssertionError("Balance went negative: " + balance);
        }
    }
}
