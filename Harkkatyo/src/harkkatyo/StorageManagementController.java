package harkkatyo;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StorageManagementController implements Initializable {
    @FXML AnchorPane bgPane;
    
    @FXML Button addStorageButton;
    @FXML Button removeStorageButton;
    
    @FXML Label messageLabel;
    
    @FXML TreeView<String> storageTreeView = new TreeView<>();
    
    private ObservableList<Storage> storages;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateStorageTreeView();
        
        addStorageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String storageName = displayStorageNameDialog();
                //Check if name empty
                if (storageName.trim().equals("")) {
                    return;
                }

                //Check if name in use already
                for (Storage s : storages) {
                    System.out.println(s.getName());
                    if (storageName.equals(s.getName())) {
                        showAndHideLabel(messageLabel, "Varaston nimi on jo käytössä.");
                        return;
                    }
                }

                Storage newStorage = new Storage(storageName);
                
                DatabaseHandler db = DatabaseHandler.getInstance();
                //Add storage to database, update treeview if added successfully
                if (db.addStorage(newStorage)) {
                    storages.add(newStorage);

                    TreeItem<String> storageItem = new TreeItem<>();
                    storageItem.setValue(newStorage.getName());
                    storageTreeView.getRoot().getChildren().add(storageItem);
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

                storageTreeView.getRoot().getChildren().remove(removedIndex);
                DatabaseHandler db = DatabaseHandler.getInstance();
                db.removeStorage(new Storage(removedStorageName));
            }
        });
         
        bgPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent t) {
                bgPane.requestFocus();
            } 
        });
        
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
            System.out.println(s.getName());
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
    
    
}
