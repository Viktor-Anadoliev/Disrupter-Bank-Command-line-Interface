package newbank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// creates a separate thread for each customer connection
public class NewBankClientHandler extends Thread{
	
	private NewBank bank;
	private BufferedReader in;
	private PrintWriter out;
	

	/* The constructor initialises NewBankClientHandler object when a customer connects to server. It retrieves NewBank
	* instance and creates input and output streams to communicate with user via socket object.
	* */
	public NewBankClientHandler(Socket s) throws IOException {
		bank = NewBank.getBank();
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		out = new PrintWriter(s.getOutputStream(), true);
	}

	public void run() {
		// keep getting requests from the client and processing them
		try {
			CustomerID customer = null;
			// keep asking for login details until correctly entered
			while(customer == null){
				// ask for username
				out.println("Enter Username");
				String userName = in.readLine();

				// ask for password
				out.println("Enter Password");
				String password = in.readLine();

				// authenticate user and get customer ID token from bank for use in subsequent requests
				customer = SecureDataStore.checkLogInDetails(userName, password);

				// if the user is authenticated then get requests from the user and process them 
				if(customer != null) {
					out.println("Log In Successful.\n"+
					"\nWelcome " + userName + "!\n"+
					"\nThe NewBank application is controlled by something called a Command Line Interface (CLI).\nTo navigate the application and complete actions you must enter a command into the terminal."+
					"\nThe commands generally consist of a command name followed by the parameters needed to complete the request."+
					"\nType INFO in the terminal to be informed of how to use the commands with their parameters and see a list of them with short descriptions.\n");
					while(true) {
						String request = in.readLine();
						// if the user logs out by issuing the EXIT command, print an exit message and go back to login screen.
						if(request.equals("EXIT")) {
							out.println("\nThank you for using NewBank, " + customer.getKey() + ". You logged out.\n");
							System.out.println(customer.getKey() + " Logged out.\n");
							customer = null; 	// reinitialise customer to null to stay in the login screen.
							break;			// break out of this while loop
						}
						System.out.println("Request from " + customer.getKey());
						// process user input using bank instance
						// First get the confirmation message from the bank - if there is one run the confirmation method
						String confirm = bank.confirmationMessage(customer, request);
						if (confirm != null) {
							out.println(confirm);
							// If the user confirms the transaction, run the request, otherwise cancel the transaction
							if (confirmTransaction()) {
								String responce = bank.processRequest(customer, request);
								out.println(responce);
							} else {
								out.println("Transaction cancelled");
							}							
						}
						// If there is no confirmation message, run the request as normal
						else {
							String responce = bank.processRequest(customer, request);
							out.println(responce);
						}
					}
				}
				else {
					out.println("Log In Failed");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				in.close();
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}

	// method to confirm a process request
	private boolean confirmTransaction() {		
		while (true) {	
			out.println("Please confirm the transaction by entering 'Y' or 'N'");			
			String input;
			try {
				input = in.readLine();
				if (input.equals("Y")) {
					return true;
				} else if (input.equals("N")) {
					return false;
				} else {
					System.out.println("Invalid input, please try again");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}		

		}
	}

}
