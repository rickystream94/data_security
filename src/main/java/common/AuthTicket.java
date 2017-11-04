package common;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ricky on 02/11/2017.
 */
public class AuthTicket implements Serializable {

    private String username;
    private String hashedPassword;
    private int sessionId;
    private Date timestamp;
    private String message;
    private static final long serialVersionUID = 42L;
    public static final int INVALID_SESSION_ID = 0;

    public AuthTicket(String username, String hashedPassword) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        sessionId = INVALID_SESSION_ID;
    }

    public String getUsername() {
        return username;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public int getSessionId() {
        return sessionId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
