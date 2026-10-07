package critical;

class Section implements Runnable {

    Critical t;
    int threadNumber;

    public Section(Critical t, int threadNumber)
    {
        this.t = t;
        this.threadNumber = threadNumber;
    }

    public void run() {

        if(threadNumber == 0)
        {
            synchronized(t) {
                t.turn = 0;
                System.out.println("In critical section, thread number = " + threadNumber);
                if(t.turn != 0)
                    throw new RuntimeException();
                System.out.println("Out critical section, thread number = " + threadNumber);
                t.turn = 1;
                t.notifyAll();
            }
        }
        else
        {
            if(threadNumber == 1)
            {
                synchronized(t) {
                    t.turn = 1;
                    System.out.println("In critical section, thread number = " + threadNumber);
                    while(t.turn != 1) {
                        try {
                            t.wait();
                        } catch (InterruptedException e) {}
                    }
                    System.out.println("Out critical section, thread number = " + threadNumber);
                    t.turn = 0;
                    t.notifyAll();
                }
            }
            else
            {
                System.err.println("This algorithm only supports two threads");
            }
        }
    }
}