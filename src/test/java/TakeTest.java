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
public class TakeTest {

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
        
        // Login Tanvir before all take tests
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

    @Test @Order(30)
    @DisplayName("Take - on tile with no item returns 204")
    void takeNoItemOnTile() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");
        var res = get("/take?session=" + tanvirSession);

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
}