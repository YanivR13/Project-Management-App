package clientGUI.Controllers.MenuControlls;

import java.util.ArrayList;
import java.util.List;
import common.Reservation;
import common.ChatIF;
import clientGUI.Controllers.ICustomerActions;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.binding.Bindings;

public class ViewReservationController extends BaseMenuController implements ICustomerActions {

    @FXML private TableView<Reservation> tableReservations;
    @FXML private TableColumn<Reservation, Long> colCode;
    @FXML private TableColumn<Reservation, String> colDate;
    @FXML private TableColumn<Reservation, Integer> colGuests;
    @FXML private TableColumn<Reservation, Void> colAction;

    @Override
    public void onClientReady() {
        // הדפסת לוג - אם בסיבוב השני מופיע 0, הבעיה היא בתפריט הראשי שלא שמר את ה-ID
        System.out.println("DEBUG: Entering screen. UserID: " + userId);
        
        if (client != null && userId != 0) {
            // וידוא שהלקוח שולח הודעות למסך הנוכחי
            client.setUI(this); 
            
            // ניקוי הטבלה לפני בקשה חדשה
            Platform.runLater(() -> tableReservations.getItems().clear());

            ArrayList<Object> message = new ArrayList<>();
            message.add("GET_ACTIVE_RESERVATIONS");
            message.add(userId);
            client.handleMessageFromClientUI(message);
        }
    }

    @FXML
    public void initialize() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("reservationDateTime"));
        colGuests.setCellValueFactory(new PropertyValueFactory<>("numberOfGuests"));

        // הגדרת שורות גבוהות (65 פיקסלים) למניעת חיתוך טקסט
        tableReservations.setFixedCellSize(65.0);
        
        // העלמת שורות ריקות (הטבלה נגמרת בדיוק אחרי ההזמנות)
        tableReservations.prefHeightProperty().bind(
            tableReservations.fixedCellSizeProperty().multiply(Bindings.size(tableReservations.getItems()).add(1.1))
        );

        // עיצוב כהה וממורכז בתוך הקוד (ללא CSS חיצוני)
        String cellStyle = "-fx-alignment: CENTER; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;";
        colCode.setStyle(cellStyle);
        colDate.setStyle(cellStyle);
        colGuests.setStyle(cellStyle);

        setupPayButton();
    }

    private void setupPayButton() {
        colAction.setStyle("-fx-alignment: CENTER;");
        colAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Pay Bill");
            {
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 8;");
                btn.setPrefHeight(40);
                btn.setOnAction(event -> {
                    Reservation res = getTableView().getItems().get(getIndex());
                    // כאן תבוא פקודת ה-UPDATE של ה-SQL
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    @Override
    public void display(Object message) {
        if (message instanceof List) {
            Platform.runLater(() -> {
                tableReservations.setItems(FXCollections.observableArrayList((List<Reservation>) message));
                tableReservations.refresh(); // כפיית רענון לתצוגה
            });
        }
    }

    @FXML
    void clickBack(ActionEvent event) {
        // שימוש בנתיבים המדויקים ביותר כדי למנוע Location is not set
        String path = "Subscriber".equalsIgnoreCase(userType) ? 
            "/clientGUI/fxmlFiles/SubscriberFXML/SubscriberMenuFrame.fxml" : 
            "/clientGUI/fxmlFiles/OccasionalFXML/OccasionalMenuFrame.fxml";
            
        navigateTo(client, event, userType, userId, path, "Bistro - Main Menu");
    }

    @Override public void viewOrderHistory(client.ChatClient client, int userId) {}
    @Override public void editPersonalDetails(client.ChatClient client, int userId) {}
}