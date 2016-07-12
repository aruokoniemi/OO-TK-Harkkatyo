package harkkatyo;

abstract public class Package {
    protected Item[] items;
    protected int contentSize;
    protected int contentWeight;
    protected int packageID;
    
    protected int senderID;
    protected int receiverID;
    
    protected int packageClass;
    protected boolean breaksItems;
    static public int packageCounter;

    private double maxDistance;
    private int maxSize;
    private int maxWeight;
    
    public Package(int packageID, int classNr, int senderID, int receiverID, boolean breaksItems, Item... i) {
        this.packageID = packageID;
        packageClass = classNr;
        this.senderID = senderID;
        this.receiverID = receiverID;
        this.breaksItems = breaksItems;
        items = new Item[i.length];
        System.arraycopy(i, 0, items, 0, i.length);
        
        for ( Item tempItem : items ) {
            contentSize += tempItem.getSize();
            contentWeight += tempItem.getWeight();
        }
        
        packageID = packageCounter;
        packageCounter++;
    }    
    
    public void breakItems() {
        if ( breaksItems )
            for ( Item i: items ) {
                i.breakItem();
            }
    }
    
    public boolean testLimits(double distanceTravelled) {
        if ( contentSize > maxSize )
            return false;
        if ( contentWeight > maxWeight )
            return false;
        if ( distanceTravelled > maxDistance ) 
            return false;
        
        return true;
    }
    
    public Item[] getItems() {
        return items;
    }
    
    public int getPackageClass() {
        return packageClass;
    }
    
    public int getPackageID() {
        return packageID;
    }

    public int getSenderID() {
        return senderID;
    }
    
    public int getReceiverID() {
        return receiverID;
    }
}

class PackageClass1 extends Package {
    final static public int maxDistance = 200;
    final static public int maxSize = 10;
    static public int maxWeight = 10;
    
    public PackageClass1(int packageID, int SenderID, int ReceiverID, Item... i) {
        super(packageID, 1, SenderID, ReceiverID, true, i);
    }    
} 

class PackageClass2 extends Package {
    final static public int maxDistance = 300;
    final static public int maxSize = 20;
    final static public int maxWeight = 20;
    
    public PackageClass2(int packageID, int SenderID, int ReceiverID, Item... i) {
        super(packageID, 2, SenderID, ReceiverID, false, i);
    }    
} 

class PackageClass3 extends Package {
    final static public int maxDistance = 400;
    final static public int maxSize = 30;
    final static public int maxWeight = 30;
    
    public PackageClass3(int packageID, int SenderID, int ReceiverID, Item... i) {
        super(packageID, 3, SenderID, ReceiverID, true, i);
    }    
} 

class PackageBuilder {
    public PackageBuilder() {}
    
    public Package createPackage(int packageID, int classNr, int SenderID, int ReceiverID, Item... i) {
        Package tempPackage = null;
        
        if ( classNr == 1 ) {
            tempPackage = new PackageClass1(packageID, SenderID, ReceiverID, i);
        }
        else if ( classNr == 2 ) {
            tempPackage = new PackageClass2(packageID, SenderID, ReceiverID, i);
        }
        else if ( classNr == 3) {
            tempPackage = new PackageClass3(packageID, SenderID, ReceiverID, i);
        }
        return tempPackage;
    }
}