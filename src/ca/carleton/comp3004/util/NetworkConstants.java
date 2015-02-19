package ca.carleton.comp3004.util;

/**
 * Constants used for communication between the processes.
 *
 * Created with IntelliJ IDEA.
 * Date: 23/01/15
 * Time: 6:49 PM
 */
public final class NetworkConstants {

    /**
     * Used to start the game.
     */
    public static final String START_GAME = "start";

    /**
     * UPPER OR LOWER FIELD, INDEX INTO ARRAY, FINAL SCORE.
     */
    public static final String SUBMIT_FORMAT = "SUBMIT_%d_%d_%d";

    /**
     * CLIENT_ID, submit_format from above.
     */
    public static final String UPDATE_BOARD = "UPDATE_%d_%d_%d_%d";

    /**
     * CLIENT_ID, Used by the clients so they know they must re-score.
     */
    public static final String SCORE_TAKEN_RETRY = "RETRY_%d";

    /**
     * CLIENT_ID - Used by the clients to request their score.
     */
    public static final String REQUEST_SCORE = "SCORE_%d";

    /**
     * CLIENT_ID, SCORE - Used by the clients to handle the response.
     */
    public static final String SCORE_RESPONSE = "SCORE_%d_%d";

    /**
     * Used to start the next round.
     */
    public static final String NEXT_ROUND = "ROUND_START";

    /**
     * Used to end the game.
     */
    public static final String GAME_OVER = "OVER_%d";

}
