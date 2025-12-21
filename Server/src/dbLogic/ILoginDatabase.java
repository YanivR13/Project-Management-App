package dbLogic;

import java.util.ArrayList;

/**
 * Interface defining the contract for database login operations.
 */
public interface ILoginDatabase {
    // Method for subscriber login
    boolean verifySubscriber(long subID);

    // Method for occasional customer login
    boolean verifyOccasional(String username, String contactInfo);

    // Method for new occasional customer registration
    boolean registerOccasional(String username, String phone, String email);
}