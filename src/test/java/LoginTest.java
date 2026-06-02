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
public class LoginTest {

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
}