/*
 * Aleksi Ruokoniemi
 */

package harkkatyo;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Mainclass extends Application {
    public static Stage mainStage;
    
    // http://smartpost.ee/fi_apt.xml;
    @Override
    public void start(Stage stage) throws Exception {
        Database db = Database.getInstance();
        //while( !db.setUpDatabaseLocation()) {}
        
        Parent root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
        
        Scene scene = new Scene(root);
        //scene.getStylesheets().add(getClass().getResource("newCascadeStyleSheet.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
