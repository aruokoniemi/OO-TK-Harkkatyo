/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

/**
 *
 * @author m8581
 */
public class PackageBuilder {
    public PackageBuilder() {
    }

    public Package createPackage(int packageID, int classNr, Item... i) {
        Package tempPackage = null;

        if (classNr == 1) {
            tempPackage = new PackageClass1(packageID, i);
        } else if (classNr == 2) {
            tempPackage = new PackageClass2(packageID, i);
        } else if (classNr == 3) {
            tempPackage = new PackageClass3(packageID, i);
        }
        return tempPackage;
    }    
    
    public Package createPackage(int packageID, int classNr, int SenderID, int ReceiverID, Item... i) {
        Package tempPackage = null;

        if (classNr == 1) {
            tempPackage = new PackageClass1(packageID, SenderID, ReceiverID, i);
        } else if (classNr == 2) {
            tempPackage = new PackageClass2(packageID, SenderID, ReceiverID, i);
        } else if (classNr == 3) {
            tempPackage = new PackageClass3(packageID, SenderID, ReceiverID, i);
        }
        return tempPackage;
    }
}
