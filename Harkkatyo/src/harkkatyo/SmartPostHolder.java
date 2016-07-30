/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */

package harkkatyo;

import java.util.ArrayList;

public class SmartPostHolder {
    private ArrayList<SmartPost> smartposts;

    public SmartPostHolder() {
        smartposts = new ArrayList<>();
    }

    public void addSmartPost(SmartPost sp) {
        for (SmartPost sp2 : smartposts) {
            if (sp.getID() == sp2.getID()) {
                return;
            }
        }
        smartposts.add(sp);
    }

    public void clearSmartPostList() {
        smartposts.clear();
    }

    public SmartPost getSmartPost(int SmartPostID) {
        for (SmartPost sp : smartposts) {
            if (sp.getID() == SmartPostID) {
                return sp;
            }
        }
        return null;
    }
    
    public ArrayList<SmartPost> getSmartPosts() {
        return smartposts;
    }
}
