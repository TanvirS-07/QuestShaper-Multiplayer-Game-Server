import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class SessionManager{
    private static final Map<String, String> session = new HashMap<>();
    private static final SecureRandom random = new SecureRandom();

    public static boolean validateLogin(String name, String encpswrd){
        String accessKey = System.getenv("ACCESS_KEY");
        String secretAccessKey = System.getenv("SECRET_ACCESS_KEY");

        if(accessKey == null || secretAccessKey == null){
            System.err.println("Access Key or Secret Access Key missing");
            return false;
        }

        if(!accessKey.equals(name)) {
            return false;
        }

        String expectedHash = hashPassword(name + ";" + secretAccessKey);
        return expectedHash.equals(encpswrd);
    }

    public static String createSession(String name){
        String token = generateToken();
        session.put(token, name);
        return token;
    }

    public static boolean isValidSession(String token){
        return token != null && session.containsKey(token);
    }

    public static boolean logout(String token){
        return session.remove(token) != null;
    }

    public static String getUsername(String token){
        return session.get(token);
    }

    private static String generateToken(){
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        StringBuilder sb = new StringBuilder();
        for(byte b : bytes){
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    private static String hashPassword(String input){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());

            StringBuilder hex = new StringBuilder();
            for(byte b : hash){
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}