/**
 * 
 */
package yahtzeeTrace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Darryl Hill
 * 
 * A class to collect all output of a networked Yahtzee game, 
 * as specified in comp3004 Winter 2105 with Jean-Pierre Corriveau. 
 *
 *public enum ScoreType {ACES, TWOS, THREES, FOURS, FIVES, SIXES, THREEOAK, FOUROAK, FULLHOUSE, 
	SMSTRAIGHT, LGSTRAIGHT, YAHTZEE }
 */
public class YahtzeeTracer implements YahtzeeTrace{

	Document doc;
	Element rootElement;
	HashMap<ScoreType, String> scoreMap;
	long order = 0;
	
	//please do not change these file names
	public final static String configDirectory = "traceFiles";
	public final static String configFile = configDirectory+"/yahtzeeTraceConfig.txt";
	public final static String traceOutput = configDirectory+"/yahtzeeOut";

	/*
	/**
	 * Strictly for testing purposes.
	 * @param args
	 */
	public static void main(String[] args) {

		YahtzeeTracer yo = new YahtzeeTracer();
		//yo.score(1,1,1,ScoreType.ACES, dice);
		yo.traceNewGame(2);
		Element element = null;
		//int[] dice = {1,2,3,2,3,4};
		Collection<Integer> dice = new ArrayList<Integer>();
		for (int i = 0; i < 10; i++){
			dice.add(i);
		}
		yo.traceScore(1,1, dice, 23, ScoreType.THREEOAK);
		yo.sleep(1);
		yo.traceScore(1,1, dice, 23, ScoreType.FOUROAK);
		yo.sleep(1);
		yo.traceScore(1,2, dice, 23, ScoreType.ACES);

		yo.tracePlayerDropped(0);

		yo.traceScore(2,1, dice, 23, ScoreType.THREEOAK);
		yo.sleep(1);
		yo.traceScore(2,3, dice, 23, ScoreType.FOUROAK);

		yo.tracePlayerDropped(1);
		yo.tracePlayerDropped(0);


		yo.tracePlayerTotal(23, 33, 77, 898, 8, 6, 55);

		yo.traceScore(2,2, dice, 23, ScoreType.ACES);

		yo.traceEndGame();
	}

	
	private void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//*/

	public YahtzeeTracer(){
		scoreMap = new HashMap<ScoreType, String>();
		String[] values = {"aces", "twos", "threes", "fours", "fives","sixes",
				"three_of_a_kind", "four_of_a_kind", "full_house", "small_straight",
				"large_straight", "yahtzee"};
		ScoreType[] scores = ScoreType.values();
		for (int i = 0; i < scores.length; i ++){
			scoreMap.put(scores[i], values[i] );
		}
	}

