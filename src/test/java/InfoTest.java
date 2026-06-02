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
public class InfoTest {

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
        
        // Login Tanvir before all info tests
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

    @Test @Order(20)
    @DisplayName("Info - valid session at player position returns 200 with map data")
    void infoValidAtPlayerPosition() throws Exception {
        assertNotNull(tanvirSession, "Tanvir must be logged in first");

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
}