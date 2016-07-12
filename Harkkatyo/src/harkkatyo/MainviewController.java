package harkkatyo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainviewController implements Initializable {
    @FXML private TabPane bgPane;
    
    // map controls
    @FXML private WebView mapView;
    @FXML private ComboBox cityComboBox;
    @FXML private Button addSmartPostsButton;
    @FXML private Button removePathsButton;
    
    // package selection
    @FXML private Button newPackageButton;
    @FXML private ComboBox storageComboBox;
    @FXML private Label selectStorageLabel;
    @FXML private ComboBox packageComboBox;
    @FXML private Button sendPackageButton;
    
    //
    @FXML private Button editDatabaseButton;
    
    // Log controls
    @FXML private Button showAllLogsButton;
    @FXML private ListView logListView;
    ArrayList<Log> logs;
    
    ArrayList<Storage> storages;
    SmartPostHolder spHolder = new SmartPostHolder();
    Database db;
    int sessionID;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get data from database to comboboxes
        db = Database.getInstance();   
        updateCitiesBox();
        updateStorages();
        sessionID = db.getNewSessionID();
        LogWriter logwriter = new LogWriter(sessionID);
        
        //
        mapView.getEngine().load(getClass().getResource("map.html").toExternalForm());
        
        newPackageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    if ( displaySendPackage() ) //update packages if package added to db 
                        updatePackages();   
                }
                catch(IOException exception) {
                }
            }
        });        
        
        editDatabaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
               try { 
                   displayEditDatabase();
               }
               catch(IOException e1234r5tr) {
                   
               }
            }
        });
        
        removePathsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                spHolder.clearSmartPostList();
                mapView.getEngine().executeScript("document.deletePaths()");
            }
        });
        
        addSmartPostsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                drawSmartPosts();
            }
        });

        sendPackageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                int selectedPackageIndex = packageComboBox.getSelectionModel().getSelectedIndex();
                int selectedStorageIndex = storageComboBox.getSelectionModel().getSelectedIndex();
                if ( selectedStorageIndex == -1 ) // if no storage selected return
                    return;
                    
                Storage selectedStorage = storages.get(selectedStorageIndex);
                ArrayList<Package> packages = selectedStorage.getPackages();
                Package selectedPackage = packages.get(selectedPackageIndex);
                
                SmartPost sender = spHolder.getSmartPost(selectedPackage.getSenderID());
                SmartPost receiver = spHolder.getSmartPost(selectedPackage.getReceiverID());

                // Return n display error msg if smartpost not added on map
                if ( (sender == null) || (receiver == null) ) {
                    return;
                }
                
                //Get locations and combine
                ArrayList<String> senderLoc = sender.getGeoPoint().getAsArrayList();
                ArrayList<String> receiverLoc = receiver.getGeoPoint().getAsArrayList();
                senderLoc.addAll(receiverLoc);
                
                int packageClass = selectedPackage.getPackageClass();
                
                db.setPackageAsSent(selectedPackage);
                db.addLogEntry(sessionID, "Paketti lähetetty", selectedPackage, new Date());
                selectedPackage.breakItems();
                String jsScript = "document.createPath(" + senderLoc + ", 'red', " + packageClass + ")";
                Double length2 = (Double) mapView.getEngine().executeScript(jsScript);
            }
        });
        
        mapView.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent e) {
                if ( e.getDeltaY() < 0 ) {
                    String jsScript = "document.zoomOut()";
                    mapView.getEngine().executeScript(jsScript);
                }
                else {
                    String jsScript = "document.zoomIn()";
                    mapView.getEngine().executeScript(jsScript);
                }
            }
        });
        
        storageComboBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                if ( newValue != null ) {
                    selectStorageLabel.setVisible(false);
                    updatePackages();
                }
            }
        });
        
        /*
        showAllLogsButton.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent e) {
               db.getAllLogs();
           }
        });
        */
        
    }
    
    //Return true if package was added to database
    public boolean displaySendPackage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SendingPackage.fxml"));
        Parent root = (Parent) loader.load();
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        
        String storageName = (String) storageComboBox.getValue();
        if ( storageName == null ) {
            selectStorageLabel.setVisible(true);
            return false;
        }
        
        SendingPackageController controller = loader.getController();
        controller.setStorage((String) storageComboBox.getValue());
        
        stage.setScene(scene);
        stage.showAndWait();
        updatePackages();
        
        return controller.packageAdded;
    }
    
    public void displayEditDatabase() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("EditDatabase.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.showAndWait();
        updateStorages();
    }
    
    public void updateCitiesBox() {  
        ArrayList cityList = db.genericSelect("name", "city", "");
        ObservableList<String> cityObservable = FXCollections.observableArrayList(cityList);
        cityComboBox.setItems(cityObservable);
    }
    
    public void updateStorages() {
        storages = db.getStorages();
        ArrayList<String> storageNames = new ArrayList<>();
        for ( Storage s : storages ) {
            storageNames.add(s.getName());
        }
        
        ObservableList<String> storageObservable = FXCollections.observableArrayList(storageNames);
        storageComboBox.setItems(storageObservable);
    }
    
    public void updatePackages() {
        int selectedIndex = storageComboBox.getSelectionModel().getSelectedIndex();
        if ( selectedIndex == -1 )
            return;
        
        Storage tempStorage = storages.get(selectedIndex);
        ArrayList<Package> packages = tempStorage.getPackages();
        ArrayList<String> packageDetails = new ArrayList<>();
        
        for ( Package p : packages ) {
            String detailString = "ID: " + p.getPackageID();
            packageDetails.add(detailString);
        }
        
        ObservableList<String> packageObservable = FXCollections.observableArrayList(packageDetails);
        packageComboBox.setItems(packageObservable);
    }
    
    public void drawSmartPosts() {
        String selectedCity = (String) cityComboBox.getSelectionModel().getSelectedItem();
        ArrayList<SmartPost> smartposts = db.getSmartPosts(selectedCity);
        String jScript = "document.goToLocation('%s, %s, %s,', '%s, %s', 'red')";
        
        for ( SmartPost sp : smartposts) {
            spHolder.addSmartPost(sp);
            String exec = String.format(jScript, sp.getLocalAddress(), String.valueOf(sp.getPostalNumber()),
                    sp.getCity(), sp.getPostOffice(), sp.getAvailability());
            
            mapView.getEngine().executeScript(exec);
        }
    }
    
    public String getStorageName() {
        String retval = (String) storageComboBox.getSelectionModel().getSelectedItem();
        return retval;
    }
    
}
