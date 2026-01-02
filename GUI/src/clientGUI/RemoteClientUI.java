package clientGUI; // Defining the package location for the client-side UI launcher

import client.ChatClient; // Importing the ChatClient class for network communication
import clientGUI.Controllers.RemoteLoginController; // Importing the controller for the landing screen
import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the base controller for the unified pipe
import javafx.application.Application; // Importing the core JavaFX Application class
import javafx.fxml.FXMLLoader; // Importing the loader to process FXML layout files
import javafx.scene.Parent; // Importing the root node class for the scene graph
import javafx.scene.Scene; // Importing the container for all visual content in a stage
import javafx.stage.Stage; // Importing the primary window container class

/**
 * Main entry point for the Bistro Remote Access client application.
 * This class initializes the JavaFX environment and the OCSF network connection.
 */
public class RemoteClientUI extends Application { // Start of RemoteClientUI class definition
    
    // Field to hold the reference to the active network client instance
    private ChatClient client; 

    /**
     * The standard main method which serves as the JVM entry point.
     */
    public static void main(String[] args) { // Start of main method
        // Launching the JavaFX application lifecycle
        launch(args); // Internal JavaFX call to setup the environment
    } // End of main method

    /**
     * The start method is called by JavaFX after the system is ready to render the UI.
     */
    @Override // Overriding the start method from the Application class
    public void start(Stage primaryStage) throws Exception { // Start of start method
        
        // --- STEP 1: UI LOADING AND RENDERING ---
        
        // Initializing the FXML Loader with the path to the primary login frame
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmlFiles/RemoteLoginFrame.fxml")); // Setting FXML path
        
        // Loading the FXML file and generating the visual root node
        Parent root = loader.load(); // Executing the load and building the UI graph
        
        // Retrieving the controller instance created by the FXMLLoader for the login screen
        RemoteLoginController controller = loader.getController(); // Accessing the controller object

        // Initializing the primary scene with the loaded root node
        Scene scene = new Scene(root); // Creating the scene object
        
        // Configuring the primary window (Stage) properties
        primaryStage.setTitle("Bistro - Remote Access Portal"); // Setting the window title text
        primaryStage.setResizable(false); // Disabling window resizing for layout consistency
        primaryStage.setScene(scene); // Assigning the initialized scene to the stage
        
        // Rendering the window to the user
        primaryStage.show(); // Displaying the window on the screen

        // --- STEP 2: NETWORK INITIALIZATION AND DATA INJECTION ---
        
        try { // Start of try block for network setup and initial configuration
            
            // Initializing the ChatClient to connect to the server on localhost at port 5555
            client = new ChatClient("localhost", 5555, controller); // Creating the OCSF client
            
            /**
             * The Unified Controller Pipe:
             * This is the initial entry point. Since the user hasn't logged in yet:
             * 1. We inject the newly created client.
             * 2. userType is set to null (No identity yet).
             * 3. userId is set to 0 (No database record yet).
             */
            controller.setClient(client, null, 0); // Injecting session data into the first controller
            
            // Informing the user via the UI log that the connection was successful
            controller.appendLog("Connected to server successfully."); // Logging success message
            
        } catch (Exception e) { // Start of catch block for connection or initialization failures
            
            // Guard Clause: Check if the controller exists before attempting to log the error
            if (controller != null) { // Null check for the UI controller
                // Inform the user via the UI log that the system is in offline mode
                controller.appendLog("Status: Offline - Connection Failed."); // Logging failure message
            } // End of controller null check
            
            // Printing the technical stack trace to the system console for debugging
            e.printStackTrace(); // Outputting technical error details
            
        } // End of try-catch block
        
    } // End of start method
    
} // End of RemoteClientUI class definition