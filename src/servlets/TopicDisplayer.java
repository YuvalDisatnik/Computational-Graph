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

        if (topic != null && !topic.isEmpty() && message != null && !message.isEmpty()) {
            // If topic and message are provided, publish the message
            try {
                double msgValue = Double.parseDouble(message);
                TopicManagerSingleton.get().getTopic(topic).publish(new Message(msgValue));

                String response = "HTTP/1.1 200 OK\r\n" +
                                  corsHeaders +
                                  "Content-Type: text/plain\r\n" +
                                  "\r\n" +
                                  "Message published successfully.";
                toClient.write(response.getBytes());
            } catch (NumberFormatException e) {
                String errorResponse = "HTTP/1.1 400 Bad Request\r\n" +
                                       corsHeaders +
                                       "Content-Type: text/plain\r\n" +
                                       "\r\n" +
                                       "Invalid message format. Must be a number.";
                toClient.write(errorResponse.getBytes());
            }
        } else {
            // Otherwise, display the topics
            String response = "HTTP/1.1 200 OK\r\n" +
                              corsHeaders +
                              "Content-Type: text/html\r\n" +
                              "\r\n" +
                              getTopicsHtml();
            toClient.write(response.getBytes());
        }
    }

    private String getTopicsHtml() {
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
        html.append("    <p>Message published successfully!</p>\n");
        html.append("    <table>\n");
        html.append("        <thead>\n");
        html.append("            <tr>\n");
        html.append("                <th>Topic Name</th>\n");
        html.append("                <th>Last Value</th>\n");
        html.append("            </tr>\n");
        html.append("        </thead>\n");
        html.append("        <tbody>\n");

        Collection<Topic> topics = TopicManagerSingleton.get().getTopics();
        if (topics.isEmpty()) {
            html.append("            <tr>\n");
            html.append("                <td colspan=\"2\" class=\"no-data\">No topics available</td>\n");
            html.append("            </tr>\n");
        } else {
            for (Topic topic : topics) {
                html.append("            <tr>\n");
                html.append("                <td>").append(escapeHtml(topic.name)).append("</td>\n");
                // Check if topic has messages and get the latest value
                String lastValue = "No messages";
                try {
                    // This is a simplified approach - adapt based on actual Topic API
                    lastValue = topic.toString(); // or whatever method provides the current value
                } catch (Exception e) {
                    lastValue = "Error retrieving value";
                }
                html.append("                <td>").append(escapeHtml(lastValue)).append("</td>\n");
                html.append("            </tr>\n");
            }
        }

        html.append("        </tbody>\n");
        html.append("    </table>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

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
        // No resources to close
    }
}
