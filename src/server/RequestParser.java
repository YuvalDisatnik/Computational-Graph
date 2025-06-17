package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        System.out.println("[RequestParser] Starting request parsing");

        /* ---------- 1. Request line ---------- */
        String start = reader.readLine();
        if (start == null || start.isEmpty()) {
            System.out.println("[RequestParser] Error: Empty request");
            throw new IOException("Empty request");
        }

        String[] first = start.split("\\s+");
        if (first.length < 2) {
            System.out.println("[RequestParser] Error: Malformed request - " + start);
            throw new IOException("Malformed request line: " + start);
        }

        String httpCommand  = first[0].trim();
        String uriWithQuery = first[1].trim();
        String pathOnly     = uriWithQuery.split("\\?")[0];
        
        System.out.println("[RequestParser] " + httpCommand + " " + uriWithQuery);

        // ---------- 2. Query-string ----------
        Map<String,String> params = new HashMap<>();
        int qMark = uriWithQuery.indexOf('?');
        if (qMark >= 0 && qMark < uriWithQuery.length() - 1) {
            String queryPart = uriWithQuery.substring(qMark + 1);
            for (String pair : queryPart.split("&")) {
                String[] kv = pair.split("=", 2);
                String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String v = kv.length == 2
                        ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8)
                        : "";
                params.put(k, v);
            }
        }

        String[] uriSegments = Arrays.stream(pathOnly.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);

        /* ---------- 3. Headers ---------- */
        int contentLength = 0;
        String line;
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            int idx = line.indexOf(':');
            if (idx == -1) continue;
            
            String name  = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if ("Content-Length".equalsIgnoreCase(name)) {
                contentLength = Integer.parseInt(value);
            }
        }

        /* ---------- 4. Extra key=value lines and Content payload ---------- */
        byte[] content;
        if (!reader.ready()) {
            content = new byte[0];
        } else {
            StringBuilder bodyBuilder = new StringBuilder();
            boolean inContent = false;
            
            while (reader.ready() && (line = reader.readLine()) != null) {
                if (!inContent) {
                    int eq = line.indexOf('=');
                    if (eq > 0) {
                        String key = line.substring(0, eq).trim();
                        String value = line.substring(eq + 1).trim();
                        params.put(key, value);
                        continue;
                    }
                    inContent = true;
                }
                bodyBuilder.append(line).append("\n");
            }
            content = bodyBuilder.toString().getBytes(StandardCharsets.UTF_8);
        }

        System.out.println("[RequestParser] Request parsed: " + httpCommand + " " + uriWithQuery + 
                         " (params: " + params.size() + ", content: " + content.length + " bytes)");
        
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