package clientGUI.Controllers.MenuControlls;

import client.ChatClient;

/**
 * BaseMenuController is an abstract foundation class for all menu-related controllers 
 * in the system (e.g., Subscriber Menu, Guest Menu, Reservation screens).
 * * Its primary purpose is to maintain a persistent session state as the user 
 * navigates between different functional frames.
 * * By using an abstract base class, we ensure that every specialized menu controller 
 * has standardized access to the network client and the current user's identification.
 * * @author Software Engineering Student
 * @version 1.0
 */
public abstract class BaseMenuController {

    /** * The persistent OCSF network client instance. 
     * Used for all outbound server requests from the various menu modules.
     */
    protected ChatClient client;

    /** * Categorizes the current user session (e.g., "Subscriber" or "Occasional").
     * This string helps specialized controllers determine which features to enable or disable.
     */
    protected String userType;

    /** * The unique database primary key for the currently logged-in user.
     * Essential for linking new reservations or orders to the correct customer record.
     */
    protected int userId; 

    /**
     * Dependency Injection Gateway:
     * This method is used by navigation engines (like {@link clientGUI.Controllers.ICustomerActions}) 
     * to 'hand off' the active session data from one scene to the next.
     *
     * @param client   The active {@link client.ChatClient} socket connection.
     * @param userType The security context/type of the user.
     * @param userId   The internal database ID of the user.
     */
    public void setClient(ChatClient client, String userType, int userId) {
        this.client = client;
        this.userType = userType;
        this.userId = userId;
    }
}