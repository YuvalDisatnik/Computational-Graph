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
import java.util.Map;

/**
 * Servlet that handles configuration file uploads and generates a computational graph visualization.
 * This servlet:
 * 1. Receives uploaded configuration files via POST requests
 * 2. Parses multipart form data to extract file content
 * 3. Saves files server-side with unique names
 * 4. Creates a GenericConfig from the file
 * 5. Generates a Graph from the configuration
 * 6. Returns an HTML response with the graph visualization
 */
public class ConfLoader implements Servlet {
    /** Directory where uploaded configuration files are stored */
    private static final String UPLOAD_DIR = "config_files";
    /** Path to the HTML template used for graph visualization */
    private static final String TEMP_HTML = "html_files/graph_temp.html";

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // Only handle POST requests
            if (!"POST".equalsIgnoreCase(ri.getHttpCommand())) {
                sendErrorResponse(toClient, 405, "Method Not Allowed", "Only POST method is supported");
                return;
            }

            // Check which endpoint is being called
            String uri = ri.getUri();
            if ("/generate-config".equals(uri)) {
                handleGenerateConfig(ri, toClient);
                return;
            }

            // Try to get filename from parameters (for simple uploads)
            String filename = ri.getParameters().get("filename");
            String fileContent = null;

            // Extract file content from request
            if (ri.getContent() != null && ri.getContent().length > 0) {
                // Handle direct content upload (for testing or simple scenarios)
                fileContent = new String(ri.getContent()).trim();
            } else {
                sendErrorResponse(toClient, 400, "Bad Request", "No file content received. Please upload a configuration file.");
                return;
            }

            if (fileContent.isEmpty()) {
                sendErrorResponse(toClient, 400, "Bad Request", "Empty file content. Please upload a valid configuration file.");
                return;
            }

            // Validate configuration file format
            if (!isValidConfigFormat(fileContent)) {
                sendErrorResponse(toClient, 400, "Bad Request", 
                    "Invalid configuration format. Expected format: each agent should have 3 lines (class name, subscriptions, publications).");
                return;
            }

            // Create upload directory if it doesn't exist
            Files.createDirectories(Paths.get(UPLOAD_DIR));

            // Save the configuration file with a unique name based on timestamp
            String fileName = (filename != null && !filename.isEmpty()) 
                ? sanitizeFilename(filename) 
                : "config_" + System.currentTimeMillis() + ".conf";
            
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            Files.write(filePath, fileContent.getBytes());

            // Process the configuration file
            GenericConfig config = new GenericConfig();
            config.setConfFile(filePath.toString());
            config.create();

            // Generate a graph representation from the topic connections
            Graph graph = new Graph();
            graph.createFromTopics();

