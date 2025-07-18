package servlets;

import server.Servlet;
import server.RequestParser.RequestInfo;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class DocLoader implements Servlet {
    private final String baseDirectory;
    private final Map<String, String> mimeTypes;

    public DocLoader() {
        this.baseDirectory = "docs";
        this.mimeTypes = initializeMimeTypes();
    }

    private Map<String, String> initializeMimeTypes() {
        Map<String, String> types = new HashMap<>();
        types.put(".html", "text/html; charset=UTF-8");
        types.put(".htm", "text/html; charset=UTF-8");
        types.put(".css", "text/css; charset=UTF-8");
        types.put(".js", "application/javascript; charset=UTF-8");
        types.put(".json", "application/json; charset=UTF-8");
        types.put(".png", "image/png");
        types.put(".jpg", "image/jpeg");
        types.put(".jpeg", "image/jpeg");
        types.put(".gif", "image/gif");
        types.put(".ico", "image/x-icon");
        types.put(".svg", "image/svg+xml");
        types.put(".txt", "text/plain; charset=UTF-8");
        return types;
    }

    @Override
    public void handle(RequestInfo ri, OutputStream toClient) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(ri.getHttpCommand())) {
                sendErrorResponse(toClient, 405, "Method Not Allowed", "Only GET method is supported");
                return;
            }

            String requestPath = ri.getUri();
            if (requestPath.startsWith("/docs/")) {
                requestPath = requestPath.substring(6);
            }
            if (requestPath.isEmpty() || requestPath.equals("/")) {
                requestPath = "index.html";
            }
            if (requestPath.contains("..") || requestPath.contains("//")) {
                sendErrorResponse(toClient, 400, "Bad Request", "Invalid file path");
                return;
            }
            Path filePath = Paths.get(baseDirectory, requestPath);
            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                sendErrorResponse(toClient, 404, "Not Found", "File not found: " + requestPath);
                return;
            }
            if (!Files.isRegularFile(filePath)) {
                sendErrorResponse(toClient, 403, "Forbidden", "Cannot serve directories");
                return;
            }
            byte[] fileContent = Files.readAllBytes(filePath);
            String mimeType = getMimeType(requestPath);
            sendFileResponse(toClient, fileContent, mimeType);
        } catch (IOException e) {
            sendErrorResponse(toClient, 500, "Internal Server Error", "Error reading file: " + e.getMessage());
        } catch (Exception e) {
            sendErrorResponse(toClient, 500, "Internal Server Error", "Unexpected error: " + e.getMessage());
        }
    }

    private String getMimeType(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return "application/octet-stream";
        }
        String extension = fileName.substring(lastDot).toLowerCase();
        return mimeTypes.getOrDefault(extension, "application/octet-stream");
    }

    private void sendFileResponse(OutputStream toClient, byte[] content, String mimeType) throws IOException {
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 200 OK\r\n");
        response.append("Content-Type: ").append(mimeType).append("\r\n");
        response.append("Content-Length: ").append(content.length).append("\r\n");
        response.append("Cache-Control: no-cache\r\n");
        response.append("\r\n");
        toClient.write(response.toString().getBytes());
        toClient.write(content);
        toClient.flush();
    }

    private void sendErrorResponse(OutputStream toClient, int statusCode, String statusText, String message) throws IOException {
        String htmlError = String.format(
            "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <title>%d %s</title>\n" +
            "    <style>\n" +
            "        body { font-family: Arial, sans-serif; margin: 40px; }\n" +
            "        h1 { color: #d32f2f; }\n" +
            "        p { color: #666; }\n" +
            "        .error-code { font-size: 3em; font-weight: bold; color: #d32f2f; }\n" +
            "        .back-link { margin-top: 20px; }\n" +
            "        .back-link a { color: #1976d2; text-decoration: none; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"error-code\">%d</div>\n" +
            "    <h1>%s</h1>\n" +
            "    <p>%s</p>\n" +
            "    <div class=\"back-link\">\n" +
            "        <a href=\"/app/\">&larr; Back to Home</a>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>",
            statusCode, statusText, statusCode, statusText, escapeHtml(message)
        );
        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        response.append("Content-Type: text/html; charset=UTF-8\r\n");
        response.append("Content-Length: ").append(htmlError.getBytes().length).append("\r\n");
        response.append("\r\n");
        response.append(htmlError);
        toClient.write(response.toString().getBytes());
        toClient.flush();
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
        // No resources to close for static file serving
    }
} 