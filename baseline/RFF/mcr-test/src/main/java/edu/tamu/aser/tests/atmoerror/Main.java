package edu.tamu.aser.tests.atmoerror;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)
public class Main {
    public static void main(String[] args) throws InterruptedException {

        BankAccount account = new BankAccount();

        Thread t1 = new Thread(new Customer(5, account));
        Thread t2 = new Thread(new Customer(5, account));

        t1.start();
        t2.start();



        t1.join();
        t2.join();
        if (account.getTotal() != 10)
            throw new RuntimeException();

//	      for(int i = 0; i < 2; i++) {
//	          new Thread(new Customer(100, account)).start();
//	      }
	      /*try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      if(account.getTotal() != 500)
	    	  throw new RuntimeException();*/
    }

    @Test
    public void test() throws Exception {

        BankAccount account = new BankAccount();

        Thread t1 = new Thread(new Customer(5, account));
        Thread t2 = new Thread(new Customer(5, account));

        t1.start();
        t2.start();



        t1.join();
        t2.join();
        if (account.getTotal() != 10)
            throw new RuntimeException();

//	      for(int i = 0; i < 2; i++) {
//	          new Thread(new Customer(100, account)).start();
//	      }
	      /*try {
			Thread.sleep(100L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      if(account.getTotal() != 500)
	    	  throw new RuntimeException();*/

    }
}