	/* (non-Javadoc)
	 * @see yahtzeeOutput.YahtzeeMonitor#recordNewGame(int)
	 */
	@Override
	public void traceNewGame(int numPlayers) {
		
		//reset the "timestamp"
		order = 0;
		/*
		 * We want to initialize the DOM object
		 */
		try {

			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// root elements
			doc = docBuilder.newDocument();
			rootElement = doc.createElement("yahtzee");
			doc.appendChild(rootElement);

		}catch(Exception e){
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see yahtzeeOutput.YahtzeeMonitor#recordScore(int, int, java.util.Collection, yahtzeeOutput.ScoreType)
	 */
	@Override
	public synchronized void traceScore(int round, int player, Collection<Integer> dice,
			int score, ScoreType type) {
		/*
		 * Record event player scoring, representing dice as a Collection of Integers.
		 */
		int[] d = new int[dice.size()];
		int count = 0;
		for (Integer i: dice){
			d[count++] = i;
		}
		traceScore(round, player, d, score, type);
	}


	/* (non-Javadoc)
	 * @see yahtzeeOutput.YahtzeeMonitor#recordScore(int, int, int[], yahtzeeOutput.ScoreType)
	 */
	@Override
	public synchronized void traceScore(int round, int player, int[] dice, int score,ScoreType type) {
		/*
		 * Record event player scoring, representing dice as an array of int.
		 */

		//get the appropriate round
		Node node = getRound(round);
		//adding a child of whatever row the score is for
		node.appendChild(scoreRound(player, dice, score, type));
	}

	/**
	 * Retrieve the DOM object associated with the given round (typically so we can add more information to it). 
	 * @param round
	 * @return
	 */
	public Node getRound(int round){
		NodeList rounds = rootElement.getChildNodes();
		NamedNodeMap attr = null;
		for (int i = 0;i < rounds.getLength(); i++){
			attr = rounds.item(i).getAttributes();
			String val = attr.item(0).getNodeValue();
			if (val != null) {
				if (round == Integer.parseInt(val)){
					return rounds.item(i);
				}
			}else{
				new Exception("DOM object (round) missing attribute").printStackTrace();
				return null;
			}

		}
		return newRound(round);
	}

	/**
	 * Return a Node containing the information of the player scoring (which will be added
	 * to the round)
	 * @param player
	 * @param dice
	 * @param score
	 * @param type
	 * @return
	 */
	public Node scoreRound(int player, int[] dice, int score, ScoreType type){
		Element row = doc.createElement((String)scoreMap.get(type));

		Element playerElement = doc.createElement("player");
		playerElement.setTextContent(Integer.toString(player));
		Element diceElement = doc.createElement("dice");
		StringBuffer diceScore = new StringBuffer();
		for (int i = 0; i < dice.length; i ++){
			diceScore.append(dice[i]);
			diceScore.append(" ");
		}
		diceElement.setTextContent(diceScore.toString());
		Element scoreElement = doc.createElement("score");
		scoreElement.setTextContent(Integer.toString(score));
		row.appendChild(getTimeStamp());
		row.appendChild(playerElement);
		row.appendChild(diceElement);
		row.appendChild(scoreElement);
		return row;
	}

	/**
	 * The round being referred to was not found, so create the round and return it
	 * @param round
	 * @return
	 */
	public Node newRound(int round){
		Element element = null;
		element = doc.createElement("round");
		element.setAttribute("number", Integer.toString(round));
		element.appendChild(getTimeStamp());
		rootElement.appendChild(element);
		return element;
	}


	/* (non-Javadoc)
	 * @see yahtzeeOutput.YahtzeeMonitor#recordPlayerDropped(int)
	 */
	@Override
	public synchronized void tracePlayerDropped(int player) {
		Element playerDrop = doc.createElement("Player_dropped");
		playerDrop.setAttribute("player", Integer.toString(player));
		playerDrop.appendChild(getTimeStamp());
		rootElement.appendChild(playerDrop);
	}

	/* (non-Javadoc)
	 * @see yahtzeeOutput.YahtzeeMonitor#recordPlayerTotals(int, int, int, int, int, int)
	 */
	@Override
	public synchronized void tracePlayerTotal(int player, int subtotal_top, int bonus_top,
			int total_top, int bonus_bottom, int total_bottom, int total_score) {

		Element playerTotals = doc.createElement("Player_totals");
		playerTotals.setAttribute("player", Integer.toString(player));
		
		playerTotals.appendChild(getTimeStamp());

		Element subTotalTop = doc.createElement("subtotal_top");
		subTotalTop.setTextContent(Integer.toString(subtotal_top));
		playerTotals.appendChild(subTotalTop);

		Element bonusTop = doc.createElement("bonus_top");
		subTotalTop.setTextContent(Integer.toString(bonus_top));
		playerTotals.appendChild(bonusTop);

		Element totalTop = doc.createElement("total_top");
		totalTop.setTextContent(Integer.toString(total_top));
		playerTotals.appendChild(totalTop);

		Element bonusBottom = doc.createElement("bonus_bottom");
		bonusBottom.setTextContent(Integer.toString(bonus_bottom));
		playerTotals.appendChild(bonusBottom);

		Element totalBottom = doc.createElement("total_bottom");
		totalBottom.setTextContent(Integer.toString(total_bottom));
		playerTotals.appendChild(totalBottom);

		Element totalScore = doc.createElement("total_score");
		totalScore.setTextContent(Integer.toString(total_score));
		playerTotals.appendChild(totalScore);

		rootElement.appendChild(playerTotals);

	}
	
	private Element getTimeStamp(){
		Element timeStamp = doc.createElement("timestamp");
		timeStamp.setTextContent(Long.toString(order++));
		return timeStamp;
	}

	/* (non-Javadoc)
	 * @see yahtzeeOutput.YahtzeeMonitor#recordEndGame()
	 */
	@Override
	public void traceEndGame() {
		/*
		 * Read in a config file that has the number of the last file written
		 */
		File directory = new File(configDirectory);
		if (!directory.exists()){
			directory.mkdir();
		}
		File f = new File(configFile);
		String content = "1";
		int fileno = 1;
		try {
			if(!f.exists()) {
				Files.write(Paths.get(configFile), content.getBytes(), StandardOpenOption.CREATE);
			}else{
				content = new String(Files.readAllBytes(Paths.get(configFile))); 
				fileno = Integer.parseInt(content);
				content = Integer.toString(++fileno);
				Files.write(Paths.get(configFile), content.getBytes(), StandardOpenOption.CREATE);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		try {
			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(traceOutput+content+".xml"));
			transformer.transform(source, result);

		} catch (TransformerException e) {
			e.printStackTrace();
			return;
		}

		System.out.println("File saved!");

	}


}
