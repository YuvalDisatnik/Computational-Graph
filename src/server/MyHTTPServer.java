package test;

import test.RequestParser.RequestInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;


public class MyHTTPServer extends Thread implements HTTPServer {
    int port;
    private volatile boolean stop;
    private final Map<String, Servlet> getServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> postServlets = new ConcurrentHashMap<>();
    private final Map<String, Servlet> deleteServlets = new ConcurrentHashMap<>();
    ExecutorService tp;

    public MyHTTPServer(int port, int nThreads) {
        this.port = port;
         tp = Executors.newFixedThreadPool(nThreads);
    }

    public void addServlet(String httpCommanmd, String uri, Servlet s) {
        // add the servlet to the appropriate map based on the HTTP command
        switch (httpCommanmd.toUpperCase()) {
            case "GET":
                getServlets.put(uri, s);
                break;
            case "POST":
                postServlets.put(uri, s);
                break;
            case "DELETE":
                deleteServlets.put(uri, s);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommanmd);
        }
    }

    public void removeServlet(String httpCommanmd, String uri) {
        // remove the servlet from the appropriate map based on the HTTP command
        switch (httpCommanmd.toUpperCase()) {
            case "GET":
                getServlets.remove(uri);
                break;
            case "POST":
                postServlets.remove(uri);
                break;
            case "DELETE":
                deleteServlets.remove(uri);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP command: " + httpCommanmd);
        }
    }

    public void run() {
        stop = false;
        startServer();
    }

    public void close() {
        stop = true;
        tp.shutdownNow(); // Shutdown the thread pool
        try {
            if (!tp.awaitTermination(60, TimeUnit.SECONDS)) {
                tp.shutdownNow();
            }
        } catch (InterruptedException e) {
            tp.shutdownNow();
        }
        getServlets.values().forEach(servlet -> {
            try {
                servlet.close();
            } catch (IOException e) {
                System.out.println("Error closing servlet: " + e.getMessage());
            }
        });
        postServlets.values().forEach(servlet -> {
            try {
                servlet.close();
            } catch (IOException e) {
                System.out.println("Error closing servlet: " + e.getMessage());
            }
        });
        deleteServlets.values().forEach(servlet -> {
            try {
                servlet.close();
            } catch (IOException e) {
                System.out.println("Error closing servlet: " + e.getMessage());
            }
        });
    }

    private void startServer() {
        try (ServerSocket server = new ServerSocket(port)) {
            server.setSoTimeout(1000);
            while (!stop) {
                try {
                    Socket client = server.accept();
                    tp.execute(() -> handleClient(client)); // Submit client handling to the thread pool
                } catch (SocketTimeoutException _) {
                    // Ignore timeout exceptions to allow checking the stop condition
                }
            }
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    private Servlet findLongestMatchingServlet(Map<String, Servlet> servletMap, String uri) {
        String longestMatch = null;
        for (String key : servletMap.keySet()) {
            if (uri.startsWith(key) && (longestMatch == null || key.length() > longestMatch.length())) {
                longestMatch = key;
            }
        }
        return longestMatch != null ? servletMap.get(longestMatch) : null;
    }

    private void handleClient(Socket client) {
        try (BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            RequestParser.RequestInfo requestInfo = RequestParser.parseRequest(input);
            String httpCommand = requestInfo.getHttpCommand();

            // Determine the appropriate servlet based on the longest URI match
            Map<String, Servlet> servletMap;
            switch (httpCommand.toUpperCase()) {
                case "GET":
                    servletMap = getServlets;
                    break;
                case "POST":
                    servletMap = postServlets;
                    break;
                case "DELETE":
                    servletMap = deleteServlets;
                    break;
                default:
                    client.getOutputStream().write("HTTP/1.1 400 Bad Request\r\n\r\n".getBytes());
                    System.out.println("Unsupported HTTP command: " + httpCommand);
                    return;
            }

            Servlet servlet = findLongestMatchingServlet(servletMap, requestInfo.getUri());

            if (servlet != null) {
                servlet.handle(requestInfo, client.getOutputStream());
            } else {
                client.getOutputStream().write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
                System.out.println("No servlet found for " + httpCommand + " " + requestInfo.getUri());
            }
        } catch (IOException e) {
            try {
                client.getOutputStream().write("HTTP/1.1 500 Internal Server Error\r\n\r\n".getBytes());
            } catch (IOException ignored) {}
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
}
