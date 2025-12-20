package entities;

import java.util.ArrayList;
import java.util.List;

import common.Reservation;
import status.SubscriberStatus;

public class Subscriber extends User{
	
	private long subscriberId; 
    private String username;     
    private String qrCode;
    private SubscriberStatus status;
    private List<Visit> visitHistory; 
    private List<Reservation> reservationHistory; 

    public Subscriber(String phone, String email, long subId, String username, String qr, SubscriberStatus status) {
        super(email, phone);
        this.subscriberId = subId;
        this.username = username;
        this.qrCode = qr;
        this.status=status;
        this.visitHistory = new ArrayList<>();
        this.reservationHistory = new ArrayList<>();
        
    }

    public long getSubscriberId() {
    	return subscriberId; 
    }
    
    public String getUsername() {
    	return username; 
    }
    
    public String getQrCode() {
    	return qrCode; 
    }
    
    public List<Visit> getVisitHistory() {
    	return visitHistory; 
   }
    
    public List<Reservation> getReservationHistory() {
    	return reservationHistory; 
   }
}
