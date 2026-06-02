import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class InfoHandler implements HttpHandler {
    private static final int VIEW_RADIUS = 5;

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
        PlayerState player = GameState.getPlayer(session);
        if (player == null) {
            sendResponse(exchange, 401, "");
            return;
        }

        int requestedY = parseIntOrDefault(params.get("y"), -1);
        int requestedX = parseIntOrDefault(params.get("x"), -1);

        // INFO only reveals the view around the requesting player's current location.
        int playerY = player.y;
        int playerX = player.x;

        if (requestedY != playerY || requestedX != playerX) {
            sendResponse(exchange, 204, "");
            return;
        }

        int top = playerY - VIEW_RADIUS;
        int left = playerX - VIEW_RADIUS;
        int bottom = playerY + VIEW_RADIUS;
        int right = playerX + VIEW_RADIUS;

        String response = buildInfoJson(playerY, playerX, top, left, bottom, right);
        sendResponse(exchange, 200, response);
    }

    private String buildInfoJson(int y, int x, int top, int left, int bottom, int right) {
        // Build the 11x11 JSON view expected by the QuestShaper client.
        StringBuilder json = new StringBuilder();

        json.append("{");
        json.append("\"y\":").append(y).append(",");
        json.append("\"x\":").append(x).append(",");
        json.append("\"top\":").append(top).append(",");
        json.append("\"left\":").append(left).append(",");
        json.append("\"bottom\":").append(bottom).append(",");
        json.append("\"right\":").append(right).append(",");
        json.append("\"info\":[");

        for (int row = top; row <= bottom; row++) {
            if (row > top) {
                json.append(",");
            }

            json.append("[");

            for (int col = left; col <= right; col++) {
                if (col > left) {
                    json.append(",");
                }

                json.append("\"").append(getTileString(row, col)).append("\"");
            }

            json.append("]");
        }

        json.append("]");
        json.append("}");

        return json.toString();
    }

    private String getTileString(int y, int x) {
        if (y < 0 || y >= GameState.mapHeight) {
            return " ";
        }

        // Match movement behavior by wrapping the visible window horizontally.
        int fixedX = x;

        if (fixedX < 0) {
            fixedX += GameState.mapWidth;
        }

        if (fixedX >= GameState.mapWidth) {
            fixedX -= GameState.mapWidth;
        }

        return GameState.getTileWithPlayers(y, fixedX);
    }

    private int parseIntOrDefault(String value, int def) {
        try {
            if (value == null) {
                return def;
            }

            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private Map<String, String> parseQuery(URI uri) {
        // Read y, x, and session from the URL query string.
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
