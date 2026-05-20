import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GameState {
    public static int playerX = 5;
    public static int playerY = 5;

    public static String[][] map;
    public static int mapWidth;
    public static int mapHeight;

    static {
        try {
            map = loadMap("maps/world.txt");
            mapHeight = map.length;
            mapWidth = map[0].length;
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
}
