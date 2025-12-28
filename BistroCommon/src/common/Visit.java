package common;

import java.io.Serializable;

/**
 * The Visit class represents a customer's presence in the restaurant.
 * It tracks the table occupancy and links the visit to a final bill.
 * * @author Software Engineering Student
 * @version 1.1
 */
public class Visit implements Serializable {

    /**
     * Enum defining the possible lifecycle states of a restaurant visit.
     */
    public enum VisitStatus {
        /** Customer is currently seated at the table. */
        ACTIVE,
        
        /** Payment is complete, table is cleared, and the visit is over. */
        FINISHED
    }

    //Standard serial version UID for OCSF transmission
    private static final long serialVersionUID = 1L;

    /** Unique code from the original reservation. */
    private long confirmationCode;
    
    /** The ID of the table assigned to this visit. */
    private int tableId;
    
    /** The ID of the user (customer) associated with the visit. */
    private int userId;
    
    /** * The ID of the generated bill. 
     * Uses Long object to allow null values before payment is processed.
     */
    private Long billId;
    
    /** Timestamp of when the visit started. */
    private String startTime;
    
    /** Current status of the visit (ACTIVE or FINISHED). */
    private VisitStatus status;

    /**
     * Default constructor for creating a Visit entity.
     * * @param confirmationCode The unique identifier for the booking.
     * @param tableId The assigned table.
     * @param userId The customer's database ID.
     * @param billId The associated bill (can be null).
     * @param startTime The entry timestamp.
     * @param status The current state of the visit.
     */
    public Visit(long confirmationCode, int tableId, int userId, long billId, String startTime, VisitStatus status) {
        this.confirmationCode = confirmationCode;
        this.tableId = tableId;
        this.userId = userId;
        this.billId = billId;
        this.startTime = startTime;
        this.status = status;
    }


    public long getConfirmationCode() { return confirmationCode; }
    public void setConfirmationCode(long confirmationCode) { this.confirmationCode = confirmationCode; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Long getBillId() { return billId; }
    public void setBillId(Long billId) { this.billId = billId; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public VisitStatus getStatus() { return status; }
    public void setStatus(VisitStatus status) { this.status = status; }

    /**
     * Debugging helper to print visit details in the server console.
     */
    @Override
    public String toString() {
        return "Visit [Code=" + confirmationCode + ", Table=" + tableId + ", Status=" + status + "]";
    }
}
