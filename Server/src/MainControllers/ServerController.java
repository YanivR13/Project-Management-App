package MainControllers; // Define the package for main system controllers

import java.io.IOException; // Import for handling network input/output errors
import java.sql.SQLException; // Import for handling database-related exceptions
import java.time.LocalDate; // Import for modern date management

import serverLogic.managmentLogic.CreateSubscriberHandler;
import serverLogic.managmentLogic.DeleteSpecialHoursHandler;
import serverLogic.managmentLogic.GenerateSubReportsHandler;
import serverLogic.managmentLogic.GenerateTimeReportsHandler;
import serverLogic.managmentLogic.UpdateHoursHandler; // Import handler for regular hours updates
import serverLogic.managmentLogic.UpdateSpecialHoursHandler; // Import handler for special hours updates
import serverLogic.menuLogic.*; // Import all menu-related logic handlers
import serverLogic.serverRestaurant.RestaurantManager; // Import the RAM-based restaurant manager
import serverLogic.terminal.JoinWaitingListHandler;

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

import serverLogic.cardReader.CardReaderHandler;

/**
 * The ServerController class is the central communication hub for the Bistro Server.
 * It manages network lifecycle, database connectivity, and command routing.
 */
public class ServerController extends AbstractServer { 

    private ServerIF serverUI; 
    
    private static ServerController serverInstance;

    public ServerController(int port, ServerIF serverUI) { 
        super(port); 
        this.serverUI = serverUI;
        serverInstance = this;
    } 

 @Override 
     protected void serverStarted() { 
         serverUI.appendLog("Server started."); 
         try { 
             DBController dbController = DBController.getInstance(); 
             dbController.connectToDB(); 
             serverUI.appendLog("Connected to database successfully."); 

             if (RestaurantManager.initialize(1)) { 
                 serverUI.appendLog("Restaurant data initialized in RAM (Inventory & Hours)."); 
             } else { 
                 serverUI.appendLog("Warning: Restaurant data could not be loaded. Check if DB is empty."); 
             } 
            
             // Automation Part
             startAutomationThread();
             serverUI.appendLog("Automation Engine: ACTIVE (Checking late arrivals & stay limits)");
             // ---------------------------

         } catch (SQLException e) { 
             serverUI.appendLog("Failed to connect to database: " + e.getMessage()); 
             e.printStackTrace(); 
         } 
     } 

    /**
     * Function helper to start thread
     */
    private void startAutomationThread() {
        Thread automationThread = new Thread(() -> {
            while (true) {
                try {
                    // Waiting 1 minute between every check
                    Thread.sleep(60000); 

                    // Cancel of late reservation and waiting list trigger 
                    UpdateManagementDBController.cancelLateReservations();
                    
                    // >120?
                    UpdateManagementDBController.checkStayDurationAlerts();
                    
                } catch (InterruptedException e) {
                    serverUI.appendLog("Automation thread stopped.");
                    break; 
                } catch (Exception e) {
                    serverUI.appendLog("Automation Error: " + e.getMessage());
                }
            }
        });
        automationThread.setDaemon(true); // Ensures thread stops when server is closed 
        automationThread.start();
    } 

    @Override 
    protected void serverStopped() { 
        serverUI.appendLog("Server has stopped."); 
        try { 
            DBController.getInstance().closeConnection(); 
            serverUI.appendLog("Database connection closed."); 
        } catch (SQLException e) { 
            serverUI.appendLog("Error closing database: " + e.getMessage()); 
            e.printStackTrace(); 
        } 
    } 

    @Override 
    protected void clientConnected(ConnectionToClient client) { 
        String ip = client.getInetAddress().getHostAddress(); 
        serverUI.appendLog("Client connected: IP = " + ip); 
    } 

    @Override 
    protected void clientDisconnected(ConnectionToClient client) { 
        serverUI.appendLog("Client disconnected: " + client); 
    }
    
    public static void log(String msg) {
        if (serverInstance != null && serverInstance.serverUI != null) {
            serverInstance.serverUI.appendLog(msg);
        }
    }

