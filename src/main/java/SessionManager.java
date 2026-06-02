import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SessionManager {
    // Active sessions are kept in memory and map session tokens to usernames.
    private static final Map<String, String> session = new HashMap<>();
    private static final SecureRandom random = new SecureRandom();

    public static boolean validateLogin(String name, String encpswrd) {
        // USERS keeps credentials out of source code and allows
        // deployment-specific logins.
        String users = System.getenv("USERS");

        if (users == null || users.isBlank()) {
            return false;
        }

        for (String userEntry : users.split(",")) {
            String[] parts = userEntry.split(":", 2);

            if (parts.length != 2) {
                continue;
            }

            String allowedName = parts[0];
            String allowedPassword = parts[1];

            if (!allowedName.equals(name)) {
                continue;
            }

            String expectedHash = hashPassword(allowedName + ";" + allowedPassword);
            return expectedHash.equals(encpswrd);
        }

        return false;
    }

    public static String createSession(String name) {
        String token = generateToken();
        session.put(token, name);
        return token;
    }

    public static boolean isValidSession(String token) {
        return token != null && session.containsKey(token);
    }

    public static boolean logout(String token) {
        return session.remove(token) != null;
    }

    public static String getUsername(String token) {
        return session.get(token);
    }

    private static String generateToken() {
        // 32 random bytes become a 64-character hexadecimal session token.
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static String hashPassword(String input) {
        // API compares the SHA-256 hash of "name;password".
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String getSessionForUsername(String username) {
        // Used on login to replace sessions for the same username.
        for (Map.Entry<String, String> entry : session.entrySet()) {
            if (entry.getValue().equals(username)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
