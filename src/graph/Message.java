package graph;

import java.util.Date;

// A class to represent a message containing some relevant information
public class Message {
    // every message is immutable -> all fields are final
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

    // constructor to initialize a Message given a string
    public Message(String s) {
        this.date = new Date();
        this.asText = s;
        this.data = s.getBytes();
        this.asDouble = parseDouble(s);
    }

    // constructor to initialize a Message given a bytes array
    public Message(byte[] b) {
        this(new String(b));
    }

    // constructor to initialize a Message given a double
    public Message(double d) {
        this(String.valueOf(d));
        System.out.println("    Message(double) constructor called with: " + d);
        System.out.println("    Message created with asText: '" + this.asText + "'");
    }

    // a helper function to convert a string into a double
    public double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return Double.NaN;
        }
    }

}
