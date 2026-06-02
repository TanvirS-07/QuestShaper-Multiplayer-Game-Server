import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class LogoutHandler implements HttpHandler {
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

        Map<String, String> map = parseQuery(exchange.getRequestURI());
        String session = map.get("session");

        if (!SessionManager.logout(session)) {
            sendResponse(exchange, 401, "");
            return;
        }

        // Remove both the session token and the player's visible map state.
        GameState.removePlayer(session);
        sendResponse(exchange, 200, "");
    }

    private Map<String, String> parseQuery(URI uri) {
        // Convert query parameters such as session=abc into a simple map.
        Map<String, String> map = new HashMap<>();
        String query = uri.getQuery();

        if (query == null) {
            return map;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
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
        exchange.sendResponseHeaders(statusCode, body.length());
        OutputStream os = exchange.getResponseBody();
        os.write(body.getBytes());
        os.close();
    }
}
