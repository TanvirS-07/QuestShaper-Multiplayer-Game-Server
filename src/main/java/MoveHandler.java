import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class MoveHandler implements HttpHandler{
    private int playerX = 5;
    private int playerY = 5;

    private static final int map_width = 20;
    private static final int map_height = 20;
    
    public void handle(HttpExchange exchange) throws IOException{

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")){
            sendResponse(exchange, 405, "");
            return ;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI());

        int dy = parseIntOrDefault(params.get("dy"), 0);
        int dy = parseIntOrDefault(params.get("dx"), 0);
    }
}
