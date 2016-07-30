/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */


package harkkatyo;

import java.util.ArrayList;
import java.util.Date;

public class LogWriter {
    private final int sessionid;

    public LogWriter() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        this.sessionid = db.getLastSessionID();
    }
    
    public void logSentPackage(Package p, double distance) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        SmartPost sender = db.getSmartPost(p.getSenderID());
        SmartPost receiver = db.getSmartPost(p.getReceiverID());
        String logMessage = "Paketti lähetetty: " + 
                sender.getCity() + " " +
                sender.getPostOffice() + " -> " +
                receiver.getCity() + " " +
                receiver.getPostOffice();
        db.addLogEntry(this.sessionid, logMessage, p, distance, new Date());
    }
    
    
    public ArrayList<Log> getAllLogs() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        ArrayList<Log> logs = db.getLogs();
        return logs;
    }
    
    public ArrayList<Log> getSessionLogs() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        ArrayList<Log> logs = db.getLogs();
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
