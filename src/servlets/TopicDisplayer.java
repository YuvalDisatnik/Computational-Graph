import graph.Message;
import graph.Topic;
import graph.TopicManagerSingleton;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class TopicDisplayer implements Servlet {

    @Override
    public void handle(String httpMethod, String uri, Map<String, String> parameters, OutputStream toClient) throws IOException {
        try {
            // Validate HTTP method
            if (!"POST".equalsIgnoreCase(httpMethod)) {
                sendErrorResponse(toClient, "Only POST method is supported");
                return;
            }

            // Extract parameters from the request
            String topicName = parameters.get("topic");
            String messageValue = parameters.get("message");

            // Validate input
            if (topicName == null || topicName.trim().isEmpty()) {
                sendErrorResponse(toClient, "Topic name is required");
                return;
            }
            
            if (messageValue == null || messageValue.trim().isEmpty()) {
                sendErrorResponse(toClient, "Message value is required");
                return;
            }

            // Get TopicManager and publish message
            TopicManagerSingleton.TopicManager topicManager = TopicManagerSingleton.get();
            Topic topic = topicManager.getTopic(topicName.trim());
            Message message = new Message(messageValue.trim());
            topic.publish(message);

            // Generate HTML response with topics table
            String htmlResponse = generateHtmlResponse(topicManager.getTopics());
            
            // Send HTTP response
            String httpResponse = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html; charset=UTF-8\r\n" +
                    "Content-Length: " + htmlResponse.getBytes().length + "\r\n" +
                    "\r\n" +
                    htmlResponse;
            
            toClient.write(httpResponse.getBytes());
            toClient.flush();

        } catch (Exception e) {
            sendErrorResponse(toClient, "Internal server error: " + e.getMessage());
        }
    }

    private String generateHtmlResponse(Collection<Topic> topics) {
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

        if (topics.isEmpty()) {
            html.append("            <tr>\n");
            html.append("                <td colspan=\"2\" class=\"no-data\">No topics available</td>\n");
            html.append("            </tr>\n");
        } else {
            for (Topic topic : topics) {
                html.append("            <tr>\n");
                html.append("                <td>").append(escapeHtml(topic.name)).append("</td>\n");
                Message lastMessage = topic.getLastMessage();
                String lastValue = (lastMessage != null) ? lastMessage.asText : "No messages";
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

    private void sendErrorResponse(OutputStream toClient, String errorMessage) throws IOException {
        String htmlError = "<!DOCTYPE html>\n" +
                "<html><head><title>Error</title></head>\n" +
                "<body><h1>Error</h1><p>" + escapeHtml(errorMessage) + "</p></body></html>";
        
        String httpResponse = "HTTP/1.1 400 Bad Request\r\n" +
                "Content-Type: text/html; charset=UTF-8\r\n" +
                "Content-Length: " + htmlError.getBytes().length + "\r\n" +
                "\r\n" +
                htmlError;
        
        toClient.write(httpResponse.getBytes());
        toClient.flush();
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
}










