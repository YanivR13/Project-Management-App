package managmentGUI; // Defining the package for management-related controllers

import java.io.IOException; // Importing for handling input/output exceptions during FXML loading
import java.time.LocalDate; // Importing for date handling
import java.util.ArrayList; // Importing for dynamic list structures
import java.util.HashMap; // Importing for key-value pair storage
import java.util.Map; // Importing for map interface

import clientGUI.Controllers.MenuControlls.BaseMenuController; // Importing the base menu controller for session data
import common.TimeRange; // Importing the TimeRange domain entity
import common.Visit;
import common.Restaurant;
import common.ServiceResponse; // Importing the server response envelope
import javafx.application.Platform; // Importing for UI thread management
import javafx.collections.FXCollections; // Importing for observable collection utilities
import javafx.collections.ObservableList; // Importing for dynamic list types used in UI components
import javafx.event.ActionEvent; // Importing for handling UI button clicks
import javafx.fxml.FXML; // Importing FXML annotation for field injection
import javafx.fxml.FXMLLoader; // Importing for loading FXML layout files
import javafx.scene.Node; // Importing for generic UI node elements
import javafx.scene.Parent; // Importing for root scene graph elements
import javafx.scene.Scene; // Importing for scene management
import javafx.scene.control.*; // Importing standard JavaFX controls
import javafx.scene.control.Alert.AlertType; // Importing alert categories
import javafx.scene.control.cell.PropertyValueFactory; // Importing for table column mapping
import javafx.scene.layout.AnchorPane; // Importing for layout container management
import javafx.stage.Stage; // Importing for window stage management

/**
 * Controller class for the Representative Dashboard.
 * Manages the dynamic loading of sub-screens within a central pane and handles operational updates.
 */
public class RepresentativeDashboardController extends BaseMenuController { // Class definition start

    // --- 1. Primary FXML Fields ---
    @FXML protected TextArea txtLog; // Console area for system feedback
    private Object currentSubController;
    @FXML protected AnchorPane contentPane; // The central container where sub-screens are injected

    // --- Sub-screen FXML Fields (Injected only when specific FXMLs are loaded) ---
    @FXML private TableView<DayScheduleRow> tableHours; // Table for displaying regular hours
    @FXML private TableColumn<DayScheduleRow, String> colDay; // Column for the day of the week
    @FXML private TableColumn<DayScheduleRow, String> colOpen; // Column for opening time
    @FXML private TableColumn<DayScheduleRow, String> colClose; // Column for closing time
    
    @FXML private DatePicker dpSpecialDate; // Date selector for special hours overrides
    @FXML private ComboBox<String> comboSpecialOpen; // Dropdown for special opening time
    @FXML private ComboBox<String> comboSpecialClose; // Dropdown for special closing time
    
    // --- Fields for CreateNewSubscriber sub-screen ---
    @FXML private TextField txtPhone; // Linked to fx:id="txtPhone" in the new FXML
    @FXML private TextField txtEmail; // Linked to fx:id="txtEmail" in the new FXML

    // Observable list to maintain live data for the hours table
    private ObservableList<DayScheduleRow> scheduleData = FXCollections.observableArrayList(); // List initialization

    // --- 2. Initialization and Infrastructure ---

    @Override // Overriding method from BaseMenuController
    public void onClientReady() { // Start of onClientReady method
        // Check if the inherited client instance is initialized
        if (client != null) { // Start if block
            // Register this dashboard instance as the UI for the client
            client.setUI(this); // Setting UI listener
            
            // Log successful loading and display the session user ID
            appendLog("Representative Dashboard Loaded. System ID: " + userId); // Appending log
            
            // Automatically load the default sub-screen (Regular Hours) upon startup
            showRegularHoursScreen(null); // Initial screen load
        } // End if block
    } // End of onClientReady method
    
    /**
     * Sends a request to the server to fetch the current restaurant operating hours.
     */
    @FXML 
    void showCurrentWorkTimes(ActionEvent event) { 
        ArrayList<Object> message = new ArrayList<>(); 
        message.add("GET_RESTAURANT_WORKTIMES"); 
        appendLog("Requesting current work times from server..."); 
        if (client != null) { 
            client.handleMessageFromClientUI(message); 
        } else { 
            appendLog("Error: Client connection is not initialized."); 
        } 
    }

