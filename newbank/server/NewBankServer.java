package newbank.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NewBankServer extends Thread{
	
	private ServerSocket server;

	public NewBankServer(int port) throws IOException {
		/* Initialises the Server Socket responsible for the listener. 
                   The listener will be started with a method call to accept()
                 */
		server = new ServerSocket(port);
	}
	
	public void run() {
		// Display port on which the NewBank server is listening
		System.out.println("New Bank Server listening on " + server.getLocalPort());
		try {
			while(true) {
				/* The listener begins to wait (listens) for client connections 
                   		   with the server sockets call to accept(). When a connection is
                   		   made to a client, the connection is handed over to a "client handler" 
                   		   Thread. The new Thread is called NewBankClientHandler
                		 */
				Socket s = server.accept(); // wait for connection from clients
				
				// starts up a new client handler thread to receive incoming connections and process requests
				NewBankClientHandler clientHandler = new NewBankClientHandler(s);
				clientHandler.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		// starts a new NewBankServer thread on a specified port number
		new NewBankServer(14002).start();
	}
}
