
package harkkatyo;

import java.util.ArrayList;
import java.util.Date;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

/* pakettien määrästä, lähtö- ja saapumispaikoista, matkan pituuksista ja paketeissa olevista esineistä*/

public class LogWriter {
    private int sessionid;
    
    public LogWriter() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        this.sessionid = db.getLastSessionID();
    }
    
    public void logSentPackage(Package p, double distance) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        SmartPost sender = db.getSmartPost(p.getSenderID());
        SmartPost receiver = db.getSmartPost(p.getReceiverID());
        String logMessage = "Paketti lähetetty: " + 
                sender.getPostOffice() + " -> " +
                receiver.getPostOffice();
        db.addLogEntry(this.sessionid, logMessage, p, distance, new Date());
    }
    
    public ArrayList<Log> getAllLogs() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        ArrayList<Log> logs = db.getLogMessages();
        return logs;
    }
    
    public ArrayList<Log> getSessionLogs() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        ArrayList<Log> logs = db.getLogMessages();
        ArrayList<Log> removedLogs = new ArrayList<>();
        
        for ( Log l : logs ) {
            if ( l.getSessionID() != this.sessionid ) {
                removedLogs.add(l);
            }
        }
        
        logs.removeAll(removedLogs);
        return logs;
    }
    
    public LogWriter(ObservableList<Log> lw) { 
    }
}
