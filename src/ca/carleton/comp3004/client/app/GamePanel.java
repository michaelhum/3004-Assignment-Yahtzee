package ca.carleton.comp3004.client.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main panel for the game. Contains the dice, buttons, etc.
 */
public class GamePanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(GamePanel.class);

    private static boolean messageShown = false;

    public Dice[] dice;

    public ScorePanel scorePanel;

    private final YahtzeeClient client;

    private ImageIcon[] images;

    private JButton rollButton;

    private JButton scoreButton;

    private JLabel roundInfoLabel;

    /**
     * Create the panel and setup the game.
     *
     * @param client the game client.
     */
    public GamePanel(final YahtzeeClient client) {
        this.client = client;
        this.loadLayout();
        this.loadImages();
        this.loadDice();
        this.loadRollButton();
        this.loadScoreButton();
        this.loadLabels();
        this.loadScoringPanel();
    }

    /**
     * Start the next round (enable the roll button).
     */
    public void startNextRound() {
        LOG.info("Starting next round...");
        this.rollButton.setEnabled(true);
    }

    /**
     * Reset the game board.
     */
    public void reset() {
        LOG.info("Resetting the board.");
        this.disableScoring();
        this.scorePanel.reset();
        this.resetDicePosition();
        if (!messageShown) {
            JOptionPane.showMessageDialog(this, "Round is now over. The roll button will re-enable when the server confirms all players are ready. This message will not repeat.");
            messageShown = true;
        }
        // we wait on rolling the dice again until a round_start has been received.
    }

    /**
     * Reset the game board for resubmission.
     */
    public void retrySubmission() {
        LOG.info("Resetting the board for resubmission.");
        this.disableScoring();
        this.scorePanel.reset();
        this.resetDicePosition();
        this.rollButton.setEnabled(true);
        this.client.rollsRemaining = 3;
        this.updateRoundInfo();
        JOptionPane.showMessageDialog(this, "Someone else entered there! You'll need to re-submit a new score. Please re-roll and re-score.");
    }

    /**
     * Update a board entry from the server.
     *
     * @param playerID    the player who now owns the entry.
     * @param scoringPart the portion of the scoring area.
     * @param scoringArea the actual entry.
     * @param score       the score.
     */
    public void updateBoardEntry(final int playerID, final int scoringPart, final int scoringArea, final int score) {
        LOG.info("Updating board entry with {}, {}, {}, {}.", playerID, scoringPart, scoringArea, score);
        if (scoringPart == 0) {
            this.scorePanel.upperButtons[scoringArea].value = score;
            this.scorePanel.upperButtons[scoringArea].setText(String.valueOf(score));
            this.scorePanel.upperButtons[scoringArea].playerID = playerID;
            this.scorePanel.upperButtons[scoringArea].setEnabled(false);
        } else {
            this.scorePanel.lowerButtons[scoringArea].value = score;
            this.scorePanel.lowerButtons[scoringArea].setText(String.valueOf(score));
            this.scorePanel.lowerButtons[scoringArea].playerID = playerID;
            this.scorePanel.lowerButtons[scoringArea].setEnabled(false);
        }
    }

    /**
     * Update the label with the info from the client.
     */
    public void updateRoundInfo() {
        this.roundInfoLabel.setText(this.client.getRoundInfo());
        this.scorePanel.upperSum.setText(String.format("Sum: %d", this.client.getUpperSum()));
    }

    /**
     * Updates the game state for "round over."
     */
    public void updateRoundOver() {
        for (int i = 0; i < this.dice.length; i++) {
            this.dice[i].setLocation(75 + i * 50, 180);
            this.dice[i].setEnabled(false);
        }

        this.rollButton.setEnabled(false);
        this.client.setRollData(this.dice);
        this.client.setPossiblePoints();
        this.enableScoring();
    }

    /**
     * Show message for the winner.
     */
    public void updateGameOverWinner() {
        JOptionPane.showMessageDialog(this, "Congratulations! The server has reported game over, and you won with a score of " + String.valueOf(this.client.totalScore) + "."
                + "\nThe client will now close.");
        this.client.gameOver();
    }

    /**
     * Show message if lost.
     */
    public void updateGameOverLoser() {
        JOptionPane.showMessageDialog(this, "Unfortunately, the server has reported that you did not win. Your final score was " + String.valueOf(this.client.totalScore) + "."
                + "\nThe client will now close.");
        this.client.gameOver();
    }

    /**
     * Enables the scoring field, allowing the user to make entries.
     */
    private void enableScoring() {
        this.rollButton.setEnabled(false);
        this.scoreButton.setEnabled(false);
        this.scorePanel.setEnabled(true);
        this.scorePanel.setBackground(Color.WHITE);
        this.scorePanel.noScore.setEnabled(true);
    }

    /**
     * Disable the scoring field.
     */
    private void disableScoring() {
        this.scorePanel.setEnabled(false);
        this.scorePanel.setBackground(Color.GRAY);
    }

    /**
     * Set the layout.
     */
    private void loadLayout() {
        // manual layout.
        this.setLayout(null);
    }

    /**
     * Load the dice images.
     */
    private void loadImages() {
        this.images = new ImageIcon[6];
        for (int i = 0; i < this.images.length; i++) {
            String image = "/images/" + String.valueOf(i + 1) + ".png";
            this.images[i] = new ImageIcon(this.getClass().getResource(image));
        }
    }

    /**
     * Load the dice locations and register their handlers.
     */
    private void loadDice() {
        this.dice = new Dice[5];
        for (int i = 0; i < this.dice.length; i++) {
            this.dice[i] = new Dice(this.images[i]);
            this.dice[i].setSize(new Dimension(50, 50));
            this.dice[i].setEnabled(false);
            this.dice[i].setLocation(75 + i * 50, 45);

            final int innerI = i;
            this.dice[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent event) {
                    GamePanel.this.dice[innerI].setLocked(!GamePanel.this.dice[innerI].isLocked());
                    GamePanel.this.updateDiceLocation(innerI);
                }
            });

            this.add(this.dice[i]);
        }
    }

    /**
     * Reset the dice position to their default.
     */
    private void resetDicePosition() {
        for (int i = 0; i < this.dice.length; i++) {
            this.dice[i].setEnabled(false);
            this.dice[i].setLocation(75 + i * 50, 45);
            this.dice[i].setLocked(false);
        }
    }

    /**
     * Load the roll button and register the handler.
     */
    private void loadRollButton() {
        this.rollButton = new JButton("Click to roll the dice.");
        this.rollButton.setSize(new Dimension(250, 30));
        this.rollButton.setLocation(75, 100);

        this.rollButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                if (GamePanel.this.client.rollsRemaining > 0) {
                    GamePanel.this.client.rollDice(GamePanel.this);
                    GamePanel.this.updateImageIcons();
                    GamePanel.this.client.rollsRemaining--;
                    GamePanel.this.setDiceEnabled(true);
                    GamePanel.this.updateRoundInfo();
                    GamePanel.this.scoreButton.setEnabled(true);

                    if (GamePanel.this.client.rollsRemaining == 0) {
                        GamePanel.this.updateRoundOver();
                    }
                }
            }
        });
        this.add(this.rollButton);
    }

    /**
     * Load the score button and register the handler.
     */
    private void loadScoreButton() {
        this.scoreButton = new JButton("Start Scoring");
        this.scoreButton.setSize(new Dimension(300, 30));
        this.scoreButton.setLocation(50, 250);
        this.scoreButton.setEnabled(false);

        this.scoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent event) {
                GamePanel.this.updateRoundOver();
            }
        });

        this.add(this.scoreButton);
    }

    /**
     * Load the scoring panel.
     */
    private void loadScoringPanel() {
        this.scorePanel = new ScorePanel(this.client);
        this.scorePanel.setSize(new Dimension(350, 350));
        this.scorePanel.setBackground(Color.GRAY);
        this.scorePanel.setLocation(25, 295);
        this.scorePanel.setEnabled(false);
        this.add(this.scorePanel);
    }

    /**
     * Load the different labels.
     */
    private void loadLabels() {
        final JLabel currentDiceLabel = new JLabel("Current dice to be rolled.");
        currentDiceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentDiceLabel.setSize(new Dimension(300, 15));
        currentDiceLabel.setLocation(50, 15);
        this.add(currentDiceLabel);

        final JLabel lockedDiceLabel = new JLabel("Current dice set aside.");
        lockedDiceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lockedDiceLabel.setSize(new Dimension(300, 15));
        lockedDiceLabel.setLocation(50, 150);
        this.add(lockedDiceLabel);

        final JLabel instructionsLabel = new JLabel("<html><center>Set aside dice by clicking on them.<br> You have up " +
                "to three rolls per round.<br>When ready, click the score button.</center></html>");
        instructionsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        instructionsLabel.setSize(new Dimension(300, 50));
        instructionsLabel.setLocation(50, 650);
        this.add(instructionsLabel);

        this.roundInfoLabel = new JLabel(this.client.getRoundInfo());
        this.roundInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        this.roundInfoLabel.setSize(new Dimension(300, 50));
        this.roundInfoLabel.setLocation(50, 700);
        this.add(this.roundInfoLabel);

    }

    /**
     * Updates the location of a given dice.
     *
     * @param index the index of the dice.
     */
    private void updateDiceLocation(final int index) {
        final Dice theButton = this.dice[index];
        if (theButton.isLocked()) {
            theButton.setLocation(75 + 50 * index, 180);
        } else {
            theButton.setLocation(75 + 50 * index, 45);
        }
    }

    /**
     * Update the image of the dice.
     */
    private void updateImageIcons() {
        for (Dice dice : this.dice) {
            if (!dice.isLocked()) {
                dice.setIcon(this.images[dice.getValue() - 1]);
            }
        }
    }

    /**
     * Set the status of the dice.
     *
     * @param status the enabled status to set.
     */
    private void setDiceEnabled(final boolean status) {
        for (Dice dice : this.dice) {
            dice.setEnabled(status);
        }
    }
}
