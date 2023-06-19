import java.security.SecureRandom;
import java.util.Base64;

public class TokenGenerator {

    public static String generateToken() {
        // Generate a random token using SecureRandom
        SecureRandom secureRandom = new SecureRandom();
        byte[] tokenBytes = new byte[16];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

}
