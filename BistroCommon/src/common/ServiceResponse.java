package common; // Define the package where the class belongs

import java.io.Serializable; // Import the Serializable interface for network transmission

/**
 * ServiceResponse serves as a generic communication envelope for messages sent 
 * from the Server to the Client.
 */
public class ServiceResponse implements Serializable { // Class definition implementing Serializable
    
    // Serial version UID to ensure compatibility during the serialization/deserialization process
    private static final long serialVersionUID = 1L;

    /**
     * Enum defining the specific outcomes of a reservation request handled by the Server.
     */
    public enum ServiceStatus { // Start of ServiceStatus enumeration
        
        // Case: The reservation was successfully placed and saved in the DB
        RESERVATION_SUCCESS,    
        
        // Case: Slot is full, but an alternative date/time is suggested
        RESERVATION_SUGGESTION, 
        
        // Case: No availability found for requested or alternative slots
        RESERVATION_FULL,
        
        // Case: The requested time falls outside of restaurant operating hours
        RESERVATION_OUT_OF_HOURS,
        
        // Case: A general update operation was completed successfully
        UPDATE_SUCCESS,

        // Case: A technical failure occurred (SQL error, connection loss, etc.)
        INTERNAL_ERROR          
    } // End of ServiceStatus enumeration

    // Field to store the specific outcome category of the response
    private ServiceStatus status;
    
    // Polymorphic field to hold the payload (ID, Date string, or Error message)
    private Object data; 

    /**
     * Constructs a new ServiceResponse with the specified status and data payload.
     */
    public ServiceResponse(ServiceStatus status, Object data) { // Constructor start
        // Assign the provided service status to the class member
        this.status = status;
        
        // Assign the provided data object (payload) to the class member
        this.data = data;
    } // Constructor end

    /**
     * Retrieves the status of the server response.
     * @return ServiceStatus enum value
     */
    public ServiceStatus getStatus() { 
        // Return the current value of the status field
        return status; 
    } // End method

    /**
     * Retrieves the data payload associated with this response.
     * @return Object containing the payload
     */
    public Object getData() { 
        // Return the current value of the data field
        return data; 
    } // End method

    /**
     * Provides a human-readable string summary of the response.
     */
    @Override // Indicates that this method overrides the default Object implementation
    public String toString() { // Start of toString method
        // Using a clear and standard format to represent the object state
        return "ServiceResponse [" + 
               "Status=" + status + 
               ", Data=" + data + 
               "]";
    } // End method
} // End of ServiceResponse class