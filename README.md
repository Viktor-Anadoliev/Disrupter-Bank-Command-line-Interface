# Disrupter-Bank-Command-line-Interface

This is a collaborative university project of 5 students.

NewBank is a disrupter bank where customers can interact with their accounts via a simple command-line interface. A customer enters the command below and sees the messages returned.

# Instructions

Run NewBankServer.java
Run ExampleClient.java
Enter username e.g. John, Christina
Enter password
Type commands in the command line

# Available Commands

**_SHOWMYACCOUNTS_**

Returns a list of all the customers' accounts along with their current balance and details

e.g. Main: 1000.0


**_NEWACCOUNT <Type> <Name>_**

Create a new CURRENT or SAVINGS account. e.g. NEWACCOUNT SAVINGS MySavings

Returns SUCCESS or FAIL


**_MOVE <Amount> <FromAccountName> <ToAccountName>_**

Move money between a customer's own accounts. e.g. MOVE 100 Main Savings

Returns SUCCESS or FAIL


**_PAY <amount> <fromAccountIBAN> <toAccountIBAN>_**

Allows customers to send money to another NewBank customer. There is a daily pay limit of 50 000. e.g. PAY GB001000011000001 GB001000021000002 100

Returns SUCCESS or FAIL


**_'PRINTSTATEMENT '_**

Prints details of all transactions in and out of the given account within the last 12 months. e.g. 'PRINTSTATEMENT GB001000011000001'


**_INFO_**

e.g. INFO

Informs the user how to use the commands with parameters and lists all user commands with short descriptions


**_LOAN <LenderAccountType> <Amount> <Person> _**

Allows customers to loan less than 100 to a NewBank Customer that does not already have an open loan account. e.g. LOAN CURRENT John 20

Returns SUCCESS or FAIL


**_REPAY_**

Allows the loan borrower to repay their loan with interest Returns SUCCESS or FAIL


**_'EXIT'_**

Logs out the current user. No more commands can be called until a user logs in.
