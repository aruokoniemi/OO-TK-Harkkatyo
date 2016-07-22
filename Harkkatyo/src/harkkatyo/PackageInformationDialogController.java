/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author m8581
 */
public class PackageInformationDialogController implements Initializable {
    @FXML ListView<Item> itemListView;
    @FXML Label packageIDLabel;
    @FXML Label packageSizeLabel;
    @FXML Label packageWeightLabel;
    @FXML Label packageClassLabel;
    @FXML Pane bgPane;
    
    private ObservableList<Item> items;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Callback itemCellFactory = new Callback<ListView<Item>, ListCell<Item>>() {
            @Override
            public ListCell<Item> call(ListView<Item> itemListView) {
                return new ListCell<Item>() {
                    @Override
                    public void updateItem(Item i, boolean empty) {
                        if ( empty ) {
                            setText(null);
                            setGraphic(null);
                        }
                        else {
                            setText(i.getItemDetails());
                            setGraphic(null);
                        }
                    }
                };
            }
        };
        
        itemListView.setCellFactory(itemCellFactory);
        itemListView.setItems(items);
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
    
}
