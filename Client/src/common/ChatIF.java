package common;

import java.util.ArrayList;

public interface ChatIF {
    public abstract void display(String message);
    
    // הוספנו את זה:
    public abstract void displayOrders(ArrayList<ArrayList<String>> orders);
}