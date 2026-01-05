package MainControllers; // Define the package for main system controllers

import java.io.IOException; // Import for handling network input/output errors
import java.sql.SQLException; // Import for handling database-related exceptions
import java.time.LocalDate; // Import for modern date management

import serverLogic.managmentLogic.CreateSubscriberHandler;
import serverLogic.managmentLogic.DeleteSpecialHoursHandler;
import serverLogic.managmentLogic.UpdateHoursHandler; // Import handler for regular hours updates
import serverLogic.managmentLogic.UpdateSpecialHoursHandler; // Import handler for special hours updates
import serverLogic.menuLogic.*; // Import all menu-related logic handlers
import serverLogic.serverRestaurant.RestaurantManager; // Import the RAM-based restaurant manager
import java.util.ArrayList; // Import for dynamic list structures
import java.util.List; // Import for generic list interfaces
import java.util.Map; // Import for key-value pair mapping

import common.Bill; // Import the Bill entity
import common.Restaurant; // Import the Restaurant entity
import common.ServerIF; // Import the interface for server-side UI logging
import common.ServiceResponse; // Import the standard response wrapper
import common.ServiceResponse.ServiceStatus;
import common.TimeRange; // Import the time range domain object
import ocsf.server.AbstractServer; // Import OCSF base server class
import ocsf.server.ConnectionToClient; // Import OCSF client connection handle
import serverLogic.serverLogin.OccasionalLoginHandler; // Import guest login logic
import serverLogic.serverLogin.OccasionalRegistrationHandler; // Import guest registration logic
import serverLogic.serverLogin.OccasionalRestUsernameHandler; // Import guest username reset logic
import serverLogic.serverLogin.SubscriberLoginHandler; // Import member login logic
import dbLogic.managmentDB.*;
import dbLogic.restaurantDB.*;
import dbLogic.systemLogin.*;
import dbLogic.restaurantDB.*;
/**
 * The ServerController class is the central communication hub for the Bistro Server.
 * It manages network lifecycle, database connectivity, and command routing.
 */
public class ServerController extends AbstractServer { // Class start inheriting from AbstractServer

    // Reference to the server's GUI interface for logging events
    private ServerIF serverUI; // Interface for status reporting

    /**
     * Constructor to initialize the server on a specific port with a UI reference.
     */
    public ServerController(int port, ServerIF serverUI) { // Constructor start
        super(port); // Call the parent OCSF constructor with the port
        this.serverUI = serverUI; // Assign the UI reference
    } // Constructor end

    /**
     * Triggered when the server starts listening for client connections.
     */
    @Override // Override OCSF hook method
    protected void serverStarted() { // Start of serverStarted method
        serverUI.appendLog("Server started."); // Log basic start message

        try { // Start of resource initialization block
            
            // Step 1: Initialize the Singleton Database Controller and connect
            DBController dbController = DBController.getInstance(); // Get instance
            dbController.connectToDB(); // Attempt SQL connection
            serverUI.appendLog("Connected to database successfully."); // Log SQL success

            // Step 2: Pre-load restaurant data (Inventory/Hours) into RAM
            if (RestaurantManager.initialize(1)) { // Attempt loading restaurant #1
                serverUI.appendLog("Restaurant data initialized in RAM (Inventory & Hours)."); // Log RAM success
            } else { // If initialization fails
                serverUI.appendLog("Warning: Restaurant data could not be loaded. Check if DB is empty."); // Log warning
            } // End of initialization check

        } catch (SQLException e) { // Catch database connection errors
            serverUI.appendLog("Failed to connect to database: " + e.getMessage()); // Log error
            e.printStackTrace(); // Print stack trace for debugging
        } // End of try-catch block
    } // End of serverStarted method

    /**
     * Triggered when the server stops listening. Ensures resources are released.
     */
    @Override // Override OCSF hook method
    protected void serverStopped() { // Start of serverStopped method
        serverUI.appendLog("Server has stopped."); // Log shutdown

        try { // Start of cleanup block
            // Cleanly close the JDBC connection via the Singleton
            DBController.getInstance().closeConnection(); // Close SQL link
            serverUI.appendLog("Database connection closed."); // Log cleanup success
        } catch (SQLException e) { // Catch cleanup errors
            serverUI.appendLog("Error closing database: " + e.getMessage()); // Log error
            e.printStackTrace(); // Print trace
        } // End of try-catch block
    } // End of serverStopped method

    /**
     * Triggered when a new client connects to the server.
     */
    @Override // Override OCSF hook method
    protected void clientConnected(ConnectionToClient client) { // Start method
        String ip = client.getInetAddress().getHostAddress(); // Get client IP
        serverUI.appendLog("Client connected: IP = " + ip); // Log IP address
    } // End method

    /**
     * Triggered when a client session ends.
     */
    @Override // Override OCSF hook method
    protected void clientDisconnected(ConnectionToClient client) { // Start method
        serverUI.appendLog("Client disconnected: " + client); // Log disconnection
    } // End method

