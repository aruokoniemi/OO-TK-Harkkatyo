package harkkatyo;

import java.util.ArrayList;

public class Storage {
    String name;
    ArrayList<Package> packages;
    
    public Storage(String name) {
        this.name = name;
        this.updatePackages();
    }
    
    public String getName() {
        return name;
    }
    
    public final void updatePackages() {
        Database db = Database.getInstance();
        this.packages = db.getPackages(name);
    }
    
    public ArrayList<Package> getPackages() {
        return packages;
    }
}
