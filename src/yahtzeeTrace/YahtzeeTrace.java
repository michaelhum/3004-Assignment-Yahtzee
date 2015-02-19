/**
 * 
 */
package yahtzeeTrace;

import java.util.Collection;

/**
 * @author Darryl Hill
 *
 * Uses the following enum, provided in a separate file:
 * 
 * public enum ScoreType {ACES, TWOS, THREES, FOURS, FIVES, SIXES, THREEOAK, FOUROAK, FULLHOUSE, 
	SMSTRAIGHT, LGSTRAIGHT, YAHTZEE }
	
 * You will be provided with a jar file with all the necessary files, along with a readme
 * with any other information you may need. 
 * 
 * This is the interface that will be implemented by a class that you will include in your
 * system (on the server side). You need to call these functions with respect to the
 * following events:
 * 
 *	- start a new game
 *  - player scores their current dice roll
 *  - round ends, at which point totals are entered for each player
 *  - game ends
 *   
 * These callback functions do no work for you, they only monitor the state of your game. 
 * It is up to you to call the proper function at the proper time with the proper information.
 * The information will be written to an xml file (which you are free to look at), which will 
 * be parsed automatically later to ensure that your implementation functions correctly.  
 * 
 */
public interface YahtzeeTrace {
	
	/**
	 * Start scoring a new game of Yahtzee. 
	 * @param numPlayers the number of players 
	 */
	public void traceNewGame(int numPlayers);
	
	/**
	 * When a player wants to record their score on the scoresheet, call either of these 
	 * functions with the appropriate parameters.
	 * 
	 * @param round as described in the assignment description
	 * @param player Each player is labeled with an integer value. 
	 * @param dice Either an array of int or some kind of Collection of Integers containing
	 * 5 integers between 1 and 6 representing the dice values.
	 * @param type See the enum ScoreType, which gives an enumeration of the different categories
	 * you may score in.
	 */
	public void traceScore(int round, int player, Collection<Integer> dice, int score, ScoreType type);
	public void traceScore(int round, int player, int[] dice, int score, ScoreType type);

	/**
	 * If, during a game a player becomes disconnected, call this event with the appropriate
	 * player id (as an int value).
	 * @param player
	 */
	public void tracePlayerDropped(int player);
	
	
	/**
	 * When the game is done, call this method will all the relevant totals (see 
	 * http://people.scs.carleton.ca/~jeanpier///304W15/Yahtzee-scoringSheet.jpg for 
	 * details).
	 * @param subtotal_top
	 * @param bonus_top
	 * @param top_total
	 * @param bottom_bonus
	 * @param bottom_total
	 * @param total_score
	 */
	public void tracePlayerTotal(int player, int subtotal_top, int bonus_top, int top_total, 
			int bottom_bonus, int bottom_total, int total_score);
	
	/**
	 * Once all the players have scored, call this function, which will write the information
	 * to an xml file. 
	 */
	public void traceEndGame();

}
