import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            sendResponse(exchange, 204, "");
            return;
        }

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "");
            return;
        }

        String body = readBody(exchange.getRequestBody());
        String name = getJsonValue(body, "name");
        String encpswrd = getJsonValue(body, "encpswrd");

        if (name == null || encpswrd == null) {
            sendResponse(exchange, 400, "");
            return;
        }

        if (!SessionManager.validateLogin(name, encpswrd)) {
            sendResponse(exchange, 401, "");
            return;
        }

        String session = SessionManager.createSession(name);
        GameState.addPlayer(session, name);

        String response = "{\"session\":\"" + session + "\"}";
        sendResponse(exchange, 200, response);
    }

    private String readBody(InputStream input) throws IOException {
        return new String(input.readAllBytes());
    }

    private String getJsonValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int keyIdx = json.indexOf(pattern);

        if (keyIdx == -1) {
            return null;
        }

        int colonIdx = json.indexOf(":", keyIdx);
        int fstQuote = json.indexOf("\"", colonIdx + 1);
        int scndQuote = json.indexOf("\"", fstQuote + 1);

        if (colonIdx == -1 || fstQuote == -1 || scndQuote == -1) {
            return null;
        }
        return json.substring(fstQuote + 1, scndQuote);
    }

    private void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, body.length());
        OutputStream os = exchange.getResponseBody();
        os.write(body.getBytes());
        os.close();
    }
}