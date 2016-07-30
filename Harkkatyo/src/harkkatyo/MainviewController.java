/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */


package harkkatyo;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
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
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ScrollEvent;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;

public class MainviewController implements Initializable {
    @FXML private TabPane bgPane;
    @FXML private Tab logTab;
    
    // map controls
    @FXML private WebView mapView;
    @FXML private ComboBox cityComboBox;
    @FXML private Button addSmartPostsButton;
    @FXML private Button removePathsButton;
    
    // package selection
    @FXML private Button newPackageButton;
    @FXML private ComboBox<Storage> storageComboBox;
    @FXML private ComboBox<Package> packageComboBox;
    @FXML private Button sendPackageButton;
    @FXML private Label selectStorageLabel;
    @FXML private Label packageLabel;

    //SmartPost selection
    @FXML private ComboBox<SmartPost> senderComboBox;
    @FXML private ComboBox<SmartPost> receiverComboBox;
    
    //Storage management
    @FXML private Button manageDatabaseButton;
    
    // Log controls
    @FXML private RadioButton thisSessionLogsRadioButton;
    @FXML private RadioButton allLogsRadioButton;
    @FXML private ToggleGroup logsToggleGroup;
    
    //Log TableView
    @FXML private TableView<Log> logTableView;
    
    //Holds all storages
    private ArrayList<Storage> storages;
    
    //Holds smartposts that are visible on map
    private final SmartPostHolder spHolder = new SmartPostHolder();
    
