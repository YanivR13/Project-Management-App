package common; // Defining the package name where this class resides

import java.io.Serializable; // Importing Serializable interface to allow object serialization

/**
 * The Bill class represents a financial record of a transaction.
 * It implements Serializable to allow instances to be sent over a network or saved to disk.
 */
public class Bill implements Serializable {

    // Unique version identifier for Serializable classes to ensure compatibility during deserialization
    private static final long serialVersionUID = 1L;
    
    // Unique ID of the bill
    private long billId;
    
    // Confirmation code associated with the transaction
    private long confirmationCode;
    
    // The initial cost before any discounts are applied
    private double baseAmount;
    
    // The percentage of discount to be subtracted from the base amount
    private double discountPercent;
    
    // The final price the customer needs to pay after calculations
    private double finalAmount;

    /**
     * Constructor to initialize a new Bill object with all required details.
     * * @param billId           The unique identifier for the bill
     * @param confirmationCode The unique code confirming the transaction
     * @param baseAmount       The original price
     * @param discountPercent  The discount rate applied
     * @param finalAmount      The total amount after discount
     */
    public Bill(long billId, long confirmationCode, double baseAmount, double discountPercent, double finalAmount) {
        // Assigning the provided bill ID to the class member
        this.billId = billId;
        
        // Assigning the provided confirmation code to the class member
        this.confirmationCode = confirmationCode;
        
        // Assigning the provided base amount to the class member
        this.baseAmount = baseAmount;
        
        // Assigning the provided discount percentage to the class member
        this.discountPercent = discountPercent;
        
        // Assigning the calculated final amount to the class member
        this.finalAmount = finalAmount;
    }

    // --- Getters Section ---

    /**
     * Retrieves the Bill ID.
     * @return long representation of the bill ID.
     */
    public long getBillId() { 
        // Returning the value of billId
        return billId; 
    }

    /**
     * Retrieves the Confirmation Code.
     * @return long representation of the confirmation code.
     */
    public long getConfirmationCode() { 
        // Returning the value of confirmationCode
        return confirmationCode; 
    }

    /**
     * Retrieves the Base Amount (before discount).
     * @return double representation of the base amount.
     */
    public double getBaseAmount() { 
        // Returning the value of baseAmount
        return baseAmount; 
    }

    /**
     * Retrieves the Discount Percentage.
     * @return double representation of the discount percent.
     */
    public double getDiscountPercent() { 
        // Returning the value of discountPercent
        return discountPercent; 
    }

    /**
     * Retrieves the Final Amount (after discount).
     * @return double representation of the final amount.
     */
    public double getFinalAmount() { 
        // Returning the value of finalAmount
        return finalAmount; 
    }
}