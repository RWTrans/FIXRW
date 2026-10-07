package edu.tamu.aser.tests.airline;

import edu.tamu.aser.reex.JUnit4MCRRunner;
//import junit.framework.Assert;
//import omcr.airline.airline;
import org.junit.Assert;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;



@RunWith(JUnit4MCRRunner.class)
public class AirlineTest {

    public static void main(String args[]) throws Exception {
        AirlineTest airlineTest = new AirlineTest();
    }



    private Airline airline;

    public void makeBookings(int numTickets) throws Exception {
        airline = new Airline(numTickets);
        airline.makeBookings();
    }


    @SuppressWarnings("deprecation")
	public void testNotTooFewTicketsSold() {
        if (airline.numberOfSeatsSold < airline.maximumCapacity) {

            Assert.fail("Too few were sold! Number of tickets sold: " + airline.numberOfSeatsSold + " out of max: " + airline.maximumCapacity);
        }
    }
    
    @Test
	public void test() throws InterruptedException {
		try {
            makeBookings(6);
            testNotTooFewTicketsSold();
        } catch (Exception e) {
			System.out.println("here");
			fail();
		}
	}

}
