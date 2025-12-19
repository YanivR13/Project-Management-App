package DB;

import java.util.List;

import entities.User;
import entities.WaitingListEntry;
import mini_project.RestaurantController;

/**
 * Controller responsible for managing the immediate waiting list for walk-in customers.
 * Handles the queue, notifications, and timeout cancellations.
 */
public class WaitingListController {
	
	private DBController dbController;
    private RestaurantController restaurantController;

    public WaitingListController(DBController dbController, RestaurantController restaurantController) {
        this.dbController = dbController;
        this.restaurantController = restaurantController;
    }

    /**
     * Purpose: Adds a customer to the waiting list when no immediate tables are available.
     * Receives: User user, int guests.
     * Returns: long (the confirmation code for the waiting list entry).
     */
    public long addToWaitingList(User user, int guests) {
        // Create new WaitingListEntry, set status to WAITING, and save to DB
        return -1;
    }

    /**
     * Purpose: Checks if any tables have become available and notifies the next suitable party in line.
     * Receives: None.
     * Returns: void.
     */
    public void notifyNextInLine() {
        // 1. Get all WAITING entries
        // 2. Check current table availability via RestaurantController
        // 3. If a table fits, update entry status to NOTIFIED and set notificationTime
    }

    /**
     * Purpose: Scans for notified customers who failed to check in within the 15-minute grace period.
     * Receives: None.
     * Returns: void.
     */
    public void handleExpiredWaiters() {
        // Find entries with status NOTIFIED where 15 minutes have passed since notificationTime
        // Set status to CANCELLED
    }

    /**
     * Purpose: Removes a customer from the waiting list if they choose to leave voluntarily.
     * Receives: long confirmationCode.
     * Returns: boolean (true if the entry was found and cancelled).
     */
    public boolean leaveWaitingList(long confirmationCode) {
        // Find the entry by code and update status to CANCELLED
        return false;
    }

    /**
     * Purpose: Finds a waiting list entry by its confirmation code.
     * Receives: long confirmationCode.
     * Returns: WaitingListEntry object (or null if not found).
     */
    public WaitingListEntry findEntryByCode(long confirmationCode) {
        // Query DBController for the specific entry
        return null;
    }

    /**
     * Purpose: Retrieves the current active waiting list for display or management.
     * Receives: None.
     * Returns: List<WaitingListEntry> (only those with WAITING or NOTIFIED status).
     */
    public List<WaitingListEntry> getActiveWaitingList() {
        // Filter all entries in DB to return only active ones
        return null;
    }
}


