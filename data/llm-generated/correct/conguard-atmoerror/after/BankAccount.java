package atmoerror;

public class BankAccount {
    private int total = 0;

    public synchronized void add(int n) {
        total += n;
    }

    public synchronized int getTotal() {
        return total;
    }
}
