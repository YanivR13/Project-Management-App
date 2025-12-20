package controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import mini_project.RestaurantController;

public class ReservationController {
	private DBController dbController;
    private RestaurantController restaurantController;

    public ReservationController(DBController dbController, RestaurantController restaurantController) {
        this.dbController = dbController;
        this.restaurantController = restaurantController;
    }

    /**
     * Purpose: Validates the requested time against restaurant policies:
     * 1. Must be between 1 hour and 1 month from now.
     * 2. Must be on the hour (:00) or half-hour (:30).
     * 3. The restaurant must be open at that time.
     */
    public boolean isTimeWindowValid(LocalDateTime requestedTime) {
    	if (requestedTime == null) {
            return false;
        }
    	
    	//ignore seconds and nanoseconds for the validation
        LocalDateTime cleanRequested = requestedTime.withSecond(0).withNano(0);
    	
        //Time Range Validation
        LocalDateTime now = LocalDateTime.now();
        // We add a 1-minute buffer to 'minAllowed' to prevent rejection 
        // due to the few milliseconds it takes for the code to run.
        LocalDateTime minAllowed = now.plusHours(1).minusMinutes(1);
        LocalDateTime maxAllowed = now.plusMonths(1);
        
        if (cleanRequested.isBefore(minAllowed) || cleanRequested.isAfter(maxAllowed)) {
            return false;
        }
        
        //Interval Validation (Must be XX:00 or XX:30)
        int minutes = cleanRequested.getMinute();
        if (minutes != 0 && minutes != 30) {
            return false;
        }
        
        //Restaurant Opening Hours Validation
        if (restaurantController == null || !restaurantController.isRestaurantOpen(cleanRequested)) {
            return false;
        }

        // All checks passed
        return true;
    }

    /**
     * Purpose: Checks theoretical table availability for a specific time and party size.
     * Receives: LocalDateTime dateTime, int guests.
     * Returns: boolean (true if there is at least one suitable table not occupied for a 2-hour window).
     */
    public boolean checkAvailability(LocalDateTime dateTime, int guests) {
        // Implementation logic: Compare total suitable tables vs active reservations in that 2-hour slot
        return false;
    }

    /**
     * Purpose: Returns a list of available half-hour intervals for a specific date and party size.
     * Receives: LocalDate date, int guests.
     * Returns: List<LocalTime> (all times when the restaurant is open and has space).
     */
    public List<LocalTime> getAvailableTimeSlots(LocalDate date, int guests) {
        // Implementation logic: Loop through opening hours and checkAvailability for each 30-min slot
        return null;
    }

    /**
     * Purpose: Creates and saves a new reservation.
     * Receives: LocalDateTime dateTime, int guests, User user.
     * Returns: long (the generated confirmationCode, or -1 if the reservation failed).
     */
    public long createReservation(LocalDateTime dateTime, int guests, User user) {
        // Implementation logic: Validate time, check availability, generate code, and save to DB
        return -1;
    }

    /**
     * Purpose: Updates an existing reservation with a new time or guest count.
     * Receives: long confirmationCode, LocalDateTime newTime, int newGuests.
     * Returns: boolean (true if the update was successful).
     */
    public boolean updateReservation(long confirmationCode, LocalDateTime newTime, int newGuests) {
        // Implementation logic: Find reservation, check new availability, update if possible
        return false;
    }

    /**
     * Purpose: Cancels a reservation by changing its status to CANCELLED.
     * Receives: long confirmationCode.
     * Returns: boolean (true if the reservation was found and cancelled).
     */
    public boolean cancelReservation(long confirmationCode) {
        // Implementation logic: Find reservation and update status
        return false;
    }

    /**
     * Purpose: Scans all active reservations and marks those more than 15 minutes late as NOSHOW.
     * Receives: None.
     * Returns: void.
     */
    public void processNoShows() {
        // Implementation logic: Get all ACTIVE reservations and compare startTime + 15 mins with now()
    }
}
