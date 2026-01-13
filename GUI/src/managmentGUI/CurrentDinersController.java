package managmentGUI;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import common.Visit;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class CurrentDinersController implements Initializable {

    @FXML private TableView<Visit> dinersTable;
    @FXML private TableColumn<Visit, String> colCode;
    @FXML private TableColumn<Visit, Integer> colTable;
    @FXML private TableColumn<Visit, String> colTime;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // השמות בגרשיים חייבים להתאים לשמות השדות במחלקה Visit (למשל confirmationCode)
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colTable.setCellValueFactory(new PropertyValueFactory<>("tableId"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("startTime"));
    }
    public void setTableData(ArrayList<Visit> visitsList) {
        if (dinersTable != null && visitsList != null) {
            dinersTable.setItems(FXCollections.observableArrayList(visitsList));
        }
    }
}