    /**
     * Utility method to load FXML sub-screens into the central contentPane.
     */
    private void loadSubScreen(String fxmlPath) {
        try {
            contentPane.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

            // 1. טיפול במסך ההזמנות הפעילות
            if (fxmlPath.contains("ActiveReservations.fxml")) {
                ActiveReservationsController controller = new ActiveReservationsController();
                loader.setController(controller);
                this.currentSubController = controller;
            }
            // 2. טיפול במסך רשימת ההמתנה (מקומי)
            else if (fxmlPath.contains("WaitingList.fxml")) {
                WaitingListController controller = new WaitingListController();
                loader.setController(controller);
                this.currentSubController = controller;
            }
            // 3. טיפול במסך הסועדים הנוכחיים
            else if (fxmlPath.contains("CurrentDiners.fxml")) {
                CurrentDinersController controller = new CurrentDinersController();
                loader.setController(controller);
                this.currentSubController = controller;
            }
            // 4. טיפול במסך רשימת מנויים (GitHub)
            else if (fxmlPath.contains("SubscribersList.fxml")) {
                SubscribersListController controller = new SubscribersListController();
                loader.setController(controller);
                this.currentSubController = controller;
            }
            else {
                loader.setController(this);
                this.currentSubController = null;
            }

            Node node = loader.load();
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);
            contentPane.getChildren().add(node);

        } catch (IOException e) {
            appendLog("Error loading screen: " + fxmlPath + " -> " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // --- 3. Screen Navigation Handlers ---

    @FXML 
    void showRegularHoursScreen(ActionEvent event) { 
        loadSubScreen("/managmentGUI/ActionsFXML/UpdateRegularHours.fxml"); 
        setupTable(); 
        initializeEmptyDays(); 
    }

    @FXML 
    void showSpecialHoursScreen(ActionEvent event) { 
        loadSubScreen("/managmentGUI/ActionsFXML/UpdateSpecialHours.fxml"); 
        setupSpecialHoursFields(); 
    }
    
    @FXML
    public void clickLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/clientGUI/fxmlFiles/RemoteLoginFrame.fxml"));
            Parent root = loader.load();
            Object nextController = loader.getController();
            if (nextController instanceof BaseMenuController) {
                ((BaseMenuController) nextController).setClient(client, null, 0);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            appendLog("Error during logout: " + e.getMessage());
        }
    }

    // --- 4. Internal UI Component Setup ---

    private void setupSpecialHoursFields() { 
        if (comboSpecialOpen == null) return;
        ObservableList<String> hours = FXCollections.observableArrayList(); 
        for (int i = 0; i < 24; i++) { 
            hours.addAll(String.format("%02d:00", i), String.format("%02d:30", i)); 
        } 
        comboSpecialOpen.setItems(hours); 
        comboSpecialClose.setItems(hours); 
        comboSpecialOpen.setValue("09:00"); 
        comboSpecialClose.setValue("22:00"); 

        dpSpecialDate.setDayCellFactory(picker -> new DateCell() { 
            @Override 
            public void updateItem(LocalDate date, boolean empty) { 
                super.updateItem(date, empty); 
                setDisable(empty || date.isBefore(LocalDate.now()) || date.isAfter(LocalDate.now().plusDays(30))); 
            } 
        }); 
        dpSpecialDate.setValue(LocalDate.now()); 
    } 

    private void setupTable() { 
        if (tableHours == null) return;
        colDay.setCellValueFactory(new PropertyValueFactory<>("day")); 
        colOpen.setCellValueFactory(new PropertyValueFactory<>("openTime")); 
        colClose.setCellValueFactory(new PropertyValueFactory<>("closeTime")); 
        colOpen.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn()); 
        colClose.setCellFactory(javafx.scene.control.cell.TextFieldTableCell.forTableColumn()); 
        colOpen.setOnEditCommit(e -> e.getRowValue().setOpenTime(e.getNewValue())); 
        colClose.setOnEditCommit(e -> e.getRowValue().setCloseTime(e.getNewValue())); 
        tableHours.setItems(scheduleData); 
        tableHours.setEditable(true); 
    } 

    private void initializeEmptyDays() { 
        String[] days = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"}; 
        scheduleData.clear(); 
        for (String day : days) { 
            scheduleData.add(new DayScheduleRow(day, "00:00", "00:00")); 
        } 
    } 

    // --- 5. Server Communication Logic ---

    @FXML 
    void updateRegularHours(ActionEvent event) { 
        Map<String, TimeRange> updatedMap = new HashMap<>(); 
        for (DayScheduleRow row : scheduleData) { 
            updatedMap.put(row.getDay(), new TimeRange(row.getOpenTime(), row.getCloseTime())); 
        } 
        ArrayList<Object> message = new ArrayList<>(); 
        message.add("UPDATE_REGULAR_HOURS"); 
        message.add(1); 
        message.add(updatedMap); 
        client.handleMessageFromClientUI(message); 
    } 

