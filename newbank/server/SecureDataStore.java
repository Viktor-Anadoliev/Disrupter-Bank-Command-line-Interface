package newbank.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.HashMap;

public class SecureDataStore {
    // hashmap facilitates retrieving customer information by username and accessing customerID token
	private static HashMap<String, Customer> customers = new HashMap<>();
    private static HashMap<String, String> passwordHashes = new HashMap<>();
    
    // Constructor
    public SecureDataStore(){
        // Not used as all methods are static. 
        // Storage is done here in hashmaps but long term these would be replaced with connection to secure databases. 
   }

   // Create a new customer object with associated information
    public static boolean addNewCustomer(String username, String inputPassword, String address, 
    String contactNumber, String email){
        // store username and user object
        if(customers.get(username) == null){
            Customer customerObj = new Customer(username, address, contactNumber, email);
            customers.put(username, customerObj);
        } else {
            // username already exists
            return false;
        } 
        
        // Validate the entered password on setup
        if(!passwordMeetsRequirements(inputPassword)){
            return false;
        }

        // store password
        try{
            passwordHashes.put(username, newPasswordHash(inputPassword));
            return true;
        } catch (NoSuchAlgorithmException e){
            return false;
        } catch (InvalidKeySpecException e){
            return false;
        }
   }

    // Fetch customer object by its username (polymorphic)
    public static Customer getCustomer(String username){
        return customers.get(username);
    }

    // Fetch customer object by its ID (polymorphic)
    public static Customer getCustomer(CustomerID customerID){
        return customers.get(customerID.getKey());
    }

    // Validate log in details entered by customer
	public static synchronized CustomerID checkLogInDetails(String username, String password) {
        // validate username
        if(customers.containsKey(username)) {
            // get customer ID
			CustomerID currentCustomerID = new CustomerID(username);

            // validate password
			if(validatePassword(username, password)){
				return currentCustomerID;
			}
		}
		return null;
    }

    // Returns collection of customers stored in data store
    public static Collection<Customer> getAllCustomers() {
        return customers.values();
    }

    // Validate if a new password meets the complexity requirements imposed
    private static boolean passwordMeetsRequirements(String password){
        boolean upper = false;
        boolean lower = false;
        boolean number = false;
        boolean special = false;

        if(password.length() < 12){
            return false;        
        }

        for(int charIndex = 0; charIndex < password.length(); charIndex++){
            char currentChar = password.charAt(charIndex);
            if(Character.isUpperCase(currentChar)){
                upper = true;
            } else if(Character.isLowerCase(currentChar)){
                lower = true;
            } else if(Character.isDigit(currentChar)){
                number = true;
            } else if(!Character.isWhitespace(currentChar)){
                special = true;
            }
        }
        
        return upper && lower && number && special;

    }

    // Validate if password entered during login is correct
    private static boolean validatePassword(String username, String inputPassword) {
        // validate username is in password system
        if(!passwordHashes.containsKey(username)){
            return false;
        }

        // get encoded password stored for username
        String userHash = passwordHashes.get(username);

        // validate the input password
        try {
            // separate stored hash into its sections
            String[] parts = userHash.split(":");
            int iterations = Integer.parseInt(parts[0]); // find iterations used
            byte[] salt = getBytesFromHex(parts[1]); // find salt used
            byte[] hash = getBytesFromHex(parts[2]); // find password hash used

            // using the same salt and iterations as stored password, encode inputPassword
            PBEKeySpec spec = new PBEKeySpec(inputPassword.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            // if there is no difference between the two encoded passwords, return true
            int diff = hash.length ^ testHash.length;
            for(int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch (NoSuchAlgorithmException e){
            return false;
        } catch (InvalidKeySpecException e){
            return false;
        }
    }
    
    // Encrypt a new password
    private static String newPasswordHash(String inputPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Use PBKDF2WithHmacSHA1 algorithm to securely store passwords
        
        // get inputs for PBE key specification
        int iterations = 1000; // determine speed of hash function (can adjust as computers get faster for longevity of code)
        char[] charPassword = inputPassword.toCharArray(); // strings are immutable but char arrays can be overwritten after use (more secure)
        byte[] salt = getSalt();

        // password based encryption: convert the password characters
        PBEKeySpec spec = new PBEKeySpec(charPassword, salt, iterations, 64 * 8);

        // create 'SecretKeyFactory' object for converting 'PBKDF2WithHmacSHA1' algorithm keys and key specifications
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        // generate secret key object from specification and encode as a byte array
        byte[] hash = skf.generateSecret(spec).getEncoded();

        return iterations + ":" + getHexFromBytes(salt) + ":" + getHexFromBytes(hash);
    }

    // Generate a pseudo-random salt for password encryption
    private static byte[] getSalt() throws NoSuchAlgorithmException {
        // Salt value needs to be stored for each password that is hashed so the hash can be re-created during password validation
        // Use the SHA1PRNG pseudo random number generator
        SecureRandom generator = SecureRandom.getInstance("SHA1PRNG");

        // Create array to store salt
        byte[] salt = new byte[16];

        // Retrieve random bytes to use as the salt
        generator.nextBytes(salt);
        return salt;
    }

    // Convert byte array to hexadecimal string
    private static String getHexFromBytes(byte[] array) throws NoSuchAlgorithmException {
        // create a big integer from byte array and convert to hexadecimal
        BigInteger bigInt = new BigInteger(1, array);
        String hex = bigInt.toString(16);
        
        // add padding as needed to ensure zero values are represented in the hex
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0) {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
            return hex;
        }
    }
    
    // Convert hexidecimal string to byte array
    private static byte[] getBytesFromHex(String hex) throws NoSuchAlgorithmException {
        // convert hexadecimal string to byte array
		byte[] bytes = new byte[hex.length() / 2];
		for(int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
   
}
