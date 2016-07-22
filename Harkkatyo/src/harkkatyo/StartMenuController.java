

package harkkatyo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StartMenuController implements Initializable {

    @FXML Button startButton;
    @FXML CheckBox clearStoragesCheckBox;
    @FXML ProgressBar pb = new ProgressBar();
    @FXML AnchorPane bgPane;
    @FXML Label progressBarLabel;
    @FXML Label percentageLabel;
    @FXML CheckBox xmlSelectionBox;
    @FXML Label mainLabel;
    
    @FXML TextField dbPathField;
    @FXML Button dbLocationButton;
    @FXML TextField userNameField;
    
    private boolean databaseOk = false;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bgPane.requestFocus();
        
        pb.setProgress(0);
        pb.setVisible(false);
        progressBarLabel.setVisible(false);
        percentageLabel.setVisible(false);
        dbPathField.setEditable(false);
        
        DatabaseHandler db = DatabaseHandler.getInstance();
        String path = getDbLocationProperty();
        db.setUpDatabaseLocation(path);

        if (db.testConnection()) {
            dbPathField.setText(path);
            databaseOk = true;
        } else {
            dbPathField.setText("ERROR: FILE NOT A DB");
        }
        
        userNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String newValue, String oldValue) {
                if (newValue.length() > 20) {
                    userNameField.setText(newValue.substring(0, 20));
                }

            }
        });
        
        
        dbLocationButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fc = new FileChooser();
                fc.setTitle("Open Database");
                File selectedFile = fc.showOpenDialog(new Stage());
                if ( selectedFile == null )
                    return;
                    
                String path = selectedFile.getPath();
                
                DatabaseHandler db = DatabaseHandler.getInstance();
                db.setUpDatabaseLocation(path);
                
                if ( db.testConnection() ) {
                    dbPathField.setText(path);
                    databaseOk = true;
                } 
                else {
                    dbPathField.setText("ERROR: FILE NOT A DB");
                    databaseOk = false;
                }
            }
        });
        
        xmlSelectionBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
                if ( newVal == true ) {
                    clearStoragesCheckBox.setSelected(true);
                    clearStoragesCheckBox.setDisable(true);
                }
                else {
                    clearStoragesCheckBox.setSelected(false);
                    clearStoragesCheckBox.setDisable(false);
                }
            }
        });

        
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if ( !databaseOk ) {
                    return;
                }
                
                //Add new session
                DatabaseHandler db = DatabaseHandler.getInstance();
                String userName = userNameField.getText();
                if ( userName.trim().isEmpty() ) userName = "Guest";
                
                System.out.println(userName);
                
                int sessionID = db.getNewSessionID();
                db.addSession(userName, sessionID);
                
                final XMLParser parser = new XMLParser(pb);
                //get data from db if selected
                if ( !db.testConnection() ) {
                    return;
                }
                
                
                if ( clearStoragesCheckBox.isSelected() ) {
                    ArrayList<Storage> storages = db.getStorages();
                    for ( Storage s : storages ) {
                        db.removeStorage(s);
                    }
                }
                if ( xmlSelectionBox.isSelected() ) {
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
                    
                    pb.progressProperty().bind(taskProperty);

                    Thread t = new Thread(parser.dataGetterTask);
                    t.start();
                }
                else {
                    changeStage();
                }
            }
        });
    }
    
    public String getDbLocationProperty() {
        Properties props = new Properties();
        String retString = null;
        try {
            InputStream in = getClass().getResourceAsStream("settings.properties");
            props.load(in);
            String path = props.getProperty("dblocation");
            retString = path;
        }
        catch(IOException e) {
            
        }
        return retString;
    }
    
    public void disableControls() {
        clearStoragesCheckBox.setDisable(true);
        startButton.setDisable(true);
        xmlSelectionBox.setDisable(true);
        dbLocationButton.setDisable(true);
        userNameField.setDisable(true);
    }
    
    //Display next stage and close this
    public void changeStage() {
        try {       
            Stage thisStage = (Stage) bgPane.getScene().getWindow();
            thisStage.close();
            
            Parent root = FXMLLoader.load(getClass().getResource("Mainview.fxml"));
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("timoStyleSheet.css").toExternalForm());
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
