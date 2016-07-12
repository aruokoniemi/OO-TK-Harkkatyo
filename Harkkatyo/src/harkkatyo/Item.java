/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

public class Item {
    public static int ItemCounter;
    private final int ItemID;
    private final String itemName;
    private final int size;
    private final int weight;
    private boolean broken;
    private boolean breakable;
    
    public Item (String itemName, int size, int weight, boolean breakable) {
        ItemID = ItemCounter;
        ItemCounter++;
        this.itemName = itemName;
        this.size = size;
        this.weight = weight;
        broken = false;
        this.breakable = breakable;
    }
    
    public Item (String itemName, int size, int weight, boolean broken, boolean breakable) {
        ItemID = ItemCounter;
        ItemCounter++;
        this.itemName = itemName;
        this.size = size;
        this.weight = weight;
        this.broken = broken;
        this.breakable = breakable;
    }
    
    public int getSize() {
        return size;
    }
    
    public String getName() {
        return itemName;
    }
    
    public int getWeight() {
        return weight;
    } 
    
    public boolean isBreakable() {
        return breakable;
    }
    
    public boolean isBroken() {
        return broken;
    }
    
    public void breakItem() {
        Database db = Database.getInstance();
        if ( this.breakable )    
            db.breakItem(this);
            this.broken = true;
    }
    
    public int getItemID() {
        return ItemID;
    }
    
    public String getDetails() {
        String booleanString;
        if ( breakable = true )
            booleanString = "rikkoutuva";
        else
            booleanString = "ei rikkoutuva";
       
            
        String itemDetails = itemName + ", " + size + " cm3, " + weight + " kg, " + booleanString;
        
        return itemDetails;
    }
}
