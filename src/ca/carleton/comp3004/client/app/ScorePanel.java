package ca.carleton.comp3004.client.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panel for the scoring area.
 */
public class ScorePanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(ScorePanel.class);

    private final YahtzeeClient client;

    public BoardEntry[] upperButtons;

    public BoardEntry[] lowerButtons;

    public JLabel[] upperLabels;

    public JLabel[] lowerLabels;

    public JLabel upperSum;

    public JLabel upperBonus;

    public JButton noScore;

    /**
     * Create the score panel.
     *
     * @param client the game client to communicate with.
     */
    public ScorePanel(final YahtzeeClient client) {
        this.client = client;
        this.setLayout(null);
        this.setupUpperScoring();
        this.setupLowerScoring();
        this.setupListeners();
    }

    /**
     * Reset the panel.
     */
    public void reset() {
        this.disableButtons();
        this.updateText();
    }

    /**
     * Setup the upper scoring block.
     */
    private void setupUpperScoring() {
        this.upperLabels = new JLabel[6];
        for (int i = 0; i < this.upperLabels.length; i++) {
            this.upperLabels[i] = new JLabel("PLACEHOLDER");
            this.upperLabels[i].setSize(new Dimension(100, 15));
            this.upperLabels[i].setLocation(50, 15 + i * 15);
            this.add(this.upperLabels[i]);
        }

        this.upperButtons = new BoardEntry[6];
        for (int i = 0; i < this.upperButtons.length; i++) {
            this.upperButtons[i] = new BoardEntry("0");
            this.upperButtons[i].setSize(new Dimension(50, 15));
            this.upperButtons[i].setLocation(210, 15 + i * 15);
            this.upperButtons[i].setEnabled(false);
            this.add(this.upperButtons[i]);
        }

        this.upperBonus = new JLabel("Bonus: 0");
        this.upperBonus.setSize(new Dimension(50, 15));
        this.upperBonus.setLocation(50, 120);
        this.add(this.upperBonus);

        this.upperSum = new JLabel("Sum: 0");
        this.upperSum.setSize(new Dimension(50, 15));
        this.upperSum.setLocation(210, 120);
        this.add(this.upperSum);

        this.upperLabels[0].setText("Ones");
        this.upperLabels[1].setText("Twos");
        this.upperLabels[2].setText("Threes");
        this.upperLabels[3].setText("Fours");
        this.upperLabels[4].setText("Fives");
        this.upperLabels[5].setText("Sixes");

    }

    /**
     * Setup the lower scoring block.
     */
    public void setupLowerScoring() {
        this.lowerLabels = new JLabel[7];
        for (int i = 0; i < this.lowerLabels.length; i++) {
            this.lowerLabels[i] = new JLabel("PLACEHOLDER");
            this.lowerLabels[i].setSize(new Dimension(100, 15));
            this.lowerLabels[i].setLocation(50, 150 + i * 15);
            this.add(this.lowerLabels[i]);
        }

        this.lowerButtons = new BoardEntry[7];
        for (int i = 0; i < this.lowerButtons.length; i++) {
            this.lowerButtons[i] = new BoardEntry("0");
            this.lowerButtons[i].setSize(new Dimension(50, 15));
            this.lowerButtons[i].setLocation(210, 150 + i * 15);
            this.lowerButtons[i].setEnabled(false);
            this.add(this.lowerButtons[i]);
        }

        this.noScore = new JButton("Submit no score for round");
        this.noScore.setSize(new Dimension(200, 15));
        this.noScore.setLocation(100, 300);
        this.noScore.setEnabled(false);
        this.add(this.noScore);

        this.lowerLabels[0].setText("Three of a Kind");
        this.lowerLabels[1].setText("Four of a Kind");
        this.lowerLabels[2].setText("Full House");
        this.lowerLabels[3].setText("Small Straight");
        this.lowerLabels[4].setText("Large Straight");
        this.lowerLabels[5].setText("Chance");
        this.lowerLabels[6].setText("YAHTZEE");
    }

    /**
     * Register action listeners for all the buttons on this panel.
     */
    private void setupListeners() {
        final ActionListener submitResult = new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                int chosenValue = Integer.parseInt(((JButton) e.getSource()).getText());

                if (JOptionPane.showConfirmDialog(ScorePanel.this, "Are you sure?") == JOptionPane.YES_OPTION) {
                    ScorePanel.this.client.scoreToSend = chosenValue;

                    for (int i = 0; i < ScorePanel.this.upperButtons.length; i++) {
                        if (e.getSource() == ScorePanel.this.upperButtons[i]) {
                            ScorePanel.this.client.scoringArea = i;
                            ScorePanel.this.client.scoringPart = 0;
                        }
                    }
                    for (int i = 0; i < ScorePanel.this.lowerButtons.length; i++) {
                        if (e.getSource() == ScorePanel.this.lowerButtons[i]) {
                            ScorePanel.this.client.scoringArea = i;
                            ScorePanel.this.client.scoringPart = 1;
                        }
                    }

                    LOG.info("User chose score {} to send.", ScorePanel.this.client.scoreToSend);
                    ScorePanel.this.disableButtons();
                    ScorePanel.this.client.submitRoundScoreToServer();

                } else {
                    // do nothing
                    LOG.info("Continuing selection.");
                }

            }
        };

        for (final JButton button : this.upperButtons) {
            button.addActionListener(submitResult);
        }
        for (final JButton button : this.lowerButtons) {
            button.addActionListener(submitResult);
        }

        this.noScore.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (JOptionPane.showConfirmDialog(ScorePanel.this, "Are you sure?") == JOptionPane.YES_OPTION) {
                    LOG.info("User chose to forfeit this round.");
                    ScorePanel.this.disableButtons();
                    ScorePanel.this.client.submitNoScoreForRound();
                }
            }
        });
    }

    /**
     * Update the button texts with the correct values.
     */
    private void updateText() {
        for (final BoardEntry button : this.upperButtons) {
            if (Integer.parseInt(button.getText()) != 0 && button.playerID == YahtzeeClient.NOT_TAKEN) {
                button.setText("0");
            }
        }
        for (final BoardEntry button : this.lowerButtons) {
            if (Integer.parseInt(button.getText()) != 0 && button.playerID == YahtzeeClient.NOT_TAKEN) {
                button.setText("0");
            }
        }
    }

    /**
     * Disable all buttons on the panel.
     */
    private void disableButtons() {
        for (final BoardEntry button : this.upperButtons) {
            button.setEnabled(false);
        }
        for (final BoardEntry button : this.lowerButtons) {
            button.setEnabled(false);
        }

        this.noScore.setEnabled(false);
    }
}
