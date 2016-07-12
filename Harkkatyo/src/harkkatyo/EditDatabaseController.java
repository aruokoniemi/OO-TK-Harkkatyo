
package harkkatyo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EditDatabaseController implements Initializable {
    @FXML Button xmlButton;
    
    @FXML Button addStorageButton;
    @FXML Button removeStorageButton;
    @FXML TextField storageNameField;
    @FXML ListView storageListView;

    ArrayList<Storage> storages;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {    
        updateStorages();
        
        addStorageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String storageName = displayStorageNameDialog();
                if ( storageName != null ) {
                    Storage newStorage = new Storage(storageName);
                    storages.add(newStorage);
                    Database db = Database.getInstance();
                    db.addStorage(newStorage);
                    updateStorages();
                }
            }
        });
        
        removeStorageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                int removedStorageIndex = storageListView.getSelectionModel().getSelectedIndex();
                
                if ( removedStorageIndex != -1 ) {
                    Storage removedStorage = storages.get(removedStorageIndex);
                    Database db = Database.getInstance();
                    storages.remove(removedStorage);
                    db.removeStorage(removedStorage);
                    updateStorages();
                }
            }
        });
        
        
        storageListView.getItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> change) {
                updateStorages();
            }
        });
        
    }   
            

    public void updateStorages() {
        Database db = Database.getInstance();
        storages = db.getStorages();
        ArrayList<String> storageNames = new ArrayList<>();
        for (Storage s : storages) {
            StringBuilder sb = new StringBuilder(s.getName() + ": " + s.getPackages().size() + " pakettia");
            storageNames.add(sb.toString());
        }

        ObservableList<String> storageObservable = FXCollections.observableArrayList(storageNames);
        storageListView.setItems(storageObservable);
    }
    
    public String displayStorageNameDialog() {
        String retVal = "";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StorageNameDialog.fxml"));
            Parent root = (Parent) loader.load();
            StorageNameDialogController controller = loader.getController();
            Scene scene = new Scene(root);
            Stage stage = new Stage();
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
