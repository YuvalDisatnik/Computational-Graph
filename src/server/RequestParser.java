package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        System.out.println("[RequestParser] === Starting request parsing ===");

        /* ---------- 1. Request line ---------- */
        System.out.println("[RequestParser] Reading request line");
        String start = reader.readLine();
        System.out.println("[RequestParser] Raw request line: " + start);
        
        if (start == null || start.isEmpty()) {
            System.out.println("[RequestParser] Error: Empty request received");
            throw new IOException("Empty request");
        }

        String[] first = start.split("\\s+");
        if (first.length < 2) {
            System.out.println("[RequestParser] Error: Malformed request line - " + start);
            throw new IOException("Malformed request line: " + start);
        }

        String httpCommand  = first[0].trim();      // GET / POST …
        String uriWithQuery = first[1].trim();      // /api/res?id=…
        String pathOnly     = uriWithQuery.split("\\?")[0];
        
        System.out.println("[RequestParser] Parsed request line:");
        System.out.println("[RequestParser] - HTTP Command: " + httpCommand);
        System.out.println("[RequestParser] - Full URI: " + uriWithQuery);
        System.out.println("[RequestParser] - Path only: " + pathOnly);

        // ---------- 2. Query-string ----------
        System.out.println("[RequestParser] Parsing query parameters");
        Map<String,String> params = new HashMap<>();
        int qMark = uriWithQuery.indexOf('?');
        if (qMark >= 0 && qMark < uriWithQuery.length() - 1) {
            String queryPart = uriWithQuery.substring(qMark + 1);
            System.out.println("[RequestParser] Found query string: " + queryPart);
            
            for (String pair : queryPart.split("&")) {
                String[] kv = pair.split("=", 2);
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String v = kv.length == 2
                        ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                        : "";
                params.put(k, v);
                System.out.println("[RequestParser] Added query parameter: " + k + " = " + v);
            }
        } else {
            System.out.println("[RequestParser] No query parameters found");
        }

        String[] uriSegments = Arrays.stream(pathOnly.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        System.out.println("[RequestParser] URI segments: " + Arrays.toString(uriSegments));

        /* ---------- 3. Headers ---------- */
        System.out.println("[RequestParser] Parsing headers");
        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            System.out.println("[RequestParser] Parsing header: " + line);
            int idx = line.indexOf(':');
            if (idx == -1) {
                System.out.println("[RequestParser] Warning: Invalid header format - " + line);
                continue;
            }
            String name  = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if ("Content-Length".equalsIgnoreCase(name)) {
                contentLength = Integer.parseInt(value);
                System.out.println("[RequestParser] Found Content-Length: " + contentLength);
            }
        }

        /* ---------- 4. Extra key=value lines and Content payload ---------- */
        System.out.println("[RequestParser] Processing request body");
        byte[] content;
        if (!reader.ready()) {
            System.out.println("[RequestParser] No request body (reader not ready)");
            content = new byte[0];                 // no body
        } else {
            StringBuilder bodyBuilder = new StringBuilder();
            boolean inContent = false;
            
            while (reader.ready() && (line = reader.readLine()) != null) {
                if (!inContent) {
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        // This is a key=value line
                        System.out.println("[RequestParser] Found key-value pair: " + line);
                        String key = line.substring(0, eq).trim();
                        String value = line.substring(eq + 1).trim();
                        params.put(key, value);
                        System.out.println("[RequestParser] Added parameter: " + key + " = " + value);
                        continue;
                    }
                    // First non-key=value line, switch to content mode
                    System.out.println("[RequestParser] Switching to content mode");
                    inContent = true;
                }
                // We're in content mode, add to body
                System.out.println("[RequestParser] Adding to body: " + line);
                bodyBuilder.append(line).append("\n");
            }
            content = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);
            System.out.println("[RequestParser] Final body length: " + content.length + " bytes");
            if (content.length > 0) {
                System.out.println("[RequestParser] Body preview: " + new String(content, 0, Math.min(100, content.length)) + "...");
            }
        }

        /* ---------- 5. Return ---------- */
        System.out.println("[RequestParser] === Request parsing completed ===");
        System.out.println("[RequestParser] Summary:");
        System.out.println("[RequestParser] - Method: " + httpCommand);
        System.out.println("[RequestParser] - URI: " + uriWithQuery);
        System.out.println("[RequestParser] - Parameters count: " + params.size());
        System.out.println("[RequestParser] - Content length: " + content.length);
        
        return new RequestInfo(httpCommand, uriWithQuery, uriSegments, params, content);
    }

    // RequestInfo given internal class
    public static class RequestInfo {
        private final String httpCommand;
        private final String uri;
        private final String[] uriSegments;
        private final Map<String, String> parameters;
        private final byte[] content;

        public RequestInfo(String httpCommand, String uri, String[] uriSegments, Map<String, String> parameters, byte[] content) {
            this.httpCommand = httpCommand;
            this.uri = uri;
            this.uriSegments = uriSegments;
            this.parameters = parameters;
            this.content = content;
        }

        public String getHttpCommand() {
            return httpCommand;
        }

        public String getUri() {
            return uri;
        }

        public String[] getUriSegments() {
            return uriSegments;
        }

        public Map<String, String> getParameters() {
            return parameters;
        }

        public byte[] getContent() {
            return content;
        }
    }
}