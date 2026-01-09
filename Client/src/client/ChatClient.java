package client;

import java.io.IOException;
import common.ChatIF;
import ocsf.client.AbstractClient;

/**
 * The ChatClient class serves as the core communication bridge on the client side,
 * implementing the logic defined by the OCSF (Object Client-Server Framework).
 * * It manages the physical connection to the restaurant server, handles outgoing 
 * requests from the user interface, and processes incoming data from the server.
 * * Key Roles:
 * 1. Network Interface: Wraps the low-level socket communication.
 * 2. Message Dispatcher: Routes server responses to the currently active UI controller.
 * 3. Lifecycle Manager: Opens and closes the connection to the host.
 * * @author Software Engineering Student
 * @see ocsf.client.AbstractClient
 */
public class ChatClient extends AbstractClient {

    /**
     * A reference to the UI layer (GUI or Console).
     * By using the ChatIF interface, the client remains decoupled from specific 
     * JavaFX Controllers, allowing it to interact with any UI that implements 'display()'.
     */
    private ChatIF clientUI;


    /**
     * Constructs a new ChatClient and immediately attempts to establish a socket connection.
     * * @param host     The server's IP address or hostname (e.g., "localhost" or "127.0.0.1").
     * @param port     The dedicated TCP port number where the server is listening.
     * @param clientUI The current UI instance assigned to handle data visualization.
     *
     * @throws IOException If the host is unreachable or the connection is refused.
     */
    public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
        // Initializes the host and port within the OCSF AbstractClient framework
        super(host, port);  
        this.clientUI = clientUI;
        
        // Triggers the low-level handshake to open the socket stream
        openConnection();   
    }
    
    /**
     * Updates the UI reference dynamically.
     * This is critical in multi-scene JavaFX applications where the active 
     * controller changes as the user navigates through different frames.
     *
     * @param clientUI The new UI instance (e.g., NewReservationController, LoginController).
     */
    public void setUI(ChatIF clientUI) {
        this.clientUI = clientUI;
    }

    /**
     * HOOK METHOD: Automatically triggered by the OCSF framework thread whenever
     * a message is received from the server.
     * * Important: Since this method runs on a background 'Client Reader' thread,
     * any JavaFX UI updates triggered by this call MUST be wrapped in Platform.runLater().
     *
     * @param msg The incoming object sent by the Server (can be String, ArrayList, or custom DTOs).
     */
    @Override
    public void handleMessageFromServer(Object msg) {
        // Dispatch the received message directly to the UI's display logic
        clientUI.display(msg);  
        
        
  
        
    }


    /**
     * Logic for sending data from the Client UI to the remote Server.
     * This method acts as the primary outbound gateway for user actions.
     *
     * @param message The data object to be sent. For the restaurant system, 
     * this is typically an ArrayList containing a Command and Data.
     */
    public void handleMessageFromClientUI(Object message) {
        try {
            // Physical transmission of the serialized object via the OCSF output stream
            sendToServer(message);  
        } catch (IOException e) {
            // Error handling for network drops or transmission timeouts
            clientUI.display("Connection Error: Unable to reach the server. Terminating session.");
            
            // Cleanly close resources on failure
            quit();
        }
    }


    /**
     * Gracefully terminates the client application.
     * It ensures the socket is closed correctly to prevent orphaned connections 
     * on the server side before shutting down the JVM.
     */
    public void quit() {
        try {
            // Signal OCSF to shut down the input/output streams and the socket
            closeConnection();
        } catch (IOException e) {
            // Non-critical errors during shutdown are logged but ignored
        }
        
        // Complete termination of the Java Virtual Machine
        System.exit(0);
    }
    
    
    
}