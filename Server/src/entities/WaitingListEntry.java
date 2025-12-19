package entities;

import java.time.LocalDateTime;

import status.WaitingListStatus;

public class WaitingListEntry {
	
	private long confirmationCode;      
    private LocalDateTime entryTime;     
    private int numberOfGuests;         
    private User customer;             
    private WaitingListStatus status; 
    private LocalDateTime notificationTime; 

    public WaitingListEntry(long confirmationCode, int numberOfGuests, User customer) {
        this.confirmationCode = confirmationCode;
        this.entryTime = LocalDateTime.now(); 
        this.numberOfGuests = numberOfGuests;
        this.customer = customer;
        this.status = WaitingListStatus.WAITING;
        notificationTime = null;
    }

    public long getConfirmationCode() {
    	return confirmationCode; 
    }
    
    public LocalDateTime getEntryTime() {
    	return entryTime; 
    }
    
    public int getNumberOfGuests() {
    	return numberOfGuests; 
    }
    
    public User getCustomer() {
    	return customer; 
    }
    
    public WaitingListStatus getStatus() {
    	return status; 
    }
    
    public LocalDateTime getNotificationTime() {
        return notificationTime;
    }
    
    public void setStatus(WaitingListStatus status) {
    	this.status = status; 
    }

    public void setNumberOfGuests(int numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }
    
    public void setNotificationTime(LocalDateTime notificationTime) {
        this.notificationTime = notificationTime;
    }

}
