package harkkatyo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHandler {
    private static DatabaseHandler dbHandler;
    private String path;
    
    private DatabaseHandler() { }
    
    public static DatabaseHandler getInstance() {
        if (dbHandler == null) {
            dbHandler = new DatabaseHandler();
        }
        return dbHandler;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    /*  Returns true if query to smartpost-table succeeds 
     *  else returns false
     */
    public boolean testConnection() {
        boolean retVal = true;
        try ( Connection c = getConnection() ) {
            c.prepareStatement("SELECT * FROM smartpost LIMIT 1");
        } 
        catch(SQLException e) {
            e.printStackTrace();
            retVal = false;
        }
        return retVal;
    }
    
    public void setPackageAsSent(Package p) {
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement
                ("UPDATE package SET sent = 1, senderid = ?, receiverid = ? WHERE packageid = ?;");
        )  {
                ps.setInt(1, p.getSenderID());
                ps.setInt(2, p.getReceiverID());
                ps.setInt(3, p.getPackageID());
                ps.executeUpdate();
            }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    private Connection getConnection() {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("JDBC:sqlite:" + path);
        }
        catch(SQLException|ClassNotFoundException e) {
            e.printStackTrace();
        }
        return c;
    }
    
    //Return PackageID of last inserted package
    public String getLastPackageID() {
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            String sql = "SELECT packageid FROM package ORDER BY packageid DESC LIMIT 1;";
            try ( ResultSet rs = stmt.executeQuery(sql) )   {
                if ( rs.next() ) {
                    return rs.getString("packageid");
                }
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //Return all logs from database
    public ArrayList<Log> getLogs() {
        ArrayList<Log> logs = new ArrayList<Log>();
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            String sqlQuery = "SELECT logentryid, sessionid, message, packageid, distance,"
                    + " logdate FROM logentry ORDER BY logentryid DESC;";
            try ( ResultSet rs = stmt.executeQuery(sqlQuery) )   {
                while ( rs.next() ) {
                    int logEntryID = rs.getInt("LogEntryID");
                    int sessionID = rs.getInt("sessionid");
                    String message = rs.getString("message");
                    int packageID = rs.getInt("packageid");
                    double distance = rs.getDouble("distance");
                    Long time = rs.getLong("logdate");
                    Date logdate = new Date(time);
                    
                    Log l = new Log(logEntryID, sessionID, message, packageID, distance, logdate);
                    logs.add(l);
                }
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }
    
    
    //Dont use values from user inpuy
    public int getLastID(String idColumnName, String tableName) {
        int lastID = -1;
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            String sqlQuery = "SELECT " + idColumnName + " FROM " + tableName + " ORDER BY " + idColumnName
                    + " DESC LIMIT 1;";
            try ( ResultSet rs = stmt.executeQuery(sqlQuery) ) {
                lastID = rs.getInt(1);
                return lastID;
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return lastID;
    }
    
    public void removeSmartPost(SmartPost sp) {
        //Get LocationID and AddressID for removed SP
        int locationID = -99;
        int addressID = -99;
        
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT locationid, addressid FROM smartpost WHERE smartpostid = ?;");) 
        {
            ps.setInt(1, sp.getID());
            try ( ResultSet rs = ps.executeQuery() ) {
                if ( rs.isBeforeFirst() ) {
                    locationID = rs.getInt("locationid");
                    addressID = rs.getInt("addressid");
                }
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if ( locationID == -99 || addressID == -99 ) {
            return;
        }
        
        //Remove Location and Address
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM location WHERE locationid = ?;");) {
            try {
                ps.setInt(1, locationID);
                ps.executeUpdate();
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM address WHERE addressid = ?;");) {
            try {
                ps.setInt(1, addressID);
                ps.executeUpdate();
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM postoffice WHERE name = ?;");) {
            try {
                ps.setString(1, sp.getPostOffice());
                ps.executeUpdate();
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        //Remove SmartPost
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM smartpost WHERE smartpostid = ?;");) {
            try {
                ps.setInt(1, sp.getID());
                ps.executeUpdate();
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
     /* Removes a storage and stored packages
      * @arg s Storage that will be removed from the database.
      *
      */
    public void removeStorage(Storage s) {
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM storage WHERE name = ?;");
        ) {
            try {
                if ( !s.getPackages().isEmpty() )
                    dbHandler.removePackages(s);
                ps.setString(1, s.getName());
                ps.executeUpdate();
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // remove not yet sent packages and items in package from arg Storage
    public void removePackages(Storage s) {
        //Remove items first
        for ( Package p : s.getPackages() ) {
            for ( Item i : p.getItems() ) {
                removeItem(i);
            }
        }
        
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM package WHERE storage = ? AND sent = 0;");
        ) {
            try {
                ps.setString(1, s.getName());
                ps.executeUpdate();
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // remove not yet sent packages and items in package from arg Storage
    public void removePackage(int PackageID) {
        ArrayList<Item> items = this.getItems(PackageID);
        //Remove items first
        for (Item i : items) {
            removeItem(i);
        }

        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM package WHERE packageid = ?;");) {
            try {
                ps.setInt(1, PackageID);
                ps.executeUpdate();
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeLog(Log l) {
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM logentry WHERE logentryid = ?;");) {
            try {
                ps.setInt(1, l.getLogEntryID());
                ps.executeUpdate();
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void removeItem(Item i) {
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM item WHERE itemid = ?;");) {
            try {
                ps.setInt(1, i.getItemID());
                ps.executeUpdate();
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Clear all tables
    public void clearDatabaseTables() {
        try (
                Connection c = getConnection();
                Statement stmt = c.createStatement();) {
            ArrayList<String> tables = getTables();
            for (String table : tables) {
                String sqlQuery = "DELETE FROM " + table + " WHERE 1=1;";
                stmt.executeUpdate(sqlQuery);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Get all table names in database
    public ArrayList<String> getTables() {
        ArrayList<String> retVal = null;
        try (
                Connection c = getConnection();
                Statement stmt = c.createStatement()) {
            try (ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master "
                    + "WHERE type = 'table'");) {
                retVal = resultSetToArrayList(rs);
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    
    
    //Return false if failed
    public boolean addPackage(Package p, Storage s) {
        //Add package first
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO package"
                    + "(packageid, senderid, receiverid, storage, class) VALUES "
                    + "(?, ?, ?, ?, ?);"); 
        ) {
            ps.setInt(1, p.getPackageID());
            ps.setInt(2, p.getSenderID());
            ps.setInt(3, p.getReceiverID());
            ps.setString(4, s.getName());
            ps.setInt(5, p.getPackageClass());
          
            ps.executeUpdate();
          }
        catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
        //then add items
        for ( Item i : p.getItems() ) {
            this.addItem(p, i);
        }
        
        return true;
    }
    
    public void addItem(Package p, Item i) {
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO item("
                    + "packageid, name, size, weight, broken, breakable) VALUES "
                    + "(?, ?, ?, ?, ?, ?);");
        ) {  
            try {
                ps.setInt(1, p.getPackageID());
                ps.setString(2, i.getName());
                ps.setInt(3, i.getSize());
                ps.setInt(4, i.getWeight());
                ps.setBoolean(5, i.isBroken());
                ps.setBoolean(6, i.isBreakable());
                ps.executeUpdate();
            }
            catch(SQLException innerException) {
                innerException.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    public boolean addCity (String name) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            try {
                String sql = "INSERT OR IGNORE INTO city(name) VALUES " + "('" + name.toUpperCase() +"');";
                stmt.executeUpdate(sql);
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //Return false if failed 
    public boolean addSmartPost(SmartPost sp, int locationID, int addressID) {
        boolean retVal= false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO smartpost ("
                    + "smartpostid, locationid, addressid, availability, postoffice)"
                        + " VALUES (?, ?, ?, ?, ?);");
        ) {
            try {
                ps.setInt(1, sp.getID());
                ps.setInt(2, locationID);
                ps.setInt(3, addressID);
                ps.setString(4, sp.getAvailability());
                ps.setString(5, sp.getPostOffice());

                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //Return false if failed
    public boolean addSession(String userName, int sessionID) {
        boolean retVal = false;
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("INSERT INTO session("
                        + "sessionid, username) VALUES (?, ?);")) {
            ps.setInt(1, sessionID);
            ps.setString(2, userName);
            try {
                ps.executeUpdate();
                retVal = true;
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //Return false if failed
    public boolean addPostOffice(String name) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO postoffice("
                    + "name) VALUES (?);")
        )  {
            ps.setString(1, name);
            try {
                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
           }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //return false if fail
    public boolean addLogEntry(int sessionID, String message, Package p, double distance, Date logDate) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO logentry("
                    + "sessionid, message, packageid, distance, logdate) VALUES "
                    + "(?, ?, ?, ?, ?);");
        ) {
            try {
                ps.setInt(1, sessionID);
                ps.setString(2, message);
                ps.setInt(3, p.getPackageID());
                ps.setDouble(4, distance);
                ps.setTimestamp(5, new java.sql.Timestamp(logDate.getTime()));
            
                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //return false if fail
    public boolean addLocation(String latitude, String longitude) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO location("
                    + "latitude, longitude) VALUES "
                    + "(?, ?);");
        ) {
            try {
                ps.setString(1, latitude);
                ps.setString(2, longitude);
                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //return false if fail
    public boolean addAddress(String address, String city, String postalnumber) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO address(localaddress, city, postalnumber) VALUES "
                    + "(?, ?, ?);")
        ) {
            try {
                ps.setString(1, address);
                ps.setString(2, city);
                ps.setString(3, postalnumber);
            
                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }

          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //return false if fail
    public boolean addPostalNumber(String postalnumber, String city) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO postalnumber("
                    + "number, city) VALUES "
                    + "(?, ?);");
        ) {
            try {
                ps.setString(1, postalnumber);
                ps.setString(2, city);
                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    
    //return false ifa ail  gmn
    public boolean addSelectableItem(String name, int size, int weight, boolean breakable) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO selectableitem"
                    + "(name, size, weight, breakable) VALUES "
                    + "(?, ?, ?, ?);");
        ) {
            try {
                ps.setString(1, name);
                ps.setInt(2, size);
                ps.setInt(3, weight);
                ps.setBoolean(4, breakable);
                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //retrguhbn
    public boolean addStorage(Storage s) {
        boolean retVal = false;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO storage("
                    + "name) VALUES "
                    + "(?);")
        ) {
            try {
                ps.setString(1, s.getName()); 
                ps.executeUpdate();
                retVal = true;
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return retVal;
    }
    
    //finds last added sessionid, returns that+1; returns 1 if no sessionids in db, -1 if error
    public int getNewPackageID() {
        int packageID = -1;
        try (
                Connection c = getConnection();
                Statement stmt = c.createStatement();) {
            try (ResultSet rs = stmt.executeQuery("SELECT packageid "
                    + "FROM package "
                    + "ORDER BY packageid DESC LIMIT 1;")) {
                if (rs.isBeforeFirst()) {
                    packageID = rs.getInt(1) + 1;
                } else {
                    packageID = 1;
                }
            } catch (SQLException InnerE) {
                InnerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return packageID;
    }
    
    //finds last added sessionid, returns that+1; returns 1 if no sessionids in db, -1 if error
    public int getNewItemID() {
        int itemID = -1;
        try (
                Connection c = getConnection();
                Statement stmt = c.createStatement();) {
            try (ResultSet rs = stmt.executeQuery("SELECT itemid "
                    + "FROM item "
                    + "ORDER BY itemid DESC LIMIT 1;")) {
                if (rs.isBeforeFirst()) {
                    itemID = rs.getInt(1) + 1;
                } else {
                    itemID = 1;
                }
            } catch (SQLException InnerE) {
                InnerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return itemID;
    }
    
    //finds last added sessionid, returns that+1; returns 1 if no sessionids in db, -1 if error
    public int getNewSessionID() {
        int sessionID = -1;
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            try ( ResultSet rs = stmt.executeQuery("SELECT sessionid "
                    + "FROM session "
                    + "ORDER BY sessionid DESC LIMIT 1;")
            ) {
                if ( rs.isBeforeFirst() )
                    sessionID = rs.getInt(1) + 1;
                else 
                    sessionID = 1;
              }
            catch(SQLException InnerE) {
                InnerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return sessionID;
    }
    
    //finds last added sessionid, returns that or integer 1 if no sessionids found
    public int getLastSessionID() {
        int sessionID = -1;
        try (
                Connection c = getConnection();
                Statement stmt = c.createStatement();) {
            try (ResultSet rs = stmt.executeQuery("SELECT sessionid "
                    + "FROM session "
                    + "ORDER BY sessionid DESC LIMIT 1;")) {
                if (rs.isBeforeFirst()) {
                    sessionID = rs.getInt(1);
                } else {
                    sessionID = 1;
                }
            } catch (SQLException InnerE) {
                InnerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return sessionID;
    }
    
    //finds last smartpostid, returns that+1; returns 1 if no smartpostids in db, -1 if error
    public int getNewSmartPostID() {
        int smartPostID = -1;
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            try ( ResultSet rs = stmt.executeQuery("SELECT smartpostid "
                    + "FROM smartpost "
                    + "ORDER BY smartpostid DESC LIMIT 1;")
            ) {
                if ( rs.isBeforeFirst() )
                    smartPostID = rs.getInt(1) + 1;
                else 
                    smartPostID = 1;
              }
            catch(SQLException InnerE) {
                InnerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return smartPostID;
    }
    
    public String getUserName(int sessionID) {
        String userName = "Guest";
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT username FROM session "
                        + "WHERE sessionid = ?;") 
        ) {
            ps.setInt(1, sessionID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.isBeforeFirst())
                    userName = rs.getString("username");
            } 
            catch (SQLException InnerE) {
                InnerE.printStackTrace();
            }
        } 
        catch (SQLException e) {
            e.printStackTrace();
        }
        return userName;
    }
    
    public ArrayList<String> getCities() {
        ArrayList<String> cities = new ArrayList<>();
        try (
                Connection c = getConnection();) {
            if (c.isClosed()) {
                System.out.print("rip");
            }
            if (c == null) {
                System.out.print("rip");
            }
            Statement stmt = c.createStatement();
            try (ResultSet rs = stmt.executeQuery("SELECT name FROM city;")) {
                while (rs.next()) {
                    String city = new String(rs.getString("name"));
                    cities.add(city);
                }
            } catch (SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cities;
    }
    
    
    public ArrayList<Storage> getStorages() {
        ArrayList<Storage> storages = new ArrayList<>();
        try (
            Connection c = getConnection();
        ) {
            if ( c.isClosed() ) {
                System.out.print("rip");
            }
            if (c == null) {
                System.out.print("rip");
            }
            Statement stmt = c.createStatement();
            try ( ResultSet rs = stmt.executeQuery("SELECT name FROM storage;")
            ) {
                while(rs.next()) {
                    Storage newStorage = new Storage(rs.getString("name"));
                    storages.add(newStorage);
                }
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return storages;
    }
    
    
    //Get packages taht are in arg storage ( not sent yet ) ) 
    public ArrayList<Package> getPackages(String storageName) {
        ArrayList<Package> packages = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT class, senderid, "
                    + "receiverid, packageid FROM package "
                    + "WHERE storage = ? AND sent = 0;");
        ) {
            ps.setString(1, storageName);
                try ( ResultSet rs = ps.executeQuery() ) {
                    ArrayList<String> resultStrings = this.resultSetToArrayList(rs);
                    PackageBuilder pb = new PackageBuilder();
                    
                    //Create packages and add to arraylist
                    for ( String result : resultStrings ) {
                        String[] columns = result.split(":");
                        int packageClass = Integer.parseInt(columns[0]);
                        int senderid = Integer.parseInt(columns[1]);
                        int receiverid = Integer.parseInt(columns[2]);
                        int packageid = Integer.parseInt(columns[3]);
                        ArrayList<Item> items = getItems(packageid);
                        Item[] itemList = items.toArray(new Item[items.size()]);

                        Package newPackage = pb.createPackage(packageid, packageClass, 
                                senderid, receiverid, itemList);
                        packages.add(newPackage);
                    }
                }
                catch(SQLException innerE) {
                    innerE.printStackTrace();
                }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        
        return packages;
    }
    
    public Package getPackage(int packageID) {
        System.out.print(packageID);
        Package retPackage = null;
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT class, senderid, "
                        + "receiverid, packageid FROM package "
                        + "WHERE packageid = ?;");
        ) {
            ps.setInt(1, packageID);
            try ( ResultSet rs = ps.executeQuery() ) {
                PackageBuilder pb = new PackageBuilder();
                //Create package
                int packageClass = rs.getInt("class");
                int senderid = rs.getInt("senderid");
                int receiverid = rs.getInt("receiverid");
                int packageid = rs.getInt("packageid");
                ArrayList<Item> items = getItems(packageid);
                Item[] itemList = items.toArray(new Item[items.size()]);

                retPackage = pb.createPackage(packageid, packageClass, senderid, receiverid, itemList);
            }
            catch (SQLException innerE) {
                innerE.printStackTrace();
            }
          } catch (SQLException e) {
            e.printStackTrace();
        }

        return retPackage;
    }
    
    public ArrayList<Item> getItems(int packageID) {
        ArrayList<Item> items = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT "
                    + "itemid, name, broken "
                    + "FROM item WHERE packageID = ?;");
        ) {
            ps.setInt(1, packageID);
            try ( ResultSet rs = ps.executeQuery(); ) {
                ItemBuilder ib = new ItemBuilder();
                while ( rs.next() ) {
                    int itemid = rs.getInt("itemid");
                    String itemName = rs.getString("name");
                    boolean broken = rs.getBoolean("broken");
                    Item newItem = ib.CreateItem(itemid, itemName, broken);
                    items.add(newItem);
                }
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }    
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public String[] getLocation(int locationID) {
        String[] coordinates = null;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT latitude, longitude "
                    + "FROM location WHERE locationid = ?;");
        ) {
            ps.setInt(1, locationID);
            
            try ( ResultSet rs = ps.executeQuery(); ) {
                coordinates = new String[2];
                coordinates[0] = rs.getString("latitude");
                coordinates[1] = rs.getString("longitude");
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
        }
        catch(SQLException e) {
            return null;
        }
        return coordinates;
    }
    
    // return arraylist with elements address city postalnumber
    public ArrayList<String> getAddress(int locationID) {
        ArrayList<String> address = null;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT localaddress, "
                    + "city, postalnumber FROM address WHERE addressid = ?;")
        ) {
            ps.setInt(1, locationID);
            try ( ResultSet rs  = ps.executeQuery() ) {
                String localAddress = rs.getString("localaddress");
                String city = rs.getString("city");
                String postalnumber = rs.getString("postalnumber");
                address = new ArrayList<>();
                address.add(localAddress);
                address.add(city);
                address.add(postalnumber);  
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            return null;
        }
        return address;
    }
    
    public SmartPost getSmartPost(int smartPostID) {
        SmartPost retSmartPost = null;
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT smartpost.smartpostid, address.localaddress, address.city, "
                        + "address.postalnumber, availability, postoffice, location.latitude, location.longitude "
                        + "FROM smartpost "
                        + "JOIN address on smartpost.addressid = address.addressid "
                        + "JOIN location on smartpost.locationid = location.locationid "
                        + "WHERE smartpostid = ?;");
        ) {
            ps.setInt(1, smartPostID);
            try (ResultSet rs = ps.executeQuery()) {
                if ( rs.next() ) {
                    int smartpostid = rs.getInt(1);
                    String localaddress = rs.getString(2);
                    String cityName = rs.getString(3);
                    String postalNumber = rs.getString(4);
                    String availability = rs.getString(5);
                    String postoffice = rs.getString(6);
                    String latitude = rs.getString(7);
                    String longitude = rs.getString(8);

                    retSmartPost = new SmartPost(smartpostid, localaddress, cityName, postalNumber,
                            availability, postoffice, latitude, longitude);
                }
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return retSmartPost;
    }
    
    public ArrayList<SmartPost> getSmartPosts(String city) {
        ArrayList<SmartPost> smartposts = null;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT smartpost.smartpostid, address.localaddress, address.city, "
                    + "address.postalnumber, availability, postoffice, location.latitude, location.longitude "
                    + "FROM smartpost "
                    + "JOIN address on smartpost.addressid = address.addressid "
                    + "JOIN location on smartpost.locationid = location.locationid "
                    + "WHERE address.city = ?;");
        ) {
            ps.setString(1, city);
            try ( ResultSet rs = ps.executeQuery() ) {
                ArrayList<String> resultStrings = this.resultSetToArrayList(rs);
                smartposts = new ArrayList<>();
                
                for ( String result : resultStrings ) {
                    String[] columns = result.split(":");
                    int smartpostid = Integer.parseInt(columns[0]);
                    String localaddress = columns[1];
                    String cityName = columns[2];
                    String postalNumber = columns[3];
                    String availability = columns[4];
                    String postoffice = columns[5];
                    String latitude = columns[6];
                    String longitude = columns[7];

                    SmartPost newSmartPost = new SmartPost(smartpostid, localaddress, city, postalNumber, 
                            availability, postoffice, latitude, longitude );
                    smartposts.add(newSmartPost);
                }
            }
          }
        catch(SQLException e) {
            return null;
        }
        return smartposts;
    }
    
    // SQL query resultset to arraylist.
      // contents formatted "column1:column2:...."
    public ArrayList<String> resultSetToArrayList(ResultSet rs) {
        try {
            ResultSetMetaData md = rs.getMetaData();
            ArrayList<String> arr = new ArrayList<>();
            
            while ( rs.next() ) {
                String data = "";
                for ( int i = 0; i < md.getColumnCount(); i++ ) {
                    data += rs.getString(i + 1) + ":";
                }
                
                arr.add(data.substring(0, data.length() - 1));
            }
            
            return arr;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    //Set item as broken if it is breakable
    public void breakItem(Item i) {
        int itemID = i.getItemID();
        try {
            Connection conn = this.getConnection();
            String sqlQuery = "UPDATE item SET broken = 1 WHERE itemid = ? AND breakable = 1;";
            PreparedStatement ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, itemID);
            ps.executeUpdate();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
        
    public int getSmartPostID(String postoffice) {
        int smartPostID = -1;
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT smartpostid"
                    + " FROM smartpost WHERE postoffice = ?;");
        ) {
            ps.setString(1, postoffice);
            try ( ResultSet rs = ps.executeQuery() ) {
                smartPostID = rs.getInt("smartpostid");
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
          }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return smartPostID;
    }
}
