package edu.tamu.aser.tests.even;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)

public class Main {

	public static void main(String[] args) {
		EvenGenerator generator = new EvenGenerator();
		Thread t1 = new Thread(new EvenChecker(generator));
		Thread t2 = new Thread(new EvenChecker(generator));
		Thread t3 = new Thread(new EvenChecker(generator));
		Thread t4 = new Thread(new EvenChecker(generator));
		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
	}
	@Test
	public void test() throws Exception{
		EvenGenerator generator = new EvenGenerator();
		Thread t1 = new Thread(new EvenChecker(generator));
		Thread t2 = new Thread(new EvenChecker(generator));
		Thread t3 = new Thread(new EvenChecker(generator));
		Thread t4 = new Thread(new EvenChecker(generator));

		t1.start();
		t2.start();
		t3.start();
		t4.start();
	}
}
