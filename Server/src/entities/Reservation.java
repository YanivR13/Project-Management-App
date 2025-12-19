package entities;

import java.time.LocalDateTime;

import status.ReservationStatus;

public class Reservation {
	
	private final long confirmationCode; 
    private LocalDateTime dateTime;        
    private int numberOfGuests;            
    private User customer;                 
    private ReservationStatus status;      

    public Reservation(long confirmationCode, LocalDateTime dateTime, int numberOfGuests, User customer) {
    	this.confirmationCode = confirmationCode;
        this.dateTime = dateTime;
        this.numberOfGuests = numberOfGuests;
        this.customer = customer;
        this.status= ReservationStatus.ACTIVE;
    }

    public long getConfirmationCode() {
    	return confirmationCode; 
    }
    
    public LocalDateTime getDateTime() {
    	return dateTime; 
    }
    
    public void setDateTime(LocalDateTime dateTime) {
    	this.dateTime = dateTime; 
    }
    
    public int getNumberOfGuests() {
    	return numberOfGuests; 
    }
    
    public void setNumberOfGuests(int num) {
    	this.numberOfGuests = num; 
    }
    
    public User getCustomer() {
    	return customer; 
    }
    
    public ReservationStatus getStatus() {
    	return status; 
    }

    public void setStatus(ReservationStatus status) {
    	this.status = status; 
    }
    
    public LocalDateTime getExpectedEndTime() {
        return dateTime.plusHours(2);
    }

}
