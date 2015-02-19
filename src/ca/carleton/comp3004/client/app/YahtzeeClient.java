package ca.carleton.comp3004.client.app;

import ca.carleton.comp3004.client.app.net.ClientNetwork;
import ca.carleton.comp3004.util.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * The main game-logic class for the client.
 */
public class YahtzeeClient {

    public static final int NOT_TAKEN = -1;

    private static final Logger LOG = LoggerFactory.getLogger(YahtzeeClient.class);

    private static final Random random = new Random();

    private static final String ROUND_INFO_FORMAT = "Round: %d || Score: %d || Rolls remaining: %d";

    public int rollsRemaining = 3;

    public int totalScore = 0;

    public int round = 1;

    public int[] currentRolls = new int[5];

    /**
     * 0 for upper area, 1 for lower area.
     */
    public int scoringPart = 0;

    /**
     * index into the array of the upper/lower buttons/
     */
    public int scoringArea = 0;

    /**
     * score to be entered.
     */
    public int scoreToSend = 0;

    public ClientNetwork network;

    public GamePanel board;

    public YahtzeeClient(final ClientNetwork network) {
        this.network = network;
        this.network.game = this;
    }

    /**
     * Roll the dice.
     *
     * @param clientPanel the panel, for access to the dice.
     */
    public void rollDice(GamePanel clientPanel) {
        for (Dice dice : clientPanel.dice) {
            if (!dice.isLocked()) {
                dice.setValue(random.nextInt((6 - 1) + 1) + 1);
            }
        }
    }

    /**
     * Test roll.
     *
     * @param clientPanel the panel, for access to the dice.
     */
    @SuppressWarnings("unused")
    public void rollDiceTest(GamePanel clientPanel) {
        clientPanel.dice[0].setValue(2);
        clientPanel.dice[1].setValue(3);
        clientPanel.dice[2].setValue(4);
        clientPanel.dice[3].setValue(5);
        clientPanel.dice[4].setValue(6);
    }

    /**
     * Submits a score to the server.
     */
    public void submitRoundScoreToServer() {
        LOG.info("Starting submission to the server...");
        final String toSend = String.format(NetworkConstants.SUBMIT_FORMAT, this.scoringPart, this.scoringArea, this.scoreToSend);
        this.network.send(toSend);
        LOG.info("Sent {} to the server for submission.", toSend);
        this.requestScoreFromServer();
        this.board.reset();
    }

    /**
     * Submit a "no score" to the server.
     */
    public void submitNoScoreForRound() {
        LOG.info("Setting data for no round submission...");
        this.rollsRemaining = 3;
        this.board.updateRoundInfo();
        this.scoringArea = -1;
        this.scoringPart = -1;
        this.scoreToSend = 0;
        this.submitRoundScoreToServer();
    }

    /**
     * Handles updates from the server.
     *
     * @param update the update to process.
     */
    public void updateFromServer(final String update) {
        final String[] message = update.split("_");
        if (message[0].equals("UPDATE")) {
            this.updateBoardFromServer(Integer.parseInt(message[1]), Integer.parseInt(message[2]), Integer.parseInt(message[3]), Integer.parseInt(message[4]));
        } else if (message[0].equals("SCORE") && message[1].equals(String.valueOf(this.network.UID))) {
            this.totalScore = Integer.parseInt(message[2]);
            this.board.updateRoundInfo();
        } else if (message[0].equals("RETRY") && message[1].equals(String.valueOf(this.network.UID))) {
            this.board.retrySubmission();
        } else if (update.equals(NetworkConstants.NEXT_ROUND)) {
            this.startNextRound();
        } else if (message[0].equals("OVER")) {
            LOG.info("Received Game over command form the client.");
            if (Integer.parseInt(message[1]) == this.network.UID) {
                // The client won
                this.board.updateGameOverWinner();
            } else {
                // Someone else won.
                this.board.updateGameOverLoser();
            }
        }
    }

    /**
     * Starts the next round for the client.
     */
    public void startNextRound() {
        this.round++;
        this.rollsRemaining = 3;
        this.board.updateRoundInfo();
        this.board.startNextRound();
    }

    /**
     * Ends the game for the client.
     */
    public void gameOver() {
        LOG.info("Ending game...");
        this.network.stop();
        ClientLauncher.gui.dispose();
        LOG.info("Disposed GUIl.");
    }

