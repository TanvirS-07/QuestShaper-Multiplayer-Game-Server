import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.MessageDigest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainTest {

    private static HttpServer server;
    private static HttpClient client;
    private static final String BASE = "http://localhost:8001";

    // Shared session tokens across tests
    private static String tanvirSession;
    private static String shaifSession;

    // -------------------------------------------------------------------------
    // Setup / Teardown
    // -------------------------------------------------------------------------

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/login",  new LoginHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/move",   new MoveHandler());
        server.createContext("/info",   new InfoHandler());
        server.createContext("/take",   new TakeHandler());
        server.createContext("/place",  new PlaceHandler());
        server.createContext("/use",    new UseHandler());
        server.start();

        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String extractSession(String body) {
        // Body looks like: {"session":"abc123"}
        int start = body.indexOf("\"session\":\"") + 11;
        int end   = body.indexOf("\"", start);
        return body.substring(start, end);
    }

 
    // LOGIN API TESTS
   

    @Test @Order(1)
    @DisplayName("Login - valid user Tanvir returns 200 with session token")
    void loginValidTanvir() throws Exception {
        String hash = sha256("Tanvir;tanvir123");
        String json = "{\"name\":\"Tanvir\",\"encpswrd\":\"" + hash + "\"}";
        var res = post("/login", json);

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("session"), "Response should contain a session token");

        tanvirSession = extractSession(res.body());
        assertFalse(tanvirSession.isBlank());
    }

    @Test @Order(2)
    @DisplayName("Login - valid user Shaif returns 200 with session token")
    void loginValidShaif() throws Exception {
        String hash = sha256("Shaif;Shaif123");
        String json = "{\"name\":\"Shaif\",\"encpswrd\":\"" + hash + "\"}";
        var res = post("/login", json);

        assertEquals(200, res.statusCode(), "Shaif should be able to login. Check USERS env var includes Shaif:Shaif123");
        assertTrue(res.body().contains("session"));

        shaifSession = extractSession(res.body());
        assertFalse(shaifSession.isBlank());
    }

    @Test @Order(3)
    @DisplayName("Login - wrong password returns 401")
    void loginWrongPassword() throws Exception {
        String hash = sha256("Tanvir;wrongpassword");
        String json = "{\"name\":\"Tanvir\",\"encpswrd\":\"" + hash + "\"}";
        var res = post("/login", json);

        assertEquals(401, res.statusCode());
    }

    @Test @Order(4)
    @DisplayName("Login - unknown user returns 401")
    void loginUnknownUser() throws Exception {
        String hash = sha256("Ghost;ghost123");
        String json = "{\"name\":\"Ghost\",\"encpswrd\":\"" + hash + "\"}";
        var res = post("/login", json);

        assertEquals(401, res.statusCode());
    }

    @Test @Order(5)
    @DisplayName("Login - missing name field returns 400")
    void loginMissingName() throws Exception {
        String json = "{\"encpswrd\":\"somehash\"}";
        var res = post("/login", json);

        assertEquals(400, res.statusCode());
    }

    @Test @Order(6)
    @DisplayName("Login - missing password field returns 400")
    void loginMissingPassword() throws Exception {
        String json = "{\"name\":\"Tanvir\"}";
        var res = post("/login", json);

        assertEquals(400, res.statusCode());
    }

    @Test @Order(7)
    @DisplayName("Login - empty body returns 400")
    void loginEmptyBody() throws Exception {
        var res = post("/login", "{}");
        assertEquals(400, res.statusCode());
    }

    @Test @Order(8)
    @DisplayName("Login - wrong HTTP method (GET) returns 405")
    void loginWrongMethod() throws Exception {
        var res = get("/login");
        assertEquals(405, res.statusCode());
    }

 
    // MOVE API TESTS
  

    @Test @Order(10)
    @DisplayName("Move - valid session moves south (dy=1) returns 200 with coordinates")
    void moveValidSouth() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/move?dy=1&dx=0&session=" + tanvirSession);

        assertEquals(200, res.statusCode());
        assertTrue(res.body().contains("\"y\""));
        assertTrue(res.body().contains("\"x\""));
    }

    @Test @Order(11)
    @DisplayName("Move - valid session moves north (dy=-1) returns 200")
    void moveValidNorth() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/move?dy=-1&dx=0&session=" + tanvirSession);

        // Could be 200 (moved) or 204 (blocked) depending on map tile
        assertTrue(res.statusCode() == 200 || res.statusCode() == 204);
    }

    @Test @Order(12)
    @DisplayName("Move - diagonal move (dy=1,dx=1) returns 204")
    void moveDiagonalBlocked() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/move?dy=1&dx=1&session=" + tanvirSession);

        assertEquals(204, res.statusCode());
    }

    @Test @Order(13)
    @DisplayName("Move - large move (dy=2) returns 204")
    void moveTwoStepsBlocked() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/move?dy=2&dx=0&session=" + tanvirSession);

        assertEquals(204, res.statusCode());
    }

    @Test @Order(14)
    @DisplayName("Move - invalid session returns 401")
    void moveInvalidSession() throws Exception {
        var res = get("/move?dy=1&dx=0&session=invalidsession");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(15)
    @DisplayName("Move - missing session returns 401")
    void moveMissingSession() throws Exception {
        var res = get("/move?dy=1&dx=0");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(16)
    @DisplayName("Move - stay in place (dy=0,dx=0) returns 200")
    void moveStayInPlace() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/move?dy=0&dx=0&session=" + tanvirSession);

        assertEquals(200, res.statusCode());
    }

    // INFO TESTS
   

    @Test @Order(20)
    @DisplayName("Info - valid session at player position returns 200 with map data")
    void infoValidAtPlayerPosition() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");

        // First get current position by staying in place
        var moveRes = get("/move?dy=0&dx=0&session=" + tanvirSession);
        assertEquals(200, moveRes.statusCode());

        String body = moveRes.body();
        int y = Integer.parseInt(body.replaceAll(".*\"y\":\\s*(\\d+).*", "$1"));
        int x = Integer.parseInt(body.replaceAll(".*\"x\":\\s*(\\d+).*", "$1"));

        var infoRes = get("/info?y=" + y + "&x=" + x + "&session=" + tanvirSession);

        assertEquals(200, infoRes.statusCode());
        assertTrue(infoRes.body().contains("\"info\""), "Response should contain info grid");
        assertTrue(infoRes.body().contains("\"top\""));
        assertTrue(infoRes.body().contains("\"left\""));
    }

    @Test @Order(21)
    @DisplayName("Info - wrong position (not where player is) returns 204")
    void infoWrongPosition() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        // Use coordinates far from starting position (5,5)
        var res = get("/info?y=0&x=0&session=" + tanvirSession);

        assertEquals(204, res.statusCode());
    }

    @Test @Order(22)
    @DisplayName("Info - invalid session returns 401")
    void infoInvalidSession() throws Exception {
        var res = get("/info?y=5&x=5&session=badsession");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(23)
    @DisplayName("Info - missing session returns 401")
    void infoMissingSession() throws Exception {
        var res = get("/info?y=5&x=5");

        assertEquals(401, res.statusCode());
    }

 
    // TAKE API TESTS


    @Test @Order(30)
    @DisplayName("Take - on tile with no item returns 204")
    void takeNoItemOnTile() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        // Player is on a grass tile with no item — taking should return 204
        // Move to a known free tile first (stay at current position)
        var res = get("/take?session=" + tanvirSession);

        // 200 if tile has item, 204 if not — either is valid
        assertTrue(res.statusCode() == 200 || res.statusCode() == 204);
    }

    @Test @Order(31)
    @DisplayName("Take - invalid session returns 401")
    void takeInvalidSession() throws Exception {
        var res = get("/take?session=invalidsession");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(32)
    @DisplayName("Take - missing session returns 401")
    void takeMissingSession() throws Exception {
        var res = get("/take");

        assertEquals(401, res.statusCode());
    }

    
    // PLACE API TESTS
   

    @Test @Order(40)
    @DisplayName("Place - with empty inventory returns 204")
    void placeEmptyInventory() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/place?session=" + tanvirSession);

        assertEquals(204, res.statusCode());
    }

    @Test @Order(41)
    @DisplayName("Place - invalid session returns 401")
    void placeInvalidSession() throws Exception {
        var res = get("/place?session=invalidsession");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(42)
    @DisplayName("Place - missing session returns 401")
    void placeMissingSession() throws Exception {
        var res = get("/place");

        assertEquals(401, res.statusCode());
    }

    // USE API TESTS


    @Test @Order(50)
    @DisplayName("Use - on tile with no door returns 204")
    void useNoInteractableTile() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        // Player is on grass — using with dy=0,dx=0 should return 204 (no door here)
        var res = get("/use?dy=0&dx=0&session=" + tanvirSession);

        assertEquals(204, res.statusCode());
    }

    @Test @Order(51)
    @DisplayName("Use - diagonal target (dy=1,dx=1) returns 204 (invalid)")
    void useDiagonalInvalid() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/use?dy=1&dx=1&session=" + tanvirSession);

        assertEquals(204, res.statusCode());
    }

    @Test @Order(52)
    @DisplayName("Use - invalid session returns 401")
    void useInvalidSession() throws Exception {
        var res = get("/use?dy=0&dx=0&session=invalidsession");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(53)
    @DisplayName("Use - missing session returns 401")
    void useMissingSession() throws Exception {
        var res = get("/use?dy=0&dx=0");

        assertEquals(401, res.statusCode());
    }


    // LOGOUT API TESTS

    @Test @Order(90)
    @DisplayName("Logout - valid session returns 200")
    void logoutValidSession() throws Exception {
        // Login a fresh user just for this test
        String hash = sha256("Andre;andre123");
        String json = "{\"name\":\"Andre\",\"encpswrd\":\"" + hash + "\"}";
        var loginRes = post("/login", json);
        assertEquals(200, loginRes.statusCode());

        String andreSession = extractSession(loginRes.body());

        var logoutRes = get("/logout?session=" + andreSession);
        assertEquals(200, logoutRes.statusCode());
    }

    @Test @Order(91)
    @DisplayName("Logout - session cannot be reused after logout")
    void logoutSessionInvalidatedAfterLogout() throws Exception {
        // Login Tahsin
        String hash = sha256("Tahsin;tahsin123");
        String json = "{\"name\":\"Tahsin\",\"encpswrd\":\"" + hash + "\"}";
        var loginRes = post("/login", json);
        assertEquals(200, loginRes.statusCode());

        String tahsinSession = extractSession(loginRes.body());

        // Logout
        var logoutRes = get("/logout?session=" + tahsinSession);
        assertEquals(200, logoutRes.statusCode());

        // Try using the session after logout — should be rejected
        var moveRes = get("/move?dy=1&dx=0&session=" + tahsinSession);
        assertEquals(401, moveRes.statusCode());
    }

    @Test @Order(92)
    @DisplayName("Logout - invalid session returns 401")
    void logoutInvalidSession() throws Exception {
        var res = get("/logout?session=invalidsession");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(93)
    @DisplayName("Logout - missing session returns 401")
    void logoutMissingSession() throws Exception {
        var res = get("/logout");

        assertEquals(401, res.statusCode());
    }

    @Test @Order(94)
    @DisplayName("Logout - same session cannot be logged out twice")
    void logoutTwiceFails() throws Exception {
        String hash = sha256("Tanvir;tanvir123");
        String json = "{\"name\":\"Tanvir\",\"encpswrd\":\"" + hash + "\"}";
        var loginRes = post("/login", json);
        assertEquals(200, loginRes.statusCode());

        String session = extractSession(loginRes.body());

        get("/logout?session=" + session);            // first logout — 200
        var second = get("/logout?session=" + session); // second — 401
        assertEquals(401, second.statusCode());
    }

}
