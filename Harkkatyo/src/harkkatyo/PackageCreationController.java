package harkkatyo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.PauseTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class PackageCreationController implements Initializable {
    
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
    
    //Choose item
    @FXML private ComboBox<Item> itemSelectionField;

    //All items
    @FXML private ListView itemListView;
    @FXML private Button itemAddButton;
    @FXML private Button itemRemoveButton;
    
    //Exit controls
    @FXML private Button packageReadyButton;
    @FXML private Button cancelButton;
    @FXML private AnchorPane bgPane;
    
    
    @FXML private Label warningLabel;
    
    private ObservableList<Item> items = FXCollections.observableArrayList();
    private Storage storage;
    private Boolean packageAdded = false;
    
    private int totalWeight;
    private int totalSize;
    private int maxWeight;
    private int maxSize;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) { 
        DatabaseHandler db = DatabaseHandler.getInstance();
        
        classGroup = new ToggleGroup();
        class1Button.setToggleGroup(classGroup);
        class2Button.setToggleGroup(classGroup);
        class3Button.setToggleGroup(classGroup); 
        
        itemListView.setItems(items);
        itemListView.setFocusTraversable(true);
        Callback itemCellFactory = new Callback<ListView<Item>, ItemCell>() {
            @Override
            public ItemCell call(ListView<Item> itemListView) {
                return new ItemCell();
            }
        };
        ItemBuilder ib = new ItemBuilder();
        ObservableList<Item> selectableItems = FXCollections.observableArrayList(ib.createAllItems());
        itemSelectionField.setItems(selectableItems);
        
        itemListView.setCellFactory(itemCellFactory);
        itemSelectionField.setCellFactory(itemCellFactory);
        itemSelectionField.setButtonCell((ListCell<Item>) itemCellFactory.call(null));
        
        
        bgPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                bgPane.requestFocus();
            }
        });
        
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
                Item selectedItem = itemSelectionField.getSelectionModel().getSelectedItem();
                //if no item selected
                if (selectedItem == null) {
                    return;
                }
                items.add(selectedItem);
            }
        });
        
        // remove item from item listveiw
        itemRemoveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent itemAddButtonClicked) {
                Item removedItem = (Item) itemListView.getSelectionModel().getSelectedItem();
                //if no item selected
                if (removedItem == null) {
                    return;
                }
                
                items.remove(removedItem);
                itemListView.getSelectionModel().select(0);
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
        
        class1Button.setSelected(true);
        
        items.addListener(new ListChangeListener<Item>() {
            @Override
            public void onChanged( Change<? extends Item> change ) {
                while ( change.next() ) {
                    totalSize = 0;
                    totalWeight = 0;
                    for ( Item i : items ) {
                        totalSize += i.getSize();
                        totalWeight += i.getWeight();
                    }
                }
                
                selectedItemsSizeLabel.setText(String.valueOf(totalSize));
                selectedItemsWeightLabel.setText(String.valueOf(totalWeight));
                testPackageLimits();
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
        
        packageReadyButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override 
            public void handle(ActionEvent e) {
                if ( !testAllLimits() )
                    return;
                createPackage();
                Stage stage = (Stage) bgPane.getScene().getWindow();
                packageAdded = true;
                stage.close();
            }
        });
        
    }

    public boolean testAllLimits() {
        String warningString = "";
        boolean limitsOk = true;
        
        if ( !selectionsValid() ) {
            warningString += "Täytä tarvittavat kentät!\n";
            limitsOk = false;
        }
        if ( !testPackageLimits() ) {
            warningString += "Esineet eivät täytä pakettiluokan vaatimuksia!";
            limitsOk = false;
        }

        if ( limitsOk == false ) {
            warningLabel.setVisible(true);
            warningLabel.setText(warningString);
        }
        
        return limitsOk;
    }

    //creates package from given info
    public Package createPackage() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        
        RadioButton selected = (RadioButton) classGroup.getSelectedToggle();
        int selectedClass = Integer.parseInt(selected.getText().substring(0, 1));
        
        Item[] itemList = items.toArray(new Item[items.size()]);
        
        PackageBuilder pb = new PackageBuilder();
        int packageID = db.getNewPackageID();
        Package newPackage = pb.createPackage(packageID, selectedClass, itemList);
        storage.addPackage(newPackage);
        
        return newPackage;
        
    }
    
    public boolean selectionsValid() {
        boolean retVal = true;
        if ( classGroup.getSelectedToggle() == null ) {
            retVal = false;
        }
        return retVal;
    }
    
    
    
    public boolean testPackageLimits() {     
        boolean retVal = true;
        if ( totalSize > maxSize ) {
            selectedItemsSizeLabel.setTextFill(Color.RED);
            retVal = false;
        }
        else {
            selectedItemsSizeLabel.setTextFill(Color.BLACK);
        }
        
        if (totalWeight > maxWeight) {
            selectedItemsWeightLabel.setTextFill(Color.RED);
            retVal = false;
        }
        else {
            selectedItemsWeightLabel.setTextFill(Color.BLACK);
        }
        return retVal;
    }
    
    public void updateSelectedClass(int selectedClass) {
        if ( selectedClass == 1 ) {
            maxWeight = PackageClass1.maxWeight;
            maxSize = PackageClass1.maxSize;
        }
        else if ( selectedClass == 2 ) {
            maxWeight = PackageClass2.maxWeight;
            maxSize = PackageClass2.maxSize;
            
        }
        else if ( selectedClass == 3 ) {
            maxWeight = PackageClass3.maxWeight;
            maxSize = PackageClass3.maxSize;
        }     
        classMaxWeightLabel.setText("/ " + String.valueOf(maxWeight));
        classMaxSizeLabel.setText("/ " + String.valueOf(maxSize));
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

    //Display package class information popup
    public void showPackageClassInformation() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("packageInformationPopup.fxml"));
        Scene scene = new Scene(root);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.showAndWait();
    }
    
    public void setStorage(Storage storage) {
        this.storage = storage;
    }
    
    public boolean packageWasAdded() {
        return packageAdded;
    }
}
