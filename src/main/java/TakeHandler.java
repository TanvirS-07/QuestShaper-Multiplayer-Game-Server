import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TakeHandler implements HttpHandler{
    private static String[][] map;
    private static int map_width;
    private static int map_height;

    private static final Map<String, Character> inventory = new HashMap<>();

    private int playerX = 5;
    private int playerY = 5;

    static{
        try{
            map = loadMap();
            map_height = map.length;
            map_width = map[0].length;
        } catch(IOException e){
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public synchronized void handle(HttpExchange exchange) throws IOException{
        addCorsHeaders(exchange);

        if(exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")){
            sendResponse(exchange, 204, "");
            return;
        }

        if(!exchange.getRequestMethod().equalsIgnoreCase("GET")){
            sendResponse(exchange, 405, "");
            return;
        }

        Map<String, String> params = parseQuery(exchange.getRequestURI());
        String session = params.get("session");

        if(!SessionManager.isValidSession(session)){
            sendResponse(exchange, 401, "");
            return;
        }

        if(playerY < 0 || playerY >= map_height || playerX < 0 || playerX >= map_width){
            sendResponse(exchange, 204, "");
            return;
        }

        String tile = map[playerY][playerX];
        Character item = firstMovableItem(tile);

        if(item == null){
            sendResponse(exchange, 204, "");
            return;
        }

        String username = SessionManager.getUsername(session);
        Character oldItem = inventory.get(username);

        map[playerY][playerX] = removeFirstItem(tile, item);

        if(oldItem != null && itemClass(oldItem) == itemClass(item)){
            map[playerY][playerX] = map[playerY][playerX] + oldItem;
        }

        inventory.put(username, item);
        sendResponse(exchange, 200, "");
    }

    private static String[][] loadMap() throws IOException{
        Path[] possiblePaths = {
                Paths.get("maps", "world.txt"),
                Paths.get("world.txt"),
                Paths.get("..", "..", "..", "maps", "world.txt")
        };

        Path path = null;
        for(Path possiblePath : possiblePaths){
            if(Files.exists(possiblePath)){
                path = possiblePath;
                break;
            }
        }

        if(path == null){
            throw new IOException("Could not find world.txt. Tried maps/world.txt, world.txt, and ../../../maps/world.txt");
        }

        List<String> lines = Files.readAllLines(path);
        int height = lines.size();
        int width = lines.get(0).length();
        String[][] loadedMap = new String[height][width];

        for(int y = 0; y < height; y++){
            String line = lines.get(y);
            for(int x = 0; x < width; x++){
                loadedMap[y][x] = String.valueOf(line.charAt(x));
            }
        }

        return loadedMap;
    }

    private Character firstMovableItem(String tile){
        for(int i = 0; i < tile.length(); i++){
            char c = tile.charAt(i);
            if(isMovableItem(c)){
                return c;
            }
        }
        return null;
    }

    private boolean isMovableItem(char c){
        return c == 'a' || c == 'c' || c == 'h' || c == 'k';
    }

    private char itemClass(char item){
        if(item == 'a'){
            return 't'; // tool
        }
        if(item == 'c' || item == 'h'){
            return 'd'; // drink
        }
        return 'r'; // artifact
    }

    private String removeFirstItem(String tile, char item){
        int index = tile.indexOf(item);
        if(index == -1){
            return tile;
        }
        String updated = tile.substring(0, index) + tile.substring(index + 1);
        if(updated.isEmpty()){
            return "g";
        }
        return updated;
    }

    private Map<String, String> parseQuery(URI uri){
        Map<String, String> result = new HashMap<>();
        String query = uri.getQuery();

        if(query == null){
            return result;
        }

        for(String pair : query.split("&")){
            String[] parts = pair.split("=", 2);
            if(parts.length == 2){
                result.put(parts[0], parts[1]);
            }
        }
        return result;
    }

    private void addCorsHeaders(HttpExchange exchange){
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String body) throws IOException{
        exchange.sendResponseHeaders(statusCode, body.length());
        OutputStream os = exchange.getResponseBody();
        os.write(body.getBytes());
        os.close();
    }
}
