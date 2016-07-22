/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

import java.util.ArrayList;

abstract public class Item {
    public static int ItemCounter;
    private int ItemID;
    private final String itemName;
    private final int size;
    private final int weight;
    private boolean broken;
    private boolean breakable;

    public Item(String itemName, int size, int weight, boolean breakable) {
        this.ItemID = -1;
        this.itemName = itemName;
        this.size = size;
        this.weight = weight;
        this.broken = false;
        this.breakable = breakable;
    }
    
    public Item(String itemName, int size, int weight, boolean broken, boolean breakable) {
        this.ItemID = -1;
        this.itemName = itemName;
        this.size = size;
        this.weight = weight;
        this.broken = broken;
        this.breakable = breakable;
    }
    
    public Item(int itemID, String itemName, int size, int weight, boolean breakable) {
        this.ItemID = itemID;
        this.itemName = itemName;
        this.size = size;
        this.weight = weight;
        broken = false;
        this.breakable = breakable;
    }

    public Item(int itemID, String itemName, int size, int weight, boolean broken, boolean breakable) {
        this.ItemID = itemID;
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
        DatabaseHandler db = DatabaseHandler.getInstance();
        if ( this.breakable )    
            db.breakItem(this);
            this.broken = true;
    }
    
    public int getItemID() {
        return ItemID;
    }
    
    public String getItemDetails() {
        String breakableString = "";
        if ( breakable ) {
            breakableString = "särkyvä";
        }
        else {
            breakableString = "ei särkyvä";
        }
        if ( broken ) {
            breakableString += ", särkynyt";
        }
        else {
            breakableString += ", ehjä";
        }
        
            
        String itemDetails = itemName + ", " + size + " cm3, " + weight + " kg, " + breakableString;
        return itemDetails;
    }
}

class Cat extends Item {
    public Cat() {
        super("Kissa", 30, 8, true);
    }
    
    public Cat(boolean broken) {
        super("Kissa", 30, 8, broken, true);
    }
    
    public Cat(int itemID, boolean broken) {
        super(itemID, "Kissa", 30, 8, broken, true);
    }
}

class Dog extends Item {
    public Dog() {
        super("Koira", 40, 12, true);
    }
    
    public Dog(boolean broken) {
        super("Koira", 40, 12, broken,true);
    }
    
    public Dog(int itemID, boolean broken) {
        super(itemID, "Koira", 40, 12, broken, true);
    }
}

class Door extends Item {
    public Door() {
        super("Ovi", 120, 10, false);
    }
    
    public Door(boolean broken) {
        super("Ovi", 120, 10, broken, false);
    }
    
    public Door(int itemID, boolean broken) {
        super(itemID, "Ovi", 120, 10, broken, false);
    }
}

class Gold extends Item {
    public Gold() {
        super("Kultaharkko", 20, 50, false);
    }
    
    public Gold(boolean broken) {
        super("Kultaharkko", 20, 50, broken, false);
    }
    
    public Gold(int itemID, boolean broken) {
        super(itemID, "Kultaharkko", 20, 50, broken, false);
    }
}

class ItemBuilder {
    public ItemBuilder() {
    }
    
    public Item CreateItem(String itemName) {
        Item i = null;

        if (itemName.equals("Kissa")) {
            i = new Cat();
        }
        if (itemName.equals("Koira")) {
            i = new Dog();
        }
        if (itemName.equals("Ovi")) {
            i = new Door();
        }
        if (itemName.equals("Kultaharkko")) {
            i = new Gold();
        }

        return i;
    }
    
    public Item CreateItem(String itemName, boolean broken) {
        Item i = null;

        if (itemName.equals("Kissa")) {
            i = new Cat(broken);
        }
        if (itemName.equals("Koira")) {
            i = new Dog(broken);
        }
        if (itemName.equals("Ovi")) {
            i = new Door(broken);
        }
        if (itemName.equals("Kultaharkko")) {
            i = new Gold(broken);
        }

        return i;
    }
    
    public Item CreateItem(int itemID, String itemName, boolean broken) {
        Item i = null;

        if (itemName.equals("Kissa")) {
            i = new Cat(itemID, broken);
        }
        if (itemName.equals("Koira")) {
            i = new Dog(itemID, broken);
        }
        if (itemName.equals("Ovi")) {
            i = new Door(itemID, broken);
        }
        if (itemName.equals("Kultaharkko")) {
            i = new Gold(itemID, broken);
        }

        return i;
    }
    
    public ArrayList<Item> createAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Cat());
        items.add(new Dog());
        items.add(new Door());
        items.add(new Gold());
        return items;
    }
}