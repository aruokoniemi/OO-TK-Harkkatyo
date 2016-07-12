

package harkkatyo;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class MainMenuController implements Initializable {

    @FXML Button startButton;
    @FXML CheckBox storageSelectBox;
    @FXML ProgressBar pb = new ProgressBar();
    @FXML AnchorPane bgPane;
    @FXML Label progressBarLabel;
    @FXML Label percentageLabel;
    @FXML CheckBox xmlSelectionBox;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pb.setProgress(0);
        pb.setVisible(false);
        progressBarLabel.setVisible(false);
        percentageLabel.setVisible(false);
        
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                final XMLParser parser = new XMLParser(pb);
                //get data from db if selected
                
                if ( storageSelectBox.isSelected() ) {
                
                }
                if ( xmlSelectionBox.isSelected() ) {
                    Database db = Database.getInstance();
                    db.clearDatabaseTables();
                    disableControls();

                    progressBarLabel.setVisible(true);
                    percentageLabel.setVisible(true);

                    ReadOnlyDoubleProperty taskProperty = parser.dataGetterTask.progressProperty();

                    taskProperty.addListener(new ChangeListener<Number>() {
                        @Override
                        public void changed(ObservableValue<? extends Number> ov, Number oldVal, Number newVal) {
                            int percentage = (int) Math.round(newVal.doubleValue() * 100);
                            percentageLabel.setText(percentage + " %");
                            if ( newVal.doubleValue() == 1 ) {
                                changeStage();
                            }
                        }
                    });

                    pb.progressProperty().bind(parser.dataGetterTask.progressProperty());

                    Thread t = new Thread(parser.dataGetterTask);
                    t.start();
                    //changeStage(t);
                }
                else {
                    changeStage();
                }
            }
        });
    }
    
    public void disableControls() {
        storageSelectBox.setDisable(true);
        startButton.setDisable(true);
    }
    
    public void changeStage() {
        //Display next stage and close this
        try {
            Parent root = FXMLLoader.load(getClass().getResource("Mainview.fxml"));
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("TIMO");
            stage.setScene(scene);
            stage.show();
          
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
