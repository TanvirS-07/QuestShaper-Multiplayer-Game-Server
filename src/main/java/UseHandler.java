import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class UseHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendResponse(exchange, 204, "");
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI());

        String session = params.get("session");
        if (!SessionManager.isValidSession(session)) {
            sendResponse(exchange, 401, "");
            return;
        }

        int dy = parseIntOrDefault(params.get("dy"), 0);
        int dx = parseIntOrDefault(params.get("dx"), 0);

        if(!isValidUse(dy, dx)) {
            sendResponse(exchange, 204, "");
            return;
        }

        int targetY = GameState.playerY + dy;
        int targetX = warpX(GameState.playerX + dx);

        if (targetY < 0 || targetY >= GameState.mapHeight) {
            sendResponse(exchange, 204, "");
            return;
        }

        String tile = GameState.map[targetY][targetX];

        if(tile.contains("D")) {
            GameState.map[targetY][targetX] = tile.replaceFirst("D", "d");
            sendResponse(exchange, 200, "");
            return;
        }

        if(tile.contains("d")) {
            GameState.map[targetY][targetX] = tile.replaceFirst("d", "D");
            sendResponse(exchange, 200, "");
            return;
        }

        sendResponse(exchange, 204, "");
    }

    private boolean isValidUse(int dy, int dx) {
        return Math.abs(dy) +Math.abs(dx) <= 1;
    }

    private int warpX(int x) {
        if (x < 0) {
            return x + GameState.mapWidth;
        }
        if (x >= GameState.mapWidth) {
            return x - GameState.mapWidth;
        }
        return x;
    }

    private int parseIntOrDefault(String s, int defaultValue) {
        try {
            if (s == null) {
                return defaultValue;
            }
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String query = uri.getQuery();

        if (query == null) {
            return result;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        if(statusCode == 204) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        
        byte[] bytes = response.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}