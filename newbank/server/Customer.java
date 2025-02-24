package newbank.server;

import java.util.ArrayList;
public class Customer {
	private ArrayList<Account> accounts;
	private ArrayList<LoanAgreement> loanAgreements ;  // List to store loan agreements
	private String username, address, contactNumber, email;
	private static int sortCode = 100000;
	private int customerSortCode;
	private boolean hasActiveLoan;

	// Constructor
	public Customer(String username, String address, String contactNumber, String email) {
		this.accounts = new ArrayList<>();
		this.loanAgreements = new ArrayList<>();
		this.username = username;
		this.address = address;
		this.contactNumber = contactNumber;
		this.email = email;
		this.createSortCode();
		hasActiveLoan = false;
	}

	// Returns a summary of customer's accounts as a string
	public String accountsToString() {
		String s = "";
		for(Account a : this.accounts) {
			s += a.toString();
		}
		return s;
	}

	// Mutator: Update static sort code tracker to generate unique customer sort code
	public void createSortCode(){
		sortCode += 1;
		this.customerSortCode = sortCode;
	}

	//Accessor: Get customer's sort code
	public int getSortCode() {
		return this.customerSortCode;
	}
	
	// Returns customer's username
	public String getUsername() {
		return this.username;
	}

	// Returns whether customer already has an active loan
	public boolean getHasActiveLoan() { return hasActiveLoan; }

	// Sets HasActiveLoan to true if customer opens a loan
	public void setHasActiveLoan(boolean hasActiveLoan) {
		this.hasActiveLoan = hasActiveLoan;
	}

	// Adds a new account to the customer's profile
	public void addAccount(Account account) {
		this.accounts.add(account);		
	}

	// Checks if customer can complete transfer based on account balance
	public boolean sufficientFunds(double amount) {
		boolean sufficient = true;
		for (Account a : accounts) {
			if (amount > a.getBalance()) {
				sufficient = false;
			} else {
				sufficient = true;
				break; 		// if any of the customer's accounts is sufficiently funded answer true.
			}
		}
		if(!sufficient) {
			System.out.println("Insufficient Funds.");  
		}
		return sufficient;
	}

	// Checks whether the customer has an account with the given name
	public Account getAccountByName(String fromAccountName) {
		for (Account a : this.accounts) {
			if (a.getAccountName().equals(fromAccountName)) {
				return a;
			}
		}
		return null;
	}

	// Gets customer's account by account type
	public Account getAccountByType(String accountType) {
		for (Account a : this.accounts) {
			if (a.getAccountType().equals(accountType)) {
				return a;
			}
		}
		return null;
	}

	// Accessor to return a list of customer's accounts
	public ArrayList<Account> getAccounts() { return accounts;}

	// Add loan agreement to the customer
	public void addLoanAgreement(LoanAgreement loanAgreement) {
		this.loanAgreements.add(loanAgreement);
	}

	// Accessor: Gets customer information
	public ArrayList<String> getCustomerInfo(){
		/* Function is not currently used, this feature is incorporated to meet the customer 
		 * requirement that this information is saved. Ideally this would be expanded in the
		 * future to be saved securely in the SecureDataStorage class (in a database) and only
		 * accessible by an Admin account, AFA functions and/or the customer who's information it is.
		 */
		ArrayList<String> customerInfo = new ArrayList<String>();
		customerInfo.add(this.address);
		customerInfo.add(this.contactNumber);
		customerInfo.add(this.email);
		return getCustomerInfo();
	}
}
