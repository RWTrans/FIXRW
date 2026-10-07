package producerConsumer;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed version:
 * - Fix data race on total
 * - Fix incorrect wait condition (& -> &&)
 * - Ensure all producers complete before consumers finish
 * - Remove unnecessary synchronization
 */
public class ProducerConsumer {
    public static int PRODS = 3;
    public static int CONS = 4;
    public static int COUNT = 8;

    // ✅ AtomicInteger eliminates data race
    public static AtomicInteger total = new AtomicInteger(0);

    public static void main(String[] args) throws Exception {
        if (args != null && args.length == 3) {
            PRODS = Integer.parseInt(args[0]);
            CONS = Integer.parseInt(args[1]);
            COUNT = Integer.parseInt(args[2]);
        }

        Producer[] prods = new Producer[PRODS];
        Consumer[] cons = new Consumer[CONS];
        Buffer b = new Buffer(5);

        for (int i = 0; i < PRODS; i++)
            prods[i] = new Producer(b);

        for (int i = 0; i < CONS; i++)
            cons[i] = new Consumer(b);

        // ✅ Wait for producers to finish
        for (Producer p : prods)
            p.join();

        b.halt();

        // ✅ Wait for consumers to finish
        for (Consumer c : cons)
            c.join();

        int expected = COUNT * PRODS;
        if (total.get() != expected) {
            throw new RuntimeException(
                "bug found - total is " + total.get() + " and should be " + expected
            );
        }
    }
}

class HaltException extends Exception {
}

interface BufferInterface {
    void put(Object x);
    Object get();
    void halt();
}

class Buffer implements BufferInterface {
    protected int SIZE;
    protected Object[] array;
    protected int putPtr = 0;
    protected int getPtr = 0;
    protected int usedSlots = 0;
    protected boolean halted = false;

    public Buffer(int b) {
        SIZE = b;
        array = new Object[b];
    }

    public synchronized void put(Object x) {
        while (usedSlots == SIZE) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }

        array[putPtr] = x;
        putPtr = (putPtr + 1) % SIZE;
        usedSlots++;
        notifyAll();
    }

    public synchronized Object get() {
        // ✅ FIX: & -> &&
        while (usedSlots == 0 && !halted) {
            try {
                wait();
            } catch (InterruptedException ignored) {}
        }

        if (usedSlots == 0 && halted)
            return null;

        Object x = array[getPtr];
        array[getPtr] = null;
        getPtr = (getPtr + 1) % SIZE;
        usedSlots--;
        notifyAll();
        return x;
    }

    public synchronized void halt() {
        halted = true;
        notifyAll();
    }
}

class Attribute {
    public int attr;
    public Attribute() { attr = 0; }
    public Attribute(int attr) { this.attr = attr; }
}

class AttrData extends Attribute {
    public int data;
    public AttrData(int attr, int data) {
        this.attr = attr;
        this.data = data;
    }
}

class Producer extends Thread {
    private final Buffer buffer;

    public Producer(Buffer b) {
        this.buffer = b;
        start();
    }

    public void run() {
        for (int i = 0; i < ProducerConsumer.COUNT; i++) {
            buffer.put(new AttrData(i, 1));
        }
    }
}

class Consumer extends Thread {
    private final Buffer buffer;

    public Consumer(Buffer b) {
        this.buffer = b;
        start();
    }

    public void run() {
        AttrData ad;
        while ((ad = (AttrData) buffer.get()) != null) {
            ProducerConsumer.total.addAndGet(ad.data);
        }
    }
}