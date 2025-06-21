package servlets;

import configs.Graph;
import server.RequestParser;
import server.Servlet;
import views.HtmlGraphWriter;

import java.io.IOException;
import java.io.OutputStream;

public class GraphDataServlet implements Servlet {

    @Override
    public void handle(RequestParser.RequestInfo ri, OutputStream toClient) throws IOException {
        String corsHeaders = "Access-Control-Allow-Origin: *\r\n";
        try {
            Graph graph = ConfLoader.getLastGraph();
            if (graph == null) {
                String errorResponse = "HTTP/1.1 404 Not Found\r\n" +
                        corsHeaders +
                        "Content-Type: text/plain\r\n" +
                        "\r\n" +
                        "Graph not created yet. Please upload a configuration file first.";
                toClient.write(errorResponse.getBytes());
                return;
            }
            String graphJson = HtmlGraphWriter.graphToJson(graph);
            String response = "HTTP/1.1 200 OK\r\n" +
                    corsHeaders +
                    "Content-Type: application/json\r\n" +
                    "\r\n" +
                    graphJson;
            toClient.write(response.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            String errorResponse = "HTTP/1.1 500 Internal Server Error\r\n" +
                    corsHeaders +
                    "Content-Type: text/plain\r\n" +
                    "\r\n" +
                    "Error generating graph data: " + e.getMessage();
            toClient.write(errorResponse.getBytes());
        }
    }

    @Override
    public void close() throws IOException {
        // No resources to close
    }
} 