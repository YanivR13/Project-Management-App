package common;

import java.io.Serializable;

/**
 * ServiceResponse serves as a generic communication envelope for messages sent 
 * from the Server to the Client.
 * It standardizes the feedback loop for the reservation process, ensuring the 
 * Client can consistently interpret successes, suggestions, or failures.
 * * <p>This class implements {@link Serializable} to allow transmission over OCSF 
 * network streams.</p>
 * * @author Software Engineering Student
 * @version 1.0
 */
public class ServiceResponse implements Serializable {
    
    /** Serial version UID for maintaining compatibility during the serialization process. */
    private static final long serialVersionUID = 1L;

    /**
     * Enum defining the specific outcomes of a reservation request handled by the Server.
     */
    public enum ReservationResponseStatus {
        /** Case 1: Availability confirmed. The booking is recorded in the Database. */
        RESERVATION_SUCCESS,    
        
        /** Case 2: Requested slot is full, but an alternative time within the next 3 days was found. */
        RESERVATION_SUGGESTION, 
        
        /** Case 3: Absolute capacity reached. No tables available in the requested or alternative slots. */
        RESERVATION_FULL,       
        
        /** General system failure, Database connection loss, or SQL execution error. */
        INTERNAL_ERROR          
    }

    /** The categorical status of the server's response. */
    private ReservationResponseStatus status;
    
    /** * A polymorphic data field that holds context-specific information based on the status:
     * <ul>
     * <li>For {@code SUCCESS}: Contains the Long confirmation code.</li>
     * <li>For {@code SUGGESTION}: Contains the String of the recommended DateTime.</li>
     * <li>For {@code ERROR}: Contains a String describing the technical failure.</li>
     * </ul>
     */
    private Object data; 

    /**
     * Constructs a new ServiceResponse.
     * * @param status The outcome category defined by the {@link ReservationResponseStatus} enum.
     * @param data The payload associated with the response (ID, Date string, or Error message).
     */
    public ServiceResponse(ReservationResponseStatus status, Object data) {
        this.status = status;
        this.data = data;
    }

    /**
     * @return The current status of the response.
     */
    public ReservationResponseStatus getStatus() { 
        return status; 
    }

    /**
     * @return The payload object. Needs to be cast based on the status type.
     */
    public Object getData() { 
        return data; 
    }

    /**
     * Overridden to provide a human-readable representation of the response for logging.
     * @return A string summary of the response status and its data.
     */
    @Override
    public String toString() {
        return "ServiceResponse [Status=" + status + ", Data=" + data + "]";
    }
}