/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */


package harkkatyo;

import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

abstract public class Package {
    protected Item[] items;
    protected int packageID;
    
    protected int senderID;
    protected int receiverID;
    
    private int contentSize;
    private int contentWeight;
    
    protected int packageClass;
    protected boolean breaksItems;

    private double maxDistance;
    private int maxSize;
    private int maxWeight;
    
    public Package(int packageID, int classNr, boolean breaksItems,
            Item... i) {
        this.packageID = packageID;
        packageClass = classNr;
        this.senderID = -1;
        this.receiverID = -1;
        this.breaksItems = breaksItems;
        items = new Item[i.length];
        System.arraycopy(i, 0, items, 0, i.length);

        for (Item tempItem : items) {
            contentSize += tempItem.getSize();
            contentWeight += tempItem.getWeight();
        }
    }
    
    public Package(int packageID, int classNr, int senderID, int receiverID, boolean breaksItems, 
                Item... i) {
        this.packageID = packageID;
        packageClass = classNr;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.breaksItems = breaksItems;
        items = new Item[i.length];
        System.arraycopy(i, 0, items, 0, i.length);
        
        for ( Item tempItem : items ) {
            contentSize += tempItem.getSize();
            contentWeight += tempItem.getWeight();
        }
    }    
    
    public void setSenderAndReceiver(int senderID, int receiverID) {
        this.senderID = senderID;
        this.receiverID = receiverID;
    }
    
    public void breakItems() {
        if ( breaksItems )
            for ( Item i: items ) {
                i.breakItem();
            }
    }
    
    public boolean testLimits(double distanceTravelled) {
        if ( contentSize > maxSize )
            return false;
        if ( contentWeight > maxWeight )
            return false;
        if ( distanceTravelled > maxDistance ) 
            return false;
        
        return true;
    }
    
    public int getContentSize() {
        return contentSize;
    }
    
    public double getMaxDistance() {
        return this.maxDistance;
    }
    
    public int getContentWeight() {
        return contentWeight;
    }
    
    public Item[] getItems() {
        return items;
    }
    
    public int getPackageClass() {
        return packageClass;
    }
    
    public int getPackageID() {
        return packageID;
    }
    
    public int getSenderID() {
        return senderID;
    }   
    
    public int getReceiverID() {
        return receiverID;
    }

    /* Creates a button that opens a dialog showing package contents and package information.
     * itemsRemovable - if true the dialog will have a button that allows the user to remove items from the package.
     */ 
    public static Button createInfoButton(final int packageID, final boolean itemsRemovable) {
        Button infoButton = new Button("Katso sisältö");
        infoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("PackageInformationDialog.fxml"));
                    Parent root = (Parent) loader.load();
                    Scene scene = new Scene(root);
                    Stage stage = new Stage();
                    stage.setResizable(false);
                    stage.sizeToScene();
                    scene.getStylesheets().add(getClass().getResource("timoStyleSheet.css").toExternalForm());
                    stage.setTitle("Paketin tiedot");
                    stage.setScene(scene);

                    PackageInformationDialogController controller = loader.getController();
                    if ( itemsRemovable )
                        controller.enableRemoveItemButton();
                    
                    controller.updateAll(packageID);

                    stage.show();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        return infoButton;
    }
    
    
}

class PackageClass1 extends Package {
    private double maxDistance = 150;
    final static public int maxSize = 25000;
    static public int maxWeight = 20;
    
    public PackageClass1(int packageID, int SenderID, int ReceiverID, Item... i) {
        super(packageID, 1, SenderID, ReceiverID, true, i);
    }    
    
    public PackageClass1(int packageID, Item... i) {
        super(packageID, 1, true, i);
    }
    
    @Override
    public double getMaxDistance() {
        return maxDistance;
    }
} 

class PackageClass2 extends Package {
    private double maxDistance = 1300;
    final static public int maxSize = 20000;
    final static public int maxWeight = 30;
    
    public PackageClass2(int packageID, int SenderID, int ReceiverID, Item... i) {
        super(packageID, 2, SenderID, ReceiverID, false, i);
    }  
    
    public PackageClass2(int packageID, Item... i) {
        super(packageID, 2, false, i);
    }
    
    @Override
    public double getMaxDistance() {
        return maxDistance;
    }
} 

class PackageClass3 extends Package {
    private double maxDistance = 1300;
    final static public int maxSize = 40000;
    final static public int maxWeight = 30;
    
    public PackageClass3(int packageID, int SenderID, int ReceiverID, Item... i) {
        super(packageID, 3, SenderID, ReceiverID, true, i);
    }   
    
    public PackageClass3(int packageID, Item... i) {
        super(packageID, 3, true, i);
    }
    
    @Override
    public double getMaxDistance() {
        return maxDistance;
    }
} 