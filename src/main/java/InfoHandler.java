import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class InfoHandler implements HttpHandler {
    private final int Radius = 5;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        int y = Main.playerY;
        int x = Main.playerX;

        int top = y - Radius;
        int bottom = y + Radius;
        int left = x - Radius;
        int right = x + Radius;

        StringBuilder response = new StringBuilder();
        response.append("{");
        response.append("\"y\":").append(y).append(",");
        response.append("\"x\":").append(x).append(",");
        response.append("\"top\":").append(top).append(",");
        response.append("\"left\":").append(left).append(",");
        response.append("\"bottom\":").append(bottom).append(",");
        response.append("\"right\":").append(right).append(",");
        response.append("\"info\":[");

        for (int row = top; row <= bottom; row++) {
            if (row > top)
                response.append(",");
            response.append("[");
            for (int col = left; col <= right; col++) {
                if (col > left)
                    response.append(",");
                int wrappedX = Main.wrapX(col);
                char tile = Main.getTile(row, wrappedX);
                response.append("\"").append(tile).append("\"");
            }
            response.append("]");
        }
        response.append("]}");

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        sendResponse(exchange, 200, response.toString());
    }

    private Map<String, String> parseQueryParams(URI uri) {
        Map<String, String> queryParams = new HashMap<>();
        String query = uri.getQuery();
        if (query == null)
            return queryParams;
        for (String pair : query.split("&")) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                queryParams.put(keyValue[0], keyValue[1]);
            }
        }
        return queryParams;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        if (body.isEmpty()) {
            exchange.sendResponseHeaders(statusCode, -1);
        } else {
            byte[] bytes = body.getBytes();
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.close();
            }
        }
    }
}