    /**
     * Create a string for the details of the round.
     *
     * @return the round info string.
     */
    public String getRoundInfo() {
        return String.format(ROUND_INFO_FORMAT, this.round, this.totalScore, this.rollsRemaining);
    }

    /**
     * Sets this class' roll data from the dice.
     *
     * @param dice the dice.
     */
    public void setRollData(final Dice[] dice) {
        for (int i = 0; i < this.currentRolls.length; i++) {
            this.currentRolls[i] = dice[i].getValue();
        }
    }

    /**
     * Sets the possible points for this round.
     */
    public void setPossiblePoints() {

        ScorePanel scorePanel = this.board.scorePanel;

        // Go through the rolls to set the upper scores, also sum the total
        int sum = 0;
        for (final Integer values : this.currentRolls) {
            if (values == 1 && scorePanel.upperButtons[0].playerID == NOT_TAKEN) {
                scorePanel.upperButtons[0].setText(String.valueOf(Integer.parseInt(scorePanel.upperButtons[0].getText()) + 1));
            }
            if (values == 2 && scorePanel.upperButtons[1].playerID == NOT_TAKEN) {
                scorePanel.upperButtons[1].setText(String.valueOf(Integer.parseInt(scorePanel.upperButtons[1].getText()) + 2));
            }
            if (values == 3 && scorePanel.upperButtons[2].playerID == NOT_TAKEN) {
                scorePanel.upperButtons[2].setText(String.valueOf(Integer.parseInt(scorePanel.upperButtons[2].getText()) + 3));
            }
            if (values == 4 && scorePanel.upperButtons[3].playerID == NOT_TAKEN) {
                scorePanel.upperButtons[3].setText(String.valueOf(Integer.parseInt(scorePanel.upperButtons[3].getText()) + 4));
            }
            if (values == 5 && scorePanel.upperButtons[4].playerID == NOT_TAKEN) {
                scorePanel.upperButtons[4].setText(String.valueOf(Integer.parseInt(scorePanel.upperButtons[4].getText()) + 5));
            }
            if (values == 6 && scorePanel.upperButtons[5].playerID == NOT_TAKEN) {
                scorePanel.upperButtons[5].setText(String.valueOf(Integer.parseInt(scorePanel.upperButtons[5].getText()) + 6));
            }
            sum += values;
        }
        // Set three of a kind
        if (scorePanel.lowerButtons[0].playerID == NOT_TAKEN) {
            scorePanel.lowerButtons[0].setText(String.valueOf(this.hasThreeOfAKind() ? sum : 0));
        }
        // Set four of a kind
        if (scorePanel.lowerButtons[1].playerID == NOT_TAKEN) {
            scorePanel.lowerButtons[1].setText(String.valueOf(this.hasFourOfAKind() ? sum : 0));
        }
        // Set full house
        if (scorePanel.lowerButtons[2].playerID == NOT_TAKEN) {
            scorePanel.lowerButtons[2].setText(String.valueOf(this.hasFullHouse() ? 25 : 0));
        }
        // Set small straight
        if (scorePanel.lowerButtons[3].playerID == NOT_TAKEN) {
            scorePanel.lowerButtons[3].setText(String.valueOf(this.hasSmallStraight() ? 30 : 0));
        }
        // Set large straight
        if (scorePanel.lowerButtons[4].playerID == NOT_TAKEN) {
            scorePanel.lowerButtons[4].setText(String.valueOf(this.hasLargeStraight() ? 40 : 0));
        }
        // Set the chance value
        if (scorePanel.lowerButtons[5].playerID == NOT_TAKEN) {
            scorePanel.lowerButtons[5].setText(String.valueOf(sum));
        }
        // Set yahtzee
        if (scorePanel.lowerButtons[6].playerID == NOT_TAKEN) {
            scorePanel.lowerButtons[6].setText(String.valueOf(this.hasYahtzee() ? 50 : 0));
        }

        // disable invalid scores.
        this.enableValidScores(scorePanel);
        // check we actually have a move to make.
        if (!this.hasAvailableMove(scorePanel)) {
            this.submitNoScoreForRound();
        }

    }

    /**
     * Returns the sum for the upper window, for the client.
     *
     * @return the sum.
     */
    public int getUpperSum() {
        int sum = 0;
        for (final BoardEntry entry : this.board.scorePanel.upperButtons) {
            if (entry.playerID == this.network.UID) {
                sum += entry.value;
            }
        }
        return sum;
    }

