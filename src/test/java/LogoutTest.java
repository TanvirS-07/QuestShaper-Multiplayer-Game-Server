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
public class LogoutTest {

    private static HttpServer server;
    private static HttpClient client;
    private static final String BASE = "http://localhost:8001";

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
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

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
        int start = body.indexOf("\"session\":\"") + 11;
        int end = body.indexOf("\"", start);
        return body.substring(start, end);
    }

    @Test @Order(90)
    @DisplayName("Logout - valid session returns 200")
    void logoutValidSession() throws Exception {
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
        String hash = sha256("Tahsin;tahsin123");
        String json = "{\"name\":\"Tahsin\",\"encpswrd\":\"" + hash + "\"}";
        var loginRes = post("/login", json);
        assertEquals(200, loginRes.statusCode());

        String tahsinSession = extractSession(loginRes.body());

        var logoutRes = get("/logout?session=" + tahsinSession);
        assertEquals(200, logoutRes.statusCode());

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

        get("/logout?session=" + session);
        var second = get("/logout?session=" + session);
        assertEquals(401, second.statusCode());
    }
}