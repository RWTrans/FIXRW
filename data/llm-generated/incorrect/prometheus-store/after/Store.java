package store;

public class Store {
	private int customerCost;
	
	public Store() {
		customerCost = 0;
	}
	
	//forgot to add 'synchronized' to this function;
	public void consume(int cost) {
		  try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
        }
        customerCost += cost;
	}
	
	public synchronized int getCost() {
		return customerCost;
	}
}
