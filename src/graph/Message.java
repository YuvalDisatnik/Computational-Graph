package graph;

import java.util.Date;

/**
 * Represents an immutable message containing data that can be transmitted between agents.
 * Messages can be created from strings, byte arrays, or double values and provide
 * convenient access to the data in different formats.
 * 
 * @author Omri Triki, Yuval Disatnik
 */
public class Message {
    /** Raw byte data of the message */
    public final byte[] data;
    /** String representation of the message */
    public final String asText;
    /** Numeric representation of the message (NaN if not a valid number) */
    public final double asDouble;
    /** Timestamp when the message was created */
    public final Date date;

    /**
     * Constructor to initialize a Message given a string value.
     * 
     * @param s The string value to create a message from
     */
    public Message(String s) {
        this.date = new Date();
        this.asText = s;
        this.data = s.getBytes();
        this.asDouble = parseDouble(s);
    }

    /**
     * Constructor to initialize a Message given a byte array.
     * 
     * @param b The byte array to create a message from
     */
    public Message(byte[] b) {
        this(new String(b));
    }

    /**
     * Constructor to initialize a Message given a double value.
     * 
     * @param d The double value to create a message from
     */
    public Message(double d) {
        this(String.valueOf(d));
    }

    /**
     * Helper function to convert a string into a double value.
     * Returns Double.NaN if the string cannot be parsed as a number.
     * 
     * @param s The string to parse
     * @return The parsed double value or Double.NaN if parsing fails
     */
    public double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

}
