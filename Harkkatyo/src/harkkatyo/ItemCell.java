/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

import javafx.scene.control.ListCell;

/**
 *
 * @author m8581
 */

/**
 * moiioio.lfal...... button/panepressed
 *
 *
 */


public class ItemCell extends ListCell<Item> {      
    @Override
    public void updateItem(Item i, boolean empty) {
        super.updateItem(i, empty);

        if ( empty ) {
            setText(null);
            setGraphic(null);
        }
        else {
            setText(i.getItemDetails());
            setGraphic(null);
        }
    }
}