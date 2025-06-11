package servlets;

import configs.GenericConfig;
import configs.Graph;
import server.Servlet;
import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servlet that handles configuration file uploads and generates a computational graph visualization.
 * This servlet:
 * 1. Receives uploaded configuration files
 * 2. Saves them server-side
 * 3. Creates a GenericConfig from the file
 * 4. Generates a Graph from the configuration
 * 5. Returns an HTML response with the graph visualization
 */
public class ConfLoader implements Servlet {
    /** Directory where uploaded configuration files are stored */
    private static final String UPLOAD_DIR = "config_files";
    /** Path to the HTML template used for graph visualization */
    private static final String TEMP_HTML = "html_files/graph_temp.html";

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // Extract and validate file content from the HTTP request
        String content = new String(ri.getContent());
        if (content.isEmpty()) {
            sendError(toClient, "No file content received");
            return;
        }

        // Save the configuration file with a unique name based on timestamp
        // This ensures no file conflicts and maintains a history of configurations
        String fileName = "config_" + System.currentTimeMillis() + ".conf";
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        Files.createDirectories(Paths.get(UPLOAD_DIR));
        Files.write(filePath, content.getBytes());

        try {
            // Create and initialize the GenericConfig with the uploaded file
            // This will parse the configuration and create the necessary agents
            GenericConfig config = new GenericConfig();
            config.setConfFile(filePath.toString());
            config.create();

            // Generate a graph representation from the topic connections
            // This creates a visual structure of how agents are connected
            Graph graph = new Graph();
            graph.createFromTopics();

            // Prepare the HTML response by:
            // 1. Reading the visualization template
            // 2. Converting the graph to JSON format
            // 3. Injecting the JSON data into the template
            String htmlContent = Files.readString(Paths.get(TEMP_HTML));
            String graphJson = HtmlGraphWriter.graphToJson(graph);
            htmlContent = htmlContent.replace("__GRAPH_DATA_JSON__", graphJson);

            // Send the HTML response with proper HTTP headers
            // The client will render this as an interactive graph visualization
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + htmlContent.length() + "\r\n" +
                    "\r\n" +
                    htmlContent;
            toClient.write(response.getBytes());

        } catch (Exception e) {
            // If any error occurs during processing, send a 400 Bad Request response
            // with details about what went wrong
            sendError(toClient, "Error processing configuration: " + e.getMessage());
        }
    }

    /**
     * Sends an error response to the client with the specified message.
     * 
     * @param toClient The output stream to write the response to
     * @param message The error message to include in the response
     * @throws IOException If there's an error writing to the output stream
     */
    private void sendError(OutputStream toClient, String message) throws IOException {
        String response = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/plain\r\n" +
                "Content-Length: " + message.length() + "\r\n" +
                "\r\n" +
                message;
        toClient.write(response.getBytes());
    }

    @Override
    public void close() throws IOException {
        // No cleanup needed as this servlet doesn't maintain any state
        // or resources that need to be released
    }
}
