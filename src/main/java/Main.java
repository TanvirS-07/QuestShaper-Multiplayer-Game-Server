import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/move", exchange -> {
            String response = "Move endpoint working";
            sendResponse(exchange, response);
        });

        server.createContext("/info", exchange -> {
            String response = "Info endpoint working";
            sendResponse(exchange, response);
        });

        server.start();
        System.out.println("Server started on port 8000");
    }

    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}