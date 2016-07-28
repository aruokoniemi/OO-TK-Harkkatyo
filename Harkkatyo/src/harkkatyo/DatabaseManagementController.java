package harkkatyo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class DatabaseManagementController implements Initializable {
    @FXML AnchorPane bgPane;
   
    //Storage management
    @FXML Button addStorageButton;
    @FXML Button removeStorageButton;
    @FXML Label messageLabel;
    @FXML TreeView<String> storageTreeView = new TreeView<>();
    private ObservableList<Storage> storages;
    @FXML Button removePackageButton;
    
    //Add SmartPost
    @FXML TextField localAddressField;
    @FXML TextField postalNumberField;
    @FXML TextField cityField;
    @FXML TextField postOfficeField;
    @FXML TextField availabilityField;
    @FXML TextField latitudeField;
    @FXML TextField longitudeField;
    @FXML Button addAutomaticButton;
    @FXML Label addAutomaticLabel;
    
    //Edit SmartPost
    @FXML TextField editableLocalAddressTField;
    @FXML TextField editableCityTField;
    @FXML TextField editablePostalNumberTField;
    @FXML TextField editableAvailabilityTField;
    @FXML TextField editablePostOfficeTField;
    @FXML TextField editableLatitudeTField;
    @FXML TextField editableLongitudeTField;
    
    @FXML ComboBox removedCityCBox;
    @FXML ComboBox automaticComboBox;
    
    @FXML Button updateSmartPostButton;
    @FXML Button removeAutomaticButton;
    @FXML Label editAutomaticLabel;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateCities();
        updateStorageTreeView();
        
        this.addLengthLimiter(localAddressField, 30);
        this.addLengthLimiter(cityField, 20);
        this.addLengthLimiter(postalNumberField, 5);
        this.addLengthLimiter(postOfficeField, 50);
        this.addLengthLimiter(availabilityField, 50);  
        
        this.addLengthLimiter(editableLocalAddressTField, 30);
        this.addLengthLimiter(editableCityTField, 20);
        this.addLengthLimiter(editablePostalNumberTField, 5);
        this.addLengthLimiter(editablePostOfficeTField, 50);
        this.addLengthLimiter(editableAvailabilityTField, 50);
        
        addStorageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String storageName = displayStorageNameDialog();
                if (storageName.trim().equals("")) {
                    return;
                }

                //Check if name in use already
                for (Storage s : storages) {
                    if (storageName.equals(s.getName())) {
                        showAndHideLabel(messageLabel, "Varaston nimi on jo käytössä.");
                        return;
                    }
                }

                Storage newStorage = new Storage(storageName);
                
                DatabaseHandler db = DatabaseHandler.getInstance();
                //Add storage to database, update treeview if added successfully
                if (db.addStorage(newStorage)) {
                    updateStorageTreeView();
                }
                else {
                    showAndHideLabel(messageLabel, "Virhe varaston lisäämisessä tietokantaan.");
                }
            }
        });
        
        removeStorageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                TreeItem<String> removedItem = storageTreeView.getSelectionModel().getSelectedItem();
                String removedStorageName = removedItem.getValue().split(":")[0];
                
                
                int removedIndex = -1;
                for (Storage s : storages) {
                    if (s.getName().equals(removedStorageName)) {
                        removedIndex = storages.indexOf(s);
                    }
                }

                if (removedIndex == -1) {
                    return;
                }
                
                DatabaseHandler db = DatabaseHandler.getInstance();
                db.removeStorage(new Storage(removedStorageName));
                updateStorageTreeView();
            }
        });
        
        removePackageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                TreeItem selected = storageTreeView.getSelectionModel().getSelectedItem();
                if ( selected == null ) {
                    return;
                }
                if ( selected.getGraphic() != null ) {
                    CustomBox selectedBox = (CustomBox) selected.getGraphic();
                    String PackageDetails = selectedBox.getLabelText();
                    String sPackageID = PackageDetails.substring(PackageDetails.indexOf(":") + 1).trim();
                    int PackageID = Integer.parseInt(sPackageID);
                    
                    DatabaseHandler db = DatabaseHandler.getInstance();
                    
                    db.removePackage(PackageID);
                    updateStorageTreeView();
                }
            }
        });
         
        bgPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                bgPane.requestFocus();
            } 
        });
        
        addAutomaticButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if ( !testSmartPostAdditionFields() ) {
                    showAndHideLabel(addAutomaticLabel, "Kentät täytetty virheellisesti!");
                    return;
                }
                String msgString = addNewAutomatic();
                showAndHideLabel(addAutomaticLabel, msgString);
                resetAdditionFields();
            }
        });
        
        updateSmartPostButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if ( !testSmartPostEditionFields() ) {
                    showAndHideLabel(editAutomaticLabel, "Kentät täytetty virheellisesti!");
                }
                String msgString = updateSmartPostDetails();
                showAndHideLabel(editAutomaticLabel, msgString);
                
                updateSmartPosts();
                removeCityIfNoSmartPosts();
            }
        });
        
        removeAutomaticButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                SmartPost removedSP = (SmartPost) automaticComboBox.getSelectionModel().getSelectedItem();
                String selectedCity = (String) removedCityCBox.getSelectionModel().getSelectedItem();
                if ( removedSP == null ) {
                    return;
                }
                DatabaseHandler db = DatabaseHandler.getInstance();
                db.removeSmartPost(removedSP);
                
                showAndHideLabel(editAutomaticLabel, "Automaatti poistettu tietokannasta.");
                resetEditionFields();
                updateSmartPosts();
                
                removeCityIfNoSmartPosts();
            }
        });
        
        
        //Update automatics when selected city changes
        removedCityCBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                if (newValue != null) {
                    resetEditionFields();
                    updateSmartPosts();
                }
            }
        });
        
        //Update Textfields when selected automatic changes
        automaticComboBox.valueProperty().addListener(new ChangeListener<SmartPost>() {
            @Override
            public void changed(ObservableValue observable, SmartPost oldSP, SmartPost newSP) {
                if (newSP != null) {
                    updateEditableFields(newSP);
                }
            }
        });
        
        // SmartPost ComboBoxes
        Callback smartPostCellFactory = new Callback<ListView<SmartPost>, ListCell<SmartPost>>() {
            @Override
            public ListCell<SmartPost> call(ListView<SmartPost> p) {
                return new ListCell<SmartPost>() {
                    @Override
                    protected void updateItem(SmartPost sp, boolean empty) {
                        super.updateItem(sp, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(sp.getCity() + ": " + sp.getPostOffice());
                            setGraphic(null);
                        }
                    }

                };
            }
        };

        automaticComboBox.setCellFactory(smartPostCellFactory);
        automaticComboBox.setButtonCell(new ListCell<SmartPost>() {
            @Override
            protected void updateItem(SmartPost sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty) {
                    setText("Valitse automaatti:");
                    setGraphic(null);
                } else {
                    setText(sp.getCity() + ": " + sp.getPostOffice());
                    setGraphic(null);
                }
            }
        });
    }  
    
    //Removes selected city from database and selection combobox if no smartposts are located in city
    private void removeCityIfNoSmartPosts() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        String selectedCity = (String) removedCityCBox.getSelectionModel().getSelectedItem();
        
        if (db.getSmartPosts(selectedCity).isEmpty()) {
            db.removeCity(selectedCity);
            updateCities();
            resetEditionFields();
        }
    }
    
    private void addLengthLimiter(final TextField textField, final int maxLength) {
        textField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> ov, String newValue, String oldValue) {
                if (textField.getText().length() > maxLength) {
                    textField.setText(textField.getText().substring(0, maxLength));
                }
            }
        });
    }

    //resets SmartPost information fields 
    private void resetAdditionFields() {
        localAddressField.setText("");
        cityField.setText("");
        postalNumberField.setText("");
        availabilityField.setText("");
        postOfficeField.setText("");
        latitudeField.setText("");
        longitudeField.setText("");
    }
    
    private void resetEditionFields() {
        editableLocalAddressTField.setText("");
        editableCityTField.setText("");
        editablePostalNumberTField.setText("");
        editableAvailabilityTField.setText("");
        editablePostOfficeTField.setText("");
        editableLatitudeTField.setText("");
        editableLongitudeTField.setText("");
    }
    
    private void updateEditableFields(SmartPost sp) {
        editableLocalAddressTField.setText(sp.getLocalAddress());
        editableCityTField.setText(sp.getCity());
        editablePostalNumberTField.setText(sp.getPostalNumber());
        editableAvailabilityTField.setText(sp.getAvailability());
        editablePostOfficeTField.setText(sp.getPostOffice());
        editableLatitudeTField.setText(sp.getGeoPoint().getAsArrayList().get(0));
        editableLongitudeTField.setText(sp.getGeoPoint().getAsArrayList().get(1));
    }
    
    //returns false if user inputs not valid
    private boolean testSmartPostAdditionFields() {        
        if ( localAddressField.getText().length() == 0 ) {
            return false;
        }
        if ( cityField.getText().length() == 0 ) {
            return false;
        }
        if (postOfficeField.getText().length() == 0) {
            return false;
        }
        if (latitudeField.getText().length() == 0) {
            return false;
        }
        if (longitudeField.getText().length() == 0) {
            return false;
        }
        if (postalNumberField.getText().length() != 5) {
            return false;
        }
        
        try {
            double latitude = Double.parseDouble(latitudeField.getText());
            double longitude = Double.parseDouble(longitudeField.getText());
            int postalNumber = Integer.parseInt(postalNumberField.getText());
            if ( latitude > 180 || latitude < -180 || longitude > 180 || longitude < -180 || postalNumber == 0 ) {
                return false;
            }
            
        }
        catch(NumberFormatException e) {
            return false;
        }
        
        return true;
    }
    
    //returns false if user inputs not valid
    private boolean testSmartPostEditionFields() {
        if (editableLocalAddressTField.getText().length() == 0) {
            return false;
        }
        if (editableCityTField.getText().length() == 0) {
            return false;
        }
        if (editablePostOfficeTField.getText().length() == 0) {
            return false;
        }
        if (editableLatitudeTField.getText().length() == 0) {
            return false;
        }
        if (editableLongitudeTField.getText().length() == 0) {
            return false;
        }
        if (editablePostalNumberTField.getText().length() != 5) {
            return false;
        }

        try {
            double latitude = Double.parseDouble(editableLatitudeTField.getText());
            double longitude = Double.parseDouble(editableLongitudeTField.getText());
            int postalNumber = Integer.parseInt(editablePostalNumberTField.getText());
            if (latitude > 180 || latitude < -180 || longitude > 180 || longitude < -180 || postalNumber == 0) {
                return false;
            }

        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }
    
    
    public void updateStorageTreeView() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        this.storages = FXCollections.observableArrayList(db.getStorages());
        
        //Create root node
        TreeItem<String> root = new TreeItem<>();
        root.setValue("Varastot");
        root.setGraphic(null);
        root.setExpanded(true);
        
        //Create storage nodes
        for (Storage s : storages) {
            String packages = " pakettia";
            if ( s.getPackages().size() == 1 )
                packages = " paketti";
            
            String storageInfo = s.getName() + ": " + s.getPackages().size() + packages;
            TreeItem<String> storageItem = new TreeItem<>();
            storageItem.setValue(storageInfo);
            storageItem.setGraphic(null);
            
            // Create package nodes
            for ( Package p : s.getPackages() ) {
                
                Label packageLabel = new Label("PackageID: " + p.getPackageID());
                Button contentsButton = Package.createInfoButton(p.getPackageID(), true);
            
                CustomBox packageBox = new CustomBox(packageLabel, contentsButton);
                TreeItem<String> packageItem = new TreeItem<>();
                packageItem.setValue("");
                packageItem.setGraphic(packageBox);
                storageItem.getChildren().add(packageItem);
            }
            root.getChildren().add(storageItem);
        }
        storageTreeView.setRoot(root);
    }

    /**
     *
     * @param l Label that will be shown
     * @param s String that will be set as label text
     */
    public void showAndHideLabel(final Label l, String s) {
        l.setText(s);
        l.setVisible(true);
        PauseTransition labelPause = new PauseTransition(Duration.seconds(5));
        labelPause.setOnFinished(new EventHandler<ActionEvent>() {
            @Override
        public void handle(ActionEvent event) {
                l.setVisible(false);
            }
        });
        labelPause.play();
    }

    
    // Opens a popup, returns given String 
    public String displayStorageNameDialog() {
        String retVal = null;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AskStorageName.fxml"));
            Parent root = (Parent) loader.load();
            AskStorageNameController controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setTitle("Anna uuden varaston nimi");
            stage.setResizable(false);
            scene.getStylesheets().add(getClass().getResource("timoStyleSheet.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();
            retVal = controller.getStorageName();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //Returns message string; what went wrong / automatic added successfully
    private String addNewAutomatic() {
        String localAddress = localAddressField.getText();
        String city = cityField.getText().toUpperCase();
        String postalNumber = postalNumberField.getText();
        String availability = availabilityField.getText();
        String postoffice = postOfficeField.getText();
        String latitude = latitudeField.getText();
        String longitude = longitudeField.getText();

        DatabaseHandler db = DatabaseHandler.getInstance();
        
        //Add city to DB if its not there already
        ArrayList<String> cities = db.getCities();
        boolean cityfound = false;
        
        for ( String cityName : cities ) {
            if ( cityName.equalsIgnoreCase(city) )
                cityfound = true;
        }
        
        if ( !cityfound ) {
            db.addCity(city);
            updateCities();
        }
        
        String labelText = "";
        while (true) {

            if (!db.addPostOffice(postoffice)) {
                labelText = "Postitoimiston lisääminen tietokantaan epäonnistui.";
                break;
            }

            if (!db.addPostalNumber(postalNumber, city)) {
                labelText = "Postinumeron lisääminen tietokantaan epäonnistui.";
                break;
            }

            int locationID;
            int addressID;
            int SmartPostID = db.getNewSmartPostID();
            SmartPost newSmartPost = new SmartPost(SmartPostID, localAddress, city, postalNumber,
                    availability, postoffice, latitude, longitude);

            //Add smartpost only if address and location added successfuly
            if (db.addAddress(localAddress, city, postalNumber)
                    && (db.addLocation(latitude, longitude))) {
                locationID = db.getLastID("locationid", "location");
                addressID = db.getLastID("addressid", "address");
                //add if got ids from db
                if (locationID != -1 && addressID != -1) {
                    db.addSmartPost(newSmartPost, locationID, addressID);
                    labelText = "Postiautomaatti lisätty tietokantaan.";
                }
            }
            else {
                labelText = "Virhe postiautomaatin lisäämisessä tietokantaan.";
            }
            break;
        }
        updateCities();
        return labelText;
    }
    
    //Returns message string; what went wrong / automatic added successfully
    private String updateSmartPostDetails() {
        //SmartPost to be updated
        SmartPost currentSmartPost = (SmartPost) automaticComboBox.getSelectionModel().getSelectedItem();
        if ( currentSmartPost == null ) {
            return "";
        }
        
        String localAddress = editableLocalAddressTField.getText();
        String city = editableCityTField.getText().toUpperCase();
        String postalNumber = editablePostalNumberTField.getText();
        String availability = editableAvailabilityTField.getText();
        String postOffice = editablePostOfficeTField.getText();
        String latitude = editableLatitudeTField.getText();
        String longitude = editableLongitudeTField.getText();
        SmartPost newSmartPost = new SmartPost(localAddress, city, postalNumber, availability, 
                postOffice, latitude, longitude);
        
        DatabaseHandler db = DatabaseHandler.getInstance();
        
        String returnString = "";
        if ( db.updateSmartPost(currentSmartPost, newSmartPost) )
            returnString = "Automaatin tiedot pävitetty.";
        else {
            returnString = "Päivityksen aikana tapahtui virhe.";
        }
        return returnString;
    }
    
    private void updateCities() {
        String selectedCity = (String) removedCityCBox.getSelectionModel().getSelectedItem();
        
        DatabaseHandler db = DatabaseHandler.getInstance();
        ArrayList cityList = db.getCities();
        ObservableList<String> cityObservable = FXCollections.observableArrayList(cityList);
        FXCollections.sort(cityObservable);
        removedCityCBox.setItems(cityObservable);

        if (cityObservable.contains(selectedCity)) {
            removedCityCBox.getSelectionModel().select(selectedCity);
        }
    }
    
    private void updateSmartPosts() {
        SmartPost selectedSPost = (SmartPost) automaticComboBox.getSelectionModel().getSelectedItem();
        
        DatabaseHandler db = DatabaseHandler.getInstance();
        String selectedCity = (String) removedCityCBox.getSelectionModel().getSelectedItem();
        
        ArrayList<SmartPost> smartposts = db.getSmartPosts(selectedCity);
        
        ObservableList<SmartPost> smartPostObservable = FXCollections.observableArrayList(smartposts);
        automaticComboBox.setItems(smartPostObservable);
        
        if ( smartPostObservable.contains(selectedSPost) ) {
            automaticComboBox.getSelectionModel().select(selectedSPost);
        }
    }
    
}
