public class PlayerState {
    // Stores the server-side state for one logged-in player session.
    public final String session;
    public final String username;
    public int x;
    public int y;
    public Character inventory;
    public final char sprite;

    public PlayerState(String session, String username, int y, int x, char sprite) {
        this.session = session;
        this.username = username;
        this.y = y;
        this.x = x;
        this.sprite = sprite;
        this.inventory = null;
    }
}
