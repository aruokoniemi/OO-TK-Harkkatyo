/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */

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

    //Settings
    @FXML private CheckBox clearLogsCBox;
    @FXML private CheckBox clearStoragesCBox;
    @FXML private CheckBox xmlSelectionCBox;
    @FXML private TextField userNameField;
    @FXML private TextField dbPathField;
    @FXML private Button dbLocationButton;
    
    @FXML private Button startButton;
    
    //XML-parsing progressbar elements
    @FXML private ProgressBar pb = new ProgressBar();
    @FXML private AnchorPane bgPane;
    @FXML private Label progressBarLabel;
    @FXML private Label percentageLabel;
    
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
        db.setPath(path);

        if (db.testConnection()) {
            dbPathField.setText(path);
            databaseOk = true;
        } else {
            dbPathField.setText("ERROR: FILE NOT A DB");
        }
        
        // Don't let user input be over 20 characters
        userNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String newValue, String oldValue) {
                if (userNameField.getText().length() > 20) {
                    userNameField.setText(userNameField.getText().substring(0, 20));
                }

            }
        });
        
        // Get database path from user and test connection
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
                db.setPath(path);
                
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
        
        /* Clear storages if XML-data will be added to database to maintain
         * database integrity      
         */
        xmlSelectionCBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean oldVal, Boolean newVal) {
                if ( newVal == true ) {
                    clearLogsCBox.setSelected(true);
                    clearLogsCBox.setDisable(true);
                    clearStoragesCBox.setSelected(true);
                    clearStoragesCBox.setDisable(true);
                }
                else {
                    clearStoragesCBox.setSelected(false);
                    clearStoragesCBox.setDisable(false);
                    clearLogsCBox.setSelected(false);
                    clearLogsCBox.setDisable(false);
                }
            }
        });

        
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Return if database connection not working
                if ( !databaseOk ) {
                    return;
                }
                
                //Add new session to database
                DatabaseHandler db = DatabaseHandler.getInstance();
                String userName = userNameField.getText();
                if ( userName.trim().isEmpty() ) userName = "Guest";
                
                int sessionID = db.getNewSessionID();
                
                db.addSession(userName, sessionID);
                
                //Clear logs, packages that are not sent and items in packages
                if ( clearLogsCBox.isSelected() ) {
                    ArrayList<Log> logs = db.getLogs();
                    for ( Log l : logs ) {
                        db.removePackage(l.getPackageID());
                        db.removeLog(l);
                    }
                    
                }
                
                //Clear storages, packages in storages and items in packages
                if ( clearStoragesCBox.isSelected() ) {
                    ArrayList<Storage> storages = db.getStorages();
                    for ( Storage s : storages ) {
                        db.removeStorage(s);
                    }
                }
                //Clear database tables, then parse SmartPost data from XML
                final XMLParser parser;
                if ( xmlSelectionCBox.isSelected() ) {
                    parser = new XMLParser(pb);
                    db.clearDatabaseTables();
                    disableControls();
                    
                    pb.setProgress(0);
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
    
    //Get database location path from properties
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
    
    //Disable all controls
    public void disableControls() {
        clearStoragesCBox.setDisable(true);
        startButton.setDisable(true);
        xmlSelectionCBox.setDisable(true);
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
