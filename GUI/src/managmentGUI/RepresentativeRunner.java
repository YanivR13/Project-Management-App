package managmentGUI; // Declaring the package where the management GUI runner resides

import client.ChatClient; // Importing the OCSF client class for network communication
import javafx.application.Application; // Importing the base JavaFX Application class
import javafx.fxml.FXMLLoader; // Importing the loader to instantiate FXML layout files
import javafx.scene.Parent; // Importing the root node class for the scene graph
import javafx.scene.Scene; // Importing the container class for window content
import javafx.stage.Stage; // Importing the primary window container class

/**
 * Main entry point for the Staff/Representative dashboard.
 * This class handles the initial server connection and UI loading for restaurant management.
 */
public class RepresentativeRunner extends Application { // Start of the RepresentativeRunner class definition

    /**
     * The start method initializes the primary stage and sets up the communication pipe.
     * @param primaryStage The main window provided by the JavaFX runtime.
     */
    @Override // Overriding the standard start method from the Application class
    public void start(Stage primaryStage) { // Start of the start method
        
        try { // Beginning of the try-catch block to handle startup and connection issues
            
            // --- STEP 1: NETWORK INITIALIZATION ---
            
            // Creating a temporary network client pointing to localhost on port 5555
            // This is required by the BaseMenuController infrastructure to send messages
            ChatClient client = new ChatClient("localhost", 5555, null); // Initializing client

            // --- STEP 2: UI LOADING ---
            
            // Initializing the FXML Loader with the path to the management dashboard layout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("RepresentativeDashboard.fxml")); // Setting FXML path
            
            // Loading the FXML file and generating the visual root node for the scene graph
            Parent root = loader.load(); // Executing the load operation

            // --- STEP 3: DATA INJECTION (THE PIPE) ---
            
            // Accessing the controller instance created by the FXMLLoader for the dashboard
            RepresentativeDashboardController controller = loader.getController(); // Retrieving controller
            
            /**
             * Uniform Injection Logic:
             * We set the user type as "Representative". 
             * According to BaseMenuController logic, this will automatically set userId to -1.
             */
            controller.setClient(client, "Representative", 0); // Injecting session data into the controller

            // --- STEP 4: STAGE DISPLAY ---
            
            // Configuring the primary window title bar
            primaryStage.setTitle("Bistro System - Representative Mode"); // Setting window title
            
            // Initializing the scene with the loaded visual root and assigning it to the stage
            primaryStage.setScene(new Scene(root)); // Setting the scene content
            
            // Rendering the stage (window) to the user
            primaryStage.show(); // Displaying the window

        } catch (Exception e) { // Handling any technical failures during startup or connection
            
            // Logging a human-readable error header to the standard error stream
            System.err.println("Error launching Representative Dashboard:"); // Error logging
            
            // Printing the technical stack trace for debugging and troubleshooting
            e.printStackTrace(); // Outputting technical details
            
        } // End of the try-catch block
        
    } // End of the start method

    /**
     * Standard JVM entry point.
     * @param args Command line arguments.
     */
    public static void main(String[] args) { // Start of the main method
        // Launching the JavaFX application thread
        launch(args); // Internal call to trigger the start() method
    } // End of the main method
    
} // End of the RepresentativeRunner class definition