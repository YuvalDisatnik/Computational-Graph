import configs.*;
import server.*;
import servlets.*;
import graph.*;

public class Main {
    public static void main(String[] args) throws Exception{

        HTTPServer server=new MyHTTPServer(8080,5);
        Servlet confLoader = new ConfLoader();

        server.addServlet("GET", "/publish", new TopicDisplayer());
        server.addServlet("POST", "/upload", confLoader);
        server.addServlet("POST", "/generate-config", confLoader);
        server.addServlet("GET", "/app/", new HtmlLoader("html_files"));
        server.addServlet("GET", "/graph-data", confLoader);

        server.start();
        System.out.println("Server is running. Please navigate to:\nhttp://localhost:8080/app/index.html");
        System.in.read();
        server.close();
        System.out.println("done");
    }
}