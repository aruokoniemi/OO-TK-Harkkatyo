package harkkatyo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class SendingPackageController implements Initializable {
    
    // Package class selection
    @FXML private RadioButton class1Button;
    @FXML private RadioButton class2Button;
    @FXML private RadioButton class3Button;
    @FXML private ToggleGroup classGroup;
    @FXML private Button classInfoButton;
    
    //
    @FXML private Label classMaxWeightLabel;
    @FXML private Label classMaxSizeLabel;
    
    // Selected items details
    @FXML private Label selectedItemsWeightLabel;
    @FXML private Label selectedItemsSizeLabel;
    private int selectedItemsWeight;
    private int selectedItemsSize;
    
    //Sender details
    @FXML private ComboBox<String> senderAutomaticField;
    @FXML private ComboBox<String> senderCityField;
    
    
    //Receiver details
    @FXML private ComboBox<String> receiverAutomaticField;
    @FXML private ComboBox<String> receiverCityField;
    
    //Choose item
    @FXML private ComboBox<String> itemSelectionField;
    
    //Create new item
    @FXML private TextField itemNameField;
    @FXML private TextField itemSizeField;
    @FXML private TextField itemWeightField;
    @FXML private CheckBox itemBreakableSelection;

    //All items
    @FXML private ListView itemListView;
    @FXML private Button itemAddButton;
    @FXML private Button itemRemoveButton;
    
    //Exit controls
    @FXML private Button packageReadyButton;
    @FXML private Button cancelButton;
    @FXML private AnchorPane bgPane;
    
    ArrayList<Item> items = new ArrayList<>();
    ObservableList<String> itemDescriptions = FXCollections.observableArrayList();
    String storageName;
    Boolean packageAdded = false;
    
    Database db = Database.getInstance();
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        classGroup = new ToggleGroup();
        class1Button.setToggleGroup(classGroup);
        class2Button.setToggleGroup(classGroup);
        class3Button.setToggleGroup(classGroup); 
        
        itemListView.setItems(itemDescriptions);
        
        updateCityLists();
        updateItemList();
        
        // package class information popup
        classInfoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    showPackageClassInformation();
                }
                catch(IOException infoButtonException) {
                    System.out.println(infoButtonException.getMessage());
                }
            }
        });
        
        // add item to item listview
        itemAddButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent itemAddButtonClicked) {
                String itemName = itemNameField.getText();
                int size = Integer.parseInt(itemSizeField.getText());
                int weight = Integer.parseInt(itemSizeField.getText());
                boolean breakable = itemBreakableSelection.isSelected();
                String breakableString;
                
                if ( breakable == true )
                    breakableString = "särkyvä";
                else
                    breakableString = "ei särkyvä";
                
                selectedItemsSize += size;
                selectedItemsWeight += weight;
                
                Item newItem = new Item(itemName, size, weight, breakable);
                items.add(newItem);
                String itemDescription = itemName + ", " + size + " cm3, " +
                                        weight + " g, " + breakableString;
                itemDescriptions.add(itemDescription);
            }
        });
        
        // remove item from item listveiw
        itemRemoveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent itemAddButtonClicked) {
                String removed = (String) itemListView.getSelectionModel().getSelectedItem();
                
                
                //if not selected
                if (removed == null) {
                    return;
                }
                
                int removedIndex = itemDescriptions.indexOf(removed);
                
                
                //
                selectedItemsSize -= items.get(removedIndex).getSize();
                selectedItemsWeight -= items.get(removedIndex).getWeight();
                
                itemDescriptions.remove(removed);
                items.remove(removedIndex);
            }
        });
        
        // listen to city field change, update smartposts accordingly
        senderCityField.getSelectionModel().selectedItemProperty().addListener(new ChangeListener <String>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue ) {
                updateSmartPosts(senderAutomaticField, newValue);
            }
                        
        });
       
        // listen to city field change, update smartposts accordingly
        receiverCityField.getSelectionModel().selectedItemProperty().addListener(new ChangeListener <String>() {
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue ) {
                updateSmartPosts(receiverAutomaticField, newValue);
            }
                        
        });
        
        
        classGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue ) {
                RadioButton selected = (RadioButton) newValue.getToggleGroup().getSelectedToggle();
                int selectedClass = Integer.parseInt(selected.getText().substring(0, 1));
                
                updateSelectedClass(selectedClass);
            }
        });
        
        itemListView.itemsProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue ) {
            }
        });
        
        
        /**exits.. button/panepressed
         *  
         * 
         */
        
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                Stage stage = (Stage) bgPane.getScene().getWindow();
                stage.close();
            }
        });
        
        bgPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                if ( e.getCode().equals(KeyCode.ENTER)) {
                    Stage stage = (Stage) bgPane.getScene().getWindow();
                    stage.close();
                }
            }
        });
        
        packageReadyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                // testLimits();
                createPackage();
                Stage stage = (Stage) bgPane.getScene().getWindow();
                packageAdded = true;
                stage.close();
            }
        });
        
    }
    
    public Package createPackage() {
        String senderPostOffice = senderAutomaticField.getSelectionModel().getSelectedItem().split(":")[0];
        String receiverPostOffice = receiverAutomaticField.getSelectionModel().getSelectedItem().split(":")[0];
        int senderID = db.getSmartPostID(senderPostOffice);
        int receiverID = db.getSmartPostID(receiverPostOffice);
        
        RadioButton selected = (RadioButton) classGroup.getSelectedToggle();
        int selectedClass = Integer.parseInt(selected.getText().substring(0, 1));
        
        Item[] itemList = items.toArray(new Item[items.size()]);
        
        PackageBuilder pb = new PackageBuilder();
        int packageID = db.getNewPackageID();
        Package newPackage = pb.createPackage(packageID, selectedClass, senderID, receiverID, itemList);
        db.addPackage(newPackage, storageName);
        
        return newPackage;
        
    }
    
    
    public void updateItemList() {
        ArrayList<Item> itemList = db.getSelectableItems();
        ArrayList<String> itemDetails = new ArrayList<>();
        for ( Item i : itemList ) {
            itemDetails.add(i.getDetails());
        }
        ObservableList<String> itemObservable = FXCollections.observableArrayList(itemDetails);
        itemSelectionField.setItems(itemObservable);
    }
    
    public void updateSelectedClass(int selectedClass) {
        /*
        if ( selectedClass == 1 ) {
            classMaxWeightLabel.setText(String.valueOf(PackageClass1.maxWeight));
            classMaxSizeLabel.setText(String.valueOf(PackageClass1.maxSize));
        }
        else if ( selectedClass == 2 ) {
            classMaxWeightLabel.setText(String.valueOf(PackageClass2.maxWeight));
            classMaxSizeLabel.setText(String.valueOf(PackageClass2.maxSize));
            
        }
        else if ( selectedClass == 3 ) {
            classMaxWeightLabel.setText(String.valueOf(PackageClass3.maxWeight));
            classMaxSizeLabel.setText(String.valueOf(PackageClass3.maxSize));
        }
                */
    }
    
    public void updateItemDetails() {
        int newWeight = 0;
        int newSize = 0;
        
        for ( Item i : items ) {
            newWeight =+ i.getWeight();
            newSize =+ i.getSize();
        }
        
        
        selectedItemsWeightLabel.setText(String.valueOf(newWeight));
        selectedItemsSizeLabel.setText(String.valueOf(newSize));
        
        
    }
    
    public void updateCityLists() {
        ArrayList cityList = db.genericSelect("name", "city", "");
        ObservableList<String> cityObservable = FXCollections.observableArrayList(cityList);

        senderCityField.setItems(cityObservable);
        receiverCityField.setItems(cityObservable);
    }
    
    //
    public void updateSmartPosts(ComboBox automaticField, String city) {
        String asd = "AND address.city = " + "'" + city + "'";
        ArrayList smartPostList = db.genericSelect(
                "postoffice, address.localaddress", "smartpost JOIN address ON smartpost.addressid = address.addressid", asd );
        ObservableList<String> smartposts = FXCollections.observableArrayList(smartPostList);

        automaticField.setItems(smartposts);
    }
    
    public void updatePackageLimits() {
        
        
    }
    
    
    //Display package class information popup
    public void showPackageClassInformation() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("packageInformationPopup.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public void setStorage(String storageName) {
        this.storageName = storageName;
    }
}