            // Check if we should return JSON or HTML
            String acceptHeader = ri.getParameters().get("Accept");
            if ("application/json".equals(acceptHeader)) {
                // Return just the JSON data for AJAX requests
                String graphJson = HtmlGraphWriter.graphToJson(graph);
                sendJsonResponse(toClient, graphJson);
            } else {
                // Return full HTML page with embedded graph
                sendHtmlResponse(toClient, graph);
            }

        } catch (IllegalArgumentException e) {
            sendErrorResponse(toClient, 400, "Bad Request", "Configuration error: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(toClient, 500, "Internal Server Error", "Error processing configuration: " + e.getMessage());
        }
    }

    /**
     * Handles the /generate-config endpoint
     */
    private void handleGenerateConfig(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            // Extract JSON data from request body
            byte[] contentBytes = ri.getContent();
            String requestBody = contentBytes != null ? new String(contentBytes) : null;
            
            if (requestBody == null || requestBody.trim().isEmpty()) {
                sendErrorResponse(toClient, 400, "Bad Request", "Request body is required");
                return;
            }

            // Parse JSON to extract description
            String description = extractDescriptionFromJson(requestBody);
            if (description == null || description.trim().isEmpty()) {
                sendErrorResponse(toClient, 400, "Bad Request", "Description is required");
                return;
            }

            // Generate a simple configuration file based on description
            String generatedConfig = generateConfigFromDescription(description);
            
            // Send the generated config as a file download
            sendConfigFileResponse(toClient, generatedConfig);
            
        } catch (Exception e) {
            sendErrorResponse(toClient, 500, "Internal Server Error", "Error generating configuration: " + e.getMessage());
        }
    }

    /**
     * Extracts description from JSON request
     */
    private String extractDescriptionFromJson(String json) {
        try {
            // Simple JSON parsing for {"description": "value"}
            String content = json.trim();
            if (content.startsWith("{") && content.endsWith("}")) {
                content = content.substring(1, content.length() - 1);
            }
            
            String[] pairs = content.split(",");
            for (String pair : pairs) {
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim().replaceAll("\"", "");
                    String value = keyValue[1].trim().replaceAll("\"", "");
                    
                    if ("description".equals(key)) {
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            // Return null if parsing fails
        }
        return null;
    }

    /**
     * Generates a simple configuration based on description
     */
    private String generateConfigFromDescription(String description) {
        StringBuilder config = new StringBuilder();
        
        // Generate a simple configuration based on common patterns
        config.append("# Generated configuration from description:\n");
        config.append("# ").append(description).append("\n\n");
        
        // Add some basic agents based on keywords in description
        if (description.toLowerCase().contains("add") || description.toLowerCase().contains("plus") || description.toLowerCase().contains("sum")) {
            config.append("test.PlusAgent\n");
            config.append("A,B\n");
            config.append("SUM\n\n");
        }
        
        if (description.toLowerCase().contains("increment") || description.toLowerCase().contains("inc")) {
            config.append("test.IncAgent\n");
            config.append("SUM\n");
            config.append("RESULT\n\n");
        }
        
        // Default simple configuration if no keywords match
        if (config.toString().split("\n").length < 5) {
            config.setLength(0);
            config.append("# Generated configuration\n");
            config.append("test.PlusAgent\n");
            config.append("INPUT1,INPUT2\n");
            config.append("OUTPUT1\n\n");
            config.append("test.IncAgent\n");
            config.append("OUTPUT1\n");
            config.append("FINAL_RESULT\n");
        }
        
        return config.toString();
    }

    /**
     * Sends a configuration file as download response
     */
    private void sendConfigFileResponse(OutputStream toClient, String configContent) throws IOException {
        String filename = "generated-config-" + System.currentTimeMillis() + ".conf";
        
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Disposition: attachment; filename=\"" + filename + "\"\r\n" +
                "Content-Length: " + configContent.getBytes().length + "\r\n" +
                "\r\n" +
                configContent;
        
        toClient.write(response.getBytes());
        toClient.flush();
    }

    /**
     * Validates the configuration file format
     */
    private boolean isValidConfigFormat(String content) {
        String[] lines = content.split("\n");
        int nonEmptyLines = 0;
        
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                nonEmptyLines++;
            }
        }
        
        // Must have a multiple of 3 non-empty lines (class, subs, pubs for each agent)
        return nonEmptyLines > 0 && nonEmptyLines % 3 == 0;
    }

    /**
     * Sanitizes filename to prevent path traversal attacks
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) return "config.conf";
        
        // Remove path components and dangerous characters
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Ensure it has a .conf extension
        if (!filename.toLowerCase().endsWith(".conf")) {
            filename += ".conf";
        }
        
        return filename;
    }

    /**
     * Sends an HTML response with the graph visualization
     */
    private void sendHtmlResponse(OutputStream toClient, Graph graph) throws IOException {
        try {
            // Read the HTML template
            String htmlContent = Files.readString(Paths.get(TEMP_HTML));
            
            // Convert graph to JSON and inject into template
            String graphJson = HtmlGraphWriter.graphToJson(graph);
            htmlContent = htmlContent.replace("__GRAPH_DATA_JSON__", graphJson);
            
            // Send the HTML response
            String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + htmlContent.getBytes().length + "\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "\r\n" +
                    htmlContent;
            
            toClient.write(response.getBytes());
            toClient.flush();
            
        } catch (IOException e) {
            // Fallback to a simple HTML response if template is not available
            String fallbackHtml = generateFallbackHtml(graph);
            sendSimpleHtmlResponse(toClient, fallbackHtml);
        }
    }

    /**
     * Sends a JSON response with the graph data
     */
    private void sendJsonResponse(OutputStream toClient, String jsonData) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: " + jsonData.getBytes().length + "\r\n" +
                "Cache-Control: no-cache\r\n" +
                "\r\n" +
                jsonData;
        
        toClient.write(response.getBytes());
        toClient.flush();
    }

    /**
     * Generates a fallback HTML response if the template is not available
     */
    private String generateFallbackHtml(Graph graph) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>Configuration Uploaded Successfully</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }\n");
        html.append("        .container { max-width: 800px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .success { color: #28a745; border: 1px solid #28a745; padding: 15px; border-radius: 5px; background: #f8fff9; }\n");
        html.append("        .graph-info { margin: 20px 0; padding: 15px; background: #f8f9fa; border-radius: 5px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"success\">\n");
        html.append("            <h2>✅ Configuration Uploaded Successfully!</h2>\n");
        html.append("            <p>Your configuration file has been processed and the computational graph has been created.</p>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"graph-info\">\n");
        html.append("            <h3>Graph Information:</h3>\n");
        html.append("            <p><strong>Total Nodes:</strong> ").append(graph.size()).append("</p>\n");
        html.append("            <p><strong>Has Cycles:</strong> ").append(graph.hasCycles() ? "Yes" : "No").append("</p>\n");
        html.append("        </div>\n");
        html.append("        <p><a href=\"/app/\">← Back to Main Application</a></p>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    /**
     * Sends a simple HTML response
     */
    private void sendSimpleHtmlResponse(OutputStream toClient, String htmlContent) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + htmlContent.getBytes().length + "\r\n" +
                "\r\n" +
                htmlContent;
        
        toClient.write(response.getBytes());
        toClient.flush();
    }

    /**
     * Sends an error response to the client with the specified status and message.
     */
    private void sendErrorResponse(OutputStream toClient, int statusCode, String statusText, String message) throws IOException {
        String htmlError = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>%d %s</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; margin: 40px; }\n" +
            "        .error { color: #dc3545; border: 1px solid #dc3545; padding: 15px; border-radius: 5px; background: #fff5f5; }\n" +
            "        .back-link { margin-top: 20px; }\n" +
            "        .back-link a { color: #007bff; text-decoration: none; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"error\">\n" +
            "        <h2>%d %s</h2>\n" +
            "        <p>%s</p>\n" +
            "    </div>\n" +
            "    <div class=\"back-link\">\n" +
            "        <a href=\"/app/\">&larr; Back to Application</a>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            statusCode, statusText, statusCode, statusText, escapeHtml(message)
        );
        
        String response = String.format(
            "HTTP/1.1 %d %s\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: %d\r\n" +
            "\r\n" +
            "%s",
            statusCode, statusText, htmlError.getBytes().length, htmlError
        );
        
        toClient.write(response.getBytes());
        toClient.flush();
    }

    /**
     * Escapes HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }

    @Override
    public void close() throws IOException {
        // No cleanup needed as this servlet doesn't maintain any persistent state
    }
}
