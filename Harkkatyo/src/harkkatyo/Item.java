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
        super("Kissa", 15000, 8, true);
    }
    
    public Cat(boolean broken) {
        super("Kissa", 15000, 8, broken, true);
    }
    
    public Cat(int itemID, boolean broken) {
        super(itemID, "Kissa", 15000, 8, broken, true);
    }
}

class Stone extends Item {
    public Stone() {
        super("Kivi", 1000, 1, false);
    }
    
    public Stone(boolean broken) {
        super("Kivi", 1000, 1, broken, false);
    }
    
    public Stone(int itemID, boolean broken) {
        super(itemID, "Kivi", 1000, 1, broken, false);
    }
}

class Computer extends Item {
    public Computer() {
        super("Tietokone", 15000, 6, true);
    }
    
    public Computer(boolean broken) {
        super("Tietokone", 15000, 6, broken, true);
    }
    
    public Computer(int itemID, boolean broken) {
        super(itemID, "Tietokone", 120, 10, broken, true);
    }
}

class Book extends Item {
    public Book() {
        super("Kirja", 1200, 1, false);
    }
    
    public Book(boolean broken) {
        super("Kirja", 1200, 1, broken, false);
    }
    
    public Book(int itemID, boolean broken) {
        super(itemID, "Kirja", 1200, 1, broken, false);
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
        if (itemName.equals("Kivi")) {
            i = new Stone();
        }
        if (itemName.equals("Tietokone")) {
            i = new Computer();
        }
        if (itemName.equals("Kirja")) {
            i = new Book();
        }

        return i;
    }
    
    public Item CreateItem(String itemName, boolean broken) {
        Item i = null;

        if (itemName.equals("Kissa")) {
            i = new Cat(broken);
        }
        if (itemName.equals("Kivi")) {
            i = new Stone(broken);
        }
        if (itemName.equals("Tietokone")) {
            i = new Computer(broken);
        }
        if (itemName.equals("Kirja")) {
            i = new Book(broken);
        }

        return i;
    }
    
    public Item CreateItem(int itemID, String itemName, boolean broken) {
        Item i = null;

        if (itemName.equals("Kissa")) {
            i = new Cat(itemID, broken);
        }
        if (itemName.equals("Kivi")) {
            i = new Stone(itemID, broken);
        }
        if (itemName.equals("Tietokone")) {
            i = new Computer(itemID, broken);
        }
        if (itemName.equals("Kirja")) {
            i = new Book(itemID, broken);
        }

        return i;
    }
    
    public ArrayList<Item> createAllItems() {
        ArrayList<Item> items = new ArrayList<>();
        items.add(new Cat());
        items.add(new Stone());
        items.add(new Computer());
        items.add(new Book());
        return items;
    }
}