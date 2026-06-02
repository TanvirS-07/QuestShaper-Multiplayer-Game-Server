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
public class MoveTest {

    private static HttpServer server;
    private static HttpClient client;
    private static final String BASE = "http://localhost:8001";
    private static String tanvirSession;

    @BeforeAll
    static void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/logout", new LogoutHandler());
        server.createContext("/move", new MoveHandler());
        server.createContext("/info", new InfoHandler());
        server.createContext("/take", new TakeHandler());
        server.createContext("/place", new PlaceHandler());
        server.createContext("/use", new UseHandler());
        server.start();

        client = HttpClient.newHttpClient();
        
        // Login Tanvir before all move tests
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest("Tanvir;tanvir123".getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) hex.append(String.format("%02x", b));
        String hashStr = hex.toString();
        
        String json = "{\"name\":\"Tanvir\",\"encpswrd\":\"" + hashStr + "\"}";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        var res = client.send(request, HttpResponse.BodyHandlers.ofString());
        
        String body = res.body();
        int start = body.indexOf("\"session\":\"") + 11;
        int end = body.indexOf("\"", start);
        tanvirSession = body.substring(start, end);
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

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
}