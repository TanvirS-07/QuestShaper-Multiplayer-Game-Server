import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MoveHandler implements HttpHandler {

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

        if (!isValidMove(dy, dx)) {
            sendResponse(exchange, 204, "");
            return;
        }

        int newX = warpX(GameState.playerX + dx);
        int newY = warpY(GameState.playerY + dy);

        if (isBlocking(newY, newX)) {
            sendResponse(exchange, 204, "");
            return;
        }

        GameState.playerX = newX;
        GameState.playerY = newY;

        String response = String.format("{\"y\": %d, \"x\": %d}", GameState.playerY, GameState.playerX);
        sendResponse(exchange, 200, response);
    }

    private boolean isValidMove(int dy, int dx) {
        return Math.abs(dy) + Math.abs(dx) <= 1;
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

    private int warpY(int y) {
        if (y < 0) {
            return 0;
        }
        if (y >= GameState.mapHeight) {
            return GameState.mapHeight - 1;
        }
        return y;
    }

    private boolean isBlocking(int y, int x) {
        String tile = GameState.map[y][x];
        return tile.contains("B") || tile.contains("D") || tile.contains("S") || tile.contains("W");
    }

    private int parseIntOrDefault(String value, int def) {
        try {
            if (value == null) {
                return def;
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            return def;
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

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        if (statusCode == 204) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        byte[] bytes = body.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
