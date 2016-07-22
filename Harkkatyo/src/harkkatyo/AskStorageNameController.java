/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    @FXML TextField storageNameField;
    @FXML Pane bgPane;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        storageNameField.setText((""));
        
        storageNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String newValue, String oldValue) {
                if ( newValue.length() > 20 ) {
                    storageNameField.setText(newValue.substring(0, 20));
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
