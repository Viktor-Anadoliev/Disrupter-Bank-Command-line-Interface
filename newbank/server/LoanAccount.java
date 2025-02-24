package newbank.server;

public class LoanAccount extends Account {

    public LoanAccount(String accountName, double openingBalance, Customer customer) {
        super(accountName, openingBalance, customer);
    }

    @Override
    public String getAccountType() {
        String accountType = "LOAN";
        return accountType;
    }

}
