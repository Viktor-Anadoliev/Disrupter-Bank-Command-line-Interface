package newbank.server;

public class CustomerID {

	// The CustomerID class represents a unique identifier for a customer within the system.
	private String key;
	// key stores the identifier
	public CustomerID(String key) {
		this.key = key;
	}
	
	public String getKey() {
		return key;
	}
}
