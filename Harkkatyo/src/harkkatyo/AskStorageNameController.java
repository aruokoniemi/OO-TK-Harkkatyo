/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */

package harkkatyo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class AskStorageNameController implements Initializable {
    @FXML private TextField storageNameField;
    @FXML private Pane bgPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        storageNameField.setText((""));
        
        // Limit user input to 20 characters
        storageNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String newValue, String oldValue) {
                if ( storageNameField.getText().length() > 20 ) {
                    storageNameField.setText(storageNameField.getText().substring(0, 20));
                }
                    
            }
        });
        
        storageNameField.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Stage stage = (Stage) bgPane.getScene().getWindow();
                stage.close();
            }
        });
        
        
    }    
    
    public String getStorageName() {
        return storageNameField.getText();
    }
}
