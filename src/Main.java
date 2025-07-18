import server.*;
import servlets.*;

/**
 * Main entry point for the Computational Graph Web Application.
 * 
 * This application provides a web-based interface for creating, visualizing, 
 * and interacting with computational graphs using a publisher/subscriber architecture.
 * 
 * The server runs on port 8080 by default and provides the following endpoints:
 * - GET /app/* - Static file serving for the web interface
 * - GET /docs/* - Static file serving for the web interface
 * - POST /upload - Configuration file upload and processing
 * - POST /generate-config - AI-powered configuration generation
 * - GET /publish - Message publishing to topics
 * - GET /graph-data - Graph data retrieval for visualization
 * 
 * @author Omri Triki, Yuval Disatnik
 */
public class Main {
    public static void main(String[] args) throws Exception{

        HTTPServer server=new MyHTTPServer(8080,5);
        Servlet confLoader = new ConfLoader();

        // Register servlets for different endpoints
        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", confLoader);
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.addServlet("GET", "/graph-data", confLoader);
        server.addServlet("GET", "/docs/", new DocLoader());

        // Start the server
        server.start();
        System.out.println("Computational Graph Server is running!");
        System.out.println("Please navigate to: http://localhost:8080/app/index.html");
        System.out.println("Press Enter to stop the server...");
        
        // Wait for user input to stop the server
        System.in.read();
        server.close();
        System.out.println("Server stopped successfully.");
    }
}