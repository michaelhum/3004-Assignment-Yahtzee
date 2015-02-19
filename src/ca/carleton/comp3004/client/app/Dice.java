package ca.carleton.comp3004.client.app;

import javax.swing.*;

/**
 * Extension of JButton that also contains the value of the dice and whether or not is is being set aside.
 */
public class Dice extends JButton {

    private int value = 0;

    private boolean locked = false;

    public Dice(final ImageIcon icon) {
        super(icon);
    }

    public boolean isLocked() {
        return this.locked;
    }

    public int getValue() {
        return this.value;
    }

    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    public void setValue(final int value) {
        this.value = value;
    }

}
