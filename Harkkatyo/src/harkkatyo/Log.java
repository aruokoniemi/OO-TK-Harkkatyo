/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

import java.util.Date;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Log {

    private final SimpleObjectProperty<Date> logDate;
    private final SimpleStringProperty message;
    private final SimpleIntegerProperty packageID;
    private final SimpleDoubleProperty distance;
    private final SimpleIntegerProperty sessionID;

    private int logEntryID;

    public Log(int logMessageID, int sessionID, String message, int packageID,
            double distance, Date logDate) {
        this.logEntryID = logMessageID;
        this.sessionID = new SimpleIntegerProperty(sessionID);
        this.message = new SimpleStringProperty(message);
        this.packageID = new SimpleIntegerProperty(packageID);
        this.distance = new SimpleDoubleProperty(distance);
        this.logDate = new SimpleObjectProperty(this, "logDate", logDate);
    }

    public int getLogEntryID() {
        return logEntryID;
    }
    
    public String getUser() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        return db.getUserName(this.getSessionID());
    }

    public int getSessionID() {
        return sessionID.get();
    }

    public String getMessage() {
        return message.get();
    }

    public int getPackageID() {
        return packageID.get();
    }

    public double getDistance() {
        return distance.get();
    }

    public Date getLogDate() {
        return logDate.get();
    }
}
