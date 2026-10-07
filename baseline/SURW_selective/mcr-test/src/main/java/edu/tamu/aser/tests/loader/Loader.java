package edu.tamu.aser.tests.loader;
//
//  -Class Loader-
//  Create array,start sort process,verify final array
//

import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.Integer;
import java.io.RandomAccessFile;

@RunWith(JUnit4MCRRunner.class)
public class Loader {

   public static void main(String[] args){
	   new Loader().go(args);
   }

    @Test
    public void main() throws Exception {
        new Loader().go(null);
    }




    public void go(String[] args)
	{
		args = new String[2];
	    args[0] = "output.txt";
	    args[1] = "low";
	    
     if(args.length<2) {
          System.out.println("wrong input");
          System.exit(1);
     }
     String outputFile=args[0];//output file name
     String conc=args[1]; //concurrency
     int len=3;    //lenght of array
     int prior=3;   //priority of sorting process
     int array[];   //array of integers

     if(conc.equals("low"))
       len=3;
     if(conc.equals("medium"))
       len=1000;
     if(conc.equals("high"))
       len=10000;


     array=new int[len];
     Thread curTh=Thread.currentThread();
     curTh.setPriority(1);
     NewThread.priority=prior;
     int i;

     for(i=0;i<len;i++){
           array[i]=len-i;
        }


     NewThread.array=array;
     NewThread ntr=new NewThread(len-1);
     ntr.start();

    try {
      while(!NewThread.endd) {
         Thread.sleep(100);
      }
    }
      catch (InterruptedException e){}

      checkResult(array,len);
	}
	public void checkResult(int[] array,int len)
	{
    int n_bugs=0;
    try{
     for(int i=0;i<len-1;i++){
      if(array[i]>array[i+1]) {
           n_bugs++;
           throw new RuntimeException();
        }
     }}
    catch(Exception e)
    {
   		"reCrash_with".equals(e);
   		e.printStackTrace();
   		System.exit(-1);
    }
     String outString="";
     if(n_bugs==0) {
          outString+="finished with No Bug";
     }
     else{
      outString+="finished with "+n_bugs+" bugs <Initialization-Sleep Pattern>";
     }

//     try{
//       RandomAccessFile outFile=new RandomAccessFile(outputFile,"rw");//create new file
//       outFile.writeBytes("SortProgram "+outString);
//     }
//     catch (Exception e){
//       System.out.println(""+e);
//     }

 }
}