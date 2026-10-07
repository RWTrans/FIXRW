package edu.tamu.aser.tests.boundedBuffer;
/* from http://www.doc.ic.ac.uk/~jnm/book/ */
/* Concurrency: State Models & Java Programs - Jeff Magee & Jeff Kramer */
/* has a deadlock */

/********************CONSUMER*******************************/

class Consumer extends Thread {

    Buffer buf;

    Consumer(Buffer b) {buf = b;}

    public void run() {
      try {
        while(true) {
//            System.out.println("Consumer trying to get - "+this);
            int tmp = ((Integer)buf.get()).intValue();
        }
      } catch(InterruptedException e ){}
    }
}
