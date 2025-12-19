package entities;

import java.time.LocalDateTime;

public class Bill {
	
	private int billId;
	private double baseAmount;      
    private double discountPercent; 
    private double finalAmount;     
    private boolean isPaid;         
    private LocalDateTime paymentTime; 

    public Bill(double baseAmount, double discountPercent) {
        this.baseAmount = baseAmount;
        this.discountPercent = discountPercent;
        this.finalAmount = baseAmount * (1 - discountPercent);
        this.isPaid = false;
    }

    public void markAsPaid() {
        this.isPaid = true;
        this.paymentTime = LocalDateTime.now();
    }

    public double getFinalAmount() {
    	return finalAmount; 
    }
    
    public boolean isPaid() {
    	return isPaid; 
    }
    
    public LocalDateTime getPaymentTime() {
    	return paymentTime; 
    }
}
