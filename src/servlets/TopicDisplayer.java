package servlets;

import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;
import server.Servlet;
import server.RequestParser.RequestInfo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        String corsHeaders = "Access-Control-Allow-Origin: *\r\n";

        Map<String, String> params = ri.getParameters();
        String topic = params.get("topic");
        String message = params.get("message");

        System.out.println("=== TopicDisplayer.handle() called ===");
        System.out.println("Request parameters: " + params);
        System.out.println("Topic parameter: '" + topic + "'");
        System.out.println("Message parameter: '" + message + "'");

        if (topic != null && !topic.isEmpty() && message != null && !message.isEmpty()) {
            // If topic and message are provided, publish the message
            System.out.println("Publishing message to topic: " + topic);
            try {
                System.out.println("Value before: " + TopicManagerSingleton.get().getTopic(topic).getLastMessage());
                double msgValue = Double.parseDouble(message);
                System.out.println("Parsed message value: " + msgValue);
                TopicManagerSingleton.get().getTopic(topic).publish(new Message(msgValue));
                System.out.println("Value after: " + TopicManagerSingleton.get().getTopic(topic).getLastMessage());

                String response = "HTTP/1.1 200 OK\r\n" +
                                  corsHeaders +
                                  "Content-Type: text/plain\r\n" +
                                  "\r\n" +
                                  "Message published successfully.";
                toClient.write(response.getBytes());
                System.out.println("Response sent: Message published successfully");
            } catch (NumberFormatException e) {
                System.out.println("Error parsing message: " + e.getMessage());
                String errorResponse = "HTTP/1.1 400 Bad Request\r\n" +
                                       corsHeaders +
                                       "Content-Type: text/plain\r\n" +
                                       "\r\n" +
                                       "Invalid message format. Must be a number.";
                toClient.write(errorResponse.getBytes());
            }
        } else {
            // Otherwise, display the topics
            System.out.println("Displaying topics (no message to publish)");
            String response = "HTTP/1.1 200 OK\r\n" +
                              corsHeaders +
                              "Content-Type: text/html\r\n" +
                              "\r\n" +
                              getTopicsHtml();
            toClient.write(response.getBytes());
            System.out.println("Response sent: HTML with topics");
        }
    }

    private String getTopicsHtml() {
        System.out.println("=== getTopicsHtml() called ===");
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <title>Topic Status</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("        h1 { color: #333; }\n");
        html.append("        table { border-collapse: collapse; width: 100%; margin-top: 20px; }\n");
        html.append("        th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }\n");
        html.append("        th { background-color: #f2f2f2; font-weight: bold; }\n");
        html.append("        tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("        .no-data { color: #666; font-style: italic; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <h1>Topic Status</h1>\n");
        html.append("    <table>\n");
        html.append("        <thead>\n");
        html.append("            <tr>\n");
        html.append("                <th>Topic Name</th>\n");
        html.append("                <th>Last Value</th>\n");
        html.append("            </tr>\n");
        html.append("        </thead>\n");
        html.append("        <tbody>\n");

        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        System.out.println("Retrieved " + topics.size() + " topics from TopicManager");
        
        if (topics.isEmpty()) {
            System.out.println("No topics available");
            html.append("            <tr>\n");
            html.append("                <td colspan=\"2\" class=\"no-data\">No topics available</td>\n");
            html.append("            </tr>\n");
        } else {
            System.out.println("Processing topics:");
            for (Topic topic : topics) {
                System.out.println("  Processing topic: '" + topic.name + "'");
                html.append("            <tr>\n");
                html.append("                <td>").append(escapeHtml(topic.name)).append("</td>\n");
                // Check if topic has messages and get the latest value
                String lastValue = "No messages";
                try {
                    String retrievedValue = topic.getLastMessage();
                    System.out.println("    Topic: " + topic.name + ", Retrieved value: '" + retrievedValue + "'");
                    System.out.println("    Retrieved value is null: " + (retrievedValue == null));
                    System.out.println("    Retrieved value is empty: " + (retrievedValue != null && retrievedValue.isEmpty()));
                    
                    if (retrievedValue != null && !retrievedValue.isEmpty()) {
                        lastValue = retrievedValue;
                        System.out.println("    Using retrieved value: '" + lastValue + "'");
                    } else {
                        System.out.println("    Using default value: '" + lastValue + "'");
                    }
                } catch (Exception e) {
                    lastValue = "Error retrieving value";
                    System.out.println("    Error retrieving value for topic " + topic.name + ": " + e.getMessage());
                    e.printStackTrace();
                }
                
                String escapedValue = escapeHtml(lastValue);
                System.out.println("    Final escaped value: '" + escapedValue + "'");
                html.append("                <td>").append(escapedValue).append("</td>\n");
                html.append("            </tr>\n");
            }
        }

        html.append("        </tbody>\n");
        html.append("    </table>\n");
        html.append("</body>\n");
        html.append("</html>");

        System.out.println("=== getTopicsHtml() completed ===");
        return html.toString();
    }

    private String escapeHtml(String text) {
        System.out.println("    escapeHtml called with: '" + text + "'");
        if (text == null) {
            System.out.println("    escapeHtml: input is null, returning empty string");
            return "";
        }
        String escaped = text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
        System.out.println("    escapeHtml result: '" + escaped + "'");
        return escaped;
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}
