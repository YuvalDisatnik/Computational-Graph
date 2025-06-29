package views;

import configs.Graph;
import configs.Node;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HtmlGraphWriter {
    private static final Logger LOGGER = Logger.getLogger(HtmlGraphWriter.class.getName());
    
    /**
     * Writes an HTML representation of the graph to the output stream
     */
    public static void write(Graph graph, OutputStream outputStream) throws IOException {
        //        LOGGER.info("Writing graph with " + (graph != null ? graph.size() : 0) + " nodes");
        String htmlContent = generateHtmlGraph(graph);
        //        LOGGER.info("Generated HTML content, " + htmlContent.length() + " bytes");
        outputStream.write(htmlContent.getBytes());
        outputStream.flush();
        //        LOGGER.info("HTML content written to output stream");
    }
    
    /**
     * Converts a Graph object to JSON format compatible with the graph visualization
     */
    public static String graphToJson(Graph graph) {
        if (graph == null || graph.isEmpty()) {
            //            LOGGER.warning("Empty graph provided");
            return "{\"nodes\":[], \"edges\":[]}";
        }
        
        //            LOGGER.info("Converting graph to JSON format");
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"nodes\":[");
        
        Set<String> processedNodes = new HashSet<>();
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        Collection<Topic> currentTopics = topicManager.getTopics();
        //            LOGGER.info("Retrieved " + currentTopics.size() + " current topics");
        
        boolean firstNode = true;
        for (Node node : graph) {
            if (processedNodes.contains(node.getName())) {
                //                LOGGER.fine("Skipping duplicate node: " + node.getName());
                continue;
            }
            processedNodes.add(node.getName());
            
            if (!firstNode) json.append(",");
            firstNode = false;
            
            String nodeType = getNodeType(node);
            //                LOGGER.fine("Processing node: " + node.getName() + " (type: " + nodeType + ")");
            
            json.append("{");
            json.append("\"id\":\"").append(escapeJson(node.getName())).append("\",");
            json.append("\"label\":\"").append(escapeJson(getDisplayLabel(node))).append("\",");
            json.append("\"type\":\"").append(nodeType).append("\"");
            
            String nodeValue = getNodeValue(node, currentTopics);
            if (nodeValue != null) {
                LOGGER.info("[graphToJson] Node '" + node.getName() + "' (type: " + nodeType + ") value: " + nodeValue);
                json.append(",\"value\":").append(nodeValue);
            }
            
            json.append("}");
        }
        
        json.append("],");
        json.append("\"edges\":[");
        
        boolean firstEdge = true;
        for (Node node : graph) {
            for (Node target : node.getEdges()) {
                if (!firstEdge) json.append(",");
                firstEdge = false;
                
                //                    LOGGER.fine("Adding edge: " + node.getName() + " -> " + target.getName());
                json.append("{");
                json.append("\"source\":\"").append(escapeJson(node.getName())).append("\",");
                json.append("\"target\":\"").append(escapeJson(target.getName())).append("\"");
                json.append("}");
            }
        }
        
        json.append("]");
        json.append("}");
        
        //            LOGGER.info("Converted graph to JSON: " + processedNodes.size() + " nodes");
        return json.toString();
    }
    
    /**
     * Returns a list of HTML strings representing the computational graph visualization.
     */
    public static List<String> getGraphHTML(Graph graph) {
        //            LOGGER.info("Generating HTML for graph with " + (graph != null ? graph.size() : 0) + " nodes");
        List<String> htmlLines = new ArrayList<>();
        
        try {
            // Try to load from file system first
            Path templatePath = Paths.get("html_files", "graph_temp.html");
            //                LOGGER.info("Attempting to load template from: " + templatePath.toAbsolutePath());
            
            String templateContent;
            if (Files.exists(templatePath)) {
                //                LOGGER.info("Found template file in filesystem");
                templateContent = Files.readString(templatePath);
            } else {
                // Fallback to classpath resource
                //                LOGGER.info("Template not found in filesystem, trying classpath resource");
                InputStream templateStream = HtmlGraphWriter.class.getClassLoader()
                    .getResourceAsStream("html_files/graph_temp.html");
                
                if (templateStream == null) {
                    //                    LOGGER.severe("Template file not found in either filesystem or classpath");
                    throw new IOException("Could not find graph_temp.html template");
                }
                
                templateContent = new String(templateStream.readAllBytes());
            }
            
            //                LOGGER.info("Successfully loaded template");
            String graphJson = graphToJson(graph);
            //                LOGGER.info("Graph converted to JSON format");
            
            // Replace the graph data in the template
            String updatedContent = templateContent.replaceAll(
                "(<script id=\"graph-data\" type=\"application/json\">)([\\s\\S]*?)(</script>)",
                "$1\n" + graphJson + "\n$3"
            );
            
            // Split the content into lines
            htmlLines.addAll(List.of(updatedContent.split("\n")));
            
        } catch (IOException e) {
            //            LOGGER.severe("Error loading template: " + e.getMessage());
            htmlLines.clear();
            htmlLines.add("<!DOCTYPE html>");
            htmlLines.add("<html><body>");
            htmlLines.add("<h1>Error Loading Graph</h1>");
            htmlLines.add("<p>Failed to load graph visualization: " + e.getMessage() + "</p>");
            htmlLines.add("</body></html>");
        }
        
        return htmlLines;
    }
    
    private static String getNodeType(Node node) {
        String name = node.getName();
        String type;
        
        if (name.startsWith("T")) {
            type = "topic";
        } else if (name.startsWith("A")) {
            type = "agent";
        } else if (name.toLowerCase().contains("result") || name.toLowerCase().contains("output")) {
            type = "result";
        } else {
            type = "topic";
        }
        
        //                LOGGER.finest("Node " + name + " classified as type: " + type);
        return type;
    }
    
    private static String getDisplayLabel(Node node) {
        String name = node.getName();
        String label = name;
        
        if (name.length() > 1 && (name.startsWith("T") || name.startsWith("A"))) {
            String cleanName = name.substring(1);
            
            if (cleanName.contains("Agent")) {
                cleanName = cleanName.replace("Agent", "");
            }
            if (cleanName.contains(".")) {
                String[] parts = cleanName.split("\\.");
                cleanName = parts[parts.length - 1];
                if (cleanName.contains("Agent")) {
                    cleanName = cleanName.replace("Agent", "");
                }
            }
            
            label = cleanName;
        }
        
        //                LOGGER.finest("Node " + name + " display label: " + label);
        return label;
    }
    
    private static String getNodeValue(Node node, Collection<Topic> currentTopics) {
        String nodeType = getNodeType(node);
        String value = null;

        if ("topic".equals(nodeType) || "result".equals(nodeType)) {
            // Get the clean topic name (without the T prefix)
            String topicName = getDisplayLabel(node);
            
            // Look for matching topic in current topics
            for (Topic topic : currentTopics) {
                if (topic.name.equals(topicName)) {
                    String lastMessage = topic.getLastMessage();
                    if (lastMessage != null) {
                        value = lastMessage;
                        LOGGER.info("[getNodeValue] Found topic '" + topicName + "' with value: " + value);
                    }
                    break;
                }
            }
        }

        // Fallback to node's own message if no topic value found
        if (value == null && node.getMsg() != null) {
            value = getNodeValue(node.getMsg());
        }

        if ("agent".equals(nodeType)) {
            return null;
        }

        return value;
    }
    
    private static String getNodeValue(Message msg) {
        if (msg == null) {
            //                    LOGGER.finest("Message is null");
            return null;
        }
        
        String value = null;
        
        if (!Double.isNaN(msg.asDouble)) {
            double doubleValue = msg.asDouble;
            if (doubleValue == Math.floor(doubleValue)) {
                value = String.valueOf((int) doubleValue);
            } else {
                value = String.valueOf(doubleValue);
            }
            //                    LOGGER.finest("Message has double value: " + value);
        }
        
        if (value == null) {
            String text = msg.asText;
            if (text != null && !text.trim().isEmpty()) {
                try {
                    Double.parseDouble(text);
                    value = "\"" + escapeJson(text) + "\"";
                } catch (NumberFormatException e) {
                    value = "\"" + escapeJson(text) + "\"";
                }
                //                    LOGGER.finest("Message has text value: " + value);
            }
        }
        
        return value;
    }
    
    private static String escapeJson(String text) {
        if (text == null) {
            //                    LOGGER.finest("Attempting to escape null text");
            return "";
        }
        String escaped = text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
        //                    LOGGER.finest("Escaped text: " + text + " -> " + escaped);
        return escaped;
    }
    
    private static String generateHtmlGraph(Graph graph) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Computational Graph</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }\n");
        html.append("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .graph-header { text-align: center; margin-bottom: 30px; padding-bottom: 20px; border-bottom: 2px solid #e2e8f0; }\n");
        html.append("        .graph-stats { display: flex; justify-content: center; gap: 30px; margin-bottom: 30px; }\n");
        html.append("        .stat { text-align: center; padding: 15px; background: #f8f9fa; border-radius: 8px; }\n");
        html.append("        .stat-number { font-size: 24px; font-weight: bold; color: #2383c4; }\n");
        html.append("        .stat-label { font-size: 14px; color: #666; margin-top: 5px; }\n");
        html.append("        .nodes-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }\n");
        html.append("        .node { padding: 15px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        html.append("        .topic { background: linear-gradient(135deg, #e3f2fd, #f4f4f9); border-left: 4px solid #2383c4; }\n");
        html.append("        .agent { background: linear-gradient(135deg, #e8f5e8, #f0f8f0); border-left: 4px solid #23c483; }\n");
        html.append("        .result { background: linear-gradient(135deg, #f3e5f5, #faf5ff); border-left: 4px solid #7c3aed; }\n");
        html.append("        .node-header { display: flex; justify-content: between; align-items: center; margin-bottom: 10px; }\n");
        html.append("        .node-title { font-size: 18px; font-weight: 600; margin: 0; }\n");
        html.append("        .node-type { font-size: 12px; padding: 4px 8px; border-radius: 12px; color: white; margin-left: auto; }\n");
        html.append("        .type-topic { background: #2383c4; }\n");
        html.append("        .type-agent { background: #23c483; }\n");
        html.append("        .type-result { background: #7c3aed; }\n");
        html.append("        .node-value { font-size: 16px; font-weight: bold; color: #333; margin: 10px 0; }\n");
        html.append("        .node-connections { font-size: 14px; color: #666; }\n");
        html.append("        .connection-list { margin-top: 5px; }\n");
        html.append("        .connection { display: inline-block; padding: 2px 6px; margin: 2px; background: #e2e8f0; border-radius: 4px; font-size: 12px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"container\">\n");
        html.append("        <div class=\"graph-header\">\n");
        html.append("            <h1>Computational Graph</h1>\n");
        html.append("        </div>\n");
        
        int topicCount = 0, agentCount = 0, resultCount = 0;
        for (Node node : graph) {
            String type = getNodeType(node);
            switch (type) {
                case "topic": topicCount++; break;
                case "agent": agentCount++; break;
                case "result": resultCount++; break;
            }
        }
        
        html.append("        <div class=\"graph-stats\">\n");
        html.append("            <div class=\"stat\">\n");
        html.append("                <div class=\"stat-number\">").append(topicCount).append("</div>\n");
        html.append("                <div class=\"stat-label\">Topics</div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"stat\">\n");
        html.append("                <div class=\"stat-number\">").append(agentCount).append("</div>\n");
        html.append("                <div class=\"stat-label\">Agents</div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"stat\">\n");
        html.append("                <div class=\"stat-number\">").append(graph.size()).append("</div>\n");
        html.append("                <div class=\"stat-label\">Total Nodes</div>\n");
        html.append("            </div>\n");
        html.append("            <div class=\"stat\">\n");
        html.append("                <div class=\"stat-number\">").append(graph.hasCycles() ? "Yes" : "No").append("</div>\n");
        html.append("                <div class=\"stat-label\">Has Cycles</div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");
        
        html.append("        <div class=\"nodes-grid\">\n");
        
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        Collection<Topic> currentTopics = topicManager.getTopics();
        
        for (Node node : graph) {
            String nodeType = getNodeType(node);
            html.append("            <div class=\"node ").append(nodeType).append("\">\n");
            html.append("                <div class=\"node-header\">\n");
            html.append("                    <h3 class=\"node-title\">").append(escapeHtml(getDisplayLabel(node))).append("</h3>\n");
            html.append("                    <span class=\"node-type type-").append(nodeType).append("\">").append(nodeType.toUpperCase()).append("</span>\n");
            html.append("                </div>\n");
            
            String nodeValue = getNodeValue(node, currentTopics);
            LOGGER.info("nodeValue is " + nodeValue);
            if (nodeValue != null) {
                String displayValue = nodeValue.replace("\"", "");
                LOGGER.info("[generateHtmlGraph] Node '" + node.getName() + "' (type: " + nodeType + ") value: " + displayValue);
                html.append("                <div class=\"node-value\">Value: ").append(escapeHtml(displayValue)).append("</div>\n");
            }
            
            if (!node.getEdges().isEmpty()) {
                html.append("                <div class=\"node-connections\">\n");
                html.append("                    <strong>Connected to:</strong>\n");
                html.append("                    <div class=\"connection-list\">\n");
                for (Node edge : node.getEdges()) {
                    html.append("                        <span class=\"connection\">").append(escapeHtml(getDisplayLabel(edge))).append("</span>\n");
                }
                html.append("                    </div>\n");
                html.append("                </div>\n");
            }
            
            html.append("            </div>\n");
        }
        
        html.append("        </div>\n");
        html.append("        <p style=\"text-align: center; margin-top: 30px;\"><a href=\"/app/\">&larr; Back to Application</a></p>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
    
    /**
     * Test method to write the HTML representation to a file
     * @param graph The graph to visualize
     * @return true if successful, false otherwise
     */
    public static boolean writeToTestFile(Graph graph) {
        //            LOGGER.info("Writing graph visualization to html_files/generated_graph.html");
        try {
            Path outputPath = Paths.get("html_files", "generated_graph.html");
            // Log file existence before deletion
            if (Files.exists(outputPath)) {
                //                    LOGGER.info("File exists before writing: " + outputPath.toAbsolutePath());
                //                    LOGGER.info("Deleting existing generated_graph.html before writing new content.");
                //                    Files.delete(outputPath);
                //                    LOGGER.info("File deleted: " + outputPath.toAbsolutePath());
            } else {
                //                    LOGGER.info("File does not exist before writing: " + outputPath.toAbsolutePath());
            }
            List<String> htmlLines = getGraphHTML(graph);
            //                    LOGGER.info("Generated " + htmlLines.size() + " lines of HTML");
            // Log the first 20 lines and the JSON part for debugging
            StringBuilder preview = new StringBuilder();
            for (int i = 0; i < Math.min(20, htmlLines.size()); i++) {
                preview.append(htmlLines.get(i)).append("\n");
            }
            // Try to find and log the JSON part
            for (String line : htmlLines) {
                if (line.trim().startsWith("{\"nodes\"")) {
                    //                    LOGGER.info("Graph JSON being written: " + line);
                    break;
                }
            }
            //                    LOGGER.info("HTML preview (first 20 lines):\n" + preview.toString());
            try (FileWriter writer = new FileWriter(outputPath.toString())) {
                //                    LOGGER.info("Opened FileWriter in overwrite mode for: " + outputPath.toAbsolutePath());
                for (String line : htmlLines) {
                    writer.write(line + "\n");
                }
                //                    LOGGER.info("Finished writing all lines to file.");
            }
            //                    LOGGER.info("Successfully wrote to html_files/generated_graph.html");
            return true;
        } catch (IOException e) {
            //                    LOGGER.severe("Failed to write html_files/generated_graph.html: " + e.getMessage());
            return false;
        }
    }
}
