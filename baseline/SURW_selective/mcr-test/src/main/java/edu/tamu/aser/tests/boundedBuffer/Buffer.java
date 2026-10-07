package edu.tamu.aser.tests.boundedBuffer;
/* from http://www.doc.ic.ac.uk/~jnm/book/ */
/* Concurrency: State Models & Java Programs - Jeff Magee & Jeff Kramer */
/* has a deadlock */

public interface Buffer {
    public void put(Object o)
       throws InterruptedException; //put object into buffer
    public Object get()
       throws InterruptedException;       //get an object from buffer
}
