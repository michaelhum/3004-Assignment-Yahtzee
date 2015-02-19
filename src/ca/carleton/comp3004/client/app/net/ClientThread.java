package ca.carleton.comp3004.client.app.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Thread to receive server messages.
 */
public class ClientThread extends Thread {

    private static final Logger LOG = LoggerFactory.getLogger(ClientThread.class);

    private final Socket socket;

    private final ClientNetwork client;

    private BufferedReader in;

    private boolean done = false;

    /**
     * Set the fields and start the thread.
     *
     * @param client the networking class to interact with.
     * @param socket the socket to communicate over.
     */
    public ClientThread(final ClientNetwork client, final Socket socket) {
        this.client = client;
        this.socket = socket;
        this.open();
    }

    /**
     * Opens the stream for communication.
     */
    public void open() {
        try {
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        } catch (final IOException exception) {
            LOG.error("Error opening input stream for thread...", exception);
            this.client.stop();
        }
    }

    /**
     * Close the stream and mark the work as done.
     */
    public void close() {
        this.done = true;
        try {
            if (this.in != null) {
                this.in.close();
            }
        } catch (final IOException exception) {
            LOG.error("Error closing input stream for thread...", exception);
        }
    }

    /**
     * Continually waits for input from the server.
     */
    public void run() {
        LOG.info("Client thread {} running.", this.socket.getLocalPort());
        while (!this.done) {
            try {
                this.client.handle(this.in.readLine());
            } catch (final IOException exception) {
                LOG.error("Listening error...", exception);
                this.close();
            }
        }
    }
}
