package newbank.server;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;

public class Transaction {
    LocalDate today, transactionDate;
    LocalTime transactionTime;
    Account fromAccount, toAccount;
    Customer fromCustomer, toCustomer;
    String fromAccountName, toAccountName;
    double transactionAmount;
    private static final double dailyLimit = 50000.0;
    private static final ConcurrentHashMap<LocalDate, Double> dailyTransactions = new ConcurrentHashMap<>();

    // Accessor: Get daily transaction limit
    public static double getDailyLimit() { return dailyLimit; }

    // Accessor: Get daily transaction total
    public static ConcurrentHashMap<LocalDate, Double> getDailyTransactions() {
        return dailyTransactions;
    }

    // Mutator: Reset daily transaction limit each day
    public static void resetDailyTransactionLimit() {
        dailyTransactions.clear();
    }

    // Constructor
    public Transaction(Account fromAccount, Account toAccount, double amount){
        this.setToday();  
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        
        // Values needed for statement
        this.transactionDate = getDate();
        this.fromCustomer = fromAccount.getOwner();
        this.toCustomer = toAccount.getOwner();
        this.fromAccountName = fromAccount.getAccountName();
        this.toAccountName = toAccount.getAccountName();
        this.transactionAmount = amount;
    }

    // Write transaction as string (for statements)
    public String toString(char operator){
        String statementRecord = "\n" + this.transactionDate 
        + "\t" + operator + "£" + this.transactionAmount
        + "\nFrom: " + this.fromAccount.getIBAN() 
        + " (" + this.fromCustomer.getUsername() + ", " + this.fromAccountName 
        + ")\nTo: " + this.toAccount.getIBAN()
        + " (" + this.toCustomer.getUsername() + ", " + this.toAccountName + ")\n";
        return statementRecord;
    }

    // Mutator: Update today value to today's date
    public void setToday(){
        this.transactionDate = LocalDate.now();
    }
    
    // Accessor: Get value of today
    public LocalDate getDate(){
        return this.transactionDate;
    }

    // Mutator: Only used as an example that the printstatement covering last 12 month feature works
    public void overrideDate(LocalDate newDate){
        this.transactionDate = newDate;
    }


}
