package views;

import configs.Graph;
import configs.Node;
import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class HtmlGraphWriter {
    
    /**
     * Writes an HTML representation of the graph to the output stream
     */
    public static void write(Graph graph, OutputStream outputStream) throws IOException {
        String htmlContent = generateHtmlGraph(graph);
        outputStream.write(htmlContent.getBytes());
        outputStream.flush();
    }
    
    /**
     * Converts a Graph object to JSON format compatible with the graph visualization
     * @param graph The Graph object to convert
     * @return JSON string representation of the graph
     */
    public static String graphToJson(Graph graph) {
        if (graph == null || graph.isEmpty()) {
            return "{\"nodes\":[], \"edges\":[]}";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"nodes\":[");
        
        // Track processed nodes to avoid duplicates
        Set<String> processedNodes = new HashSet<>();
        
        // Get current topic manager to access real-time values
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        Collection<Topic> currentTopics = topicManager.getTopics();
        
        // Add all nodes
        boolean firstNode = true;
        for (Node node : graph) {
            if (processedNodes.contains(node.getName())) {
                continue;
            }
            processedNodes.add(node.getName());
            
            if (!firstNode) {
                json.append(",");
            }
            firstNode = false;
            
            json.append("{");
            json.append("\"id\":\"").append(escapeJson(node.getName())).append("\",");
            json.append("\"label\":\"").append(escapeJson(getDisplayLabel(node))).append("\",");
            json.append("\"type\":\"").append(getNodeType(node)).append("\"");
            
            // Add value - try to get real-time value for topics
            String nodeValue = getNodeValue(node, currentTopics);
            if (nodeValue != null) {
                json.append(",\"value\":").append(nodeValue);
            }
            
            json.append("}");
        }
        
        json.append("],");
        json.append("\"edges\":[");
        
        // Add all edges
        boolean firstEdge = true;
        for (Node node : graph) {
            for (Node target : node.getEdges()) {
                if (!firstEdge) {
                    json.append(",");
                }
                firstEdge = false;
                
                json.append("{");
                json.append("\"source\":\"").append(escapeJson(node.getName())).append("\",");
                json.append("\"target\":\"").append(escapeJson(target.getName())).append("\"");
                json.append("}");
            }
        }
        
        json.append("]");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Determines the type of a node based on its name prefix and characteristics
     */
    private static String getNodeType(Node node) {
        String name = node.getName();
        
        if (name.startsWith("T")) {
            return "topic";
        } else if (name.startsWith("A")) {
            return "agent";
        } else if (name.toLowerCase().contains("result") || name.toLowerCase().contains("output")) {
            return "result";
        } else {
            return "topic"; // Default to topic for unknown types
        }
    }
    
    /**
     * Gets a display-friendly label for a node
     */
    private static String getDisplayLabel(Node node) {
        String name = node.getName();
        
        // Remove prefixes (T for topics, A for agents)
        if (name.length() > 1 && (name.startsWith("T") || name.startsWith("A"))) {
            String cleanName = name.substring(1);
            
            // Handle common agent class names for better display
            if (cleanName.contains("Agent")) {
                cleanName = cleanName.replace("Agent", "");
            }
            if (cleanName.contains(".")) {
                // Handle package names like "test.PlusAgent" -> "Plus"
                String[] parts = cleanName.split("\\.");
                cleanName = parts[parts.length - 1];
                if (cleanName.contains("Agent")) {
                    cleanName = cleanName.replace("Agent", "");
                }
            }
            
            return cleanName;
        }
        
        return name;
    }
    
    /**
     * Extracts the current value for a node, prioritizing real-time topic data
     */
    private static String getNodeValue(Node node, Collection<Topic> currentTopics) {
        String nodeType = getNodeType(node);
        
        if ("topic".equals(nodeType)) {
            // For topics, try to get the real current value from TopicManager
            String topicName = getDisplayLabel(node);
            
            for (Topic topic : currentTopics) {
                if (topic.name.equals(topicName)) {
                    // Topic found, but topics don't store values directly
                    // They just forward messages, so check the node's message
                    break;
                }
            }
        }
        
        // Fall back to node's stored message
        if (node.getMsg() != null) {
            return getNodeValue(node.getMsg());
        }
        
        // For agents, don't show a value
        if ("agent".equals(nodeType)) {
            return null;
        }
        
        // For topics without values, show a placeholder
        return null;
    }
    
    /**
     * Extracts a numeric or string value from a message for display
     */
    private static String getNodeValue(Message msg) {
        if (msg == null) {
            return null;
        }
        
        // Try to use the double value if it's not NaN
        if (!Double.isNaN(msg.asDouble)) {
            // Format nicely - avoid unnecessary decimal places
            double value = msg.asDouble;
            if (value == Math.floor(value)) {
                return String.valueOf((int) value);
            } else {
                return String.valueOf(value);
            }
        }
        
        // Fall back to quoted text value if it's not just a number representation
        String text = msg.asText;
        if (text != null && !text.trim().isEmpty()) {
            // Don't quote if it looks like a number that failed to parse
            try {
                Double.parseDouble(text);
                return "\"" + escapeJson(text) + "\"";
            } catch (NumberFormatException e) {
                return "\"" + escapeJson(text) + "\"";
            }
        }
        
        return null;
    }
    
    /**
     * Escapes special characters for JSON
     */
    private static String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Generates a complete HTML representation of the graph
     */
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
        
        // Add graph statistics
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
        
        // Get current topics for real-time values
        TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
        Collection<Topic> currentTopics = topicManager.getTopics();
        
        for (Node node : graph) {
            String nodeType = getNodeType(node);
            html.append("            <div class=\"node ").append(nodeType).append("\">\n");
            html.append("                <div class=\"node-header\">\n");
            html.append("                    <h3 class=\"node-title\">").append(escapeHtml(getDisplayLabel(node))).append("</h3>\n");
            html.append("                    <span class=\"node-type type-").append(nodeType).append("\">").append(nodeType.toUpperCase()).append("</span>\n");
            html.append("                </div>\n");
            
            // Show value if available
            String nodeValue = getNodeValue(node, currentTopics);
            if (nodeValue != null) {
                String displayValue = nodeValue.replace("\"", ""); // Remove quotes for display
                html.append("                <div class=\"node-value\">Value: ").append(escapeHtml(displayValue)).append("</div>\n");
            }
            
            // Show connections
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
    
    /**
     * Escapes HTML special characters
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}