    private boolean hasThreeOfAKind() {
        final Map<Integer, Integer> frequency = this.getRollFrequency();

        for (final Integer roll : frequency.keySet()) {
            if (frequency.get(roll) >= 3) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFourOfAKind() {
        final Map<Integer, Integer> frequency = this.getRollFrequency();

        for (final Integer roll : frequency.keySet()) {
            if (frequency.get(roll) >= 4) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFullHouse() {
        final Map<Integer, Integer> frequency = this.getRollFrequency();

        boolean hasTwo = false;
        boolean hasThree = false;

        for (final Integer roll : frequency.keySet()) {
            if (frequency.get(roll) == 2) {
                hasTwo = true;
            }
            if (frequency.get(roll) == 3) {
                hasThree = true;
            }
        }

        return hasTwo && hasThree;
    }

    private boolean hasSmallStraight() {
        final Map<Integer, Integer> frequency = this.getRollFrequency();

        // check 1-2-3-4
        if (frequency.containsKey(1) && frequency.containsKey(2) && frequency.containsKey(3) && frequency.containsKey(4)) {
            return true;
        }

        // check 2-3-4-5
        if (frequency.containsKey(2) && frequency.containsKey(3) && frequency.containsKey(4) && frequency.containsKey(5)) {
            return true;
        }

        // check 3-4-5-6
        return frequency.containsKey(3) && frequency.containsKey(4) && frequency.containsKey(5) && frequency.containsKey(6);

    }

    private boolean hasLargeStraight() {
        final Map<Integer, Integer> frequency = this.getRollFrequency();

        // check 1-2-3-4-5
        if (frequency.containsKey(1) && frequency.containsKey(2) && frequency.containsKey(3) && frequency.containsKey(4) && frequency.containsKey(5)) {
            return true;
        }

        // check 2-3-4-5-6
        return frequency.containsKey(2) && frequency.containsKey(3) && frequency.containsKey(4) && frequency.containsKey(5) && frequency.containsKey(6);

    }

    private boolean hasYahtzee() {
        return this.currentRolls[0] == this.currentRolls[1] &&
                this.currentRolls[1] == this.currentRolls[2] &&
                this.currentRolls[2] == this.currentRolls[3] &&
                this.currentRolls[3] == this.currentRolls[4];
    }

    /**
     * Request the score from the server.
     */
    private void requestScoreFromServer() {
        LOG.info("Requesting score from the server...");
        this.network.send(String.format(NetworkConstants.REQUEST_SCORE, this.network.UID));
        LOG.info("Sent request for score form the server.");
    }

    /**
     * Update the board from the server.
     *
     * @param playerID    the player ID.
     * @param scoringPart the scoring area.
     * @param scoringArea the actual entry.
     * @param score       the score.
     */
    private void updateBoardFromServer(final int playerID, final int scoringPart, final int scoringArea, final int score) {
        this.board.updateBoardEntry(playerID, scoringPart, scoringArea, score);
    }

    private Map<Integer, Integer> getRollFrequency() {
        final Map<Integer, Integer> frequency = new HashMap<Integer, Integer>();
        for (final Integer roll : this.currentRolls) {
            if (!frequency.containsKey(roll)) {
                frequency.put(roll, 1);
            } else {
                frequency.put(roll, frequency.get(roll) + 1);
            }
        }
        return frequency;
    }

    /**
     * Determines which scores are valid for the player to be used.
     *
     * @param scorePanel the panel.
     */
    private void enableValidScores(final ScorePanel scorePanel) {
        for (BoardEntry button : scorePanel.upperButtons) {
            if (!button.getText().equals("0") && button.playerID == NOT_TAKEN) {
                button.setEnabled(true);
            }
        }
        for (BoardEntry button : scorePanel.lowerButtons) {
            if (!button.getText().equals("0") && button.playerID == NOT_TAKEN) {
                button.setEnabled(true);
            }
        }
    }

    /**
     * Determines if the client has a available move or not.
     *
     * @param scorePanel the panel.
     * @return true if yes, false if no.
     */
    private boolean hasAvailableMove(final ScorePanel scorePanel) {
        int availableMoves = 0;
        for (BoardEntry button : scorePanel.upperButtons) {
            if (button.isEnabled()) {
                availableMoves++;
            }
        }
        for (BoardEntry button : scorePanel.lowerButtons) {
            if (button.isEnabled()) {
                availableMoves++;
            }
        }
        return availableMoves > 0;
    }
}
