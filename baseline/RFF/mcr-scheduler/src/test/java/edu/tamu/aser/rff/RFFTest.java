package edu.tamu.aser.rff;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test cases for RFF implementation.
 * These tests verify that RFF can detect common concurrency bugs.
 */
public class RFFTest {
    
    /**
     * Test data race detection
     */
    @RunWith(RFFJUnitRunner.class)
    public static class DataRaceTest {
        private int sharedValue = 0;
        
        @Test
        public void testDataRace() throws InterruptedException {
            Thread t1 = new Thread(() -> {
                sharedValue = 1;
            });
            
            Thread t2 = new Thread(() -> {
                int val = sharedValue; // May read 0 or 1
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
    
    /**
     * Test atomicity violation detection
     */
    @RunWith(RFFJUnitRunner.class)
    public static class AtomicityViolationTest {
        private int balance = 100;
        
        @Test
        public void testAtomicityViolation() throws InterruptedException {
            Thread t1 = new Thread(() -> {
                // Check-then-act pattern
                if (balance >= 50) {
                    // Simulate some work
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
    
    /**
     * Test deadlock detection
     */
    @RunWith(RFFJUnitRunner.class)
    public static class DeadlockTest {
        private final Object lock1 = new Object();
        private final Object lock2 = new Object();
        
        @Test
        public void testDeadlock() throws InterruptedException {
            Thread t1 = new Thread(() -> {
                synchronized (lock1) {
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                    synchronized (lock2) {
                        // Critical section
                    }
                }
            });
            
            Thread t2 = new Thread(() -> {
                synchronized (lock2) {
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                    synchronized (lock1) {
                        // Critical section
                    }
                }
            });
            
            t1.start();
            t2.start();
            t1.join(1000);
            t2.join(1000);
            
            if (t1.isAlive() && t2.isAlive()) {
                throw new AssertionError("Deadlock detected!");
            }
        }
    }
    
    /**
     * Test order violation detection
     */
    @RunWith(RFFJUnitRunner.class)
    public static class OrderViolationTest {
        private boolean initialized = false;
        private int value = 0;
        
        @Test
        public void testOrderViolation() throws InterruptedException {
            Thread writer = new Thread(() -> {
                value = 42;
                initialized = true;
            });
            
            Thread reader = new Thread(() -> {
                if (initialized) {
                    if (value != 42) {
                        throw new AssertionError("Order violation: value=" + value);
                    }
                }
            });
            
            writer.start();
            reader.start();
            writer.join();
            reader.join();
        }
    }
    
    /**
     * Test simple counter with race condition
     */
    @RunWith(RFFJUnitRunner.class)
    public static class CounterRaceTest {
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
}
