package ca.carleton.comp3004.server.app;

import ca.carleton.comp3004.server.app.net.ServerNetwork;
import ca.carleton.comp3004.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for the server.
 * <p/>
 * Created with IntelliJ IDEA.
 * Date: 23/01/15
 * Time: 6:10 PM
 */
public class ServerLauncher {

    private static final Logger LOG = LoggerFactory.getLogger(ServerLauncher.class);

    public static void main(String[] args) {
        ServerNetwork network = new ServerNetwork(Config.DEFAULT_PORT, Config.DEFAULT_NUMBER_OF_PLAYERS);
        LOG.info("Server terminated.");
    }

}
