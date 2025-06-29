package servlets;

import configs.GenericConfig;
import configs.Graph;
import server.Servlet;
import server.RequestParser.RequestInfo;
import views.HtmlGraphWriter;
import graph.TopicManagerSingleton;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    private static Graph lastGraph = null;

    /** Directory where uploaded configuration files are stored */
    private static final String UPLOAD_DIR = "config_files";
    /** Path to the HTML template used for graph visualization */
    private static final String TEMP_HTML = "html_files/graph_temp.html";

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        // CORS headers
        String corsHeaders = "Access-Control-Allow-Origin: *\r\n";

        if ("GET".equalsIgnoreCase(ri.getHttpCommand())) {
            if ("/graph-data".equals(ri.getUri())) {
                handleGetGraphData(toClient, corsHeaders);
            } else {
                sendErrorResponse(toClient, 405, "Method Not Allowed", "Only POST and GET /graph-data are supported", corsHeaders);
            }
            return;
        }

        try {
            System.out.println("[ConfLoader] Starting request handling");
            System.out.println("[ConfLoader] HTTP Command: " + ri.getHttpCommand());
            System.out.println("[ConfLoader] URI: " + ri.getUri());

            // Only handle POST requests
            if (!"POST".equalsIgnoreCase(ri.getHttpCommand())) {
                System.out.println("[ConfLoader] Error: Invalid HTTP method - " + ri.getHttpCommand());
                sendErrorResponse(toClient, 405, "Method Not Allowed", "Only POST method is supported", corsHeaders);
                return;
            }

            // Check which endpoint is being called
            String uri = ri.getUri();
            if ("/generate-config".equals(uri)) {
                System.out.println("[ConfLoader] Handling generate-config endpoint");
                handleGenerateConfig(ri, toClient, corsHeaders);
                return;
            }
            // Try to get filename from parameters (for simple uploads)
            String filename = ri.getParameters().get("filename");
            System.out.println("[ConfLoader] Filename from parameters: " + filename);
            String fileContent = null;

            // Extract file content from request
            if (ri.getContent() != null && ri.getContent().length > 0) {
                fileContent = new String(ri.getContent()).trim() + "\n";
                //System.out.println("[ConfLoader] Content preview: " + fileContent.substring(0, Math.min(100, fileContent.length())) + "...");
            } else {
                System.out.println("[ConfLoader] Error: No content received");
                sendErrorResponse(toClient, 400, "Bad Request", "No file content received. Please upload a configuration file.", corsHeaders);
                return;
            }

            if (fileContent.isEmpty()) {
                System.out.println("[ConfLoader] Error: Empty file content");
                sendErrorResponse(toClient, 400, "Bad Request", "Empty file content. Please upload a valid configuration file.", corsHeaders);
                return;
            }

            // Validate configuration file format
            if (!isValidConfigFormat(fileContent)) {
                System.out.println("[ConfLoader] Error: Invalid configuration format");
                sendErrorResponse(toClient, 400, "Bad Request", 
                    "Invalid configuration format. Expected format: each agent should have 3 lines (class name, subscriptions, publications).", corsHeaders);
                return;
            }
            Files.createDirectories(Paths.get(UPLOAD_DIR));
            // Save the configuration file with a unique name based on timestamp
            String fileName = (filename != null && !filename.isEmpty()) 
                ? sanitizeFilename(filename) 
                : "config_" + System.currentTimeMillis() + ".conf";
            
            // saving the file to the upload directory
            Path filePath = Paths.get(UPLOAD_DIR, fileName);
            System.out.println("[ConfLoader] Saving file to: " + filePath);
            Files.write(filePath, fileContent.getBytes());
            // Process the configuration file
            TopicManagerSingleton.get().clear();
            GenericConfig config = new GenericConfig();
            config.setConfFile(filePath.toString());
            config.create();

            // Print graph data before creation
            //GenericConfig.logGraphData(null); // Before creating the graph
            Graph graph = new Graph();
            //GenericConfig.logGraphData(graph); // After creating the graph
            graph.createFromTopics();
            //GenericConfig.logGraphData(graph); // After creating the graph
            lastGraph = graph;
            System.out.println("[ConfLoader] Graph created successfully");

            // Check if we should return JSON or HTML
            String acceptHeader = ri.getParameters().get("Accept");
            System.out.println("[ConfLoader] Accept header: " + acceptHeader);
            if ("application/json".equals(acceptHeader)) {
                System.out.println("[ConfLoader] Sending JSON response");
                String graphJson = HtmlGraphWriter.graphToJson(graph);
                sendJsonResponse(toClient, graphJson, corsHeaders);
            } else {
                System.out.println("[ConfLoader] Sending HTML response");
                sendHtmlResponse(toClient, graph, corsHeaders);
            }

        } catch (IllegalArgumentException e) {
            System.out.println("[ConfLoader] Configuration error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(toClient, 400, "Bad Request", "Configuration error: " + e.getMessage(), corsHeaders);
        } catch (Exception e) {
            System.out.println("[ConfLoader] Unexpected error: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(toClient, 500, "Internal Server Error", "Error processing configuration: " + e.getMessage(), corsHeaders);
        }
    }

    private void handleGetGraphData(OutputStream toClient, String corsHeaders) throws IOException {
        System.out.println("[ConfLoader] handleGetGraphData called");
        if (lastGraph == null) {
            System.out.println("[ConfLoader] No graph available, sending 404");
            sendErrorResponse(toClient, 404, "Not Found", "Graph not available. Please upload a config file first.", corsHeaders);
            return;
        }
        System.out.println("[ConfLoader] Converting graph to JSON");
        String graphJson = HtmlGraphWriter.graphToJson(lastGraph);
        System.out.println("[ConfLoader] Graph JSON generated, length: " + graphJson.length());
        System.out.println("[ConfLoader] Graph JSON preview: " + graphJson.substring(0, Math.min(200, graphJson.length())) + "...");
        sendJsonResponse(toClient, graphJson, corsHeaders);
        System.out.println("[ConfLoader] JSON response sent");
    }

    public static Graph getLastGraph() {
        return lastGraph;
    }

    /**
     * Handles the /generate-config endpoint
     */
    private void handleGenerateConfig(RequestInfo ri, OutputStream toClient, String corsHeaders) throws IOException {
        try {
            byte[] contentBytes = ri.getContent();
            String requestBody = contentBytes != null ? new String(contentBytes) : null;
            
            System.out.println("[ConfLoader] Request body: " + requestBody);
            
            if (requestBody == null || requestBody.trim().isEmpty()) {
                System.out.println("[ConfLoader] Error: Empty request body");
                sendErrorResponse(toClient, 400, "Bad Request", "Request body is required", corsHeaders);
                return;
            }

            String description = extractDescriptionFromJson(requestBody);
            System.out.println("[ConfLoader] Extracted description: " + description);

            if (description == null || description.trim().isEmpty()) {
                System.out.println("[ConfLoader] Error: Empty description");
                sendErrorResponse(toClient, 400, "Bad Request", "Description is required", corsHeaders);
                return;
            }

            String generatedConfig = generateConfigFromDescription(description);
            System.out.println("[ConfLoader] Generated config:\n" + generatedConfig);
            
            sendConfigFileResponse(toClient, generatedConfig, corsHeaders);
            System.out.println("[ConfLoader] Config file sent successfully");
            
        } catch (Exception e) {
            System.out.println("[ConfLoader] Error in generate-config: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(toClient, 500, "Internal Server Error", "Error generating configuration: " + e.getMessage(), corsHeaders);
        }
    }

    /**
     * Extracts description from JSON request
     */
    private String extractDescriptionFromJson(String json) {
        System.out.println("[ConfLoader] Extracting description from JSON: " + json);
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
                        System.out.println("[ConfLoader] Found description: " + value);
                        return value;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("[ConfLoader] Error parsing JSON: " + e.getMessage());
        }
        System.out.println("[ConfLoader] No description found in JSON");
        return null;
    }

    /**
     * Generates a simple configuration based on description
     */
    private String generateConfigFromDescription(String description) {
        System.out.println("[ConfLoader] Generating config from description: " + description);
        StringBuilder config = new StringBuilder();
        
        // Generate a simple configuration based on common patterns
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
        
        System.out.println("[ConfLoader] Generated configuration:\n" + config.toString());
        return config.toString();
    }

    /**
     * Sends a configuration file as download response
     */
    private void sendConfigFileResponse(OutputStream toClient, String configContent, String corsHeaders) throws IOException {
        String filename = "generated-config-" + System.currentTimeMillis() + ".conf";
        System.out.println("[ConfLoader] Using filename: " + filename);
        
        String response = "HTTP/1.1 200 OK\r\n" +
                corsHeaders +
                "Content-Type: application/octet-stream\r\n" +
                "Content-Disposition: attachment; filename=\"" + filename + "\"\r\n" +
                "Content-Length: " + configContent.getBytes().length + "\r\n" +
                "\r\n" +
                configContent;
        
        toClient.write(response.getBytes());
        toClient.flush();
        System.out.println("[ConfLoader] Config file response sent");
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
        boolean isValid = nonEmptyLines > 0 && nonEmptyLines % 3 == 0;
        return isValid;
    }

    /**
     * Sanitizes filename to prevent path traversal attacks
     */
    private String sanitizeFilename(String filename) {
        if (filename == null) {
            System.out.println("[ConfLoader] Filename is null, using default");
            return "config.conf";
        }
        
        // Remove path components and dangerous characters
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        // Ensure it has a .conf extension
        if (!filename.toLowerCase().endsWith(".conf")) {
            filename += ".conf";
        }
        
        System.out.println("[ConfLoader] Sanitized filename: " + filename);
        return filename;
    }

    /**
     * Sends an HTML response with the graph visualization
     */
    private void sendHtmlResponse(OutputStream toClient, Graph graph, String corsHeaders) throws IOException {
        try {
            // Use HtmlGraphWriter to generate the HTML
            List<String> htmlLines = HtmlGraphWriter.getGraphHTML(graph);
            
            // Convert the list of strings to a single string
            StringBuilder htmlContent = new StringBuilder();
            for (String line : htmlLines) {
                htmlContent.append(line).append("\n");
            }
            
            System.out.println("[ConfLoader] Sending HTML response");
            String response = "HTTP/1.1 200 OK\r\n" +
                    corsHeaders +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + htmlContent.toString().getBytes().length + "\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "\r\n" +
                    htmlContent.toString();
            
            toClient.write(response.getBytes());
            toClient.flush();
            
            // For testing purposes, also write to test.html
            HtmlGraphWriter.writeToTestFile(graph);
            
        } catch (IOException e) {
            System.out.println("[ConfLoader] Error generating HTML, using fallback HTML");
            String fallbackHtml = generateFallbackHtml(graph);
            sendSimpleHtmlResponse(toClient, fallbackHtml, corsHeaders);
        }
    }

    /**
     * Sends a JSON response with the graph data
     */
    private void sendJsonResponse(OutputStream toClient, String jsonData, String corsHeaders) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                corsHeaders +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: " + jsonData.getBytes().length + "\r\n" +
                "Cache-Control: no-cache\r\n" +
                "\r\n" +
                jsonData;
        
        toClient.write(response.getBytes());
        toClient.flush();
        System.out.println("[ConfLoader] JSON response sent successfully");
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
        
        System.out.println("[ConfLoader] Fallback HTML generated");
        return html.toString();
    }

    /**
     * Sends a simple HTML response
     */
    private void sendSimpleHtmlResponse(OutputStream toClient, String htmlContent, String corsHeaders) throws IOException {
        String response = "HTTP/1.1 200 OK\r\n" +
                corsHeaders +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + htmlContent.getBytes().length + "\r\n" +
                "\r\n" +
                htmlContent;
        
        toClient.write(response.getBytes());
        toClient.flush();
        System.out.println("[ConfLoader] Simple HTML response sent");
    }

    /**
     * Sends an error response to the client with the specified status and message.
     */
    private void sendErrorResponse(OutputStream toClient, int statusCode, String statusText, String message, String corsHeaders) throws IOException {
        System.out.println("[ConfLoader] Sending error response - " + statusCode + " " + statusText + ": " + message);
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
            corsHeaders +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Content-Length: %d\r\n" +
            "\r\n" +
            "%s",
            statusCode, statusText, htmlError.getBytes().length, htmlError
        );
        
        toClient.write(response.getBytes());
        toClient.flush();
        System.out.println("[ConfLoader] Error response sent");
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
        System.out.println("[ConfLoader] Closing servlet");
    }
}
