package edu.tamu.aser.tests.consisitency;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnit4MCRRunner.class)

public class Main implements Runnable{
	public static int THREAD_NUMBER = 3;
	
	public static int a = 0;
	public static int b = 0;
	private int num;
	
//	public Main(int num) {
//		this.num = num;
//	}
	
	public void run() {
		a = num;
		b = num;
	}
	
//	public static void main(String[] args) throws Exception {
//		Thread[] t = new Thread[THREAD_NUMBER];
//		for (int i = 0; i < THREAD_NUMBER; i++) {
//			t[i] = new Thread(new Main(i));
//			t[i].start();
//		}
//
//		for (int i = 0; i < THREAD_NUMBER; i++) {
//			t[i].join();
//		}
//
//		System.out.println("a = " + a + ", b = " + b);
//		if (a != b) {
//			throw new Exception("bug found.");
//		}
//	}
	@Test
	public void test() throws Exception {
		Thread[] t = new Thread[THREAD_NUMBER];
		for (int i = 0; i < THREAD_NUMBER; i++) {
			Main m=new Main();
			m.num=i;
			t[i] = new Thread(m);
			t[i].start();
		}

		for (int i = 0; i < THREAD_NUMBER; i++) {
			t[i].join();
		}

		System.out.println("a = " + a + ", b = " + b);
		if (a != b) {
			throw new Exception("bug found.");
		}

	}
}
