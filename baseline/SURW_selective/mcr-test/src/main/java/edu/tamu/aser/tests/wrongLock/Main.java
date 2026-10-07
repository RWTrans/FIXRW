package edu.tamu.aser.tests.wrongLock;

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Xuan
 * Created on Apr 27, 2005
 * 
 * Test Case 1 
   number of threads that have a lock on data             :  1
   number of threads that have a wrong lock on the class :  1
 */
@RunWith(JUnit4MCRRunner.class)
public class Main {
    static int iNum1=1;
    static int iNum2=1;
	
    public void run() {
    	Data data=new Data();
    	WrongLock wl=new WrongLock(data);

    	for (int i=0;i<iNum1;i++)
    		new TClass1(wl).start();
    	for (int i=0;i<iNum2;i++)
    		new TClass2(wl).start();
    }

    public static void main(String[] args) {
	if (args.length < 2){
           //System.out.println("ERROR: Expected 2 parameters");
           Main t = new Main();
    	   t.run();
	}else{
	   iNum1 = Integer.parseInt(args[0]);
	   iNum2 = Integer.parseInt(args[1]);
	   Main t = new Main();
	   t.run();
	}
    }
	@Test
	public void test(){
		Main t = new Main();
		t.run();
	}
}
