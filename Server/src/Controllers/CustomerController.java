package Controllers;

import java.util.concurrent.Flow.Subscriber;

import entities.OccasionalCustomer;
import entities.User;

public class CustomerController {
	
	private DBController dbController;
	
	public CustomerController(DBController dbController) {
        this.dbController = dbController;
    }

	/**
     * Purpose: Searches for an existing user in the database.
     * Receives: long phoneNumber, String email.
     * Returns: User object if found, null otherwise.
     */
    public User identifyUser(long phoneNumber, String email) {
        // Query the database to find a match for phone or email
        return null;
    }
    
    /**
     * Purpose: Creates a new occasional customer profile.
     * Receives: long phoneNumber, String email.
     * Returns: OccasionalCustomer (the newly created object).
     */
    public OccasionalCustomer createOccasionalUser(long phoneNumber, String email) {
        // Instantiate a new OccasionalCustomer and save it via DBController
        return null;
    }

    /**
     * Purpose: Identifies a registered subscriber by their ID or QR code.
     * Receives: String subscriberId (or QR code data).
     * Returns: Subscriber object (or null if not found).
     */
    public Subscriber identifySubscriber(long subscriberId) {
        // Search the database for the unique subscriber ID
        return null;
    }

    /**
     * Purpose: Registers a new subscriber into the system.
     * Receives: long phone, String email, String username.
     * Returns: Subscriber (the newly created object).
     */
    public Subscriber registerSubscriber(long phone, String email, String username) {
        // Create new subscriber, generate ID/QR, and save to database
        return null;
    }

    /**
     * Purpose: Updates contact information for an existing subscriber.
     * Receives: String subId, long newPhone, String newEmail.
     * Returns: boolean (true if update was successful).
     */
    public boolean updateSubscriberDetails(long subId, long newPhone, String newEmail) {
        // Find subscriber, validate changes, and update fields
        return false;
    }

}
