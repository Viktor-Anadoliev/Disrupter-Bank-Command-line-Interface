package newbank.server;


public class SavingsAccount extends Account {

    private static final double interestRate = 2.0; // example of 2% interest is used

    // Constructor
    public SavingsAccount(String accountName, double openingBalance, Customer customer) {
        super(accountName, openingBalance, customer);
    }

    // Mutator that deposits interest into savings account
    public void addInterest() {
            double interest = this.getBalance() * (interestRate / 100);
            this.deposit(interest);
    }

    // Accessor: Get type of account (savings or current)
    @Override
    public String getAccountType() {
        String accountType = "SAVINGS";
        return accountType;
    }
}
