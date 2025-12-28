package common;

import java.io.Serializable;

public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private long billId;
    private long confirmationCode;
    private double baseAmount;
    private double discountPercent;
    private double finalAmount;

    public Bill(long billId, long confirmationCode, double baseAmount, double discountPercent, double finalAmount) {
        this.billId = billId;
        this.confirmationCode = confirmationCode;
        this.baseAmount = baseAmount;
        this.discountPercent = discountPercent;
        this.finalAmount = finalAmount;
    }

    // Getters
    public long getBillId() { return billId; }
    public long getConfirmationCode() { return confirmationCode; }
    public double getBaseAmount() { return baseAmount; }
    public double getDiscountPercent() { return discountPercent; }
    public double getFinalAmount() { return finalAmount; }
}