package account;

//import java.lang.*;

public class Account {
    double amount;
    String  name;

    //constructor
  public Account(String nm,double amnt ) {
        amount=amnt;
        name=nm;
  }
  //functions
  synchronized  void depsite(double money){
      amount+=money;
      }

  synchronized  void withdraw(double money){
      amount-=money;
      }

  void transfer(Account ac,double mn){
    Account first = this;
    Account second = ac;

    // 按 identityHashCode 固定锁顺序
    if (System.identityHashCode(first) > System.identityHashCode(second)) {
        first = ac;
        second = this;
    }

    synchronized (first) {
        synchronized (second) {
            this.amount -= mn;
            ac.amount += mn;
        }
    }
  }

 synchronized void print(){
  }

      }//end of class Acount
