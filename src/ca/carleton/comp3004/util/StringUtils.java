package ca.carleton.comp3004.util;

/**
 * Utility class to mock apache, because I'm too lazy to add it.
 */
public class StringUtils {

    public static boolean isEmpty(final String string) {
        return string == null || string.isEmpty();
    }

    public static boolean isNotEmpty(final String string) {
        return !isEmpty(string);
    }
}
