package accountsubtype;

public class PersonalAccount extends Account {
  public PersonalAccount(int number, int initialBalance) {
    super(number, initialBalance);
  }

  public synchronized void transfer(Account ac, int mn){
      Account first = this.number < ac.number ? this : ac;
      Account second = this.number < ac.number ? ac : this;
      synchronized (first) {
           synchronized (second) {
              amount-=mn;
             // BUG : update to field of "non-this" account is unprotected
            ac.amount+=mn;
          }
      }
  }
}
