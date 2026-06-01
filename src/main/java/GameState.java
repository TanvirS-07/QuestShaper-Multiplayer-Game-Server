import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GameState {
    public static String[][] map;
    public static int mapWidth;
    public static int mapHeight;
    public static final Map<String, PlayerState> players = new HashMap<>();
    private static int nextSpriteIndex = 1;

    static {
        try {
            map = loadMap("maps/world.txt");
            mapHeight = map.length;
            mapWidth = map[0].length;
            addItem(2, 1, 'a');
            addItem(0, 14, 'k');
            addItem(16, 8, 'c');
            addItem(13, 15, 'h');
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static String[][] loadMap(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename));

        int height = lines.size();
        int width = lines.get(0).length();

        String[][] loadedMap = new String[height][width];

        for (int y = 0; y < height; y++) {
            String line = lines.get(y);

            for (int x = 0; x < width; x++) {
                loadedMap[y][x] = String.valueOf(line.charAt(x));
            }
        }

        return loadedMap;
    }

    private static void addItem(int y, int x, char item) {
        map[y][x] = map[y][x] + item;
    }

    public static synchronized PlayerState addPlayer(String session, String username) {
        PlayerState existing = players.get(session);
        if (existing != null) {
            return existing;
        }

        char sprite = (char) ('0' + (nextSpriteIndex % 10));
        nextSpriteIndex++;

        PlayerState player = new PlayerState(session, username, 5, 5, sprite);
        players.put(session, player);
        return player;
    }

    public static synchronized PlayerState getPlayer(String session) {
        return players.get(session);
    }

    public static synchronized void removePlayer(String session) {
        players.remove(session);
    }

    public static synchronized boolean isOccupiedByOtherPlayer(int y, int x, String session) {
        for (PlayerState player : players.values()) {
            if (!player.session.equals(session) && player.y == y && player.x == x) {
                return true;
            }
        }

        return false;
    }

    public static synchronized String getTileWithPlayers(int y, int x) {
        String tile = map[y][x];

        for (PlayerState player : players.values()) {
            if (player.y == y && player.x == x) {
                tile = tile + player.sprite;
            }
        }

        return tile;
    }

}
