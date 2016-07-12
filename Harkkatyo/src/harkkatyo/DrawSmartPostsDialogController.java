/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author m8581
 */
public class DrawSmartPostsDialogController implements Initializable {
    @FXML Button yesButton;
    @FXML Button noButton;
    @FXML AnchorPane bgPane;
    
    private boolean selection = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        yesButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                selection = true;
                closeStage();
            }
        });
        
        
        noButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                closeStage();
            }
        });
    }
    
    public boolean getSelection() {
        return selection;
    }
    
    public void closeStage() {
        Stage thisStage = (Stage) bgPane.getScene().getWindow();
        thisStage.close();
    }
    
}
