package ca.carleton.comp3004.server.app;

import ca.carleton.comp3004.server.app.net.PlayerThread;
import ca.carleton.comp3004.server.app.net.ServerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yahtzeeTrace.ScoreTypeConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main game class for the server.
 * <p/>
 * Created with IntelliJ IDEA.
 * Date: 23/01/15
 * Time: 9:16 PM
 */
public class YahtzeeGame {

    public static final int MAX_ROUNDS = 13;

    private static final Logger LOG = LoggerFactory.getLogger(YahtzeeGame.class);

    public BoardEntry[] upperBoard;

    public BoardEntry[] lowerBoard;

    private final ServerNetwork network;

    private int round = 1;

    private int playersScoredThisTurn = 0;

    public YahtzeeGame(final ServerNetwork network) {
        this.network = network;
        this.setupBoard();
    }

    /**
     * Gets a score for the player.
     *
     * @param player the player to look for.
     * @return their score.
     */
    public int getScoreForPlayer(final int player) {
        int temp = 0;

        for (BoardEntry entry : this.upperBoard) {
            if (entry.playerID == player) {
                temp += entry.value;
            }
        }
        for (BoardEntry entry : this.lowerBoard) {
            if (entry.playerID == player) {
                temp += entry.value;
            }
        }

        return temp;
    }

    /**
     * Attempts to start the next round, if possible.
     */
    public void tryToStartNextRound() {
        if (this.playersScoredThisTurn == this.network.numberOfPlayers) {
            LOG.info("All players have scored this turn, sending next round command.");
            this.network.startNextRound();
            this.round++;

            if (this.round > YahtzeeGame.MAX_ROUNDS) {
                LOG.info("Maximum rounds reached. Game over.");
                final PlayerThread winner = this.determineWinner();
                this.network.endGame(winner.getUID());
                this.gameOver();
            }

            this.playersScoredThisTurn = 0;
        }

    }

    /**
     * Update the board.
     *
     * @param entry    the entry.
     * @param playerID the player who made the move.
     * @return true if successful.
     */
    public boolean updateBoardEntry(final String entry, final int playerID) {

        // Deconstruct result
        final String[] results = entry.split("_");
        int scoringPart = Integer.parseInt(results[1]);  // 0 for upper board, 1 for lower
        int scoringArea = Integer.parseInt(results[2]);  // index of the array
        int score = Integer.parseInt(results[3]);  // the value

        if (scoringPart == -1) {
            LOG.info("Client {} submitted no score for this round.", playerID);
            this.playersScoredThisTurn++;
            // Note here tracer will not report properly, since the user submitted no score for the round.
            ServerNetwork.TRACER.traceScore(this.round, playerID, new int[]{0, 0, 0, 0, 0, 0}, 0, ScoreTypeConverter.getScoreTypeFromInt(scoringPart,scoringArea));
            return false;
        } else if (scoringPart == 0) {
            // no entry yet, valid move.
            if (this.upperBoard[scoringArea].value == 0) {
                this.upperBoard[scoringArea].playerID = playerID;
                this.upperBoard[scoringArea].value = score;
                LOG.info("Successfully entered score.");
                ServerNetwork.TRACER.traceScore(this.round, playerID, new int[]{0, 0, 0, 0, 0, 0}, score, ScoreTypeConverter.getScoreTypeFromInt(scoringPart, scoringArea));
            } else {
                LOG.warn("Upper Board Location already taken by another player.");
                return false;
            }
        } else {
            // no entry yet, valid move.
            if (this.lowerBoard[scoringArea].value == 0) {
                this.lowerBoard[scoringArea].playerID = playerID;
                this.lowerBoard[scoringArea].value = score;
                LOG.info("Successfully entered score.");
                ServerNetwork.TRACER.traceScore(this.round, playerID, new int[]{0, 0, 0, 0, 0, 0}, score, ScoreTypeConverter.getScoreTypeFromInt(scoringPart, scoringArea));
            } else {
                LOG.warn("Lower Board Location already taken by another player.");
                return false;
            }
        }

        this.playersScoredThisTurn++;

        if (this.isGameOver()) {
            LOG.info("All filled slots have been determined. Calculating winner and sending message.");
            final PlayerThread winner = this.determineWinner();
            this.network.endGame(winner.getUID());
            this.gameOver();
        }
        return true;
    }

    /**
     * Setup the server side board.
     */
    private void setupBoard() {
        this.upperBoard = new BoardEntry[6];
        this.lowerBoard = new BoardEntry[7];

        for (int i = 0; i < this.upperBoard.length; i++) {
            this.upperBoard[i] = new BoardEntry();
        }

        for (int i = 0; i < this.lowerBoard.length; i++) {
            this.lowerBoard[i] = new BoardEntry();
        }
    }

    /**
     * Ends the game.
     */
    public void gameOver() {

        ServerNetwork.TRACER.traceEndGame();

        final List<Integer> ids = new ArrayList<Integer>();

        for (final PlayerThread players : this.network.players) {
            ids.add(players.getUID());
        }

        for (final Integer id : ids) {
            this.network.removePlayer(id);
        }

        LOG.info("Threads removed, server exiting.");
        System.exit(0);

    }

    /**
     * Determines if the game is over.
     *
     * @return true if yes, false if no.
     */
    private boolean isGameOver() {
        int unfilledSlots = 0;

        for (BoardEntry entry : this.upperBoard) {
            if (entry.value == 0 && entry.playerID == -1) {
                unfilledSlots++;
            }
        }
        for (BoardEntry entry : this.lowerBoard) {
            if (entry.value == 0 && entry.playerID == -1) {
                unfilledSlots++;
            }
        }

        return unfilledSlots == 0;
    }

    /**
     * Determine who won based on their points.
     *
     * @return The thread that won.
     */
    private PlayerThread determineWinner() {
        final Map<PlayerThread, Integer> scores = new HashMap<PlayerThread, Integer>();

        for (PlayerThread player : this.network.players) {
            if (!scores.containsKey(player)) {
                scores.put(player, 0);
            }
            for (BoardEntry entry : this.upperBoard) {
                if (entry.playerID == player.getUID()) {
                    scores.put(player, scores.get(player) + entry.value);
                }
            }
            for (BoardEntry entry : this.lowerBoard) {
                if (entry.playerID == player.getUID()) {
                    scores.put(player, scores.get(player) + entry.value);
                }
            }
        }

        int currHighest = 0;
        PlayerThread toReturn = null;

        for (PlayerThread player : scores.keySet()) {
            ServerNetwork.TRACER.tracePlayerTotal(player.getUID(), 0, 0, 0, 0, 0, scores.get(player));
            if (scores.get(player) >= currHighest) {
                toReturn = player;
                currHighest = scores.get(player);
            }
        }
        return toReturn;
    }

}
