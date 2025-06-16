package server;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class RequestParserTest {
    public static void main(String[] args) {
        System.out.println("=== Testing Request Parser ===\n");

        // Test Case 1: Simple POST
        String test1 = "POST /upload HTTP/1.1\n" +
                      "Content-Type: text/plain\n" +
                      "Content-Length: 45\n" +
                      "\n" +
                      "test.PlusAgent\n" +
                      "A,B\n" +
                      "SUM\n";

        // Test Case 2: POST with query params
        String test2 = "POST /upload?filename=test.conf HTTP/1.1\n" +
                      "Content-Type: text/plain\n" +
                      "Content-Length: 45\n" +
                      "\n" +
                      "test.PlusAgent\n" +
                      "A,B\n" +
                      "SUM\n";

        // Test Case 3: POST with Accept header
        String test3 = "POST /upload HTTP/1.1\n" +
                      "Content-Type: text/plain\n" +
                      "Accept: application/json\n" +
                      "Content-Length: 45\n" +
                      "\n" +
                      "filename2=test.conf\n" +
                      "\n" +
                      "test.PlusAgent\n" +
                      "A,B\n" +
                      "SUM\n";

        testRequest("Test Case 1: Simple POST", test1);
        testRequest("Test Case 2: POST with query params", test2);
        testRequest("Test Case 3: POST with Accept header", test3);
    }

    private static void testRequest(String testName, String request) {
        System.out.println(testName);
        System.out.println("Input request:");
        System.out.println(request);
        
        // Print raw bytes for debugging
        System.out.println("\nRaw request bytes:");
        byte[] requestBytes = request.getBytes(StandardCharsets.UTF_8);
        for (byte b : requestBytes) {
            System.out.printf("%02X ", b);
        }
        System.out.println("\n");
        
        System.out.println("Parsed result:");

        try {
            BufferedReader reader = new BufferedReader(new StringReader(request));
            RequestParser.RequestInfo info = RequestParser.parseRequest(reader);

            System.out.println("HTTP Command: " + info.getHttpCommand());
            System.out.println("URI: " + info.getUri());
            System.out.println("URI Segments: " + String.join(", ", info.getUriSegments()));
            System.out.println("Parameters: " + info.getParameters());
            System.out.println("Content: " + new String(info.getContent(), StandardCharsets.UTF_8));
            System.out.println("\n" + "=".repeat(50) + "\n");

        } catch (Exception e) {
            System.out.println("Error parsing request: " + e.getMessage());
            e.printStackTrace();
            System.out.println("\n" + "=".repeat(50) + "\n");
        }
    }
} 