    @Override 
    protected void handleMessageFromClient(Object msg, ConnectionToClient client) { 
                
        serverUI.appendLog("Message received: " + msg + " from " + client); 

        if (msg instanceof ArrayList) { 
            
            @SuppressWarnings("unchecked") 
            ArrayList<Object> messageList = (ArrayList<Object>) msg; 
            
            if (messageList.isEmpty()) { 
                serverUI.appendLog("Warning: Received empty ArrayList from " + client); 
                return; 
            } 

            String command = (String) messageList.get(0); 

            switch (command) { 
            
            	case "CARD_READER_LOGIN":
            	case "CARD_READER_GET_CODES":
            	case "CARD_READER_VERIFY_CODE":
            		new CardReaderHandler().handle((ArrayList<Object>)messageList, client);
            		break;

            		
                case "LOGIN_SUBSCRIBER": 
                    new SubscriberLoginHandler().handle(messageList, client); 
                    break; 

                    
                case "LOGIN_OCCASIONAL": 
                    new OccasionalLoginHandler().handle(messageList, client); 
                    break; 

                    
                case "RESET_OCCASIONAL_USERNAME": 
                    new OccasionalRestUsernameHandler().handle(messageList, client); 
                    break; 

                case "REGISTER_OCCASIONAL": 
                    new OccasionalRegistrationHandler().handle(messageList, client); 
                    break; 
                                        
                case "GET_ALL_TABLES":
                    List<common.Table> allTables = dbLogic.restaurantDB.TableDBController.getAllTables();
                    try {
                        client.sendToClient(new ArrayList<>(allTables));
                    } catch (IOException e) {
                        serverUI.appendLog("Error sending tables list: " + e.getMessage());
                    }
                    break;
                    
                case "UPDATE_TABLE_CAPACITY":
                    try {
                        int tableIdToUpdate = (int) messageList.get(1);
                        int newCapacity = (int) messageList.get(2);
                        boolean isUpdated = dbLogic.restaurantDB.TableDBController.updateTableCapacity(tableIdToUpdate, newCapacity);
                        
                        if (isUpdated) {
                            client.sendToClient(new ServiceResponse(ServiceStatus.UPDATE_SUCCESS, "Table capacity updated successfully"));
                        } else {
                            client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "Failed to update capacity in DB"));
                        }
                    } catch (Exception e) {
                        serverUI.appendLog("Error in UPDATE_TABLE_CAPACITY: " + e.getMessage());
                    }
                    break;
                    
                case "ADD_NEW_TABLE":
                    try {
                        int cap = (int) messageList.get(1);
                        boolean success = dbLogic.restaurantDB.TableDBController.addNewTable(cap);
                        if (success) {
                            client.sendToClient(new ServiceResponse(ServiceStatus.UPDATE_SUCCESS, "Table added successfully"));
                        } else {
                            client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "Database refused to add table"));
                        }
                    } catch (Exception e) {
                        serverUI.appendLog("Server Error in ADD_NEW_TABLE: " + e.getMessage());
                    }
                    break;

                case "DELETE_TABLE":
                    try {
                        int id = (int) messageList.get(1);
                        boolean success = dbLogic.restaurantDB.TableDBController.deleteTable(id);
                        if (success) {
                            client.sendToClient(new ServiceResponse(ServiceStatus.UPDATE_SUCCESS, "Table deleted"));
                        } else {
                            client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "SQL Error during deletion"));
                        }
                    } catch (Exception e) {
                        serverUI.appendLog("Server Error in DELETE_TABLE: " + e.getMessage());
                    }
                    break;

                
                case "PROCESS_PAYMENT": 
                    Bill billToProcess = (Bill) messageList.get(1); 
                    boolean isSuccess = dbLogic.restaurantDB.PaymentController.finalizePayment(billToProcess); 
                    try { 
                        if (isSuccess) { 
                            client.sendToClient("PAYMENT_SUCCESS"); 
                            serverUI.appendLog("Payment processed successfully for Bill ID: " + billToProcess.getBillId()); 
                        } else { 
                            client.sendToClient("PAYMENT_FAILED"); 
                            serverUI.appendLog("Failed to process payment for Bill ID: " + billToProcess.getBillId()); 
                        } 
                    } catch (IOException e) { e.printStackTrace(); } 
                    break; 
                    
                case "GET_VISIT_BY_CODE": 
                    try { 
                        long code = (long) messageList.get(1); 
                        ArrayList<Object> responseData = dbLogic.restaurantDB.PaymentController.getVisitWithSubscriberStatus(code); 
                        
                        if (responseData != null) { 
                            client.sendToClient(responseData); 
                        } else { 
                            client.sendToClient("VISIT_NOT_FOUND"); 
                        } 
                    } catch (Exception e) { 
                        serverUI.appendLog("Error in GET_VISIT_BY_CODE: " + e.getMessage()); 
                    } 
                    break;
                    
                case "CANCEL_RESERVATION": 
                    long codeToCancel = (long) messageList.get(1); 
                    boolean isCanceled = dbLogic.restaurantDB.viewReservationController.cancelReservationByCode(codeToCancel); 
                    try { 
                        if (isCanceled) { 
                            client.sendToClient("CANCEL_SUCCESS"); 
                            serverUI.appendLog("Successfully canceled reservation #" + codeToCancel); 
                        } else { 
                            client.sendToClient("CANCEL_FAILED"); 
                            serverUI.appendLog("Failed to cancel reservation #" + codeToCancel); 
                        } 
                    } catch (IOException e) { 
                        serverUI.appendLog("Error sending cancel response to client: " + e.getMessage()); 
                    } 
                    break; 
                    
                case "GET_ACTIVE_RESERVATIONS": 
                    int userId = (int) messageList.get(1); 
                    List<common.Reservation> activeReservations = dbLogic.restaurantDB.viewReservationController.getActiveReservationsByUserId(userId); 
                    try { 
                        client.sendToClient(activeReservations); 
                        serverUI.appendLog("Sent " + activeReservations.size() + " active reservations to user " + userId); 
                    } catch (IOException e) { 
                        serverUI.appendLog("Error sending reservations to client: " + e.getMessage()); 
                    } 
                    break; 
                    
                case "CANCEL_WAITING_LIST": 
                    try { 
                        int uId = (int) messageList.get(1); 
                        int result = dbLogic.restaurantDB.CancelWaitingListController.cancelWaitingEntry(uId); 
                        if (result == 1) { 
                            client.sendToClient("CANCEL_WAITING_SUCCESS"); 
                        } else if (result == 0) { 
                            client.sendToClient("NOT_ON_WAITING_LIST"); 
                        } else { 
                            client.sendToClient("SERVER_ERROR"); 
                        } 
                    } catch (Exception e) { 
                        System.err.println("Critical error in CANCEL_WAITING_LIST: " + e.getMessage()); 
                        try { client.sendToClient("SERVER_ERROR"); } catch (IOException ioException) { ioException.printStackTrace(); } 
                    } 
                    break; 
                    
                case "CANCEL_WAITING_LIST_BY_CODE": 
                    try { 
                        // Extract the code as a long instead of userId as an int 
                        long thecodeToCancel = (long) messageList.get(1); 
                        
                        // Pass the long confirmation code to the updated DB controller
                        int result = dbLogic.restaurantDB.CancelWaitingListController.cancelWaitingEntry(thecodeToCancel); 
                        
                        if (result == 1) { 
                            client.sendToClient("CANCEL_WAITING_SUCCESS"); 
                        } else if (result == 0) { 
                            client.sendToClient("NOT_ON_WAITING_LIST"); 
                        } else { 
                            client.sendToClient("SERVER_ERROR"); 
                        } 
                    } catch (Exception e) { 
                        serverUI.appendLog("Critical error in CANCEL_WAITING_LIST: " + e.getMessage()); 
                        try { client.sendToClient("SERVER_ERROR"); } catch (IOException io) { io.printStackTrace(); } 
                    } 
                    break;
                    
                case "GET_RESERVATIONS_HISTORY": 
                    // Server command: retrieves reservation history for a specific subscriber
                    // Routed to the ReservationHistoryHandler for processing
                    new ReservationHistoryHandler().handle(messageList, client); 
                    break;

                    
                case "UPDATE_SUBSCRIBER_DETAILS": 
                    // Server command: updates personal details of a specific subscriber
                    // Routed to the EditDetailsHandler for processing
                    new EditDetailsHandler().handle(messageList, client); 
                    break;

                    
                case "JOIN_WAITING_LIST":
                    // Server command: adds a subscriber to the restaurant waiting list
                    // Routed to the JoinWaitingListHandler for processing
                    new JoinWaitingListHandler().handle(messageList, client);
                    break;

                
                case "GET_TIME_REPORTS": {
                    new GenerateTimeReportsHandler().handle(messageList, client);
                    break;
                }
                
                case "GET_SUBSCRIBER_REPORTS": 
                    new GenerateSubReportsHandler().handle(messageList, client);
                    break;
                    
                case "GET_RESTAURANT_WORKTIMES": 
                    try { 
                        Restaurant rest = RestaurantManager.getInstance(); 
                        if (rest != null) { 
                            client.sendToClient(rest); 
                            serverUI.appendLog("Successfully sent restaurant worktimes to " + client); 
                        } else { 
                            serverUI.appendLog("Error: Restaurant data is null in RAM!"); 
                            client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "Server Error: Restaurant data not found.")); 
                        } 
                    } catch (IOException e) { 
                        serverUI.appendLog("Failed to transmit restaurant data: " + e.getMessage()); 
                    } 
                    break; 
                    
                case "CREATE_RESERVATION": 
                    serverUI.appendLog("Routing to CreateOrderHandler for Client: " + client); 
                    new CreateOrderHandler().handle(messageList, client); 
                    break; 
                    
                case "UPDATE_REGULAR_HOURS": 
                    int restaurantId = (int) messageList.get(1); 
                    @SuppressWarnings("unchecked")
                    Map<String, TimeRange> newHoursMap = (Map<String, TimeRange>) messageList.get(2); 
                    new UpdateHoursHandler().handle(restaurantId, newHoursMap, client); 
                    break; 
                    
                case "UPDATE_SPECIAL_HOURS": 
                    int restId = (int) messageList.get(1); 
                    LocalDate sDate = (LocalDate) messageList.get(2); 
                    String sOpen = (String) messageList.get(3); 
                    String sClose = (String) messageList.get(4); 
                    new UpdateSpecialHoursHandler().handle(restId, sDate, sOpen, sClose, client); 
                    break; 
                    
                case "DELETE_ALL_SPECIAL_HOURS": 
                    int targetRestId = (int) messageList.get(1); 
                    new DeleteSpecialHoursHandler().handle(targetRestId, client); 
                    break; 
                    
                case "CREATE_NEW_SUBSCRIBER": 
                    String phone = (String) messageList.get(1); 
                    String email = (String) messageList.get(2);
                    new CreateSubscriberHandler().handle(phone, email, client); 
                    break; 

                case "PROCESS_TERMINAL_ARRIVAL":
                	try {
                        long code = (Long) messageList.get(1);
                        String result = VisitController.processTerminalArrival(code);
                        client.sendToClient(result);
                    } catch (Exception e) {
                        serverUI.appendLog("Critical error in PROCESS_TERMINAL_ARRIVAL: " + e.getMessage());
                        try { client.sendToClient("DATABASE_ERROR"); } catch (IOException io) { io.printStackTrace(); }
                    }
                	break;
                	
                case "CHECK_STATUS_UPDATE":
                    long confirmationCode = (long) messageList.get(1);
                    String statusResult = VisitController.checkCurrentStatus(confirmationCode);
                    try {
                        client.sendToClient(statusResult);
                    } catch (IOException e) {
                        serverUI.appendLog("Error sending status update: " + e.getMessage());
                    }
                    break;

                default: 
                    serverUI.appendLog("Unknown command received: " + command); 
                    try { 
                        client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "ERROR: Unknown Command '" + command + "'")); 
                    } catch (Exception e) { serverUI.appendLog("Failed to send Error message: " + e.getMessage()); } 
                    break; 
            } 
        } else { 
            serverUI.appendLog("Received unexpected message type: " + msg.getClass().getSimpleName()); 
            try { 
                client.sendToClient(new ServiceResponse(ServiceStatus.INTERNAL_ERROR, "ERROR: Invalid Protocol (Expected ArrayList)")); 
            } catch (Exception e) { serverUI.appendLog("Error notifying client: " + e.getMessage()); } 
        } 
    } 

}
