package dbLogic;

/**
 * The ILoginDatabase interface defines the formal contract for all database 
 * operations related to user authentication and guest registration.
 * * This abstraction allows the system to support multiple login strategies 
 * (Subscribers vs. Occasional Guests) while providing a consistent API for 
 * the server-side controllers.
 * * @version 1.0
 */
public interface ILoginDatabase {

    /**
     * Verifies a registered subscriber's credentials.
     * * @param subID The unique numeric identifier assigned to the subscriber.
     * @return The internal system 'userId' (int) if the subscriber is found and active; 
     * returns -1 if authentication fails.
     */
    int verifySubscriber(long subID);

    /**
     * Verifies an occasional guest's credentials based on their chosen identity.
     * * @param username    The guest's unique username.
     * @param contactInfo The registered phone number or email associated with the guest.
     * @return The internal system 'userId' (int) if the guest is found; 
     * returns -1 if authentication fails.
     */
    int verifyOccasional(String username, String contactInfo);

    /**
     * Registers a new occasional customer in the database.
     * Implementation should ensure data integrity across the 'user' and 
     * 'occasional_customer' tables.
     * * @param username The desired username for the new guest.
     * @param phone    The guest's mobile phone number.
     * @param email    The guest's email address.
     * @return true if the registration was successful and committed to the DB; 
     * false otherwise.
     */
    boolean registerOccasional(String username, String phone, String email);
}