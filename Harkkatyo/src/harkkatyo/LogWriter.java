
package harkkatyo;

import java.util.Date;
import javafx.collections.ObservableList;

/* pakettien määrästä, lähtö- ja saapumispaikoista, matkan pituuksista ja paketeissa olevista esineistä*/

public class LogWriter {
    int sessionid;
    
    public LogWriter(int sessionid){
        this.sessionid = sessionid;
    }
    public LogWriter(ObservableList<Log> lw) { 
    }
}
    
class Log {
    private int logMessageID;
    private int sessionID;
    private String message; // max 40char
    private int fromID;
    private int toID;
    private Date logDate;

    public Log(int logMessageID, int sessionID, String message, int fromID, int toID, Date logDate) {
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
    private int getFromID() {
        return fromID;
    }
    private int getToID() {
        return toID;
    }
    public Date getLogDate() {
        return logDate;
    }
}
