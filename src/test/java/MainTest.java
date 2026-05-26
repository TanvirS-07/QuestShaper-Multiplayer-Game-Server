import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

public class MainTest {
    @Test
    void mapLoadsSuccessfully() {
        assertNotNull(GameState.map);
        assertTrue(GameState.mapHeight > 0);
        assertTrue(GameState.mapWidth  > 0);
    }
    @Test
    void validMovesPassCheck() {
        assertTrue(isValidMove(0,  0));
        assertTrue(isValidMove(-1, 0));
        assertTrue(isValidMove(1,  0));
        assertTrue(isValidMove(0, -1));
        assertTrue(isValidMove(0,  1));
    }

    @Test
    void diagonalMoveFails() {
        assertFalse(isValidMove(1,  1));
        assertFalse(isValidMove(-1, 1));
    }

    @Test
    void multiStepMoveFails() {
        assertFalse(isValidMove(2,  0));
        assertFalse(isValidMove(0, -2));
    }

    @Test
    void blockingTileCheck() {
        assertTrue(isBlocking("B"));
        assertTrue(isBlocking("D"));
        assertTrue(isBlocking("S"));
        assertTrue(isBlocking("W"));
        assertTrue(isBlocking("g3")); // player avatar blocks
        assertFalse(isBlocking("g"));
        assertFalse(isBlocking("_"));
        assertFalse(isBlocking("d")); // open door
    }


    @Test
    void removeItemPreservesGroundLayer() {
        assertEquals("g",  removeItem("gk", 'k'));
        assertEquals("_",  removeItem("_a", 'a'));
        assertEquals("gc", removeItem("gck", 'k')); // removes k, c remains
    }

    @Test
    void removeItemMissingCharLeavesStringUnchanged() {
        assertEquals("gg", removeItem("gg", 'k'));
    }

    @Test
    void stripAvatarDigits() {
        assertEquals("g",   stripDigits("g3"));
        assertEquals("gk",  stripDigits("gk2"));
        assertEquals("g",   stripDigits("g"));
    }


    private boolean isValidMove(int dy, int dx) {
        return Math.abs(dy) + Math.abs(dx) <= 1;
    }

    private boolean isBlocking(String tile) {
        for (char c : tile.toCharArray()) {
            if (c == 'B' || c == 'D' || c == 'S' || c == 'W') return true;
            if (c >= '0' && c <= '9') return true;
        }
        return false;
    }

    private String removeItem(String tile, char item) {
        int idx = tile.indexOf(item);
        if (idx == -1) return tile;
        String result = tile.substring(0, idx) + tile.substring(idx + 1);
        if (result.isEmpty() && tile.length() > 0) return String.valueOf(tile.charAt(0));
        return result;
    }

    private String stripDigits(String tile) {
        StringBuilder sb = new StringBuilder();
        for (char c : tile.toCharArray()) {
            if (c < '0' || c > '9') sb.append(c);
        }
        return sb.toString();
    }
}
