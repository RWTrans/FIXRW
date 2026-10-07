package edu.tamu.aser.tests.manager;


import edu.tamu.aser.reex.JUnit4MCRRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;

@RunWith(JUnit4MCRRunner.class)
public class Manager
{
    static int request_counter = 0;
    static int released_counter = 0;
    static boolean flag = false;
    static String req_counter_lock=new String();
    static String rel_counter_lock=new String ();
    static String notes_lock=new String();
    static int num_of_notes_set = 0;



    @Test
    public void main(){
        String[] arg = new String[3];
        arg[0] = "output.txt";
        arg[1] = "3";
        arg[2] = "3";

        int init_req_counter;
        if ( arg.length == 2)
        {
            init_req_counter=request_counter = 100;
        }
        else if (arg.length != 3){
            System.out.println("ERROR - wrong number of arguments");
            System.out.println("Usage:Manager OutputFile NumOfThreads NumOfPtrs ");
            return;
        }
        else
        {
            init_req_counter=request_counter = Integer.parseInt(arg[2]);
        }
        int num_of_threads = Integer.parseInt(arg[1]);
        System.out.println("Number of memory blocks to release:" + init_req_counter);
        System.out.println("Number of memory blocks released:" + released_counter);
        FileOutputStream outStream;
        DataOutputStream outputStream;
        try
        {

//            outStream = new FileOutputStream(arg[0],true);
//            outputStream = new DataOutputStream (outStream);
            flag = !(init_req_counter == released_counter);
            if(flag)
            {
                try{
                    throw new RuntimeException("ERROR: records don't match !!!");
                }catch(Exception e)
                {
                    "reCrash_with".equals(e);
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
//            outputStream.writeBytes( "Program name: Manager , Bug found: " + flag + "\r\n");
//            outputStream.flush();
//            outputStream.close();
//            outStream.close();
        }
        catch (Exception E){System.out.println("Unable to write results to file "+E.getMessage());}

    }

    /**
     * gets 2 parameters:   1. number of threads
     *                      2. number of pointers to release
     */
    public static void main(String[] arg)
    {
    	arg = new String[3];
    	arg[0] = "output.txt";
    	arg[1] = "3";
    	arg[2] = "3";
    	
        int init_req_counter;
        if ( arg.length == 2)
        {
            init_req_counter=request_counter = 100;
        }
        else if (arg.length != 3){
            System.out.println("ERROR - wrong number of arguments");
            System.out.println("Usage:Manager OutputFile NumOfThreads NumOfPtrs ");
            return;
        }
        else
        {
            init_req_counter=request_counter = Integer.parseInt(arg[2]);
        }
        int num_of_threads = Integer.parseInt(arg[1]);
        System.out.println("Number of memory blocks to release:" + init_req_counter);
        System.out.println("Number of memory blocks released:" + released_counter);
        FileOutputStream outStream;
        DataOutputStream outputStream;
        try
        {
        	
//            outStream = new FileOutputStream(arg[0],true);
//            outputStream = new DataOutputStream (outStream);
            flag = !(init_req_counter == released_counter);
            if(flag)
            {
    			try{
    				throw new RuntimeException("ERROR: records don't match !!!");
    			}catch(Exception e)
    			{
               		"reCrash_with".equals(e);
              		e.printStackTrace();
              		System.exit(-1);
    			}
            }
//            outputStream.writeBytes( "Program name: Manager , Bug found: " + flag + "\r\n");
//            outputStream.flush();
//            outputStream.close();
//            outStream.close();
        }
        catch (Exception E){System.out.println("Unable to write results to file "+E.getMessage());}
    }


//    Manager(int num_of_threads)
//    {
//        Trelease releasers[] = new Trelease[ num_of_threads ];
//        TmemoryHandler t =        new TmemoryHandler();
//        t.start();
//        for ( int i = 0 ; i < num_of_threads ; ++i)
//        {
//            releasers[i] = new Trelease(i);
//            releasers[i].start();
//        }
//        for ( int i = 0 ; i < num_of_threads ; ++i)
//        {
//            try
//            {
//                 releasers[i].join();
//            }catch( InterruptedException e){ }
//
//
//        }
//        try
//                    {
//                         t.join();
//                    }catch( InterruptedException e){ }
//
//    }

    public static void setNote(int index,boolean op)
    {
        synchronized(Manager.notes_lock)
        {
            if ( op )
            {
                num_of_notes_set++;

            }
            else
            {
                 num_of_notes_set--;
            }
        }

    }

    public static boolean isOtherNoteSet()
    {
        synchronized(Manager.notes_lock)
        {
            if (num_of_notes_set == 1)
            {
                return true;
            }
            else return false;
        }

    }


}