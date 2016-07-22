/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

import java.util.ArrayList;

/**
 *
 * @author m8581
 */
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
