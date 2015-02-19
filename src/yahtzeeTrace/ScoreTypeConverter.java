package yahtzeeTrace;

/**
 * Attempt to convert the given implementation to mine.
 * <p/>
 * Created with IntelliJ IDEA.
 * Date: 25/01/15
 * Time: 5:17 AM
 */
public final class ScoreTypeConverter {

    public static ScoreType getScoreTypeFromInt(final int scoringPart, final int scoringArea) {

        if (scoringPart == 0) {
            if (scoringArea == 0) {
                return ScoreType.ACES;
            }
            if (scoringArea == 1) {
                return ScoreType.TWOS;
            }
            if (scoringArea == 2) {
                return ScoreType.THREES;
            }
            if (scoringArea == 3) {
                return ScoreType.FOURS;
            }
            if (scoringArea == 4) {
                return ScoreType.FIVES;
            }
            if (scoringArea == 5) {
                return ScoreType.SIXES;
            }
        } else {
            if (scoringArea == 0) {
                return ScoreType.THREEOAK;
            }
            if (scoringArea == 1) {
                return ScoreType.FOUROAK;
            }
            if (scoringArea == 2) {
                return ScoreType.FULLHOUSE;
            }
            if (scoringArea == 3) {
                return ScoreType.SMSTRAIGHT;
            }
            if (scoringArea == 4) {
                return ScoreType.LGSTRAIGHT;
            }
            if (scoringArea == 5) {
                return ScoreType.YAHTZEE;
            }
        }
        return ScoreType.ACES;
    }
}
