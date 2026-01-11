package managmentGUI;

import java.util.ArrayList;
import common.Visit;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class CurrentDinersController {

    @FXML private TableView<Visit> dinersTable; // וודא שה-fx:id ב-SceneBuilder תואם

    /**
     * מתודה המקבלת את הרשימה מה-Dashboard ומציגה אותה בטבלה
     */
    public void setTableData(ArrayList<Visit> visitsList) {
        if (dinersTable != null) {
            dinersTable.setItems(FXCollections.observableArrayList(visitsList));
        }
    }
}