package controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import common.Reservation;
import entities.Table;
import entities.User;
import entities.WaitingListEntry;
import status.ReservationStatus;
import status.WaitingListStatus;

/**
 * Controller responsible for managing the immediate waiting list for walk-in
 * customers. Handles the queue, notifications, and timeout cancellations.
 */

public class WaitingListController {
	private DBController dbController;
	private RestaurantController restaurantController;
	private ReservationController resController;

	public WaitingListController(DBController dbController, RestaurantController restaurantController, ReservationController resController) {
		this.dbController = dbController;
		this.restaurantController = restaurantController;
		this.resController = resController;
	}

	/**
	 * Purpose: Adds a customer to the waiting list when no immediate tables are
	 * available. Receives: User user, int guests. Returns: long (the confirmation
	 * code for the waiting list entry).
	 */

	public long addToWaitingList(User user, int guests) {
		// need to fix confirmation code method
		long confirmationCode = generateCode();
	    WaitingListEntry waiter = new WaitingListEntry(confirmationCode, guests, user);
	    dbController.save(waiter);
	    return confirmationCode;
	}

	
	/**
	 * Scans the waiting list and notifies the next customer if a table is truly
	 * available. It performs a seating simulation to ensure future reservations
	 * (next 2 hours) are not compromised by walk-in customers.
	 */
	public void notifyNextInLine() {
		// Fetch all active waiting list entries, sorted by their arrival time (FIFO)
		List<WaitingListEntry> waitingList = dbController.getWaitingListByStatus(WaitingListStatus.WAITING);
		if (waitingList.isEmpty())
			return;
		// Explicitly sort by registration time to guarantee FIFO
		waitingList.sort(Comparator.comparing(WaitingListEntry::getEntryTime));
		// Fetch all future reservations starting within the next 120 minutes
		// This represents the "commitments" the restaurant has for the near future
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime windowEnd = now.plusMinutes(120);
		List<Reservation> futureReservations = dbController.getActiveReservatiosnByTimeRange(now, windowEnd);
		// Get all tables that are NOT physically occupied (status is not SEATED)
		List<Table> availableTables = restaurantController.getAllFreeTables();
		if (availableTables.isEmpty())
			return;
		// Create a temporary working list of tables for simulation
		List<Table> simulationTables = new ArrayList<>(availableTables);
		// Sort future reservations by size (Descending) to perform "Best Fit
		// Decreasing" simulation
		futureReservations.sort((r1, r2) -> Integer.compare(r2.getNumberOfGuests(), r1.getNumberOfGuests()));
		// Subtract tables needed for future reservations from our simulation list
		for (Reservation res : futureReservations) {
			Table bestFit = null;
			for (Table t : simulationTables) {
				if (t.getCapacity() >= res.getNumberOfGuests()) {
					if (bestFit == null || t.getCapacity() < bestFit.getCapacity()) {
						bestFit = t;
					}
				}
			}
			if (bestFit != null) {
				simulationTables.remove(bestFit); // This table is "reserved" for a future guest
			}
		}
		// Try to seat customers from the waiting list using the remaining tables
		for (WaitingListEntry entry : waitingList) {
			Table bestFitForWaiter = null;
			for (Table t : simulationTables) {
				if (t.getCapacity() >= entry.getNumberOfGuests()) {
					if (bestFitForWaiter == null || t.getCapacity() < bestFitForWaiter.getCapacity()) {
						bestFitForWaiter = t;
					}
				}
			}
			// If a suitable table is found after the simulation
			if (bestFitForWaiter != null) {
				// send notification somehow
				// Update entry: Set notification time and link the table
				entry.setNotificationTime(now);
				entry.setStatus(WaitingListStatus.NOTIFIED);
				dbController.updateWaitStatus(entry.getConfirmationCode(), WaitingListStatus.NOTIFIED);
				simulationTables.remove(bestFitForWaiter);
				// update status
				entry.setStatus(WaitingListStatus.NOTIFIED);
			}
		}
	}

	/**
	 * Purpose: Scans for notified customers who failed to check in within the
	 * 15-minute grace period. Receives: None. Returns: void.
	 */
	public void handleExpiredWaiters() {
		// Calculate the cutoff time (current time minus 15 minutes)
		// Any notification sent BEFORE this time is now expired.
		LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
		// We filter for NOTIFIED status.
		List<WaitingListEntry> notifiedWaiters = dbController.getWaitingListByStatus(WaitingListStatus.NOTIFIED);
		if (notifiedWaiters == null || notifiedWaiters.isEmpty()) {
			return; // Nothing to process
		}
		boolean wasAnyCancelled = false;
		for (WaitingListEntry entry : notifiedWaiters) {
			// check if the notification time is older than our 15-minute cutoff.
			if (entry.getNotificationTime() != null && entry.getNotificationTime().isBefore(cutoff)) {
				// Update the status to CANCELLED as they missed their window
				entry.setStatus(WaitingListStatus.CANCELLED);
				// Update the database
				updateWaitStatus(entry.getConfirmationCode(), WaitingListStatus.CANCELLED);
				wasAnyCancelled = true;
			}
		}
		if (wasAnyCancelled)
			notifyNextInLine();
	}

	/**
	 * Purpose: Removes a customer from the waiting list if they choose to leave
	 * voluntarily. Receives: long confirmationCode. Returns: boolean (true if the
	 * entry was found and cancelled).
	 */
	public boolean leaveWaitingList(long confirmationCode) {
		WaitingListEntry entry = dbController.findById(WaitingListEntry.class, confirmationCode);
		// If the entry exists and isn't already processed (e.g., SEATED or CANCELLED)
		if (entry != null && (entry.getStatus() == WaitingListStatus.WAITING
				|| entry.getStatus() == WaitingListStatus.NOTIFIED)) {
			// Save the previous status to decide if we need to trigger a re-scan
			WaitingListStatus previousStatus = entry.getStatus();
			// Update status locally
			entry.setStatus(WaitingListStatus.CANCELLED);
			// Update the database
			boolean isUpdated = updateWaitStatus(confirmationCode, WaitingListStatus.CANCELLED);
			if (isUpdated) {
				// If the person who left was already NOTIFIED, a "virtual" table is now free!
				// We should immediately check if we can notify the next person.
				if (previousStatus == WaitingListStatus.NOTIFIED) {
					notifyNextInLine();
				}
				return true;
			}
		}
		return false;
	}
}