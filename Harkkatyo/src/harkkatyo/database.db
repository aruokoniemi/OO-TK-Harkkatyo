PRAGMA foreign_keys=OFF;
BEGIN TRANSACTION;
CREATE TABLE package (
    PackageID INTEGER PRIMARY KEY UNIQUE NOT NULL,
    class INTEGER NOT NULL,
    
    CHECK (class in (1,2,3))
);
CREATE TABLE item (
    ItemID INTEGER PRIMARY KEY UNIQUE NOT NULL,
    name VARCHAR(40),
    size INTEGER NOT NULL,
    weight INTEGER NOT NULL,    
    breakable INTEGER DEFAULT 0
);
CREATE TABLE includes (
    PackageID INTEGER,
    ItemID INTEGER,
    
    FOREIGN KEY (PackageID) REFERENCES package(PackageID),
    FOREIGN KEY (ItemID) REFERENCES item(ItemID)
);
CREATE TABLE city (
    cityID INTEGER PRIMARY KEY UNIQUE NOT NULL,
    name VARCHAR(20) NOT NULL UNIQUE
);
CREATE TABLE storage (
    StorageID INTEGER PRIMARY KEY UNIQUE NOT NULL,
    name VARCHAR(20),
    city VARCHAR(20),

    FOREIGN KEY (city) REFERENCES city(name)  
);
CREATE TABLE road (
    RoadID INTEGER PRIMARY KEY UNIQUE NOT NULL,
    FirstSmartPostID INTEGER,
    SecondSmartPostID INTEGER,
    distance INTEGER NOT NULL,

    FOREIGN KEY (FirstSmartPostID) REFERENCES smartpost(SmartPostID),
    FOREIGN KEY (SecondSmartPostID) REFERENCES smartpost(SmartPostID)
);
CREATE TABLE postoffice (
    PostOfficeID INTEGER PRIMARY KEY UNIQUE NOT NULL,    
    name VARCHAR(20),
    city VARCHAR(20),

    FOREIGN KEY (city) REFERENCES city(name)
);
CREATE TABLE smartpost (
    SmartPostID INTEGER PRIMARY KEY UNIQUE NOT NULL,
    address VARCHAR(30) NOT NULL,
    city VARCHAR(20),
    postoffice VARCHAR(30) NOT NULL,
    availability VARCHAR(50),
    latitude VARCHAR(20) NOT NULL,
    longitude VARCHAR(20) NOT NULL,

    FOREIGN KEY (city) REFERENCES city(name),
    FOREIGN KEY (postoffice) REFERENCES postoffice(name)
);
CREATE TABLE user (
    UserID INTEGER PRIMARY KEY UNIQUE NOT NULL,
    name VARCHAR(40) NOT NULL,
    address VARCHAR(30) NOT NULL,
    city VARCHAR(20),

    FOREIGN KEY (city) REFERENCES city(name)
);
CREATE TABLE shipment (
    ShipmentID INTEGER PRIMARY KEY UNIQUE NOT NULL,    
    SenderID INTEGER NOT NULL,
    ReceiverID INTEGER NOT NULL,
    SenderSmartPostID INTEGER NOT NULL,
    ReceiverSmartPostID INTEGER NOT NULL,
    PackageID INTEGER NOT NULL,

    FOREIGN KEY (SenderID) REFERENCES user(UserID),
    FOREIGN KEY (ReceiverID) REFERENCES user(UserID),
    FOREIGN KEY (PackageID) REFERENCES package(PackageID)
    FOREIGN KEY (SenderSmartPostID) REFERENCES smartpost(SmartPostID),
    FOREIGN KEY (ReceiverSmartPostID) REFERENCES smartpost(SmartPostID)
);
COMMIT;
