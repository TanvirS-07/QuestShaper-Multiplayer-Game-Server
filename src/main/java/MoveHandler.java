import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoveHandler implements HttpHandler {
    private static int playerX = 5;
    private static int playerY = 5;

    private static char[][] map;
    private static int map_width;
    private static int map_height;

    static {
        try {
            map = loadMap("maps/world.txt");
            map_height = map.length;
            map_width = map[0].length;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

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

        int newX = warpX(playerX + dx);
        int newY = warpY(playerY + dy);

        if (isBlocking(newY, newX)) {
            sendResponse(exchange, 204, "");
            return;
        }

        playerX = newX;
        playerY = newY;

        String response = String.format("{\"y\": %d, \"x\": %d}", playerY, playerX);
        sendResponse(exchange, 200, response);
    }

    private static char[][] loadMap(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));

        int height = lines.size();
        int width = lines.get(0).length();

        char[][] map = new char[height][width];

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);
            for (int x = 0; x < width; x++) {
                map[y][x] = line.charAt(x);
            }
        }

        return map;
    }

    private boolean isValidMove(int dy, int dx) {
        return Math.abs(dy) + Math.abs(dx) <= 1;
    }

    private int warpX(int x) {
        if (x < 0) {
            return x + map_width;
        }
        if (x >= map_width) {
            return x - map_width;
        }
        return x;
    }

    private int warpY(int y) {
        if (y < 0) {
            return 0;
        }
        if (y >= map_height) {
            return map_height - 1;
        }
        return y;
    }

    private boolean isBlocking(int y, int x) {
        char tile = map[y][x];
        if (tile == 'B' || tile == 'D' || tile == 'S' || tile == 'W') {
            return true;
        } else {
            return false;
        }
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
            String[] parts = pair.split("=");
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

    public static int getPlayerX() {
        return playerX;
    }

    public static int getPlayerY() {
        return playerY;
    }

    public static int getMapWidth() {
        return map_width;
    }

    public static int getMapHeight() {
        return map_height;
    }

    public static char getTile(int y, int x) {
        return map[y][x];
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        if (statusCode == 204) {
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.close();
            return;
        }

        byte[] bytes = body.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
