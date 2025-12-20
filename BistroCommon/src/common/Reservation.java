package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import status.ReservationStatus;

/**
 * The final shared entity for Reservations.
 * Merges original logic with OCSF Serialization and Payment tracking.
 */
public class Reservation implements Serializable {
    
    // Serialization ID is recommended for OCSF stability
    private static final long serialVersionUID = 1L;

    private long confirmationCode; 
    private LocalDateTime dateTime;        
    private int numberOfGuests;            
    private User customer;                 
    private ReservationStatus status;      
    private boolean isPaid; // Added for the Payment/View screen logic

    // Updated Constructor
    public Reservation(long confirmationCode, LocalDateTime dateTime, int numberOfGuests, User customer) {
        this.confirmationCode = confirmationCode;
        this.dateTime = dateTime;
        this.numberOfGuests = numberOfGuests;
        this.customer = customer;
        this.status = ReservationStatus.ACTIVE;
        this.isPaid = false; // Default value for new reservations
    }

    // --- Existing Getters and Setters ---

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

    // --- New Logic for Payment ---

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    // --- Helpers for JavaFX TableView ---
    // These help the TableView display data easily without complex formatting logic in the controller

    public String getDateString() {
        return dateTime.toLocalDate().toString();
    }

    public String getTimeString() {
        return dateTime.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getPaymentStatusText() {
        return isPaid ? "Paid" : "Unpaid";
    }

    // --- Original Domain Logic ---
    
    public LocalDateTime getExpectedEndTime() {
        return dateTime.plusHours(2);
    }
}