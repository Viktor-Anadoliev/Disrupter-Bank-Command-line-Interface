package newbank.server;

public class LoanAgreement {
    private final double loanAmount;
    private final Customer borrower;
    private final Customer lender;
    private final Account lenderAccount;
    private final CurrentAccount borrowerAccount;
    private LoanAccount borrowerLoanAccount;
    private static final double INTEREST_RATE = 0.07;
    private static final double MAX_LOAN_AMOUNT = 100;

    // Constructor
    public LoanAgreement (double amount, Customer borrower, Customer lender, CurrentAccount borrowerAccount,
                          Account lenderAccount) {
        this.loanAmount = amount;
        this.borrower = borrower;
        this.lender = lender;
        this.borrowerAccount = borrowerAccount;
        this.lenderAccount = lenderAccount;
        this.borrowerLoanAccount = null;
    }

    // Checks if a loan is valid based on current loan agreement requirements
    public boolean isValidLoan() {
        if (borrower.getHasActiveLoan()) {
            System.out.println("Borrower already has an active loan.");
            return false;
        }
        if (loanAmount <= 0) {
            System.out.println("Please enter a valid loan amount.");
            return false;
        }

        if (loanAmount > MAX_LOAN_AMOUNT) {
            System.out.println("You cannot lend more than 100 currency.");
            return false;
        }

        if (!lenderAccount.getOwner().sufficientFunds(loanAmount)) {
            System.out.println("Insufficient balance in lender's account.");
            return false;
        }
        return true;
    }

    // Complete loan process

    /* This involves withdrawing the loan amount from the lender's account, creating a new loan account for the borrower
    * and depositing the loan amount into the loan account, withdrawing the loan amount from the loan account and
    * depositing it into the borrower's current account.
    *  */
    public void performLoanProcess() {
        lenderAccount.withdraw(loanAmount);
        borrowerLoanAccount = new LoanAccount("LOAN", loanAmount, borrower);
        borrowerLoanAccount.withdraw(loanAmount);
        borrowerAccount.deposit(loanAmount);
        borrower.setHasActiveLoan(true);

        System.out.println("Loan of " + loanAmount + " lent to " + borrower.getUsername() + ".");
    }

    // Checks that loan account is active and that borrower has the necessary balance to repay loan
    public boolean isValidRepayment() {
        if (borrowerLoanAccount == null || borrowerAccount.getBalance() < repaymentAmount()) {
            System.out.println("Repayment failed due to insufficient balance");
            return false;
        }

        return true;
    }

    // Complete loan repayment process
    /* This involves withdrawing the loan amount plus interest from the borrower's account and depositing the loan amount
    plus interest into the lender's current account.
     */
    public void performRepaymentProcess() {
        double repaymentAmount = repaymentAmount();

        // withdraws loan amount plus interest from borrower's account
        borrowerAccount.withdraw(repaymentAmount);
        // deposits loan amount plus interest into lender's account
        lenderAccount.deposit(repaymentAmount);
        borrower.setHasActiveLoan(false);

        System.out.println("Loan of " + loanAmount + " paid to " + lender.getUsername()
                + " with " + INTEREST_RATE + " interest.");
    }

    // Calculates loan amount plus interest to be repaid by the borrower to the lender
    private double repaymentAmount() {
        return loanAmount * (1 + INTEREST_RATE);
    }
}
