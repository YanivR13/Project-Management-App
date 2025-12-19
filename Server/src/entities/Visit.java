package entities;

import java.time.LocalDateTime;

import entities.Bill;
import entities.Table;
import entities.User;
import status.VisitStatus;

public class Visit {
	
	private Table table;               
    private User customer;              
    private long confirmationCode;       
    private LocalDateTime startTime;    
    private Bill bill;                 
    private VisitStatus status;
    
    public Visit(Table table, User customer, long confirmationCode) {
    	this.table = table;
        this.customer = customer;
        this.confirmationCode = confirmationCode;
        this.startTime = LocalDateTime.now();
        this.status = VisitStatus.IN_PROGRESS;
        this.table.setAvailable(false);
    }

    public Table getTable() {
    	return table; 
    }
    
    public User getCustomer() {
    	return customer; 
    }
    
    public LocalDateTime getStartTime() {
    	return startTime; 
    }
    
    public long getConfirmationCode() {
    	return confirmationCode; 
    }
    
    public VisitStatus getStatus() {
    	return status; 
    }
    
    public Bill getBill() {
    	return bill; 
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public void setStatus(VisitStatus status) {
        this.status = status;
    }
    
    public void completeVisit() {
        if (this.bill != null && this.bill.isPaid()) {
            this.status = VisitStatus.PAID;
            this.table.setAvailable(true);
        }
    }

}
