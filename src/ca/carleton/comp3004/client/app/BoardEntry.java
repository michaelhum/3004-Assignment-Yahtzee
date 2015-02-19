package ca.carleton.comp3004.client.app;

import javax.swing.*;

/**
 * Extension of JButton that contains who owns the current entry, as well as its value.
 */
public class BoardEntry extends JButton {

    public int playerID;

    public int value;

    public BoardEntry(final String text) {
        this.playerID = YahtzeeClient.NOT_TAKEN;
        this.value = 0;
        this.setText(text);
    }
}