    /**
     * The main message dispatcher. Parses incoming protocols and routes them to logic handlers.
     */
    @Override // Override OCSF core communication method
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) { // Start method
                
        // Trace the incoming activity for server monitoring
        serverUI.appendLog("Message received: " + msg + " from " + client); // Log incoming message

        // Protocol Check: The system communicates using ArrayLists
        if (msg instanceof ArrayList) { // Start of protocol validation
            
            @SuppressWarnings("unchecked") // Suppress casting warning for generics
            ArrayList<Object> messageList = (ArrayList<Object>) msg; // Cast the message to a list
            
            // Validation: Ensure the list is not empty
            if (messageList.isEmpty()) { // Start empty check
                serverUI.appendLog("Warning: Received empty ArrayList from " + client); // Log warning
                return; // Exit method
            } // End empty check

            // Command Extraction: The first index is always the string command
            String command = (String) messageList.get(0); // Extract command

            // Command Dispatcher: Using a switch-case to route requests efficiently
            switch (command) { // Start of routing switch

                case "LOGIN_SUBSCRIBER": // Handle member login
                    new SubscriberLoginHandler().handle(messageList, client); // Delegate to handler
                    break; // Exit case

                case "LOGIN_OCCASIONAL": // Handle guest login
                    new OccasionalLoginHandler().handle(messageList, client); // Delegate to handler
                    break; // Exit case

                case "RESET_OCCASIONAL_USERNAME": // Handle guest username update
                    new OccasionalRestUsernameHandler().handle(messageList, client); // Delegate to handler
                    break; // Exit case

                case "REGISTER_OCCASIONAL": // Handle new guest registration
                    new OccasionalRegistrationHandler().handle(messageList, client); // Delegate to handler
                    break; // Exit case
                    
                case "PROCESS_PAYMENT": // Handle bill finalization
                    Bill billToProcess = (Bill) messageList.get(1); // Extract Bill DTO
                    boolean isSuccess = dbLogic.restaurantDB.PaymentController.finalizePayment(billToProcess); // Call controller
                    
                    try { // Start of network response block
                        if (isSuccess) { // If update succeeded
                            client.sendToClient("PAYMENT_SUCCESS"); // Notify client
                            serverUI.appendLog("Payment processed successfully for Bill ID: " + billToProcess.getBillId()); // Log
                        } else { // If update failed
                            client.sendToClient("PAYMENT_FAILED"); // Notify client
                            serverUI.appendLog("Failed to process payment for Bill ID: " + billToProcess.getBillId()); // Log
                        } // End check
                    } catch (IOException e) { e.printStackTrace(); } // Handle I/O error
                    break; // Exit case
                    
                case "GET_VISIT_BY_CODE": // Verify code for payment entry
                    try { // Start handling
                        long code = (long) messageList.get(1); // Extract confirmation code
                        common.Visit visitData = dbLogic.restaurantDB.PaymentController.getVisitDetails(code); // Fetch details
                        
                        if (visitData != null) { // If record exists
                            client.sendToClient(visitData); // Send DTO back to UI
                            serverUI.appendLog("Visit found for code: #" + code); // Log
                        } else { // If no record
                            client.sendToClient("VISIT_NOT_FOUND"); // Send error string
                            serverUI.appendLog("No active visit for code: #" + code); // Log
                        } // End check
                    } catch (IOException e) { // Catch network errors
                        serverUI.appendLog("Network error: " + e.getMessage()); // Log
                    } catch (Exception e) { // Catch logic errors
                        serverUI.appendLog("Error in GET_VISIT_BY_CODE: " + e.getMessage()); // Log
                    } // End try-catch
                    break; // Exit case
                    
                case "CANCEL_RESERVATION": // Handle booking cancellation
                    long codeToCancel = (long) messageList.get(1); // Extract code
                    boolean isCanceled = dbLogic.restaurantDB.viewReservationController.cancelReservationByCode(codeToCancel); // Call DB controller
                    
                    try { // Start response
                        if (isCanceled) { // If DB update worked
                            client.sendToClient("CANCEL_SUCCESS"); // Notify success
                            serverUI.appendLog("Successfully canceled reservation #" + codeToCancel); // Log
                        } else { // If failed
                            client.sendToClient("CANCEL_FAILED"); // Notify failure
                            serverUI.appendLog("Failed to cancel reservation #" + codeToCancel); // Log
                        } // End check
                    } catch (IOException e) { // Catch network errors
                        serverUI.appendLog("Error sending cancel response to client: " + e.getMessage()); // Log
                    } // End try-catch
                    break; // Exit case
                    
                case "GET_ACTIVE_RESERVATIONS": // Fetch current bookings for a user
                    int userId = (int) messageList.get(1); // Extract ID
                    List<common.Reservation> activeReservations = dbLogic.restaurantDB.viewReservationController.getActiveReservationsByUserId(userId); // Query
                    
                    try { // Start response
                        client.sendToClient(activeReservations); // Send list to UI
                        serverUI.appendLog("Sent " + activeReservations.size() + " active reservations to user " + userId); // Log
                    } catch (IOException e) { // Catch network errors
                        serverUI.appendLog("Error sending reservations to client: " + e.getMessage()); // Log
                    } // End try-catch
                    break; // Exit case
                    
                case "CANCEL_WAITING_LIST": // Remove guest from waiting queue
                    try { // Start handling
                        int uId = (int) messageList.get(1); // Extract ID
                        int result = dbLogic.restaurantDB.CancelWaitingListController.cancelWaitingEntry(uId); // Call logic
                        
                        if (result == 1) { // Success status
                            client.sendToClient("CANCEL_WAITING_SUCCESS"); // Notify UI
                        } else if (result == 0) { // Not found status
                            client.sendToClient("NOT_ON_WAITING_LIST"); // Notify UI
                        } else { // Generic error status
                            client.sendToClient("SERVER_ERROR"); // Notify UI
                        } // End check
                    } catch (Exception e) { // Catch logic errors
                        System.err.println("Critical error in CANCEL_WAITING_LIST: " + e.getMessage()); // Log to console
                        try { client.sendToClient("SERVER_ERROR"); } catch (IOException ioException) { ioException.printStackTrace(); } // Inform client
                    } // End try-catch
                    break; // Exit case
                    
                case "GET_RESERVATIONS_HISTORY": // Delegate to historical data handler
                    new ReservationHistoryHandler().handle(messageList, client); // Dispatch
                    break; // Exit case
                    
                case "UPDATE_SUBSCRIBER_DETAILS": // Delegate to profile update handler
                    new EditDetailsHandler().handle(messageList, client); // Dispatch
                    break; // Exit case
                    
                case "GET_RESTAURANT_WORKTIMES": // Fetch RAM-cached operating hours
                    try { // Start handling
                        Restaurant rest = RestaurantManager.getInstance(); // Access RAM instance
                        
                        if (rest != null) { // If data exists
                            client.sendToClient(rest); // Send restaurant object
                            serverUI.appendLog("Successfully sent restaurant worktimes to " + client); // Log
                        } else { // If RAM is empty
                            serverUI.appendLog("Error: Restaurant data is null in RAM!"); // Log error
                            client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "Server Error: Restaurant data not found.")); // Inform client
                        } // End check
                    } catch (IOException e) { // Catch transmission errors
                        serverUI.appendLog("Failed to transmit restaurant data: " + e.getMessage()); // Log
                    } // End try-catch
                    break; // Exit case
                    
                case "CREATE_RESERVATION": // Main booking logic
                    serverUI.appendLog("Routing to CreateOrderHandler for Client: " + client); // Log routing
                    new CreateOrderHandler().handle(messageList, client); // Dispatch to complex logic handler
                    break; // Exit case
                    
                case "UPDATE_REGULAR_HOURS": // Staff action: update standard schedule
                    int restaurantId = (int) messageList.get(1); // Extract target ID
                    Map<String, TimeRange> newHoursMap = (Map<String, TimeRange>) messageList.get(2); // Extract hours map
                    new UpdateHoursHandler().handle(restaurantId, newHoursMap, client); // Dispatch
                    break; // Exit case
                    
                case "UPDATE_SPECIAL_HOURS": // Staff action: update specific date overrides
                    int restId = (int) messageList.get(1); // Extract target ID
                    LocalDate sDate = (LocalDate) messageList.get(2); // Extract override date
                    String sOpen = (String) messageList.get(3); // Extract open time
                    String sClose = (String) messageList.get(4); // Extract close time
                    new UpdateSpecialHoursHandler().handle(restId, sDate, sOpen, sClose, client); // Dispatch
                    break; // Exit case
                    
                case "DELETE_ALL_SPECIAL_HOURS": // Protocol command sent from the Representative Dashboard
                    int targetRestId = (int) messageList.get(1); // Extract the restaurant ID from the incoming ArrayList
                    // Create and invoke the handler to process the deletion logic
                    new DeleteSpecialHoursHandler().handle(targetRestId, client); // Delegate work to the handler class
                    break; // Exit the switch block for this command
                    
                case "CREATE_NEW_SUBSCRIBER": // Command received from RepresentativeDashboardController
                    // Extract phone and email from the ArrayList
                    String phone = (String) messageList.get(1); 
                    String email = (String) messageList.get(2);
                    
                    // Dispatch the request to the new handler (which we will create next)
                    new CreateSubscriberHandler().handle(phone, email, client); 
                    break; // Exit the switch block

                default: // Fallback for unknown protocols
                    serverUI.appendLog("Unknown command received: " + command); // Log error
                    try { // Start error response
                        client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "ERROR: Unknown Command '" + command + "'")); // Inform UI
                    } catch (Exception e) { serverUI.appendLog("Failed to send Error message: " + e.getMessage()); } // Catch fail
                    break; // Exit switch
            } // End of routing switch
        } else { // If message is not an ArrayList
            serverUI.appendLog("Received unexpected message type: " + msg.getClass().getSimpleName()); // Log violation
            try { // Notify client of protocol error
                client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "ERROR: Invalid Protocol (Expected ArrayList)")); // Inform UI
            } catch (Exception e) { serverUI.appendLog("Error notifying client: " + e.getMessage()); } // Catch fail
        } // End of protocol validation
    } // End of handleMessageFromClient method
} // End of ServerController class