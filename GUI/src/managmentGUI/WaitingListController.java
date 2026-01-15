package managmentGUI;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import common.WaitingListEntry;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Controller for the Waiting List sub-screen.
 * Responsible for displaying customers currently waiting for a table.
 */
public class WaitingListController implements Initializable {

    // --- FXML Components ---
    @FXML private TableView<WaitingListEntry> waitingTable;
    @FXML private TableColumn<WaitingListEntry, Long> colCode;
    @FXML private TableColumn<WaitingListEntry, String> colTime;
    @FXML private TableColumn<WaitingListEntry, Integer> colUserId;

    /**
     * Initializes the table columns by mapping them to WaitingListEntry fields.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // המיפוי מתבצע מול שמות השדות/הגטרים ב-WaitingListEntry
        colCode.setCellValueFactory(new PropertyValueFactory<>("confirmationCode"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("entryTime"));
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userId"));
    }

    /**
     * Populates the table with data received from the server.
     * @param waitingList The list of waiting entries to display.
     */
    public void setTableData(ArrayList<WaitingListEntry> waitingList) {
        if (waitingTable != null && waitingList != null) {
            // עדכון ה-UI עם הרשימה שהגיעה מהשרת  
            waitingTable.setItems(FXCollections.observableArrayList(waitingList));
        }
    }
}