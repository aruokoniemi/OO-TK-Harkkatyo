
package harkkatyo;

import java.util.ArrayList;
import java.util.Date;
import javafx.collections.ObservableList;

/* pakettien määrästä, lähtö- ja saapumispaikoista, matkan pituuksista ja paketeissa olevista esineistä*/

public class LogWriter {
    private int sessionid;
    
    public LogWriter(int sessionid){
        this.sessionid = sessionid;
    }
    
    public void logSentPackage(Package p, double distance) {
        Database db = Database.getInstance();
        db.addLogEntry(this.sessionid, "Paketti lähetetty", p, distance, new Date());
    }
    
    public ArrayList<Log> getLogs() {
        Database db = Database.getInstance();
        ArrayList<Log> logs = db.getLogMessages();
        return logs;
    }
    
    public LogWriter(ObservableList<Log> lw) { 
    }
}
    
class Log {
    private int logMessageID;
    private int sessionID;
    private String message; // max 40char
    private int packageID;
    private double distance;
    private Date logDate;

    public Log(int logMessageID, int sessionID, String message, int packageID, 
                double distance, Date logDate) {
        this.logMessageID = logMessageID;
        this.sessionID = sessionID;
        this.message = message;
        this.packageID = packageID;
        this.distance = distance;
        this.logDate = logDate;
    }

    public int getLogMessageID() {
        return logMessageID;
    }
    public int getSessionID() {
        return sessionID;
    }
    public String getMessage() {
        return message;
    }
    public int getPackageID() {
        return packageID;
    }
    public double getDistance() {
        return distance;
    }
    public Date getLogDate() {
        return logDate;
    }
}
