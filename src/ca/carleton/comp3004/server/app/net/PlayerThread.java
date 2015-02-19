package ca.carleton.comp3004.server.app.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

/**
 * Thread to handle communications back and forth with the clients.
 * <p/>
 * Created with IntelliJ IDEA.
 * Date: 23/01/15
 * Time: 6:17 PM
 */
public class PlayerThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(PlayerThread.class);

    private int UID = -1;

    private BufferedReader in;

    private BufferedWriter out;

    private boolean done;

    private final Socket socket;

    private final ServerNetwork server;

    public PlayerThread(final ServerNetwork serverLauncher, final Socket socket) {
        super();
        this.server = serverLauncher;
        this.socket = socket;
        this.UID = socket.getPort();
    }

    /**
     * Send a message to the client.
     *
     * @param message the message.
     */
    public void send(final String message) {
        try {
            this.out.write(message + "\n");
            LOG.info("Sent {} to client {}.", message, this.UID);
            this.out.flush();
        } catch (final IOException exception) {
            LOG.error("Error sending message!", exception);
            this.server.removePlayer(this.getUID());
        }
    }

    /**
     * Handle messages from the client and passes them to the server for processing.
     */
    public void run() {
        LOG.info("Thread {} running.", this.UID);
        while (!this.done) {
            try {
                this.server.handle(this.in.readLine(), this.UID);
            } catch (final IOException exception) {
                LOG.warn("Client ID -- {}. Error reading input - connection may be closed.", this.UID);
                this.server.removePlayer(this.UID);
                break;
            }
        }
    }

    /**
     * Opens the streams.
     *
     * @throws IOException
     */
    public void open() throws IOException {
        LOG.info("Opening streams for client {}", this.UID);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    }

    /**
     * Close the client socket and marks the work done.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        this.done = true;
        if (this.socket != null) {
            this.socket.close();
        }
        if (this.in != null) {
            this.in.close();
        }
    }

    /**
     * Return the UID for this thread.
     *
     * @return the uid.
     */
    public int getUID() {
        return this.UID;
    }

    public String toString() {
        return super.toString() + " : " + this.UID;
    }
}
