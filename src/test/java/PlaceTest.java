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
public class PlaceTest {

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
        
        // Login Tanvir before all place tests
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
}