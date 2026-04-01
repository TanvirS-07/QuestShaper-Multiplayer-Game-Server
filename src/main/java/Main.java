import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    static char[][] map;
    static int mapWidth;
    static int mapHeight;
    static int playerX = 5;
    static int playerY = 5;

    private static final String blockingTiles = "WSB";

    public static void main(String[] args) throws IOException {
        loadMap("maps/world.txt");

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/info", new InfoHandler());
        server.createContext("/move", new MoveHandler());
        server.start();
        System.out.println("Server started on port 8000");
    }

    private static void loadMap(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        mapHeight = lines.size();
        mapWidth = lines.get(0).length();
        map = new char[mapHeight][mapWidth];
        for (int y = 0; y < mapHeight; y++) {
            String line = lines.get(y);
            for (int x = 0; x < mapWidth; x++) {
                map[y][x] = line.charAt(x);
            }
        }
    }

    static boolean isBlocking(int y, int x) {
        if (y < 0 || y >= mapHeight || x < 0 || x >= mapWidth) {
            return true;
        }
        return blockingTiles.indexOf(map[y][x]) != -1;
    }

    static char getTile(int y, int x) {
        if (y < 0 || y >= mapHeight || x < 0 || x >= mapWidth) {
            return ' ';
        }
        return map[y][x];
    }

    static int wrapX(int x) {
        if (x < 0)
            return x + mapWidth;
        if (x >= mapWidth)
            return x - mapWidth;
        return x;
    }

    static int wrapY(int y) {
        if (y < 0)
            return 0;
        if (y >= mapHeight)
            return mapHeight - 1;
        return y;
    }
}