import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PlaceHandler implements HttpHandler {

    @Override
    public synchronized void handle(HttpExchange exchange) throws IOException {
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
        PlayerState player = GameState.getPlayer(session);
        if (player == null) {
            sendResponse(exchange, 401, "");
            return;
        }

        Character item = player.inventory;

        if (item == null) {
            sendResponse(exchange, 204, "");
            return;
        }

        if (player.y < 0 || player.y >= GameState.mapHeight ||
                player.x < 0 || player.x >= GameState.mapWidth) {
            sendResponse(exchange, 204, "");
            return;
        }

        String tile = GameState.map[player.y][player.x];

        // Keep placement simple: one movable item per tile.
        if (containsMovableItem(tile)) {
            sendResponse(exchange, 204, "");
            return;
        }

        GameState.map[player.y][player.x] = tile + item;
        player.inventory = null;

        sendResponse(exchange, 200, "");
    }

    private boolean containsMovableItem(String tile) {
        // Prevent stacking two movable inventory items on the same map tile.
        for (int i = 0; i < tile.length(); i++) {
            char c = tile.charAt(i);
            if (c == 'a' || c == 'c' || c == 'h' || c == 'k') {
                return true;
            }
        }
        return false;
    }

    private Map<String, String> parseQuery(URI uri) {
        // PLACE only needs the session token from the query string.
        Map<String, String> result = new HashMap<>();
        String query = uri.getQuery();

        if (query == null) {
            return result;
        }

        for (String pair : query.split("&")) {
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
