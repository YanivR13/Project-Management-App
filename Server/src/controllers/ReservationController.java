package controllers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entities.User;
import entities.Reservation;
import entities.Table;
import entities.TimeRange;

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
     * Purpose: Checks if the restaurant can accommodate the new reservation.
     * 1. Validates the time according to restaurant policy.
     * 2. Fetches all reservations that overlap with the requested time (+/- 119 mins).
     * 3. Simulates seating everyone to see if a suitable physical table remains.
     */
    public boolean checkAvailability(LocalDateTime dateTime, int guests) {
        //Define the overlap window (119 minutes before and after)
        LocalDateTime startTimeWindow = dateTime.minusMinutes(119);
        LocalDateTime endTimeWindow = dateTime.plusMinutes(119);

        // Fetch overlapping reservations from DB
        List<Reservation> overlappingRes = dbController.getReservatiosnByTimeRange(startTimeWindow, endTimeWindow);

        //Get all physical tables
        List<Table> availableTables = dbController.getAll(Table.class);

        //Sort existing reservations by size (Largest first) to ensure they get prioritized
        overlappingRes.sort(Comparator.comparingInt(Reservation::getNumberOfGuests).reversed());        
        //Sort tables by capacity (Smallest first) for "Best Fit" strategy
        availableTables.sort(Comparator.comparingInt(Table::getCapacity));
        
        //Simulate "seating" existing customers
        for (Reservation res : overlappingRes) {
            for (int i = 0; i < availableTables.size(); i++) {
                if (availableTables.get(i).getCapacity() >= res.getNumberOfGuests()) {
                    availableTables.remove(i); // This table is now "virtually" taken
                    break;
                }
            }
        }
        
        //Final Check: Can the NEW reservation fit in any of the REMAINING tables?
        for (Table table : availableTables) {
            if (table.getCapacity() >= guests) {
                return true; 
            }
        }
        return false;
    }
    
    
    /**
    * Scans the entire day using the "Bucket" optimization method.
    * Each reservation occupies 4 consecutive 30-minute slots (120 minutes total).
    * @param date The requested date.
    * @param guests Number of guests.
    * @return List of available LocalTime slots.
    */
    public List<LocalTime> getAvailableTimeSlots(LocalDate date, int guests) {
        
    	List<LocalTime> availableSlots = new ArrayList<>();

        //Get operating hours for the specific date
        LocalTime openTime = restaurantController.getOpeningHour(date);
        LocalTime closeTime = restaurantController.getClosingHour(date);
        if (openTime == null || closeTime == null) {
        	return availableSlots;
        } 

        //Fetch all daily reservations from DB
        LocalDateTime fetchStart = LocalDateTime.of(date, openTime);
        LocalDateTime fetchEnd = LocalDateTime.of(date, closeTime);
        List<Reservation> dailyReservations = dbController.getReservatiosnByTimeRange(fetchStart, fetchEnd);

        //We create a map where each key is a 30-min interval and the value is the list of active reservations.
        Map<LocalTime, List<Reservation>> timeBuckets = new HashMap<>();
        
        //Initialize buckets for the entire working day
        LocalTime tempTime = openTime;
        while (!tempTime.isAfter(closeTime)) {
            timeBuckets.put(tempTime, new ArrayList<>());
            tempTime = tempTime.plusMinutes(30);
            if (tempTime.equals(LocalTime.MIN))
            	break;
        }

        //Assign each reservation to its 4 occupied slots (total of 120 mins)
        for (Reservation res : dailyReservations) {
            LocalTime resTime = res.getDateTime().toLocalTime();
            
            //A 2-hour reservation occupies the current slot + the next 3 slots (30, 60, 90 mins later)
            for (int i = 0; i < 4; i++) {
                LocalTime occupiedSlot = resTime.plusMinutes(i * 30);
                if (timeBuckets.containsKey(occupiedSlot)) {
                    timeBuckets.get(occupiedSlot).add(res);
                }
            }
        }

        //sort the tables
        List<Table> masterTableList = new ArrayList<>(dbController.getAll(Table.class));
        masterTableList.sort(Comparator.comparingInt(Table::getCapacity));

        //Scan intervals and perform the simulation
        LocalTime currentSlot = openTime;
        while (!currentSlot.isAfter(closeTime)) {
            LocalDateTime slotDateTime = LocalDateTime.of(date, currentSlot);

            //Check if slot is at least 1 hour in the future
            if (slotDateTime.isAfter(LocalDateTime.now().plusHours(1))) {
                
                //Get already filtered and mapped reservations for THIS specific bucket
                List<Reservation> activeReservationsInSlot = timeBuckets.get(currentSlot);
                
                //Simulation logic
                List<Table> availableTables = new ArrayList<>(masterTableList);
                
                //Sort active reservations by size (Largest First)
                activeReservationsInSlot.sort(Comparator.comparingInt(Reservation::getNumberOfGuests).reversed());        

                //Virtual seating simulation
                for (Reservation res : activeReservationsInSlot) {
                    for (int i = 0; i < availableTables.size(); i++) {
                        if (availableTables.get(i).getCapacity() >= res.getNumberOfGuests()) {
                            availableTables.remove(i);
                            break;
                        }
                    }
                }

                // Final check: Can we fit the NEW reservation?
                for (Table table : availableTables) {
                    if (table.getCapacity() >= guests) {
                        availableSlots.add(currentSlot);
                        break;
                    }
                }
            }
            
            currentSlot = currentSlot.plusMinutes(30);
            if (currentSlot.equals(LocalTime.MIN)) break;
        }

        return availableSlots;
    }

    /**
     * Purpose: Creates and saves a new reservation.
     * Receives: LocalDateTime dateTime, int guests, User user.
     * Returns: long (the generated confirmationCode, or -1 if the reservation failed).
     */
    public long createReservation(LocalDateTime dateTime, int guests, User user) {
    	if(!checkAvailability(dateTime, guests))
    		return -1;
    	long confirmationCode = 2;
    	Reservation newRes = new Reservation(confirmationCode, dateTime, guests, user);
    	dbController.save(newRes);
    	return confirmationCode;
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
