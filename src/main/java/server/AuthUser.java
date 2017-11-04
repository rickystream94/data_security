package server;

/**
 * Created by ricky on 01/11/2017.
 */
public class AuthUser {

    private String username;
    private int sessionId;

    public AuthUser(String username, int sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    public int getSessionId() {
        return sessionId;
    }
}
