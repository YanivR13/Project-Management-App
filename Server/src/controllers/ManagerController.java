package controllers;

import java.util.List;
import java.util.Map;

import entities.Subscriber;
import entities.Visit;

/**
 * Controller responsible for administrative tasks, generating analytical reports,
 * and monitoring restaurant performance metrics.
 */
public class ManagerController {
	
	private DBController dbController;

    public ManagerController(DBController dbController) {
        this.dbController = dbController;
    }

    /**
     * Purpose: Generates a monthly time report showing arrival times, departure times, and delays.
     * Receives: int month, int year.
     * Returns: List<Visit> (a list of all visits that occurred in the specified period).
     */
    public List<Visit> getMonthlyTimeReport(int month, int year) {
        // 1. Fetch all visits from DBController
        // 2. Filter visits by the specified month and year
        // 3. Return the filtered list for UI rendering (e.g., a bar chart)
        return null;
    }

    /**
     * Purpose: Provides statistics about subscriber activities, including visit frequency and No-Shows.
     * Receives: None (or a specific Subscriber ID for a detailed report).
     * Returns: Map<Subscriber, Integer> (a map linking subscribers to their total number of completed visits).
     */
    public Map<Subscriber, Integer> getSubscriberActivityReport() {
        // 1. Fetch all subscribers and all visits
        // 2. Aggregate data to count visits/cancellations per subscriber
        // 3. Return the compiled statistics
        return null;
    }

    /**
     * Purpose: Analyzes and reports on No-Show incidents for both reservations and waiting lists.
     * Receives: int month.
     * Returns: List<Object> (list of Reservation and WaitingListEntry items marked as NOSHOW or CANCELLED due to delay).
     */
    public List<Object> getNoShowReport(int month) {
        // 1. Search for all entries with status NOSHOW or late CANCELLED
        // 2. Filter by the relevant month
        return null;
    }

    /**
     * Purpose: Calculates the average stay duration of customers for operational optimization.
     * Receives: None.
     * Returns: double (average minutes per visit).
     */
    public double calculateAverageVisitDuration() {
        // 1. Get all PAID visits
        // 2. Calculate duration (paymentTime - startTime) for each
        // 3. Return the average
        return 0.0;
    }

    /**
     * Purpose: Retrieves a summary of total revenue generated in a specific timeframe.
     * Receives: int month, int year.
     * Returns: double (total sum of all finalized bills).
     */
    public double getTotalRevenue(int month, int year) {
        // 1. Fetch all Bills from DBController
        // 2. Sum the finalAmount of all bills marked as isPaid within the date range
        return 0.0;
    }

}
