package datarace;

public class Account {

	private int balance;

	public synchronized int getBalance() {
		return balance;
	}

	public synchronized void setBalance(int balance) {
		this.balance = balance;
	}
	
}