    private final DatabaseHandler db = DatabaseHandler.getInstance();
    private final LogWriter logWriter = new LogWriter();
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Get data from database to comboboxes
        updateCitiesBox();
        updateStorages();
        updatePackages();
        
        
        // wanted logs selection: defaults to logs from this session
        logsToggleGroup = new ToggleGroup();
        thisSessionLogsRadioButton.setToggleGroup(logsToggleGroup);
        thisSessionLogsRadioButton.setSelected(true);
        allLogsRadioButton.setToggleGroup(logsToggleGroup);
        
        
        // Update logs when tab changed
        bgPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
            @Override
            public void changed(ObservableValue<? extends Tab> observableValue, Tab oldVal, Tab newVal) {
                if ( newVal == logTab ) {
                    updateLogs();
                }
            }
        });
        
        // Update logs when radiobutton changed
        logsToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                updateLogs();
            }
        });
        
        mapView.getEngine().load(getClass().getResource("map.html").toExternalForm());
        
        newPackageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                try {
                    if ( displaySendPackage() ) // update packages if package added to db 
                        updatePackages();   
                }
                catch(IOException ex) {
                    ex.printStackTrace();
                }
            }
        });        
        
        manageDatabaseButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
               try { 
                   displayDatabaseManagement();
                   updateCitiesBox();
                   updateStorages();
                   updatePackages();
               }
               catch(IOException ex) {
                   ex.printStackTrace();
               }
            }
        });
        
        removePathsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                mapView.getEngine().executeScript("document.deletePaths()");
            }
        });
        
        // Draw SmartPost markers to map and add SmartPost instances to SmartPostHolder
        addSmartPostsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                String selectedCity = (String) cityComboBox.getSelectionModel().getSelectedItem();
                ArrayList<SmartPost> smartposts = db.getSmartPosts(selectedCity);
                String jScript = "document.goToLocation('%s, %s, %s,', '%s, %s', 'red')";

                for (SmartPost sp : smartposts) {
                    spHolder.addSmartPost(sp);
                    String exec = String.format(jScript, sp.getLocalAddress(), String.valueOf(sp.getPostalNumber()),
                            sp.getCity(), sp.getPostOffice(), sp.getAvailability());

                    mapView.getEngine().executeScript(exec);
                }

                updateSmartPosts();
            }
        });

        sendPackageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                SmartPost sender = senderComboBox.getSelectionModel().getSelectedItem();
                SmartPost receiver = receiverComboBox.getSelectionModel().getSelectedItem();
                if ( sender == null || receiver == null ) {
                    return;
                }
                
                Package selectedPackage = packageComboBox.getSelectionModel().getSelectedItem();
                if ( selectedPackage == null ) {
                    return;
                }
                
                //Get SmartPost locations and combine them
                ArrayList<String> senderLoc = sender.getGeoPoint().getAsArrayList();
                ArrayList<String> receiverLoc = receiver.getGeoPoint().getAsArrayList();
                senderLoc.addAll(receiverLoc);
                
                //Test distance before sending package: show error message if distance too long
                String jScript = "document.getDistance(" + senderLoc + ")";
                Double distance = (Double) mapView.getEngine().executeScript(jScript);
                if ( distance > selectedPackage.getMaxDistance() ) {
                    String msgString = "Matka on liian pitkä (" + distance + " km).\nLuokan " +
                            selectedPackage.getPackageClass() + " paketteja voidaan lähettää korkeintaan "
                            + selectedPackage.getMaxDistance() + " km päähän.";
                    showAndHideLabel(packageLabel, msgString);
                    return;
                }
                
                //Send package
                selectedPackage.setSenderAndReceiver(sender.getID(), receiver.getID());
                selectedPackage.breakItems();
                db.setPackageAsSent(selectedPackage);
        
                //Draw path on map
                jScript = "document.createPath(" + senderLoc + ", 'red'," +
                        selectedPackage.getPackageClass() + ")";
                mapView.getEngine().executeScript(jScript);
                showAndHideLabel(packageLabel, "Paketti lähetetty!");
                
                //Log sent package
                logWriter.logSentPackage(selectedPackage, distance);
                
                updatePackages();
            }
        });
        
        //Zoom with mouse scroll wheel
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
        
        //Update packages when selected storage changes
        storageComboBox.valueProperty().addListener(new ChangeListener<Storage>() {
            @Override
            public void changed(ObservableValue observable, Storage oldValue, Storage newValue) {
                if ( newValue != null ) {
                    updatePackages();
                }
            }
        });
        
        /** Log TableColumns
         * 
         * 
         */
        TableColumn<Log, Integer> contentColumn = new TableColumn("Sisältö");
        contentColumn.setSortable(false);
        contentColumn.setCellValueFactory(new PropertyValueFactory<Log, Integer>("packageID"));
        contentColumn.setCellFactory(new Callback<TableColumn<Log, Integer>, TableCell<Log, Integer>>() {
            @Override
            public TableCell<Log, Integer> call(TableColumn<Log, Integer> tc) {
                return new TableCell<Log, Integer>() {
                    @Override
                    protected void updateItem(Integer pID, boolean empty) {
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Button contentButton = Package.createInfoButton(pID, false);
                            setText(null);
                            setGraphic(contentButton);
                        }
                    }
                };
            }
        });
        
        TableColumn<Log, Integer> userColumn = new TableColumn("Käyttäjä");
        userColumn.setCellValueFactory(new PropertyValueFactory<Log, Integer>("sessionID"));
        userColumn.setCellFactory(new Callback<TableColumn<Log, Integer>, TableCell<Log, Integer>>() {
            @Override
            public TableCell<Log, Integer> call(TableColumn<Log, Integer> tc) {
                return new TableCell<Log, Integer>() {
                    @Override
                    protected void updateItem(Integer sID, boolean empty) {
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String user = db.getUserName(sID.intValue());
                            setText(user);
                        }
                    }
                };
            }
        });
        
        //TableColumn<Log, Date> 
        TableColumn<Log, Date> dateColumn = new TableColumn("Päivämäärä");
        dateColumn.setSortable(false);
        dateColumn.setCellValueFactory(new PropertyValueFactory<Log, Date>("logDate"));
        dateColumn.setCellFactory(new Callback<TableColumn<Log, Date>, TableCell<Log, Date>>() {
            @Override
            public TableCell<Log, Date> call(TableColumn<Log, Date> tc) {
                return new TableCell<Log, Date>() {
                    @Override
                    protected void updateItem(Date d, boolean empty) {
                        if (empty) {
                            setText(null);
                        } else {
                            setTextAlignment(TextAlignment.LEFT);
                            setText(d.toString());
                        }
                    }
                };
            }
        });
    
        
        TableColumn<Log, String> actionColumn = new TableColumn("Tapahtuma");
        actionColumn.setSortable(false);
        actionColumn.setCellValueFactory(new PropertyValueFactory<Log, String>("message"));
        actionColumn.setCellFactory(new Callback<TableColumn<Log, String>, TableCell<Log, String>>() {
            @Override
            public TableCell<Log, String> call(TableColumn<Log, String> tc) {
                return new TableCell<Log, String>() {
                    @Override
                    protected void updateItem(String s, boolean empty) {
                        if ( empty ) {
                            setText(null);
                        }
                        else {
                            setTextAlignment(TextAlignment.LEFT);
                            setText(s);
                        }
                    }
                };
            }
        });
        
        TableColumn<Log, Double> distanceColumn = new TableColumn("Matka");
        distanceColumn.setSortable(false);
        distanceColumn.setCellValueFactory(new PropertyValueFactory<Log, Double>("distance"));
        distanceColumn.setCellFactory(new Callback<TableColumn<Log, Double>, TableCell<Log, Double>>() {
            @Override
            public TableCell<Log, Double> call(TableColumn<Log, Double> tc) {
                return new TableCell<Log, Double>() {
                    @Override
                    protected void updateItem(Double d, boolean empty) {
                        if (empty) {
                            setText(null);
                        } else {
                            setText(d.toString() +" km");
                        }
                    }
                };
            }
        });
        
        
        dateColumn.prefWidthProperty().bind(logTableView.widthProperty().multiply(0.2));
        userColumn.prefWidthProperty().bind(logTableView.widthProperty().multiply(0.1));
        actionColumn.prefWidthProperty().bind(logTableView.widthProperty().multiply(0.7));
        distanceColumn.prefWidthProperty().bind(logTableView.widthProperty().multiply(0.1));
        contentColumn.prefWidthProperty().bind(logTableView.widthProperty().multiply(0.15));
        
        logTableView.setPlaceholder(new Label("Ei lokitietoja valitulta ajalta"));
        logTableView.getColumns().addAll(dateColumn, userColumn, actionColumn, distanceColumn, contentColumn);        
        logTableView.getSortOrder().addAll(dateColumn);
        
        
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
        
        receiverComboBox.setCellFactory(smartPostCellFactory);
        senderComboBox.setCellFactory(smartPostCellFactory);
        receiverComboBox.setButtonCell(new ListCell<SmartPost>() {
            @Override
            protected void updateItem(SmartPost sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty) {
                    setText("Valitse vastaanottaja:");
                    setGraphic(null);
                } else {
                    setText(sp.getCity() + ": " + sp.getPostOffice());
                    setGraphic(null);
                }
            }
        });
        
        senderComboBox.setButtonCell(new ListCell<SmartPost>() {
            @Override
            protected void updateItem(SmartPost sp, boolean empty) {
                super.updateItem(sp, empty);
                if (empty) {
                    setText("Valitse lähettäjä");
                    setGraphic(null);
                } else {
                    setText(sp.getCity() + ": " + sp.getPostOffice());
                    setGraphic(null);
                }
            }
        });
        
        Callback packageCellFactory = new Callback<ListView<Package>, ListCell<Package>>() {
            @Override
            public ListCell<Package> call(ListView<Package> p) {
                return new ListCell<Package>() {
                    @Override
                    protected void updateItem(Package p, boolean empty) {
                        super.updateItem(p, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText("ID: " + p.getPackageID());
                        }
                    }

                };
            }
        };
        
        packageComboBox.setCellFactory(packageCellFactory);

        packageComboBox.setButtonCell(new ListCell<Package>() {
            @Override
            protected void updateItem(Package p, boolean empty) {
                super.updateItem(p, empty);
                if (empty) {
                    setText("Valitse paketti");
                    setGraphic(null);
                } else {
                    setText("ID: " + p.getPackageID() );
                    setGraphic(null);
                }
            }
        });
        
        Callback storageCellFactory = new Callback<ListView<Storage>, ListCell<Storage>>() {
            @Override
            public ListCell<Storage> call(ListView<Storage> p) {
                return new ListCell<Storage>() {
                    @Override
                    protected void updateItem(Storage s, boolean empty) {
                        super.updateItem(s, empty);
                        if (empty) {
                            setText("Valitse varasto: ");
                            setGraphic(null);
                        } else {
                            setText(s.getName());
                            setGraphic(null);
                        }
                    }

                };
            }
        };

        storageComboBox.setCellFactory(storageCellFactory);
        storageComboBox.setButtonCell((ListCell<Storage>) storageCellFactory.call(null));
    }
    
    //Return true if package was added to database
    public boolean displaySendPackage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("PackageCreation.fxml"));
        Parent root = (Parent) loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("timoStyleSheet.css").toExternalForm());
        Stage stage = new Stage();
        
        Storage selectedStorage = storageComboBox.getSelectionModel().getSelectedItem();
        if ( selectedStorage == null ) {
            String msgString;
            if ( storageComboBox.getItems().size() == 0 ) {
                msgString = "Lisää tietokantaan varasto!";
            }
            else {
                msgString = "Valitse varasto johon paketti asetetaan!";
            }
            showAndHideLabel(selectStorageLabel, msgString);
            return false;
        }
        
        PackageCreationController controller = loader.getController();
        controller.setStorage(storageComboBox.getSelectionModel().getSelectedItem());
        
        stage.setScene(scene);
        stage.setTitle("Luo paketti");
        stage.showAndWait();
        
        return controller.packageWasAdded();
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
    
    public void displayDatabaseManagement() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("DatabaseManagement.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("timoStyleSheet.css").toExternalForm());
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Tietokannan hallinta");
        stage.showAndWait();
        updateStorages();
    }
    
    public void updateCitiesBox() {  
        String selectedCity = (String) cityComboBox.getSelectionModel().getSelectedItem();
        
        ArrayList cityList = db.getCities();
        ObservableList<String> cityObservable = FXCollections.observableArrayList(cityList);
        FXCollections.sort(cityObservable);
        
        cityComboBox.setItems(cityObservable);
        if ( cityObservable.contains(selectedCity) ) {
            cityComboBox.getSelectionModel().select(selectedCity);
        }
    }
    
    public void updateStorages() {
        Storage selectedStorage = storageComboBox.getSelectionModel().getSelectedItem();
        
        storages = db.getStorages();
        ObservableList<Storage> storageObservable = FXCollections.observableArrayList(storages);
        storageComboBox.setItems(storageObservable);
        
        if ( storageObservable.contains(selectedStorage) ) {
            storageComboBox.getSelectionModel().select(selectedStorage);
        }
    }
    
    public void updatePackages() {
        Package selectedPackage = packageComboBox.getSelectionModel().getSelectedItem();
        
        Storage selectedStorage = storageComboBox.getSelectionModel().getSelectedItem();
        if ( selectedStorage == null )
            return;
        
        ArrayList<Package> packages = selectedStorage.getPackages();
        ObservableList<Package> packageObservable = FXCollections.observableArrayList(packages);
        packageComboBox.setItems(packageObservable);
        
        if (packageObservable.contains(selectedPackage)) {
            packageComboBox.getSelectionModel().select(selectedPackage);
        }
    }
    
    public void updateSmartPosts() {
        SmartPost selectedReceiver = receiverComboBox.getSelectionModel().getSelectedItem();
        SmartPost selectedSender = senderComboBox.getSelectionModel().getSelectedItem();

        ObservableList<SmartPost> smartPostObservable = FXCollections.observableArrayList(spHolder.getSmartPosts());
        receiverComboBox.setItems(smartPostObservable);
        senderComboBox.setItems(smartPostObservable); 

        receiverComboBox.getSelectionModel().select(selectedReceiver);
        senderComboBox.getSelectionModel().select(selectedSender);
    }
    
    public String getStorageName() {
        Storage selectedStorage = storageComboBox.getSelectionModel().getSelectedItem();
        return selectedStorage.getName();
    }
    
    private void updateLogs() {
        ArrayList<Log> logs;
        if ( thisSessionLogsRadioButton.isSelected () ) {
            logs = logWriter.getSessionLogs();
        }
        else {
            logs = logWriter.getAllLogs();
        }

        ObservableList logObservable = FXCollections.observableArrayList(logs);

        logTableView.setItems (logObservable);
    }
}
