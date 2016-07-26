
package harkkatyo;

import java.util.ArrayList;
import java.util.Date;

public class LogWriter {
    private final int sessionid;

    public LogWriter() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        this.sessionid = db.getLastSessionID();
    }
    
    //
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
    
    //Returns logs only from this session
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
}
