/* 
 * Tekijä: Aleksi Ruokoniemi
 * Oppilasnumero: 0452334
 */


package harkkatyo;

import javafx.scene.control.ListCell;

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