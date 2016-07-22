/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package harkkatyo;

import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;

/**
 *
 * @author m8581
 */
public class CustomBox extends HBox {
    private Label itemText;
    private Button button;
    
    public CustomBox(Label l) {
        super(40);
        this.itemText = l;
        this.getChildren().add(itemText);
    }
    
    public CustomBox(Label l, Button b) {
        super(40);
        this.itemText = l;
        this.button = b;
        this.getChildren().addAll(itemText, button);
    }
}
