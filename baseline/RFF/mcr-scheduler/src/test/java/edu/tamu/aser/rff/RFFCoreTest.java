package edu.tamu.aser.rff;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple unit tests for RFF core components.
 */
public class RFFCoreTest {
    
    @Test
    public void testAbstractEvent() {
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10);
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5);
        
        assertEquals(AbstractEvent.Op.READ, read.getOp());
        assertEquals("x", read.getLocation());
        assertEquals(10, read.getLine());
        
        assertEquals(AbstractEvent.Op.WRITE, write.getOp());
        assertEquals("x", write.getLocation());
        assertEquals(5, write.getLine());
    }
    
    @Test
    public void testReadsFromConstraint() {
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5);
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10);
        
        ReadsFromConstraint positive = ReadsFromConstraint.positive(write, read);
        assertTrue(positive.isPositive());
        assertFalse(positive.isNegative());
        
        ReadsFromConstraint negative = ReadsFromConstraint.negative(write, read);
        assertFalse(negative.isPositive());
        assertTrue(negative.isNegative());
        
        // Test negation
        ReadsFromConstraint negated = positive.negate();
        assertFalse(negated.isPositive());
        assertTrue(negated.isNegative());
    }
    
    @Test
    public void testAbstractSchedule() {
        AbstractEvent write1 = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5);
        AbstractEvent read1 = new AbstractEvent(AbstractEvent.Op.READ, "x", 10);
        AbstractEvent write2 = new AbstractEvent(AbstractEvent.Op.WRITE, "y", 3);
        AbstractEvent read2 = new AbstractEvent(AbstractEvent.Op.READ, "y", 8);
        
        AbstractSchedule schedule = new AbstractSchedule();
        schedule.addConstraint(ReadsFromConstraint.positive(write1, read1));
        schedule.addConstraint(ReadsFromConstraint.negative(write2, read2));
        
        assertEquals(1, schedule.getPositiveConstraints().size());
        assertEquals(1, schedule.getNegativeConstraints().size());
        assertEquals(2, schedule.getAllConstraints().size());
    }
    
    @Test
    public void testScheduleMutator() {
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5);
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10);
        
        AbstractSchedule schedule = new AbstractSchedule();
        schedule.addConstraint(ReadsFromConstraint.positive(write, read));
        
        ScheduleMutator mutator = new ScheduleMutator();
        AbstractSchedule mutated = mutator.mutate(schedule, schedule.getObservedEvents());
        
        assertNotNull(mutated);
        // Mutation should produce a different schedule
        // (though it could theoretically be the same in rare cases)
    }
    
    @Test
    public void testReadsFromTracker() {
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5);
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10);
        
        ReadsFromTracker tracker = new ReadsFromTracker();
        tracker.recordReadsFrom(read, write);
        
        assertEquals(write, tracker.getObservedWrite(read));
        assertEquals(1, tracker.getObservedPairs().size());
        assertTrue(tracker.observedPair(write, read));
    }
    
    @Test
    public void testGreyboxFeedback() {
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5);
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10);
        
        ReadsFromTracker tracker = new ReadsFromTracker();
        tracker.recordReadsFrom(read, write);
        
        GreyboxFeedback feedback = new GreyboxFeedback();
        
        // First execution should be interesting (new pair)
        assertTrue(feedback.isInteresting(tracker, false));
        
        feedback.updateFeedback(tracker);
        
        // Second execution with same pair should not be interesting
        ReadsFromTracker tracker2 = new ReadsFromTracker();
        tracker2.recordReadsFrom(read, write);
        assertFalse(feedback.isInteresting(tracker2, false));
        
        // Crash should always be interesting
        assertTrue(feedback.isInteresting(tracker2, true));
    }
    
    @Test
    public void testRFProactiveScheduler() {
        AbstractEvent write = new AbstractEvent(AbstractEvent.Op.WRITE, "x", 5);
        AbstractEvent read = new AbstractEvent(AbstractEvent.Op.READ, "x", 10);
        
        AbstractSchedule schedule = new AbstractSchedule();
        schedule.addConstraint(ReadsFromConstraint.positive(write, read));
        
        RFProactiveScheduler scheduler = new RFProactiveScheduler();
        scheduler.setAbstractSchedule(schedule);
        
        // Test thread selection with single thread
        Set<String> threads = new HashSet<>();
        threads.add("Thread-1");
        String chosen = scheduler.chooseNextThread(threads);
        assertEquals("Thread-1", chosen);
        
        // Test with multiple threads
        threads.add("Thread-2");
        chosen = scheduler.chooseNextThread(threads);
        assertNotNull(chosen);
        assertTrue(threads.contains(chosen));
    }
    
    @Test
    public void testRFFConfig() {
        RFFConfig config = new RFFConfig();
        config.setTimeoutMillis(60000)
              .setMaxIterations(5000)
              .setMaxEnergy(8)
              .setDebug(true);
        
        assertEquals(60000, config.getTimeoutMillis());
        assertEquals(5000, config.getMaxIterations());
        assertEquals(8, config.getMaxEnergy());
        assertTrue(config.isDebug());
    }
}
