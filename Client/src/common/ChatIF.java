package common;

/**
 * The ChatIF interface defines the communication contract for the Client-side 
 * User Interface (UI).
 * Any class that acts as a UI layer in this OCSF-based application must 
 * implement this interface to receive data asynchronously from the server.
 * * This interface is a key part of the 'Observer' or 'Listener' design pattern, 
 * allowing the {@link client.ChatClient} to forward server responses without 
 * needing to know which specific JavaFX Controller is currently active.
 * * @author Software Engineering Student
 * @version 1.0
 */
public interface ChatIF {
    
    /**
     * This method is invoked to display or process data received from the server.
     * * Implementation Note:
     * In a JavaFX environment, since the server message arrives on a background thread, 
     * any UI updates inside the implementation of this method should be wrapped in 
     * {@code Platform.runLater()}.
     *
     * @param message The message object sent from the server. 
     * Using the generic {@link Object} type provides maximum flexibility, 
     * allowing the server to send Strings, ArrayLists, or custom DTOs 
     * like 'Reservation' or 'ServiceResponse'.
     */
    void display(Object message);
}