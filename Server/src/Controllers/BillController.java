package Controllers;

import entities.Bill;

public class BillController {
	
	private DBController dbController;

    public BillController(DBController dbController) {
        this.dbController = dbController;
    }

    /**
     * Purpose: Generates a bill detail for a specific visit. 
     * Includes logic to apply 10% discount if the user is a Subscriber.
     * Receives: long confirmationCode, double baseAmount.
     * Returns: Bill (the newly created bill object).
     */
    public Bill generateBill(long confirmationCode, double baseAmount) {
        // 1. Find the Visit/User associated with the code
        // 2. Check if the User is an instance of Subscriber
        // 3. Create a Bill with the calculated final price (apply discount if needed)
        // 4. Save the Bill to the database
        return null;
    }

    /**
     * Purpose: Processes the payment for a bill and marks it as paid.
     * Receives: long confirmationCode.
     * Returns: boolean (true if payment was successful and the bill was updated).
     */
    public boolean processPayment(long confirmationCode) {
        // 1. Find the Bill associated with the confirmation code
        // 2. Update isPaid to true and set paymentTime
        // 3. Trigger VisitController to release the table
        return false;
    }

    /**
     * Purpose: Retrieves the final amount to be paid for a specific visit.
     * Receives: long confirmationCode.
     * Returns: double (the final price after discounts).
     */
    public double getFinalAmount(long confirmationCode) {
        // Find the bill and return its finalAmount field
        return 0.0;
    }

}
