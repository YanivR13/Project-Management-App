package clientGUI.Controllers.SubscriberControlls;

import java.util.ArrayList;
import common.Reservation;

import client.ChatClient;
import common.ChatIF;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class ReservationHistoryController implements ChatIF {
	
	private ChatClient client;

	public void setClient(ChatClient client) {
	    this.client = client;
	    client.setUI(this); // ⬅️ זה החיבור החשוב
	}
	
	@FXML private Button btnBack;
	
	@FXML private TableView<Reservation> reservationsTable;

	@FXML private TableColumn<Reservation, Long> codeCol;
	@FXML private TableColumn<Reservation, String> dateCol;
	@FXML private TableColumn<Reservation, String> timeCol;
	@FXML private TableColumn<Reservation, Integer> guestCol;
	@FXML private TableColumn<Reservation, String> statusCol;
	
	@FXML
	private void initialize() {
		codeCol.setCellValueFactory(
	        new PropertyValueFactory<>("confirmationCode")
	    );

	    dateCol.setCellValueFactory(
	        new PropertyValueFactory<>("reservationDate")
	    );

	    timeCol.setCellValueFactory(
	        new PropertyValueFactory<>("reservationTime")
	    );

	    guestCol.setCellValueFactory(
	        new PropertyValueFactory<>("numberOfGuests")
	    );

	    statusCol.setCellValueFactory(
	        new PropertyValueFactory<>("statusString")
	    );
	}

	
	@FXML
	private void clickBack(ActionEvent event) {
	    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	    stage.close();
	}
	
	public void loadReservationsForUser(int userId) {
	    if (client == null) {
	        System.out.println("Error: client is null");
	        return;
	    }

	    ArrayList<Object> msg = new ArrayList<>();
	    msg.add("GET_RESERVATIONS_HISTORY"); // פקודה לשרת
	    msg.add(userId);              // הנתון

	    client.handleMessageFromClientUI(msg);
	}

	@Override
	public void display(Object message) {
	    if (message instanceof ArrayList) {
	        ArrayList<?> data = (ArrayList<?>) message;
	        
	        String command = data.get(0).toString();

	        if ("RESERVATION_HISTORY".equals(command)) {
	            ArrayList<Reservation> reservations = (ArrayList<Reservation>) data.get(1);

	            Platform.runLater(() -> {
	                if (reservations == null || reservations.isEmpty()) {
	                    showNoReservationsAndClose();
	                    return;
	                }
	                
	                reservationsTable.getItems().clear();
	                reservationsTable.getItems().addAll(reservations);
	            });
	        }
	    }
	    else {
	    	Platform.runLater(() -> showUnexpectedErrorAndClose());
	    }
	}
	
	
	/**
	 * Displays a message indicating that no reservations exist
	 * and closes the reservation history window.
	 */
	private void showNoReservationsAndClose() {

	    Alert alert = new Alert(Alert.AlertType.INFORMATION);
	    alert.setTitle("Reservation History");
	    alert.setHeaderText(null);
	    alert.setContentText("You have no reservations in your history.");
	    alert.showAndWait();

	    Stage stage = (Stage) reservationsTable.getScene().getWindow();
	    stage.close();
	}
	
	
	/**
	 * Displays an unexpected error message to the user
	 * and closes the current Order/Reservation History window.
	 */
    private void showUnexpectedErrorAndClose() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Unexpected Error");
        alert.setHeaderText(null);
        alert.setContentText(
            "An unexpected error occurred while loading your reservation history.\n" +
            "Please try again later."
        );
        alert.showAndWait();

        Stage stage = (Stage) reservationsTable.getScene().getWindow();
        stage.close();
    }
}
