/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */

package harkkatyo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class PackageInformationDialogController implements Initializable {
    @FXML private ListView itemListView;
    @FXML private Label packageIDLabel;
    @FXML private Label packageSizeLabel;
    @FXML private Label packageWeightLabel;
    @FXML private Label packageClassLabel;
    @FXML private Pane bgPane;
    @FXML private Button removeItemButton;
    
    
    private ObservableList<Item> items;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        removeItemButton.setVisible(false);
        removeItemButton.setDisable(true);
        
        Callback itemCellFactory = new Callback<ListView<Item>, ItemCell>() {
            @Override
            public ItemCell call(ListView<Item> itemListView) {
                return new ItemCell();
            }
        };
        
        itemListView.setCellFactory(itemCellFactory);
        itemListView.setItems(items);
        
        removeItemButton.setOnAction(new EventHandler<ActionEvent>() {
           @Override
           public void handle(ActionEvent e) {
               DatabaseHandler db = DatabaseHandler.getInstance();
               Item selectedItem = (Item) itemListView.getSelectionModel().getSelectedItem();
               if ( selectedItem != null ) {
                   db.removeItem(selectedItem);
                   items.remove(selectedItem);
               }
           }
        });
    }    
    
    public void updateAll(int packageID) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        Package p = db.getPackage(packageID);
        items = FXCollections.observableArrayList(p.getItems());
        itemListView.setItems(items);
        updateLabels(p);
    }
    
    public void updateLabels(Package p) {
        packageIDLabel.setText(String.valueOf(p.getPackageID()));
        packageWeightLabel.setText(String.valueOf(p.getContentWeight()) + " kg");
        packageSizeLabel.setText(String.valueOf(p.getContentSize()) + " cm3");
        packageClassLabel.setText(String.valueOf(p.getPackageClass()));
    }
    
    public void enableRemoveItemButton() {
        removeItemButton.setVisible(true);
        removeItemButton.setDisable(false);
    }
}
