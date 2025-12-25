package clientGUI.Controllers.MenuControlls;

import client.ChatClient;
import common.ChatIF;


public abstract class BaseMenuController implements ChatIF { // הוספנו ChatIF כאן

    protected ChatClient client;
    protected String userType;
    protected int userId; 

    public void setClient(ChatClient client, String userType, int userId) {
        this.client = client;
        this.userType = userType;
        this.userId = userId;

        if (this.client != null) {
            this.client.setUI(this); 
            
            onClientReady();
        }
    }

    /**
     * Hook Method automatically triggered by the BaseMenuController.
     * * WHEN TO USE: 
     * Use this method whenever you need to perform server-side operations 
     * (e.g., fetching initial data) as soon as the screen loads, 
     * without waiting for a specific user button click.
     * * WHY USE THIS INSTEAD OF initialize()?
     * In the JavaFX lifecycle, initialize() runs before the dependency injection occurs. 
     * By using onClientReady(), we are 100% certain that the 'client', 'userType', 
     * and 'userId' objects have been successfully injected and are not null. 
     * This makes it the only safe place to initiate network communication during screen startup.
     */
    @Override
    protected void onClientReady() {
        // Specific screen logic goes here (e.g., client.handleMessageFromClientUI(...))
    }

    /**
     * Default implementation of display to satisfy ChatIF.
     * Subclasses can override this to handle specific server messages.
     */
    @Override
    public void display(Object message) {
        // Default: Do nothing or print to console for debug
    }
}