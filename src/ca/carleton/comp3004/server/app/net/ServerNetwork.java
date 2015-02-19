package ca.carleton.comp3004.server.app.net;

import ca.carleton.comp3004.server.app.YahtzeeGame;
import ca.carleton.comp3004.util.NetworkConstants;
import ca.carleton.comp3004.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yahtzeeTrace.YahtzeeTracer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Networking class that handles communications with the clients.
 * <p/>
 * Created with IntelliJ IDEA.
 * Date: 23/01/15
 * Time: 6:11 PM
 */
public class ServerNetwork {

    public static final YahtzeeTracer TRACER = new YahtzeeTracer();

    private static final Logger LOG = LoggerFactory.getLogger(ServerNetwork.class);

    private ServerSocket socket;

    public int numberOfPlayers;

    public PlayerThread[] players;

    private final YahtzeeGame game;

    public ServerNetwork(final int port, final int initialNumberOfPlayers) {
        this.game = new YahtzeeGame(this);
        this.players = new PlayerThread[initialNumberOfPlayers];
        TRACER.traceNewGame(initialNumberOfPlayers);
        this.start(port);
    }

    /**
     * Starts the server.
     *
     * @param port the port to use.
     */
    public void start(final int port) {
        try {
            LOG.info("Starting server listening on port {}.", port);
            this.socket = new ServerSocket(port);
            this.socket.setReuseAddress(true);
            LOG.info("Server started successfully.");
            this.waitForConnections();
        } catch (final IOException exception) {
            LOG.error("Unable to initialize server.", exception);
        }
    }

    /**
     * Waits until the number of playrs has been reached.
     *
     * @throws IOException
     */
    private void waitForConnections() throws IOException {
        LOG.info("Waiting for {} connections before starting the game...", this.players.length);
        while (this.numberOfPlayers < this.players.length) {
            this.addPlayer(this.socket.accept());
            LOG.info("Added player. Waiting for {} more connections.", this.players.length - this.numberOfPlayers);
        }
        LOG.info("Number of players reached. Sending start game command to clients.");
        this.startGame();

    }

    /**
     * Tells the clients to start the game.
     */
    private void startGame() {
        try {
            for (final PlayerThread player : this.players) {
                player.send(NetworkConstants.START_GAME);
            }
        } catch (final Exception exception) {
            LOG.error("Error with communication with clients.", exception);
        }

        LOG.info("Sent start game commands to the clients.");

    }

    /**
     * Add a client.
     *
     * @param socket the client's socket.
     * @throws IOException
     */
    private void addPlayer(final Socket socket) throws IOException {
        if (this.numberOfPlayers < this.players.length) {
            LOG.info("Accepting client: " + socket);
            this.players[this.numberOfPlayers] = new PlayerThread(this, socket);
            this.players[this.numberOfPlayers].open();
            this.players[this.numberOfPlayers].start();
            this.numberOfPlayers++;
        } else {
            LOG.warn("Maximum client count reached. Client refused. [Max - {}]", this.players.length);
        }
    }

    /**
     * Remove a client.
     *
     * @param id the player to remove.
     */
    public synchronized void removePlayer(final int id) {
        final int index = this.findPlayer(id);
        if (index >= 0) {
            PlayerThread threadToRemove = this.players[index];
            LOG.info("Removing client thread {}.", threadToRemove);
            // shift clients
            this.players[index] = null;
            PlayerThread[] newArray = new PlayerThread[this.numberOfPlayers - 1];
            int count = 0;
            for (final PlayerThread player : this.players) {
                if (player != null) {
                    newArray[count++] = player;
                }
            }

            this.players = newArray;
            this.numberOfPlayers--;

            try {
                threadToRemove.close();
            } catch (final IOException exception) {
                LOG.error("Error closing client thread!", exception);
            }

            LOG.info("Removed played {}. New player count --> {}.", id, this.numberOfPlayers);

            TRACER.tracePlayerDropped(id);

            this.game.tryToStartNextRound();
        }
    }

    /**
     * Handle a message from a client.
     *
     * @param command  the message sent.
     * @param clientID the client sending it.
     */
    public synchronized void handle(final String command, final int clientID) {
        if (StringUtils.isEmpty(command)) {
            return;
        }

        if (command.equals("quit")) {
            LOG.info("Removing client {}.", clientID);
            final int index = this.findPlayer(clientID);
            if (index != -1) {
                this.players[index].send("quit");
                this.removePlayer(clientID);
            }
        } else {
            // Handle all commands from here.
            LOG.info("Received {} from client {}.", command, clientID);

            final String[] deconstructed = command.split("_");

            if (deconstructed[0].equals("SUBMIT")) {
                if (this.game.updateBoardEntry(command, clientID)) {
                    LOG.info("Sending out update command.");
                    for (final PlayerThread player : this.players) {
                        player.send(String.format(NetworkConstants.UPDATE_BOARD,
                                this.players[this.findPlayer(clientID)].getUID(),
                                Integer.parseInt(deconstructed[1]),
                                Integer.parseInt(deconstructed[2]),
                                Integer.parseInt(deconstructed[3])));
                    }
                } else {
                    if (!(Integer.parseInt(deconstructed[1]) == -1)) {
                        LOG.info("Sending INVALID (try again) reply.");
                        PlayerThread player = this.players[this.findPlayer(clientID)];
                        player.send(String.format(NetworkConstants.SCORE_TAKEN_RETRY, player.getUID()));
                    } else {
                        LOG.info("Ignore submission, was invalid.");
                    }
                }
                this.game.tryToStartNextRound();
            } else if (deconstructed[0].equals("SCORE")) {
                LOG.info("Sending score response...");
                int score = this.game.getScoreForPlayer(clientID);
                PlayerThread player = this.players[this.findPlayer(clientID)];
                player.send(String.format(NetworkConstants.SCORE_RESPONSE, player.getUID(), score));
            }
        }
    }

    /**
     * Tells the clients to start the next round.
     */
    public void startNextRound() {
        for (final PlayerThread player : this.players) {
            player.send(String.format(NetworkConstants.NEXT_ROUND));
        }
        LOG.info("Sent next round command to clients.");
    }

    /**
     * Tells the clients to end the game.
     *
     * @param winningPlayerID the winner.
     */
    public void endGame(final int winningPlayerID) {
        LOG.info("Sending out game over message.");
        for (final PlayerThread player : this.players) {
            player.send(String.format(NetworkConstants.GAME_OVER, winningPlayerID));
        }
    }

    /**
     * Find a player (index into the array) from the clientID.
     *
     * @param id the UID.
     * @return the index.
     */
    private synchronized int findPlayer(final int id) {
        for (int i = 0; i < this.numberOfPlayers; i++) {
            if (this.players[i].getUID() == id) {
                return i;
            }
        }
        return -1;
    }
}
