package edu.tamu.aser.tests.boundedBuffer;/* from http://www.doc.ic.ac.uk/~jnm/book/ */
/* Concurrency: State Models & Java Programs - Jeff Magee & Jeff Kramer */
/* has a deadlock */

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/****************************MAIN**************************/
@RunWith(JUnit4MCRRunner.class)
public class BoundedBuffer {
  static int SIZE = 1; /* parameter */
  static int PRODS = 4; /* parameter */
  static int CONS = 4; /* parameter */
  static int MODCOUNT = 2; /* parameter */
  static Buffer buf;

//  public static void main(String[] args) {
//    if (args != null && args.length == 4) {
//      SIZE = Integer.parseInt(args[0]);
//      PRODS = Integer.parseInt(args[1]);
//      CONS = Integer.parseInt(args[2]);
//      MODCOUNT = Integer.parseInt(args[3]);
//    }
//
//    buf = new BufferImpl(SIZE);
//
//    for (int i = 0; i < PRODS; i++) {
//      new Producer(buf, MODCOUNT).start();
//    }
//    for (int i = 0; i < CONS; i++) {
//      new Consumer(buf).start();
//    }
//  }

  @Test
  public void test() throws Exception {
    buf = new BufferImpl(SIZE);

    for (int i = 0; i < PRODS; i++) {
      new Producer(buf, MODCOUNT).start();
    }
    for (int i = 0; i < CONS; i++) {
      new Consumer(buf).start();
    }
  }
}