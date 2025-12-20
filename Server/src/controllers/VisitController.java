package controllers;

import java.util.List;

import common.Reservation;
import entities.Table;
import entities.User;
import entities.WaitingListEntry;
import mini_project.RestaurantController;
import mini_project.Visit;
import status.VisitStatus;



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
        int numberOfGuests = -1;
        User customer = null;

        // 1. Attempt to find the details in the reservations list (Reservation)
        Reservation res = dbController.findById(Reservation.class, confirmationCode);
        if (res != null) {
            numberOfGuests = res.getNumberOfGuests();
            customer = res.getCustomer();
        } else {
            // 2. If not found in reservations, search the waiting list (WaitingListEntry)
            WaitingListEntry entry = dbController.findById(WaitingListEntry.class, confirmationCode);
            if (entry != null) {
                numberOfGuests = entry.getNumberOfGuests();
                customer = entry.getCustomer();
            }
        }

        // 3. If the confirmation code was not found in either list
        if (numberOfGuests == -1) {
            System.out.println("Invalid confirmation code: " + confirmationCode);
            return -1;
        }

        // 4. Search for an available table that matches the required number of guests
        Table suitableTable = null;
        for (Table table : restaurantController.getAllTables()) {
            if (table.isAvailable() && table.getCapacity() >= numberOfGuests) {
                suitableTable = table;
                break;
            }
        }

        // 5. If no suitable table is currently available
        if (suitableTable == null) {
            return -1;
        }

        // 6. Create the visit and seat the customer
        Visit newVisit = new Visit(suitableTable, customer, confirmationCode);
        dbController.save(newVisit);

        return suitableTable.getTableID();
    }

   
    
    
    
    
    
    
    
    /**
     * Purpose: Checks if there is a table currently free that fits the party size.
     * Receives: int guests.
     * Returns: boolean (true if a suitable table is available right now).
     */
    public boolean isImmediateTableAvailable(int guests) {
        //Iterate over all tables via the RestaurantController
        for (Table table : restaurantController.getAllTables()) {
            
            //Check: is the table physically available? (isAvailable)
            //and can it accommodate the number of guests? (capacity >= guests)
            if (table.isAvailable() && table.getCapacity() >= guests) {
                
                // Found at least one such table; no need to continue searching
                return true;
            }
        }
        
        //If the loop finishes and no suitable table was found
        return false;
    }

    
    

    
    /**
     * Purpose: Seats a walk-in customer immediately if a table is available.
     * Receives: User user, int guests.
     * Returns: long (the generated confirmation code for this visit, or -1 if no table available).
     */
    public long seatWalkIn(User user, int guests) {
        //Check whether a table is currently available
        if (!isImmediateTableAvailable(guests)) {
            return -1;
        }

        //Find the most suitable table (best fit)
        Table bestTable = null;
        for (Table t : restaurantController.getAllTables()) {
            if (t.isAvailable() && t.getCapacity() >= guests) {
                if (bestTable == null || t.getCapacity() < bestTable.getCapacity()) {
                    bestTable = t;
                }
            }
        }

        //Generate a unique confirmation code (e.g., based on system time)
        long generatedCode = System.currentTimeMillis();

        //Create the visit (the Visit constructor occupies the table)
        Visit newVisit = new Visit(bestTable, user, generatedCode);
        
        //Save the visit to the database
        dbController.save(newVisit);

        return generatedCode;
    }

    
    
    
    

    
    
    
    /**
     * Purpose: Ends a customer's visit, clears the table, and triggers the billing process.
     * Receives: long confirmationCode.
     * Returns: void.
     */
    public void endVisit(long confirmationCode) {
        //Locate the active visit in the system using the confirmation code
        Visit currentVisit = dbController.findById(Visit.class, confirmationCode);

        //Check that the visit exists (to prevent NullPointerException)
        if (currentVisit != null && currentVisit.getStatus() == VisitStatus.IN_PROGRESS) {
            
            //Update the visit status to BILLED (as noted in your implementation comments)
            currentVisit.setStatus(VisitStatus.BILLED);

            //Release the table and make it available again in the system
            Table table = currentVisit.getTable();
            if (table != null) {
                table.setAvailable(true);
            }

            //Save the changes to the database
            dbController.save(currentVisit);
            
            System.out.println("Visit ended for code " + confirmationCode + ". Table is now free.");
        } else {
            System.out.println("Visit not found or already ended for code: " + confirmationCode);
        }
    }

    
    
    
    /**
     * Purpose: Finds the current active visit associated with a specific table.
     * Receives: int tableId.
     * Returns: Visit object (or null if the table is empty).
     */
    public Visit getVisitByTable(int tableId) {
        // 1. Retrieve all visits currently in "IN_PROGRESS" status from the DB
        //    Using the getAll method of DBController to get the list of visits
        List<Visit> allVisits = dbController.getAll(Visit.class);
        
        if (allVisits == null) {
            return null;
        }

        // 2. Iterate over all visits
        for (Visit visit : allVisits) {
            // 3. Check whether the visit is associated with the requested table
            //    and is still active (IN_PROGRESS ensures we don't get an old paid visit)
            if (visit.getTable().getTableID() == tableId && 
                visit.getStatus() == VisitStatus.IN_PROGRESS) {
                
                return visit; // Found the visit associated with the table
            }
        }

        // 4. If no active visit was found for this table, return null
        return null;
    }

    
    
    
    
    /**
     * Purpose: Retrieves all customers currently seated in the restaurant.
     * Receives: None.
     * Returns: List<Visit> (all visits with IN_PROGRESS status).
     */
    public List<Visit> getActiveVisits() {
        // 1. Create a new list to hold only the active visits
        List<Visit> activeVisits = new java.util.ArrayList<>();

        // 2. Retrieve all visits from the DBController
        List<Visit> allVisits = dbController.getAll(Visit.class);

        // 3. If the list is empty, return an empty list (to prevent NullPointerException in the UI)
        if (allVisits == null) {
            return activeVisits;
        }

        // 4. Iterate over all visits and filter by status
        for (Visit visit : allVisits) {
            // Only add visits that are still in progress
            if (visit.getStatus() == VisitStatus.IN_PROGRESS) {
                activeVisits.add(visit);
            }
        }

        return activeVisits;
    }
}



