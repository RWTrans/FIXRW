package accountsubtype;

public class BusinessAccount extends Account {
  public BusinessAccount(int number, int amnt ) {
    super(number, amnt);
  }

  public synchronized void transfer(Account dest, int transferAmount){
 Account first = this.number < dest.number ? this : dest;
Account second = this.number < dest.number ? dest : this;
synchronized (first) {
 synchronized (second) {
this.amount -= transferAmount;
 dest.amount += transferAmount;
     }
    }
  }
}
