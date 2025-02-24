package newbank.server;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Account {
	private String accountName;
	private double balance;
	private Customer accountOwner;
	private static int accountNumberTracker = 1000000;
	private String accountIBAN;
	private static HashMap<String, Account> accounts = new HashMap<>();
	private ArrayList<Transaction> allTransactions;

	// Constructor
    public Account(String accountName, double openingBalance, Customer customer) {
		this.accountName = accountName;
		this.accountOwner = customer;

		this.balance = 0;
		this.deposit(openingBalance);
		
		createIBAN();
		accounts.put(this.accountIBAN, this);

		this.allTransactions = new ArrayList<Transaction>();
		//System.out.println("all accounts: " + Account.accounts);
	}

	// Print initial account summary
	public String toString() {
		return (getAccountName() + "(" + getAccountType() 
		+ ", " + getIBAN()
		+ "): " + getBalance() + "\n");
	}

	// Mutator: Create unique IBAN identifier for account
	private void createIBAN(){
		accountNumberTracker += 1;
		int accountNumber = accountNumberTracker;
		this.accountIBAN = "GB" + "00" + this.accountOwner.getSortCode() + accountNumber;
	}

	// Accessor: Get IBAN account identifier
	public String getIBAN(){
		return this.accountIBAN;
	}

	// Accessor: Get balance
	public double getBalance() {
		return this.balance;
	}

	// Accessor: Get account name as a string
	public String getAccountName() {
		return accountName;
	}

	// Accessor: Get account owner as Customer object
	public Customer getOwner() {
		return this.accountOwner;
	}

	// Mutator: Deposit funds
	public void deposit(double amount) {
		this.balance += amount;
	}

	// Mutator: Withdraw funds
	public void withdraw(double amount) {
		this.balance -= amount;
	}

	// Mutator: Add transaction object to array record (for statement)
	public void addTransaction(Transaction newTransaction){
		this.allTransactions.add(0, newTransaction);
	}

	// Accessor: Get all transactions in array (for printing statement)
	public String getAllTransactions(){
		String statement = "";
		LocalDate cutOffDate = LocalDate.now().minusMonths(12);

		int i = 0;
		while(i < this.allTransactions.size()){
			if(this.allTransactions.get(i).getDate().isAfter(cutOffDate)){
				Transaction transaction = this.allTransactions.get(i);
				String newLine = transaction.toString(' ');;
				// If money leaving this account, add minus
				if(transaction.fromAccount.getIBAN().equals(this.getIBAN())){
					newLine = transaction.toString('-');
				// If money entering this account, add plus
				} else if(transaction.toAccount.getIBAN().equals(this.getIBAN())){
					newLine = transaction.toString('+');
				}
				statement = statement + newLine;
			} else {
				break;
			}
			i++;
		}
		return statement;
	}
	
	public boolean sufficientFunds(double amount) {
		return (this.getBalance() >= amount);
	}

	// Abstract method: Get account type
	public abstract String getAccountType();

	// Static method: Get account from IBAN
	public static Account getAccountFromIBAN(String inputIBAN){
		Account accountObj = Account.accounts.get(inputIBAN);
		return accountObj;
	}

}
