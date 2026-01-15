package managmentGUI;

import java.util.ArrayList;
import common.Subscriber;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class SubscribersListController {
    @FXML private TableView<Subscriber> tableSubscribers;
    @FXML private TableColumn<Subscriber, Integer> colUserId;
    @FXML private TableColumn<Subscriber, Integer> colSubId;
    @FXML private TableColumn<Subscriber, String> colUsername;
    @FXML private TableColumn<Subscriber, String> colQrCode;

    @FXML
    public void initialize() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colSubId.setCellValueFactory(new PropertyValueFactory<>("subscriberId"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colQrCode.setCellValueFactory(new PropertyValueFactory<>("qrCode"));
    }

    public void setTableData(ArrayList<Subscriber> list) {
        tableSubscribers.setItems(FXCollections.observableArrayList(list));
    }
}