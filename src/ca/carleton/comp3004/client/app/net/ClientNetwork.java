package ca.carleton.comp3004.client.app.net;


import ca.carleton.comp3004.client.app.YahtzeeClient;
import ca.carleton.comp3004.util.NetworkConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;

/**
 * Networking class that handles client communication to the server.
 */
public class ClientNetwork implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(ClientNetwork.class);

    public YahtzeeClient game;

    public boolean startedSuccessfully;

    public int UID;

    private Socket socket;

    private Thread thread;

    private ClientThread client;

    private BufferedReader in;

    private BufferedWriter out;

    /**
     * Initializes the socket and streams.
     *
     * @param host the host to connect to.
     * @param port the port to use.
     */
    public ClientNetwork(final String host, final int port) throws IOException {
        LOG.info("Initial client connection starting to {}:{}.", host, port);

        try {
            this.socket = new Socket(host, port);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
            this.UID = this.socket.getLocalPort();

            LOG.info("Connection successful. Waiting for server reply before starting the game...");
        } catch (final IOException exception) {
            LOG.error("Unable to connect.");
            throw (ConnectException) exception;
        }
    }

    /**
     * We don't actually need to run anything when this starts, only deal with server replies.
     */
    @Override
    public void run() {
    }

    public synchronized void handle(final String command) {
        if (command.equalsIgnoreCase("quit")) {
            LOG.info("{} - Quit command received. Press return to exit.", this.UID);
            this.stop();
        } else {
            LOG.info("Received {} command from the server.", command);
            this.game.updateFromServer(command);
        }
    }

    /**
     * Handle initial connection to the server. Waits until the number of players is reached before returning.
     */
    public void start() {

        if (this.thread == null) {
            this.client = new ClientThread(this, this.socket);
            this.thread = new Thread(this);
            this.thread.start();
        }

        try {
            final String reply = this.in.readLine();
            this.startedSuccessfully = reply.equals(NetworkConstants.START_GAME);
            this.client.start();
        } catch (final IOException exception) {
            LOG.error("Error communicating with server.", exception);
        }
    }

    /**
     * Send a message to the server.
     *
     * @param message the message to send.
     * @see ca.carleton.comp3004.util.NetworkConstants
     */
    public void send(final String message) {
        try {
            this.out.write(message + "\n");
            this.out.flush();
        } catch (final IOException exception) {
            LOG.error("Error sending message!", exception);
            this.stop();
        }
    }

    /**
     * Stop the client networking thread, as well as close the socket.
     */
    public void stop() {
        try {
            if (this.thread != null) {
                this.thread = null;
            }
            if (this.socket != null) {
                this.socket.close();
            }

            this.in = null;
            this.out = null;
            this.socket = null;

        } catch (final IOException exception) {
            LOG.error("Error closing connection...", exception);
        }
        this.client.close();
    }

}
