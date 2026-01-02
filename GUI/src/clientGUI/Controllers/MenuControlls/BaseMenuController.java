package clientGUI.Controllers.MenuControlls; // Defining the package for GUI menu controllers

import client.ChatClient; // Importing the ChatClient class for communication
import common.ChatIF; // Importing the ChatIF interface for UI consistency

/**
 * BaseMenuController is an abstract class that provides common session data 
 * and client handling for all menu screens in the application.
 */
public abstract class BaseMenuController implements ChatIF { // Defining the abstract class and implementing ChatIF

    // Protected reference to the client to allow subclasses to send messages
    protected ChatClient client;
    
    // Protected field to store the type of user (e.g., "Representative", "Customer")
    protected String userType;
    
    // Protected field to store the unique database ID of the user
    protected int userId; 

    // --- Public Getters ---

    /**
     * Returns the current ChatClient instance.
     */
    public ChatClient getClient() { // Start of getClient method
        return client; // Returning the client reference
    } // End of getClient method

    /**
     * Returns the user type string.
     */
    public String getUserType() { // Start of getUserType method
        return userType; // Returning the userType value
    } // End of getUserType method

    /**
     * Returns the unique user identifier.
     */
    public int getUserId() { // Start of getUserId method
        return userId; // Returning the userId value
    } // End of getUserId method

    /**
     * Updates session-specific data and applies role-based logic.
     */
    public void setSessionData(String userType, int userId) { // Start of setSessionData method
        // Assign the provided user type to the instance variable
        this.userType = userType;
        
        // Assign the provided user ID to the instance variable
        this.userId = userId;

        // Guard Clause: If userType is null, stop processing to avoid NullPointerException
        if (userType == null) { // Checking if userType is null
            return; // Exiting the method immediately
        } // End of null check

        // Refactored to use switch for better readability as requested
        switch (userType) { // Start of switch block on userType string
            
            case "Representative": // Case for staff members
                this.userId = -1; // Setting ID to -1 to represent administrative/staff status
                break; // Exiting switch after matching "Representative"
                
            default: // Default case for other user types (e.g., Subscriber)
                // No specific override needed, keeping the assigned userId
                break; // Exiting switch
        } // End of switch block
        
    } // End of setSessionData method

    /**
     * Sets the client and triggers the session logic.
     */
    public void setClient(ChatClient client, String userType, int userId) { // Start of setClient method
        // Initializing the client reference
        this.client = client;
        
        // Using setSessionData to apply business rules (like the Representative ID override)
        setSessionData(userType, userId);

        // Verification: If client exists, link this controller as the active UI
        if (this.client != null) { // Checking if client is not null
            this.client.setUI(this); // Registering this controller as the UI in the client
            onClientReady(); // Triggering the lifecycle method for specific screen setup
        } // End of client check
        
    } // End of setClient method

    /**
     * Placeholder method meant to be overridden by subclasses for screen-specific logic.
     */
    public void onClientReady() { // Start of onClientReady method
        // Default implementation does nothing; subclasses will implement their own logic
    } // End of onClientReady method

    /**
     * Implementation of ChatIF display method.
     */
    @Override // Overriding the method from ChatIF interface
    public void display(Object message) { // Start of display method
        // Default implementation for receiving messages from the client
    } // End of display method
    
} // End of BaseMenuController class