    @FXML 
    void updateSpecialHours(ActionEvent event) { 
        if (dpSpecialDate == null || dpSpecialDate.getValue() == null) return;
        ArrayList<Object> msg = new ArrayList<>(); 
        msg.add("UPDATE_SPECIAL_HOURS"); 
        msg.add(1); 
        msg.add(dpSpecialDate.getValue()); 
        msg.add(comboSpecialOpen.getValue()); 
        msg.add(comboSpecialClose.getValue()); 
        client.handleMessageFromClientUI(msg); 
    } 
    
    @FXML 
    void deleteAllSpecialHours(ActionEvent event) { 
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION); 
        confirmAlert.setTitle("Confirm Mass Deletion"); 
        confirmAlert.setHeaderText("Permanently Delete All Special Hours?"); 
        confirmAlert.setContentText("Are you sure?"); 
        java.util.Optional<ButtonType> result = confirmAlert.showAndWait(); 
        if (result.isPresent() && result.get() == ButtonType.OK) { 
            ArrayList<Object> message = new ArrayList<>(); 
            message.add("DELETE_ALL_SPECIAL_HOURS"); 
            message.add(1); 
            appendLog("Requesting server to clear all special hour records..."); 
            if (client != null) client.handleMessageFromClientUI(message); 
        } else { 
            appendLog("Deletion canceled by user."); 
        } 
    } 

    // --- 6. Sub-Menu Navigation Handlers ---

    @FXML void createNewSubscriber(ActionEvent event) { 
        loadSubScreen("/managmentGUI/ActionsFXML/CreateNewSubscriber.fxml"); 
    } 
    
    @FXML 
    void processCreateSubscriber(ActionEvent event) { 
        String phone = txtPhone.getText().trim(); 
        String email = txtEmail.getText().trim(); 
        if (!phone.matches("\\d{10}")) { 
            new Alert(Alert.AlertType.ERROR, "Invalid Phone: Must be 10 digits.").show(); 
            return; 
        }
        if (!email.contains("@")) { 
            new Alert(Alert.AlertType.ERROR, "Invalid Email.").show();
            return; 
        }
        ArrayList<Object> message = new ArrayList<>(); 
        message.add("CREATE_NEW_SUBSCRIBER"); 
        message.add(phone); 
        message.add(email); 
        appendLog("Sending registration request for phone: " + phone); 
        if (client != null) client.handleMessageFromClientUI(message); 
    } 

    @FXML 
    void viewSubscribersList(ActionEvent event) { 
        loadSubScreen("/managmentGUI/ActionsFXML/SubscribersList.fxml"); 
        ArrayList<Object> message = new ArrayList<>();
        message.add("GET_ALL_SUBSCRIBERS");
        appendLog("System: Requesting subscriber list from server...");
        client.handleMessageFromClientUI(message);
    }
    
    @FXML 
    void viewActiveReservations(ActionEvent event) { 
        loadSubScreen("/managmentGUI/ActionsFXML/ActiveReservations.fxml"); 
        try {
            appendLog("System: Fetching all active reservations for staff view...");
            ArrayList<Object> message = new ArrayList<>();
            message.add("GET_ALL_ACTIVE_RESERVATIONS_STAFF"); 
            client.sendToServer(message); 
        } catch (IOException e) {
            appendLog("Error: Failed to send request to server.");
            e.printStackTrace();
        }
    }

    @FXML 
    void viewCurrentDiners(ActionEvent event) { 
        loadSubScreen("/managmentGUI/ActionsFXML/CurrentDiners.fxml"); 
        try {
            appendLog("System: Fetching active diners list...");
            ArrayList<Object> message = new ArrayList<>();
            message.add("GET_ACTIVE_DINERS_LIST"); 
            if (client != null) client.handleMessageFromClientUI(message); 
        } catch (Exception e) {
            appendLog("Error: Failed to send request.");
            e.printStackTrace();
        }
    }

    @FXML 
    void viewWaitingList(ActionEvent event) { 
        loadSubScreen("/managmentGUI/ActionsFXML/WaitingList.fxml"); 
        try {
            appendLog("System: Requesting current waiting list from server...");
            ArrayList<Object> message = new ArrayList<>();
            message.add("GET_WAITING_LIST"); 
            if (client != null) client.handleMessageFromClientUI(message); 
        } catch (Exception e) {
            appendLog("Error: Failed to send request for waiting list.");
            e.printStackTrace();
        }
    }

    @FXML
    void clickCustomerPortal(ActionEvent event) {
        try {
            String fxmlPath = "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object nextController = loader.getController();
            if (nextController instanceof BaseMenuController) {
            	((BaseMenuController) nextController).setOriginalUserType(userType);
            	((BaseMenuController) nextController).setActingAsSubscriber(true);
                ((BaseMenuController) nextController).setClient(client, "Subscriber", userId);
                
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Bistro - Subscriber Menu");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            appendLog("Navigation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    public void showManageTablesScreen(ActionEvent event) {
        try {
            contentPane.getChildren().clear();
            appendLog("Loading Tables Management...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/managmentGUI/ActionsFXML/ManageTables.fxml"));
            Node node = loader.load();
            ManageTablesController childCtrl = loader.getController();
            childCtrl.setClient(this.client, this.userType, this.userId);
            childCtrl.onClientReady(); 
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);
            contentPane.getChildren().add(node);
        } catch (IOException e) {
            appendLog("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override 
    public void display(Object message) { 
        if (message instanceof ServiceResponse) { 
            ServiceResponse response = (ServiceResponse) message; 
            Platform.runLater(() -> { 
                if (response.getStatus() == ServiceResponse.ServiceStatus.UPDATE_SUCCESS) {
                    if (response.getData() instanceof Long) {
                        appendLog("SUCCESS: Created ID: " + response.getData()); 
                        new Alert(Alert.AlertType.INFORMATION, "Created Successfully! ID: " + response.getData()).show();
                    } else {
                        appendLog("Server Response: " + response.getStatus());
                        new Alert(Alert.AlertType.INFORMATION, "Success! System updated.").show(); 
                    }
                } 
                else if (response.getStatus() == ServiceResponse.ServiceStatus.INTERNAL_ERROR) {
                    appendLog("SERVER ERROR: " + response.getData());
                    new Alert(Alert.AlertType.ERROR, "Operation Failed: " + response.getData()).show();
                }
            }); 
        } 
        else if (message instanceof Restaurant) { 
            Restaurant rest = (Restaurant) message; 
            Platform.runLater(() -> appendLog(rest.getFormattedOpeningHours()));
        }
        else if (message instanceof ArrayList) { 
            ArrayList<?> genericList = (ArrayList<?>) message; 
            if (genericList.isEmpty()) {
                Platform.runLater(() -> appendLog("SERVER DATA: Received an empty list."));
                return;
            }
            Object firstItem = genericList.get(0);
            Platform.runLater(() -> { 
                if (firstItem instanceof Object[]) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Object[]> resList = (ArrayList<Object[]>) genericList; 
                    appendLog("SERVER DATA: Received " + resList.size() + " active reservations."); 
                    if (currentSubController instanceof ActiveReservationsController) {
                        ((ActiveReservationsController) currentSubController).setTableData(resList);
                    }
                } 
                else if (firstItem instanceof common.Visit) {
                    @SuppressWarnings("unchecked")
                    ArrayList<common.Visit> visitsList = (ArrayList<common.Visit>) genericList;
                    appendLog("System: Displaying currently active diner groups.");
                    if (currentSubController instanceof CurrentDinersController) {
                        ((CurrentDinersController) currentSubController).setTableData(visitsList);
                    }
                }
                // מקרה ג': רשימת ממתינים (מקומי)
                else if (firstItem instanceof common.WaitingListEntry) {
                    @SuppressWarnings("unchecked")
                    ArrayList<common.WaitingListEntry> waitingList = (ArrayList<common.WaitingListEntry>) genericList;
                    appendLog("System: Displaying the active waiting list.");
                    if (currentSubController instanceof WaitingListController) {
                        ((WaitingListController) currentSubController).setTableData(waitingList);
                    }
                }
                // מקרה ד': רשימת מנויים (GitHub)
                else if (firstItem instanceof common.Subscriber) {
                    @SuppressWarnings("unchecked")
                    ArrayList<common.Subscriber> subList = (ArrayList<common.Subscriber>) genericList;
                    appendLog("System: Received subscriber data.");
                    if (currentSubController instanceof SubscribersListController) {
                        ((SubscribersListController) currentSubController).setTableData(subList);
                    }
                }
            }); 
        }
    } 
    
    protected void appendLog(String msg) { 
        if (txtLog != null) { 
            Platform.runLater(() -> txtLog.appendText("> " + msg + "\n")); 
        } 
    } 

    public static class DayScheduleRow { 
        private String day; 
        private String openTime; 
        private String closeTime; 
        public DayScheduleRow(String day, String openTime, String closeTime) { 
            this.day = day; this.openTime = openTime; this.closeTime = closeTime; 
        } 
        public String getDay() { return day; } 
        public String getOpenTime() { return openTime; } 
        public void setOpenTime(String ot) { this.openTime = ot; } 
        public String getCloseTime() { return closeTime; } 
        public void setCloseTime(String ct) { this.closeTime = ct; } 
    } 
    
    public void showWorkTimesAlert(String formattedHours) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Restaurant Operating Hours");
        alert.setHeaderText("Current Updated Schedule");
        TextArea textArea = new TextArea(formattedHours);
        textArea.setEditable(false); 
        textArea.setWrapText(true);  
        textArea.setPrefWidth(500);
        textArea.setPrefHeight(400);
        textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 13;");
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
}