package print;

import com.github.windpapi4j.WinAPICallFailedException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.StringReader;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

/**
 * Created by ricky on 02/11/2017.
 */
public class DBMS {

    private Connection con;
    private static final String TABLE_USERS = "users";
    private static final String TAG = "*** [print.DBMS] ***: ";
    private static final int SHA_BYTE_LENGTH = 256;
    private static String encryptedProperties;

    public static void setEncryptedProperties(String encryptedProperties) {
        DBMS.encryptedProperties = encryptedProperties;
    }

    //todo: read from file: if empty/not existing, create.
    public void init() throws SQLException, WinAPICallFailedException {
        logInfo("Initializing MySQL Database connection with encrypted parameters...");
        JsonObject properties = decryptProperties();
        con = connect(properties.getString("host"), properties.getInt("port"), properties.getString("database"), properties.getString("user"), properties.getString("password"));
        logInfo("Connected!");
    }

    private JsonObject decryptProperties() throws WinAPICallFailedException {
        JsonReader jsonReader = Json.createReader(new StringReader(PasswordManager.decrypt(encryptedProperties)));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }

    private Connection connect(String host, int port, String dbName, String user, String password) throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?autoReconnect=true&useSSL=false&serverTimezone=Europe/Rome", user, password);
    }

    public boolean isPasswordMatching(String username, String providedPassword) throws SQLException {
        String query = "SELECT userId AS userId FROM " + TABLE_USERS + " WHERE username=? AND SHA2(CONCAT(?,salt)," + SHA_BYTE_LENGTH + ")=password";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, providedPassword);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            int userId = resultSet.getInt("userId");
            logInfo("Found user ID " + userId + " for provided password! Login successful!");
            return true;
        } else {
            logInfo("No user found with provided password!");
            return false;
        }
    }

    /**
     * Registers new user in DB by saving hash of password+salt
     *
     * @param username
     * @param password
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    public void registerUser(String username, String password) throws SQLException, NoSuchAlgorithmException {
        byte[] salt = PasswordManager.getSalt();
        String stringSalt = PasswordManager.getStringSaltFromByteArr(salt);
        String concat = password + stringSalt;
        String query = "INSERT INTO " + TABLE_USERS + "(username,password,salt) VALUES (?,SHA2(?," + SHA_BYTE_LENGTH
                + "),?)";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, concat);
        preparedStatement.setString(3, stringSalt);
        int rows = preparedStatement.executeUpdate();
        if (rows != 1)
            throw new SQLException("No rows have been affected!");
    }

    public boolean userExists(String username) throws SQLException {
        String query = "SELECT username FROM " + TABLE_USERS + " WHERE username=?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, username);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            if (resultSet.getString("username").equals(username))
                return true;
        }
        return false;
    }

    private void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }

    public String createJsonFromDbProperties(String host, String dbName, String user, String password, int port) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("host", host);
        builder.add("database", dbName);
        builder.add("user", user);
        builder.add("password", password);
        builder.add("port", port);
        JsonObject object = builder.build();
        return object.toString();
    }
}
