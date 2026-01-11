package managmentGUI;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleObjectProperty;
import java.util.ArrayList;

public class ActiveReservationsController {
    @FXML private TableView<Object[]> activeReservationsTable;
    @FXML private TableColumn<Object[], Object> colCode, colDate, colGuests, colPhone, colStatus;

    public void initialize() {
        // מיפוי העמודות לפי האינדקסים של השאילתה ב-viewReservationController
        colCode.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue()[0]));
        colDate.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue()[1]));
        colGuests.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue()[2]));
        colPhone.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue()[3]));
        colStatus.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue()[4]));
    }

    public void setTableData(ArrayList<Object[]> data) {
        activeReservationsTable.setItems(FXCollections.observableArrayList(data));
    }
}