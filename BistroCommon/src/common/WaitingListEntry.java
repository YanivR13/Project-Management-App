package common;

import java.io.Serializable;

public class WaitingListEntry implements Serializable {
    private long confirmationCode;
    private String entryTime;
    private int numberOfGuests;
    private int userId;
    private String status; // WAITING, NOTIFIED, CANCELLED, ARRIVED
    private String notificationTime;

    // Enum for statuses to avoid typos
    public enum WaitingStatus {
        WAITING, NOTIFIED, CANCELLED, ARRIVED
    }

    public WaitingListEntry(long confirmationCode, String entryTime, int numberOfGuests, int userId, String status, String notificationTime) {
        this.confirmationCode = confirmationCode;
        this.entryTime = entryTime;
        this.numberOfGuests = numberOfGuests;
        this.userId = userId;
        this.status = status;
        this.notificationTime = notificationTime;
    }

    // Getters and Setters
    public long getConfirmationCode() { return confirmationCode; }
    public int getUserId() { return userId; }
    public String getStatus() { return status; }
}
