package common; // Defining the package location for this class

import java.io.Serializable; // Importing the Serializable interface for object transmission

/**
 * The WaitingListEntry class represents a customer waiting for an available table.
 * This class is used to track people who couldn't find an immediate reservation.
 */
public class WaitingListEntry implements Serializable { // Defining the class and implementing Serializable

    // Serial version identifier to ensure class compatibility during serialization
    private static final long serialVersionUID = 1L;

    // Unique confirmation code identifying this specific waiting entry
    private long confirmationCode;
    
    // The timestamp when the customer was added to the waiting list
    private String entryTime;
    
    // Total number of guests associated with this entry
    private int numberOfGuests;
    
    // The unique ID of the user who is waiting
    private int userId;
    
    // Current status of the entry stored as a String (WAITING, NOTIFIED, etc.)
    private String status; 
    
    // The timestamp when the customer was notified about an available table
    private String notificationTime;

    /**
     * Enum for statuses to avoid typos and provide a reference for valid states.
     */
    public enum WaitingStatus { // Beginning of WaitingStatus enum definition
        WAITING,   // The customer is currently in the queue
        NOTIFIED,  // The customer has been alerted that a table is ready
        CANCELLED, // The customer or system removed the entry
        ARRIVED    // The customer has arrived and been seated
    } // End of WaitingStatus enum definition

    /**
     * Constructor to initialize a new WaitingListEntry with all required fields.
     */
    public WaitingListEntry(long confirmationCode, String entryTime, int numberOfGuests, int userId, String status, String notificationTime) { // Constructor start
        
        // Assigning the provided confirmation code to the instance field
        this.confirmationCode = confirmationCode;
        
        // Assigning the provided entry time to the instance field
        this.entryTime = entryTime;
        
        // Assigning the provided number of guests to the instance field
        this.numberOfGuests = numberOfGuests;
        
        // Assigning the provided user ID to the instance field
        this.userId = userId;
        
        // Assigning the provided status string to the instance field
        this.status = status;
        
        // Assigning the provided notification time to the instance field
        this.notificationTime = notificationTime;
        
    } // End of constructor

    // =========================================================================
    // Getters and Setters Section
    // =========================================================================

    /**
     * Retrieves the confirmation code of the entry.
     * @return long confirmationCode
     */
    public long getConfirmationCode() { // Start of getConfirmationCode method
        return confirmationCode;        // Returning the long value
    } // End of method

    /**
     * Retrieves the user ID associated with this entry.
     * @return int userId
     */
    public int getUserId() { // Start of getUserId method
        return userId;       // Returning the integer value
    } // End of method

    /**
     * Retrieves the current status of the waiting entry.
     * @return String status
     */
    public String getStatus() { // Start of getStatus method
        return status;          // Returning the status string
    } // End of method

} // End of WaitingListEntry class