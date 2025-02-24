package newbank.server;

import java.util.Arrays;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class NewBank {
	private static final NewBank bank = new NewBank();
	Transaction transaction;
	LoanAgreement loanAgreement;

	// creates one instance of NewBank and initialises test data
	private NewBank() {
		addTestData();
		runInterestThread();
	}

	// adds test data for program demonstration
	private void addTestData() {
		String defaultPassword = "Password123!";

		SecureDataStore.addNewCustomer("Bhagy", defaultPassword, "123 Road A10 B11", 
		"07777777771", "bhagy@test.com");
		Customer bhagy = SecureDataStore.getCustomer("Bhagy");
		bhagy.addAccount(new CurrentAccount("Main", 1000.0, bhagy));

		SecureDataStore.addNewCustomer("Christina", defaultPassword, "123 Road A10 B11", 
		"07777777772", "christina@test.com");
		Customer christina = SecureDataStore.getCustomer("Christina");
		christina.addAccount(new SavingsAccount("Savings", 1500.0, christina));

		SecureDataStore.addNewCustomer("John", defaultPassword, "123 Road A10 B11", 
		"07777777773", "john@test.com");
		Customer john = SecureDataStore.getCustomer("John");

		Account johnChecking = new CurrentAccount("Checking", 250.0, john);
		Account johnSavings = new SavingsAccount("Savings", 100.0, john);
		john.addAccount(johnChecking);
		john.addAccount(johnSavings);

		// Example transactions for John (to demonstrate 12 month statement)
		this.transaction = new Transaction(johnChecking, johnSavings, 30);
		this.transaction.overrideDate(LocalDate.now().minusMonths(13));
		johnChecking.addTransaction(this.transaction);
		johnSavings.addTransaction(this.transaction);
		this.transaction = new Transaction(johnSavings, johnChecking, 20);
		this.transaction.overrideDate(LocalDate.now().minusMonths(10));
		johnSavings.addTransaction(this.transaction);
		johnChecking.addTransaction(this.transaction);
		moveMoney(10, johnChecking.getIBAN(), johnSavings.getIBAN());
	}

	// returns single instance of NewBank
	public static NewBank getBank() {

		// Schedule a task to reset daily transaction limit at midnight
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

		// Use UK time zone to determine next midnight
		ZoneId ukTime = ZoneId.of("Europe/London");
		LocalTime midnight = LocalTime.MIDNIGHT;
		ZonedDateTime now = ZonedDateTime.now(ukTime);
		ZonedDateTime nextMidnight = now.toLocalDate().atTime(midnight).atZone(ukTime);

		// If nextMidnight is in the past compared to the current time, adjust nextMidnight to the upcoming midnight
		if (now.compareTo(nextMidnight) > 0) {
			nextMidnight = nextMidnight.plusDays(1);
		}

		// Calculate the delay in time before next daily transaction limit reset
		Duration initialDelayDuration = Duration.between(now, nextMidnight);
		long initialDelay = initialDelayDuration.getSeconds();
		long period = Duration.ofDays(1).getSeconds(); // 24 hours in seconds

		// Schedule the task
		scheduler.scheduleAtFixedRate(
				Transaction::resetDailyTransactionLimit,
				initialDelay,
				period,
				TimeUnit.SECONDS
		);
		return bank;

	}

	// commands from the NewBank customer are processed in this method
	/**
	 * @param customer The CustomerID of the customer making the request
	 * @param request The request string received from the customer
	 * @return The response string based on the processed request
	 */
	public synchronized String processRequest(CustomerID customer, String request) {
		/* Splits a multi-word customer request/command to separate a command from its parameters
		  for effective processing */

		String command = getCommand(request);

		ArrayList<String> otherParams = getOtherParams(request);

		// Check if the customer is in the bank's database, if so retrieve the customer's information i.e. acct details.
		if(SecureDataStore.getCustomer(customer) != null) {
			switch(command) {

				// display info about the application/commands
				case "INFO":
					return infoCommand();

				// move money between a customer's accounts
				case "MOVE":
					return moveCommand(customer, otherParams);

				// displays all the account of a specified customer
				case "SHOWMYACCOUNTS" :
					return showMyAccounts(customer);

				// opens a new account of type/name acctType
				case "NEWACCOUNT":					
			    if(otherParams.size() == 2) {
					String accountType = otherParams.get(0);	// get account type which is the 2nd parameter
					String accountName = otherParams.get(1);	// get account name which is the 3rd parameter
				        return newAccount(customer, accountName, accountType);
				    } else {
					return "The number of specified parameters in the command is incorrect.\n" +
					"Type INFO to see a list of all commands with their corresponding parameters including short descriptions.\n" +
					"FAIL\n";
					 // the call to open a new account fails if the account type is not specified
				    }

				// move money from one customer's account to another customer's account
				case "PAY":
					if (otherParams.size() == 3) { // checks that there are 2 remaining parameters
						return pay(customer, otherParams.get(0), otherParams.get(1), otherParams.get(2));
					// from account is 2nd parameter, recipient is 3rd parameter and amount is 4th parameter
					} else {

					return "The number of specified parameters in the command is incorrect.\n" +
					"Type INFO to see a list of all commands with their corresponding parameters including short descriptions.\n"+
					"FAIL\n"; 
					}					
				//

				case "PRINTSTATEMENT":
					if (otherParams.size() == 1) { // checks that there is 1 parameter
						return printStatement(customer, otherParams.get(0));
					} else {
					return "The number of specified parameters in the command is incorrect.\n" +
					"Type INFO to see a list of all commands with their corresponding parameters including short descriptions.\n"+
					"FAIL\n";
					}

				// complete loan as a lender to another NewBank customer
				case "LOAN":
					if (otherParams.size() == 3) {
						return processLoanCommand(customer, otherParams.get(0), otherParams.get(1), otherParams.get(2));
					} else {
						return "FAIL";
					}

				// complete repayment to lender
				case "REPAY":
					return repayLoanCommand();

				// fail if no case is recognised
				default : 
					return "Your command " + command + " is invalid. \n" +
					"Type INFO to see a list of all commands with their corresponding parameters including short descriptions.\n"+
					"FAIL\n";
			}
		}
		return "There is no information about this customer in the bank's database.\n"+
		"FAIL\n";
		
	}

	// Fetch additional parameters entered in command line
	private ArrayList<String> getOtherParams(String request) {
		// initialize an array for the remaining parameters
		String[] params = request.split(" ");
		int paramsLength = params.length;
		ArrayList<String> otherParams = new ArrayList<>();
		if(paramsLength > 1) {
		    for(String param : (Arrays.copyOfRange(params, 1, paramsLength))) {
                        otherParams.add(param);		// store other parameters in the initialised ArrayList
		   }
		}
		return otherParams;
	}

	// Fetch commands when entered in command line
	private String getCommand(String request) {
		String[] params = request.split(" ");
		String command = params[0];
		return command; 		// extract the command/request which is the first parameter
	}

	// Returns a string representation of the accounts associated with the customer
	private String showMyAccounts(CustomerID customer) {
		return (SecureDataStore.getCustomer(customer).accountsToString());
	}

	// Create a new customer account
	private String newAccount(CustomerID customer, String name, String accountType) {
		// create new current or savings account
		Customer owner = SecureDataStore.getCustomer(customer);
		if(accountType.equals("CURRENT")){
			// create a new account object starting with a zero balance
			Account nAcct = new CurrentAccount(name, 0.0, owner);

			// add the new account to the list of the specified customer's accounts
			SecureDataStore.getCustomer(customer).addAccount(nAcct);

		} else if(accountType.equals("SAVINGS")){
			// create a new account object starting with a zero balance
			Account nAcct = new SavingsAccount(name, 0.0, owner);

			// add the new account to the list of the specified customer's accounts
			owner.addAccount(nAcct);
		} else {
			return "Invalid Account type! Account types are CURRENT and SAVINGS.\n"+
			"Make sure to type a correct Account type parameter.\n"+
			"FAIL\n";
		}		
		return "New " + accountType + " account named " + name + " has been successfully created.\n"+
		"SUCCESS\n";
	}

	// Checks which accounts in the data store are savings accounts and adds interest to them
	private void addInterestToSavingsAccounts() {
		for (Customer customer : SecureDataStore.getAllCustomers()) {
				for (Account a : customer.getAccounts()) {
				  if (a instanceof SavingsAccount) {
					  SavingsAccount savingsAccount = (SavingsAccount) a;
					  savingsAccount.addInterest();
				}
			}
		}
	}

	// Runs a background thread to automatically deposit interest into savings accounts every 30 days
	private void runInterestThread() {
		Thread interestThread = new Thread(() -> {
			while (true) {
				try {
					Thread.sleep(30 * 24 * 60 * 60 * 1000); // Sleep for 30 days
					addInterestToSavingsAccounts();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		interestThread.start();
	}

	// Method to handle payments to a person or company in NewBank
	private String pay(CustomerID fromCustomer, String amount, String fromIBAN, String toIBAN) {
		/**
		 * @param fromCustomer, customer initiating payment
		 * @param to, customer that will receive payment
		 * @param amount, amount of money to be paid
		 * */
		double numAmount;
		Customer recipient = Account.getAccountFromIBAN(toIBAN).getOwner();
		Customer sender = Account.getAccountFromIBAN(fromIBAN).getOwner();

		// Convert amount from String to double
		try {
			numAmount = Double.parseDouble(amount);
		} catch(NumberFormatException e) {
			return "Invalid amount input\n"+
			"FAIL\n";
		}

		// check that customer initiating payment is the fromAccount owner
		if(!sender.equals(SecureDataStore.getCustomer(fromCustomer))){
			return "Payment not initiated by account owner ("
			+ sender.getUsername() + ", "
			+ SecureDataStore.getCustomer(fromCustomer).getUsername() + ")\n"+
			"FAIL\n";
		}

		// check that sender is not the recipient
		if(sender.equals(recipient)){
			return "Sender is recipient\n!"+
			"FAIL\n";
		}

		// check that there are sufficient funds in customer's account to send payment, then complete payment
		Account fromAcct = Account.getAccountFromIBAN(fromIBAN);
		if (fromAcct.sufficientFunds(numAmount)) {
			LocalDate today = LocalDate.now();
			double currentSum = Transaction.getDailyTransactions().getOrDefault(today, 0.0);
			// check that the transaction amount does not exceed the daily transaction limit
			if (currentSum + numAmount > Transaction.getDailyLimit()) {
				return "Transaction amount exceeds daily limit.\n"+
				"FAIL\n";
			}
			this.moveMoney(numAmount, fromIBAN, toIBAN);
			return "The payment was successful!\n"+
			"SUCCESS\n";
			
		}
		else {
			return "Insufficient funds in this account to send" + amount + "to \n" + recipient.getUsername() + ": " + toIBAN + "\n"+
			"Your account's funds in the account " + Account.getAccountFromIBAN(fromIBAN).getAccountName() + "(" + fromIBAN + ") are " + Account.getAccountFromIBAN(fromIBAN).getBalance() + ".\n" +
			"FAIL\n";
		}
	}

	// Tries to transfer money from one account to another, returns true if successful
	private boolean moveMoney(double amount, String fromIBAN, String toIBAN) {
		try{
			Account fromAccount = Account.getAccountFromIBAN(fromIBAN);
			Account toAccount = Account.getAccountFromIBAN(toIBAN);
			if(fromAccount.sufficientFunds(amount)){
			    fromAccount.withdraw(amount);
			    toAccount.deposit(amount);
			    this.recordTransaction(fromAccount, toAccount, amount);
			} else {
				return false;
			}
		}
		catch(Exception e){
			return false;
		}
		return true;
	}

	// Record the transaction in the statements of all involved accounts
	private void recordTransaction(Account fromAccount, Account toAccount, double amount){
		this.transaction = new Transaction(fromAccount, toAccount, amount);
		System.out.println("Owner: " + fromAccount.getOwner().getUsername());
		fromAccount.addTransaction(this.transaction);
		toAccount.addTransaction(this.transaction);
	}

	// Print the account's 12 month statement to command line
	private String printStatement(CustomerID customer, String statementIBAN){
		String statement;
		Account account = Account.getAccountFromIBAN(statementIBAN);
		Customer accountOwner = Account.getAccountFromIBAN(statementIBAN).getOwner();
		//Check that customer initiating the print statement command request is the account owner
		try{
			if(!accountOwner.equals(SecureDataStore.getCustomer(customer))){
				return "Print statement request not initiated by account owner ("
				+ accountOwner.getUsername() + ")\n" +
				"FAIL\n";
			}
			else{
			statement = account.getAllTransactions();
			}
	 	}
	 	catch(Exception e){
	 		return "Invalid IBAN.\n";
	 	}
	 	return statement;
	}

	// Print information explaining available commands to the command line
	private String infoCommand(){
		return
		"\nWelcome! \nTo navigate the application and complete actions you must enter a command into the terminal."+
		"\nThe commands generally consist of a command name followed by the parameters needed to complete the request, which are detailed below:\n"+
		"\nSHOWMYACCOUNTS\nThis command will show all the accounts owned by a customer and the balance for each.\n"+
		"\nNEWACCOUNT <account type> <account name>\nThis command can be used for setting up a new account for a customer. Separated by a space, enter: \n\t1) the command \n\t2) the account type (CURRENT or SAVINGS)\n\t3) your chosen account name.\n"+
		"\nMOVE <amount> <fromAccountName> <toAccountName>\nThis command is used for moving money between a customers accounts. Separated by a space, enter:\n\t1) the command\n\t2) the amount to transfer (a number)\n\t3) the name of the account the money is coming from\n\t4) the name of the account the money is going to.\nExample: MOVE 100 Current Savings\n"+
		"\nPAY <amount> <fromAccountIBAN> <toAccountIBAN>\nThis command is used for sending money to another customer. \nSeparated by a space, enter:\n\t1) the command\n\t2) the amount of money to be paid (a number)\n\t3) the origin account's IBAN\n\t4) the receiving account's IBAN identifier.\n"+
		"\nPRINTSTATEMENT <accountIBAN>\nSee all transactions on this account in the last 12 months.\n"+
		"\nLOAN <LenderAccountType> <username> <amount>\nLoan another NewBank customer money. \nSeparated by a space, enter: \n\t1) the command\n\t2) the account type you will be lending from\n\t3) the NewBank customer's username\n\t4) the amount of money.\n"+
		"\nREPAY\nThis command will allow you to repay your active loan. It will automatically withdraw the loan amount plus interest from your current account.\n"+
		"\nEXIT\nThis command is used to log out of your session."+
		"\n\nWhat do you want to do?\n";
	}

	// Move money between one customer's accounts
	private String moveCommand(CustomerID customer, ArrayList<String> otherParams) {
		if (otherParams.size() >= 3) {
			// Move money from account name inputs within own accounts
			double amount = Double.parseDouble(otherParams.get(0));
			String fromAccountName = otherParams.get(1);
			String toAccountName = otherParams.get(2);
			Customer customerObj = SecureDataStore.getCustomer(customer);
			Account fromAccount = customerObj.getAccountByName(fromAccountName);
			Account toAccount = customerObj.getAccountByName(toAccountName);
			String fromIBAN = fromAccount.getIBAN();
			String toIBAN = toAccount.getIBAN();

			// if the accounts are valid, move the money, else reject
			if (fromAccount != null && toAccount != null) {
				if (moveMoney(amount, fromIBAN, toIBAN)) {
					return "SUCCESS\n";
				} else {
					return "FAIL: Insufficient funds in the " + fromAccountName + " account.\n"+
					"Your account's funds in the account " + Account.getAccountFromIBAN(fromIBAN).getAccountName() + "(" + fromIBAN + ") are " + Account.getAccountFromIBAN(fromIBAN).getBalance() + ".\n" +
					"FAIL\n";
				}
			} else {
				return "FAIL: Invalid account names.\n";
			}
		} else {

			return "FAIL: Invalid parameters. Please enter command in the following format (without the chevrons): MOVE <amount> <fromAccount> <toAccount>\n";

		}
	}

	// Method to handle loan command to loan money to another NewBank customer
	private String processLoanCommand(CustomerID customer, String lenderAccountType, String borrowerUsername, String amount) {

		// Get the borrower's account by username
		Customer borrower = SecureDataStore.getCustomer(borrowerUsername);
		if (borrower == null) {
			return "FAIL: Borrower not found.";
		}

		// Get lender's account by specified account type
		Customer lender = SecureDataStore.getCustomer(customer);
		Account lenderAccount = lender.getAccountByType(lenderAccountType);

		// Get borrower's account
		Account borrowerAccount = borrower.getAccountByType("CURRENT");
		if (borrowerAccount == null) {
			return "FAIL: Borrower's account not found.";
		}

		double numAmount;

		// Convert amount from String to double
		try {
			numAmount = Double.parseDouble(amount);
		} catch(NumberFormatException e) {
			System.out.println("Invalid amount input");
			return "FAIL";
		}

		// Check if the borrowerAccount is a CurrentAccount
		if (!(borrowerAccount instanceof CurrentAccount)) {
			return "FAIL: Borrower account is not a CurrentAccount.";
		}

		// Cast the borrowerAccount to a CurrentAccount
		CurrentAccount currentBorrowerAccount = (CurrentAccount) borrowerAccount;

		// Create a new LoanAgreement instance with the lender, borrower, and loan amount
		loanAgreement = new LoanAgreement(numAmount, borrowerAccount.getOwner(),
				lenderAccount.getOwner(), currentBorrowerAccount, lenderAccount);

		// Check if the loan agreement is valid
		if (!loanAgreement.isValidLoan()) {
			return "FAIL";
		}

		// Add the loan agreement to the borrower's loan agreements list
//		Customer borrower = borrowerAccount.getOwner();
		borrower.addLoanAgreement(loanAgreement);

		// Perform the loan process, including debiting lender and crediting borrower
		loanAgreement.performLoanProcess();
		return "SUCCESS";
	}

	// method to handle repay command to repay loan with interest to lender's account
	private String repayLoanCommand() {

		// Check if the loan agreement is valid for repayment
		if (!loanAgreement.isValidRepayment()) {
			return "FAIL";
		}
		// Perform the repayment process from borrower to lender with interest
		loanAgreement.performRepaymentProcess();
		return "SUCCESS";
	}
	/*
	 * This method creates a confirmation message where applicable (i.e. when a user is about to move money between
	 * accounts, create a new account or pay another user)
	 */
	public String confirmationMessage(CustomerID customer, String request) {
		String command = getCommand(request);
		ArrayList<String> otherParams = getOtherParams(request);
		switch (command) {
			case "MOVE":
				return "You are attempting to send " + otherParams.get(0) + " from " + otherParams.get(1) + " to " + otherParams.get(2);
			case "PAY":
				return "You are attempting to send " + otherParams.get(1) + " to " + otherParams.get(0);
			case "NEWACCOUNT":
				return "You are attempting to create a new " + otherParams.get(0) + " account called " + otherParams.get(1);
			case "LOAN":
				return "You are attempting to loan " + otherParams.get(2) + " from your " + otherParams.get(0) + " account to " + otherParams.get(1);
			case "REPAY":
				return "You are attempting to repay your loan";
			default:
				return null;
		}

	}
}
