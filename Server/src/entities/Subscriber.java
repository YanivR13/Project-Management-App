package entities;

import java.util.ArrayList;
import java.util.List;

public class Subscriber extends User{
	
	private long subscriberId; 
    private String username;     
    private String qrCode;      
    private List<Visit> visitHistory; 
    private List<Reservation> reservationHistory; 

    public Subscriber(long phone, String email, long subId, String username, String qr) {
        super(email, phone);
        this.subscriberId = subId;
        this.username = username;
        this.qrCode = qr;
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
