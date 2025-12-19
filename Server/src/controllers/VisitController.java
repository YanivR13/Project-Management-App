package controllers;

import java.util.List;

import entities.User;
import mini_project.RestaurantController;
import mini_project.Visit;

/**
 * Controller responsible for the physical seating of customers, 
 * assigning specific tables, and managing active visits.
 */
public class VisitController {
	
	private DBController dbController;
    private RestaurantController restaurantController;

    public VisitController(DBController dbController, RestaurantController restaurantController) {
        this.dbController = dbController;
        this.restaurantController = restaurantController;
    }

    /**
     * Purpose: Seats a customer who has a pre-existing reservation or waiting list entry.
     * Receives: long confirmationCode.
     * Returns: int (the assigned table number, or -1 if no suitable table is available).
     */
    public int seatCustomer(long confirmationCode) {
        // 1. Find the Reservation or WaitingListEntry by code
        // 2. Find an currently available Table that fits the number of guests
        // 3. Create a Visit object, link it to the table and user, and save to DB
        // 4. Update the Table's status to occupied (not available)
        return -1;
    }
    
    /**
     * Part of VisitController
     * Purpose: Checks if an immediate seating is possible for a walk-in.
     * Receives: int guests.
     * Returns: boolean (true if a table is free right now).
     */
    public boolean isImmediateTableAvailable(int guests) {
        // Call RestaurantController to check if any suitable table has isAvailable = true
        return false;
    }

    /**
     * Purpose: Seats a walk-in customer immediately if a table is free.
     * Receives: User user, int guests.
     * Returns: int (the assigned table number, or -1 if the customer must join the waiting list).
     */
    public int seatWalkIn(User user, int guests) {
        // 1. Check if there is an immediately available Table for this many guests
        // 2. If found: Generate a new confirmationCode and create a Visit
        // 3. If not found: Return -1 (indicating UI should offer waiting list)
        return -1;
    }

    /**
     * Purpose: Finalizes a visit and frees the table for the next customer.
     * Receives: long confirmationCode.
     * Returns: void.
     */
    public void endVisit(long confirmationCode) {
        // 1. Find the active Visit by confirmation code
        // 2. Update the Visit status to PAID (or CLOSED)
        // 3. Set the associated Table back to available
        // 4. Record the payment/end time
    }

    /**
     * Purpose: Finds the current active visit associated with a specific table.
     * Receives: int tableId.
     * Returns: Visit object (or null if the table is empty).
     */
    public Visit getVisitByTable(int tableId) {
        // Implementation: Search all active visits for the one linked to this tableId
        return null;
    }

    /**
     * Purpose: Retrieves all customers currently seated in the restaurant.
     * Receives: None.
     * Returns: List<Visit> (all visits with IN_PROGRESS status).
     */
    public List<Visit> getActiveVisits() {
        // Implementation: Query DB for all visits that haven't ended yet
        return null;
    }

}
