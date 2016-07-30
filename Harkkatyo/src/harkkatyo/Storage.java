/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */

package harkkatyo;

import java.util.ArrayList;

public class Storage {
    String name;
    
    public Storage(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    public void addPackage(Package p) {
        DatabaseHandler db = DatabaseHandler.getInstance();
        db.addPackage(p, this);
    }
    
    public ArrayList<Package> getPackages() {
        DatabaseHandler db = DatabaseHandler.getInstance();
        return db.getPackages(name);
    }
}
