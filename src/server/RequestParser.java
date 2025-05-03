package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RequestParser {

    public static RequestInfo parseRequest(BufferedReader reader) throws IOException {
        /* ---------- 1. Request line ---------- */
        String start = reader.readLine();
        if (start == null || start.isEmpty())
            throw new IOException("Empty request");

        String[] first = start.split("\\s+");
        if (first.length < 2)
            throw new IOException("Malformed request line: " + start);

        String httpCommand  = first[0].trim();      // GET / POST …
        String uriWithQuery = first[1].trim();      // /api/res?id=…
        String pathOnly     = uriWithQuery.split("\\?")[0];

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
            if ("Content-Length".equalsIgnoreCase(name))
                contentLength = Integer.parseInt(value);
        }

        /* ---------- 4. Extra key=value lines (e.g. filename="…") ---------- */
        while (reader.ready() && (line = reader.readLine()) != null && !line.isEmpty()) {
            int eq = line.indexOf('=');
            if (eq > 0) {
                params.put(line.substring(0, eq).trim(),
                        line.substring(eq + 1).trim());
            }
        }



        /* ---------- 5. Content payload ---------- */
        byte[] content;
        if (!reader.ready()) {
            content = new byte[0];                 // no body
        } else {
            String bodyLine = reader.readLine();   // קורא שורה אחת של תוכן
            if (bodyLine == null) bodyLine = "";
            content = (bodyLine + "\n")
                    .getBytes(StandardCharsets.UTF_8);
        }


        /* ---------- 6. Return ---------- */
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