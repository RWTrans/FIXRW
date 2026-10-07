package producerConsumer;
public class ProducerConsumer {
    static Object objR16 =  new Object();  static Object objR1 =  new Object();  static Object objR6 =  new Object();  static Object objR11 =  new Object();
    public static int PRODS = 3;
    public static int CONS = 4;
    public static int COUNT = 8;
    public static int total = 0;
    public static void main(String[] args) throws Exception {
        if (args != null && args.length == 3) {
            synchronized(objR1) { PRODS = Integer.parseInt(args[0]); }

            synchronized(objR11) { CONS = Integer.parseInt(args[1]); }

            synchronized(objR6) { synchronized(objR16) { COUNT = Integer.parseInt(args[2]); }   }

        }
        Consumer[] cons;  synchronized(objR11) { cons = new Consumer[CONS];  }

        Buffer b = new Buffer(5);
        Producer p;
        synchronized(objR1) { for (int i = 0; i < PRODS; i++)
            p = new Producer(b); }

        synchronized(objR11) { for (int i = 0; i < CONS; i++)
            cons[i] = new Consumer(b); }

        synchronized(objR11) { for (int i = 0; i < CONS; i++)
            cons[i].join(); }

        synchronized(objR1) { synchronized(objR16) { if (total != COUNT*PRODS)
            throw new RuntimeException("bug found - total is "+total+" and should be "+COUNT*PRODS); }   }
    }
}
class HaltException extends Exception {
}
interface BufferInterface {
    public void put(Object x);
    public Object get();
    public void halt();
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
            } catch (InterruptedException ex) {
            }
        }
        array[putPtr] = x;
        putPtr = (putPtr + 1) % SIZE;
        if (usedSlots == 0) {
            notifyAll();
        }
        usedSlots++;
    }
    public synchronized Object get() {
        while ((usedSlots == 0) & !halted) {
            try {
                wait();
            } catch (InterruptedException ex) {
            }
        }
        if (usedSlots == 0) {
            return null;
        }
        Object x = array[getPtr];
        array[getPtr] = null;
        getPtr = (getPtr + 1) % SIZE;
        if (usedSlots == SIZE) {
            notifyAll();
        }
        usedSlots--;
        return x;
    }
    public synchronized void halt() {
        halted = true;
        notifyAll();
    }
}
class Attribute {
    public int attr;
    public Attribute() {
        attr = 0;
    }
    public Attribute(int attr) {
        this.attr = attr;
    }
}
class AttrData extends Attribute {
    public int data;
    public AttrData(int attr, int data) {
        this.attr = attr;
        this.data = data;
    }
}
class Producer extends Thread {
    private Buffer buffer;
    public Producer(Buffer b) {
        buffer = b;
        this.start();
    }
    public void run() {
        for (int i = 0; i < ProducerConsumer.COUNT; i++) {
            AttrData ad = new AttrData(i, 1);
            buffer.put(ad);
        }
        if (Thread.currentThread().getId() == ProducerConsumer.PRODS) {
             buffer.halt();
         }
    }
}
class Consumer extends Thread {
    private Buffer buffer;

    public Consumer(Buffer b) {
        buffer = b;
        this.start();
    }

    public void run() {
        int count = 0;
        AttrData[] received = new AttrData[10];
        while (count < received.length) {
            received[count] = (AttrData) buffer.get();
            if (received[count] == null) {
                break;
            } else {
                inc(received[count].data);
            }
            count++;
        }
    }

    public void inc(int x) {
        synchronized (ProducerConsumer.class) {
            ProducerConsumer.total = ProducerConsumer.total + x;
        }

    }
}