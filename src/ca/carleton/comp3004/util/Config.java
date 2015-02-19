package ca.carleton.comp3004.util;

/**
 * Configuration class, in lieu of a properties file.
 */
public final class Config {

    public static final int MAX_CLIENTS = 4;

    public static final int DEFAULT_PORT = 11111;

    public static final String DEFAULT_HOST = "127.0.0.1";

    public static final int DEFAULT_WIDTH = 400;

    public static final int DEFAULT_HEIGHT = 800;

    public static final int DEFAULT_NUMBER_OF_PLAYERS = 2;

    private Config () {
        // Do nothing.
    }

}
