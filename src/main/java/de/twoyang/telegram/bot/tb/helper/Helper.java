package de.twoyang.telegram.bot.tb.helper;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author chrisotpher
 * @since 4/21/17
 */
public interface Helper {
    /**
     * Method for generating an GUID as an String in Hexadecimal.
     * The uniqueness is not garantied
     *
     * @return the Id
     */
    static String getUniqueId() {
        String id = Math.random() + "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            id = String.format("%x", new BigInteger(1, md.digest(id.getBytes())));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * splits the given text at one or more whitespaces by using the pattern "[\\s]+" for identifying whitespaces
     *
     * @param text the text to split
     * @return an Array of the contained words in the text
     */
    static String[] split(String text) {
        return text.trim().split("[\\s]+");
    }

    /**
     * @see #concat(String[], String, int, int)
     */
    static String concat(String[] parts, int offset) {
        return concat(parts, " ", offset);
    }

    /**
     * @see #concat(String[], String, int, int)
     */
    static String concat(String[] parts, String connector) {
        return concat(parts, connector, 0);
    }

    /**
     * @see #concat(String[], String, int, int)
     */
    static String concat(String[] parts, String connector, int offset) {
        return concat(parts, connector, offset, parts.length);
    }

    /**
     * @see #concat(String[], String, int, int)
     */
    static String concat(String[] parts) {
        return concat(parts, 0);
    }

    /**
     * concats the given array to a String.
     *
     * @param parts     the array to concat
     * @param connector the connector between each word (if not given space is used)
     * @param offset    the index for the first word in parts to use (default 0 if not specified)
     * @param end       index + 1 of the last word in parts to concat (default parts.length if not specified)
     * @return the concated String
     */
    static String concat(String[] parts, String connector, int offset, int end) {
        StringBuilder sb = new StringBuilder("");
        for (int i = offset; i < end && i < parts.length; i++) {
            sb.append(parts[i]);
            sb.append(connector);
        }
        if (sb.length() > 0)
            sb.delete(sb.length() - connector.length(), sb.length());
        return sb.toString();
    }
}
