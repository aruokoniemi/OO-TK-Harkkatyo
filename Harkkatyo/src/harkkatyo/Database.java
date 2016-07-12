package harkkatyo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Database {
    private static Database db;
    String url = "jdbc:sqlite:/net/homes/u006/m8581/m8581/Harkkatyo/dbnew";
    
    private Database() { }
    
    public static Database getInstance() {
        if (db == null) {
            db = new Database();
        }
        return db;
    }
    
    
    //Return false if connection test fails
    public boolean setUpDatabaseLocation() {
        url = "jdbc:sqlite:/net/homes/u006/m8581/m8581/Harkkatyo/dbnew";
        return true;
        /*
        if ( !findDatabaseLocationFromSettings() ) {
            System.out.print("moi");
            //Get database path from user input
            FileChooser fc = new FileChooser();
            fc.setTitle("Open Database");
            File selectedFile = fc.showOpenDialog(new Stage());
            this.url = "jdbc:sqlite:" + selectedFile.getPath();
            System.out.print(url);
            
            //this.writeToSettings("DB_LOCATION", selectedFile.getPath());
        }
            
        
        //Test connection
        Connection conn = getConnection();
        if( conn == null )
                return false;
        
        return true;
        */
    }
    
    public void setPackageAsSent(Package p) {
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement
                ("UPDATE package SET sent = 1 WHERE packageid = ?;");
        )   {
                ps.setInt(1, p.getPackageID());
                ps.executeUpdate();
            }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    public boolean findDatabaseLocationFromSettings() {
        // See if database location set up already in settings file
        BufferedReader br = null;
        try {
            InputStream in = getClass().getResourceAsStream("settings.txt");
            br = new BufferedReader(new InputStreamReader(in));
            String inputLine;
            while( (inputLine = br.readLine() ) != null ) {
                if ( inputLine.startsWith("DB_LOCATION") ) {
                    String dbLocation = inputLine.split(":")[1];
                    if ( !dbLocation.equals("") ) {
                        this.url = "jdbc:sqlite:" + dbLocation;
                        return true;
                    }
                    break;
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                br.close();
            }
            catch(IOException e124) {
                e124.printStackTrace();
            }
            return false;
        }
    }
    
    public void writeToSettings(String settingID, String writable) {
        BufferedReader reader = null;
        PrintWriter writer = null;
        
        try {
            //Read file first
            InputStream in = getClass().getResourceAsStream("settings.txt");
            reader = new BufferedReader(new InputStreamReader(in));
            ArrayList<String> lines = new ArrayList<>();
            String newString = settingID + ":" + writable;
            String inputLine;
            Boolean settingChanged = false;
            
            //Find setting and replace text
            while ( (inputLine = reader.readLine()) != null ) {
                if(inputLine.split(":")[0].equals(settingID)) {
                    lines.add(newString);
                    settingChanged = true;
                }
                lines.add(inputLine);
            }
            
            //Write setting to file if it wasnt there already
            if ( settingChanged == false ) {
                lines.add(newString);
            }
            
            reader.close();
            File settingsFile = new File(this.getClass().getResource("settings.txt").getPath());
            System.out.print(this.getClass().getResource("settings.txt").getPath());
            writer = new PrintWriter(settingsFile);
            
            for ( String line : lines ) {
                writer.println(line);
            }
            
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( reader != null ) 
                try { reader.close();} catch (IOException ex) {} 
            if ( writer != null )
                writer.close();
        }
    }
    
    private Connection getConnection() {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(url);
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
    
        //Return PackageID of last inserted package
    public ArrayList<Log> getLogMessages() {
        ArrayList<Log> logs = new ArrayList<Log>();
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            String sqlQuery = "SELECT logmessageid, sessionid, message, packageid, distance,"
                    + " logdate FROM logmessage;";
            try ( ResultSet rs = stmt.executeQuery(sqlQuery) )   {
                ArrayList<String> resultStrings = this.resultSetToArrayList(rs);
                for ( String s : resultStrings ) {
                    String[] columns = s.split(":");
                    int logMessageID = Integer.parseInt(columns[0]);
                    int sessionID = Integer.parseInt(columns[1]);
                    String message = columns[2];
                    int packageID = Integer.parseInt(columns[3]);
                    double distance = Double.valueOf(columns[4]);
                    Long time = Long.parseLong(columns[5]);
                    Date logdate = new Date(time);
                    
                    Log l = new Log(logMessageID, sessionID, message, packageID, distance, logdate);
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
    
    
  /*  public ResultSet getItems() {
        try {
            Connection connection = getConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT packageid FROM package ORDER BY packageid DESC LIMIT 1;";
            ResultSet rs = stmt.executeQuery(sql);
            String retval;
            if ( rs.next() ) {
                retval = rs.getString(1);
                return retval;
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    */
    /*
    public void closeResources(Connection c, Statement s, PreparedStatement ps, ResultSet rs) {
        if ( c != null )
            try {
                c.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        if ( s != null )
            try {
                s.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        if ( ps != null )
            try {
                ps.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        if ( rs != null )
            try {
                rs.close();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
    }
    */
    
    public void removeStorage(Storage s) {
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM storage WHERE name = ?;");
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
    
    // remove packages from arg Storage
    public void removePackage(Storage s) {
        try (
                Connection c = getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM package WHERE storage = ?;");
        ) {
            try {
            ps.setString(1, s.getName());
            ps.executeQuery();
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public void clearDatabaseTables() {
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        )   {
                ArrayList<String> tables = getTables();
                for ( String table : tables ) {
                    String sqlQuery = "DELETE FROM " + table + " WHERE 1=1;";
                    stmt.executeUpdate(sqlQuery);
                }
            }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
    
    //Return false if failed
    public boolean addPackage(Package p, String storage) {
        //Add package first
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO package"
                    + "(packageid, senderid, receiverid, storage, class) VALUES "
                    + "(?, ?, ?, ?, ?);"); 
        ) {
            ps.setInt(1, p.getPackageID());
            ps.setInt(2, p.getReceiverID());
            ps.setInt(3, p.getSenderID());
            ps.setString(4, storage);
            ps.setInt(5, p.getPackageClass());
          
            ps.executeUpdate();
          }
        catch(SQLException e) {
            e.printStackTrace();
            return false;
        }
        //then add items
        for ( Item i : p.getItems() ) {
            this.addPackagedItem(p, i);
        }
        
        return true;
    }
    
    public void addPackagedItem(Package p, Item i) {
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("INSERT INTO packageditem("
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
            PreparedStatement ps = c.prepareStatement("INSERT INTO logmessage("
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
    public int getNewSessionID() {
        int sessionID = -1;
        try (
            Connection c = getConnection();
            Statement stmt = c.createStatement();
        ) {
            try ( ResultSet rs = stmt.executeQuery("SELECT sessionid "
                    + "FROM logmessage "
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
    
    public ArrayList<String> getTables() {
        ArrayList<String> retVal = null;
        try ( 
            Connection c = getConnection();
            Statement stmt = c.createStatement()
        ) {
            try ( ResultSet rs = stmt.executeQuery("SELECT name FROM sqlite_master "
                    + "WHERE type = 'table'");
            ) {
                retVal = resultSetToArrayList(rs);
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
                    newStorage.updatePackages();
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
                        ArrayList<Item> items = getPackagedItems(packageid);
                        Item[] itemList = items.toArray(new Item[items.size()]);

                        Package newPackage = pb.createPackage(packageid, packageClass, senderid, receiverid, itemList);
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
    
    public ArrayList<Item> getPackagedItems(int packageID) {
        ArrayList<Item> items = new ArrayList<>();
        try (
            Connection c = getConnection();
            PreparedStatement ps = c.prepareStatement("SELECT "
                    + "name, size, weight, broken, breakable "
                    + "FROM packageditem WHERE packageID = ?;");
        ) {
            ps.setInt(1, packageID);
            try ( ResultSet rs = ps.executeQuery(); ) {
                ArrayList<String> resultStrings = this.resultSetToArrayList(rs);

                for ( String result : resultStrings ) {
                    String[] columns = result.split(":");
                    String name = columns[0];
                    int size = Integer.parseInt(columns[1]);
                    int weight = Integer.parseInt(columns[2]);

                    boolean broken;
                    broken = Integer.parseInt(columns[3]) != 0;
                    boolean breakable;
                    breakable = Integer.parseInt(columns[4]) != 0;


                    Item newItem = new Item(name, size, weight, broken, breakable);
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
                if ( rs.isBeforeFirst() ) {
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
    
    
    
    public ArrayList<Item> getSelectableItems() {
        ArrayList<Item> itemList = null;
        try {
            Connection connection = getConnection();
            Statement stmt = connection.createStatement();
            String sql = "SELECT name, size, weight, breakable FROM selectableitem;";
            ArrayList<String> itemData = resultSetToArrayList(stmt.executeQuery(sql));
            itemList = new ArrayList<>();
            
            //
            for ( String item : itemData ) {
                String[] itemDetails = item.split(":");
                int size = Integer.parseInt(itemDetails[1]);
                int weight = Integer.parseInt(itemDetails[2]);
                boolean breakable;
                breakable = !itemDetails[3].equals("0");
                
                Item newItem = new Item(itemDetails[0], size, weight, breakable);
                itemList.add(newItem);
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return itemList;
    }    
    
    
    //not safe
    public ArrayList<String> genericSelect(String select, String from, String where) {
        ArrayList<String> resultStrings = null;
        try ( Connection c = getConnection() ) {
            String sqlQuery = 
                    "SELECT " + select + " FROM " + from + " WHERE 1=1 " + where + ";";
            try ( Statement stmt = c.createStatement() ) {
                resultStrings = resultSetToArrayList(stmt.executeQuery(sqlQuery));
            }
            catch(SQLException innerE) {
                innerE.printStackTrace();
            }
        }
        catch(SQLException e) {
            e.printStackTrace();
        }
        return resultStrings;
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
    
    

    public void breakItem(Item i) {
        int itemID = i.getItemID();
        
        try {
            Connection conn = this.getConnection();
            String sqlQuery = "UPDATE packageditem SET broken = 1 WHERE itemid = ? AND breakable = 1;";
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
    
    
    /* turha,.,?
    public int countGeneric(String column) throws SQLException {
        Connection connection = getConnection();
        Statement stmt = connection.createStatement();
        String sql = "SELECT COUNT(*) FROM " + column + ";";
        ResultSet rs = stmt.executeQuery(sql);
        return rs.getHoldability();
    } */
}
