package ca.carleton.comp3004.client.app;

import ca.carleton.comp3004.client.app.net.ClientNetwork;
import ca.carleton.comp3004.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Arrays;

/**
 * Client main class.
 * <p/>
 * Created with IntelliJ IDEA.
 * Date: 22/01/15
 * Time: 12:06 PM
 */
public class ClientLauncher extends JFrame {

    public static ClientLauncher gui;

    private static final Logger LOG = LoggerFactory.getLogger(ClientLauncher.class);

    private final YahtzeeClient client;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    gui = new ClientLauncher("Hum Yahtzee - 100883995", new YahtzeeClient(new ClientNetwork(Config.DEFAULT_HOST, Config.DEFAULT_PORT)));
                    if (gui.ready()) {
                        gui.setVisible(true);
                    } else {
                        LOG.info("Client was unable to start successfully.");
                    }

                } catch (final IOException exception) {
                    LOG.error("Unrecoverable error. Cause --> {}.", exception.getMessage());
                }
            }
        });
    }

    /**
     * Sets the name of the frame, sets up the default settings, etc.
     *
     * @param frameName name of the frame.
     * @param client    the game client to use.
     */
    public ClientLauncher(final String frameName, final YahtzeeClient client) {
        super(frameName);
        this.setupFrame();
        this.client = client;
        final GamePanel view = new GamePanel(this.client);
        this.client.board = view;
        this.add(view);
    }

    /**
     * Set the default settings (size, resize, etc.).
     */
    private void setupFrame() {
        this.setSize(new Dimension(Config.DEFAULT_WIDTH, Config.DEFAULT_HEIGHT));
        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    /**
     * Wait for the ready from the server before starting.
     *
     * @return true if ready to start, false if an error has occurred.
     * @throws IOException
     */
    private boolean ready() throws IOException {
        this.client.network.start();
        LOG.info("Received game start. Client GUI starting.");
        return this.client.network.startedSuccessfully;
    }
}
