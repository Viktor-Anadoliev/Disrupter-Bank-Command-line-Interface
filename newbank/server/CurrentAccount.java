package newbank.server;

public class CurrentAccount extends Account {
    
    // Constructor
    public CurrentAccount(String accountName, double openingBalance, Customer customer) {
        super(accountName, openingBalance, customer);

    }

    // Accessor: Get type of account (savings or current)
    @Override
    public String getAccountType() {
        String accountType = "CURRENT";
        return accountType;
    }
}
