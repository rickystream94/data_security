package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import com.github.windpapi4j.InitializationFailedException;
import com.github.windpapi4j.WinAPICallFailedException;
import com.github.windpapi4j.WinDPAPI;
import com.github.windpapi4j.WinDPAPI.CryptProtectFlag;

import javax.xml.bind.DatatypeConverter;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by ricky on 02/11/2017.
 */
public class CryptoManager {

    private static final int SALT_BYTE_LENGTH = 16;
    private static final String SALT_ALGORITHM_NAME = "SHA1PRNG";
    private static final String HASHING_ALGORITHM = "SHA-256";
    private static WinDPAPI winDPAPI;

    public static boolean checkWinDPAPI() throws InitializationFailedException {
        if (!WinDPAPI.isPlatformSupported()) {
            System.err.println("The Windows Data Protection API (DPAPI) is not available on " + System.getProperty("os.name") + ".");
            return false;
        } else {
            winDPAPI = WinDPAPI.newInstance(CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
            return true;
        }
    }

    /**
     * Hashes the provided password by adding automatically a salt
     *
     * @param password original password
     * @return the hashed password if the hashing algorithm is found, null otherwise
     */
    public static String getHashedPassword(String password) {
        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance(HASHING_ALGORITHM);
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return generatedPassword;
    }

    public static String getStringSaltFromByteArr(byte[] salt) {
        return DatatypeConverter.printBase64Binary(salt);
    }

    /**
     * Returns a random salt to be used to hash a password.
     *
     * @return a 16 bytes random salt
     */
    public static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance(SALT_ALGORITHM_NAME);
        byte[] salt = new byte[SALT_BYTE_LENGTH];
        sr.nextBytes(salt);
        return salt;
    }

    public static String encrypt(String plaintext) throws WinAPICallFailedException {
        byte[] encryptedBytes = winDPAPI.protectData(plaintext.getBytes(UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedString) throws WinAPICallFailedException {
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedString);
        return new String(winDPAPI.unprotectData(encryptedBytes), UTF_8);
    }
}
