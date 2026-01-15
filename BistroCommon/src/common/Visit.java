package common; // Defining the package where the class is located

import java.io.Serializable; // Importing the interface for object serialization

/**
 * The Visit class represents a customer's presence in the restaurant.
 * It tracks the table occupancy and links the visit to a final bill.
 */
public class Visit implements Serializable { // Defining the Visit class which implements Serializable

    /**
     * Enum defining the possible lifecycle states of a restaurant visit.
     */
    public enum VisitStatus { // Defining internal Enum for visit lifecycle states
        
        // The customer is currently seated and the visit is ongoing
        ACTIVE,
        
        // The visit has concluded, payment is done, and the table is free
        FINISHED,
        
        //The visit has come to an end - the customer need to pay
        BILL_PENDING
    } // End of VisitStatus enumeration

    // Standard serial version UID for ensuring compatibility during OCSF network transmission
    private static final long serialVersionUID = 1L;

    // Unique confirmation code derived from the initial reservation (Primary Link)
    private long confirmationCode;
    
    // The specific identifier of the table where the customer is seated
    private int tableId;
    
    // The identifier of the user (customer) associated with this specific visit
    private int userId;
    
    // The ID of the generated bill (Long wrapper allows null if bill isn't created yet)
    private Long billId;
    
    // String representation of the timestamp when the customer arrived
    private String startTime;
    
    // The current status of the visit (either ACTIVE or FINISHED)
    private VisitStatus status;

    /**
     * Full constructor to initialize all fields of a Visit entity.
     */
    public Visit(long confirmationCode, int tableId, int userId, long billId, String startTime, VisitStatus status) { // Constructor start
        
        // Assigning the provided confirmation code to the instance variable
        this.confirmationCode = confirmationCode;
        
        // Assigning the provided table ID to the instance variable
        this.tableId = tableId;
        
        // Assigning the provided user ID to the instance variable
        this.userId = userId;
        
        // Assigning the provided bill ID to the instance variable (can be null)
        this.billId = billId;
        
        // Assigning the provided start time string to the instance variable
        this.startTime = startTime;
        
        // Assigning the provided status enum to the instance variable
        this.status = status;
        
    } // Constructor end

    // =========================================================================
    // Getters and Setters Section
    // =========================================================================

    

    
    
    
    public long getConfirmationCode() { // Method to retrieve confirmation code
        return confirmationCode;        // Returns the long value
    } // End method

    public void setConfirmationCode(long confirmationCode) { // Method to update confirmation code
        this.confirmationCode = confirmationCode;            // Assigning new value
    } // End method

    public int getTableId() { // Method to retrieve table ID
        return tableId;       // Returns the int value
    } // End method

    public void setTableId(int tableId) { // Method to update table ID
        this.tableId = tableId;           // Assigning new value
    } // End method

    public int getUserId() { // Method to retrieve user ID
        return userId;       // Returns the int value
    } // End method

    public void setUserId(int userId) { // Method to update user ID
        this.userId = userId;           // Assigning new value
    } // End method

    public Long getBillId() { // Method to retrieve bill ID
        return billId;        // Returns the Long object (could be null)
    } // End method

    public void setBillId(Long billId) { // Method to update bill ID
        this.billId = billId;            // Assigning new value
    } // End method

    public String getStartTime() { // Method to retrieve start time
        return startTime;          // Returns the time string
    } // End method

    public void setStartTime(String startTime) { // Method to update start time
        this.startTime = startTime;              // Assigning new value
    } // End method

    public VisitStatus getStatus() { // Method to retrieve visit status
        return status;               // Returns the VisitStatus enum
    } // End method

    public void setStatus(VisitStatus status) { // Method to update visit status
        this.status = status;                   // Assigning new value
    } // End method

    
    
 // שדה חדש להחזקת כמות הסועדים (לצורכי תצוגה בלבד)
    private int numberOfGuests;

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }
    
    
    // =========================================================================
    // Utility Methods
    // =========================================================================

    /**
     * Returns a summarized string representation of the Visit object.
     */
    @Override // Overriding Object's toString method
    public String toString() { // Method start
        
        // Building a clear string for console debugging and logging
        return "Visit [" + 
               "Code=" + confirmationCode + 
               ", Table=" + tableId + 
               ", Status=" + status + 
               "]";
               
    } // End method

    
    
    
    
    
    
    
 // הוסף את השדה הזה למעלה עם שאר השדות
    private String reservationDateTime;

    // הוסף את ה-Getter וה-Setter האלו
    public String getReservationDateTime() {
        return reservationDateTime;
    }

    public void setReservationDateTime(String reservationDateTime) {
        this.reservationDateTime = reservationDateTime;
    }
    
    
    
} // End of Visit class