package server;

import common.AuthTicket;
import common.IAuthenticator;
import common.Util;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Authenticator implements IAuthenticator {

    private static final String TAG = "[*** Authenticator ***]: ";
    private List<AuthUser> authUsers;
    private int MAX_SESSION_DURATION_MILLIS = 60000; //1 minute
    private DbmsManager dbmsManager;
    private PolicyManager policyManager;

    Authenticator(DbmsManager dbmsManager, PolicyManager policyManager) {
        super();
        authUsers = new ArrayList<>();
        this.dbmsManager = dbmsManager;
        this.policyManager = policyManager;
    }

    @Override
    public AuthTicket login(AuthTicket authTicket) {
        try {
            //Check if user exists, otherwise create new user
            if (!userExists(authTicket.getUsername()))
                return signUp(authTicket);

            try {
                //Check if user is already authenticated
                isUserAuthenticated(authTicket);
                authTicket.setMessage(TAG + "User " + authTicket.getUsername() + " is already logged in with session " + authTicket.getSessionId());
                return authTicket;
            } catch (Exception e) {
                //If we get here, user is not authenticated and we have to check the password
                //First step: verify authentication to respect of password correctness
                if (!passwordIsVerified(authTicket)) {
                    authTicket.setMessage(TAG + " UNAUTHORIZED ACCESS ATTEMPT! Login was unsuccessful because of wrong password.");
                    return authTicket;
                }

                //If password is correct, user must be authenticated
                authTicket = authenticateUser(authTicket);
                authTicket.setMessage(TAG + "Welcome " + authTicket.getUsername() + "! You're now logged in with session ID " + authTicket.getSessionId() + ". Are you ready to print?");
                return authTicket;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            authTicket.setMessage(e.getMessage());
            return authTicket;
        }
    }

    private boolean passwordIsVerified(AuthTicket authTicket) throws SQLException {
        return dbmsManager.isPasswordMatching(authTicket.getUsername(), authTicket.getHashedPassword());
    }

    private boolean userExists(String username) throws SQLException {
        return dbmsManager.userExists(username);
    }

    private AuthTicket signUp(AuthTicket authTicket) {
        String username = authTicket.getUsername();
        String hashedPassword = authTicket.getHashedPassword();
        logInfo("Signing up new user " + username + "...");
        try {
            dbmsManager.registerUser(username, hashedPassword);
            logInfo("User " + username + " is now signed up. Authenticating...");
            authTicket = authenticateUser(authTicket);
            authTicket.setMessage(TAG + " Welcome " + username + ", your account has just been registered and you're now logged in.");
            return authTicket;
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks if specified user is already authenticated. Returns silently if user is authenticated, otherwise it raises an exception with an error message.
     *
     * @param authTicket
     * @return
     */
    void isUserAuthenticated(AuthTicket authTicket) throws Exception {
        String error;
        //1) Verify if user is not authenticated
        if (authUsers.stream().noneMatch(x -> x.getUsername().equals(authTicket.getUsername()))) {
            error = "User " + authTicket.getUsername() + " is not found among authenticated users. Please login first.";
            logInfo(error);
            throw new Exception(error);
        }

        //2) Verify if his session has not expired
        logInfo("User " + authTicket.getUsername() + " was found among Authenticated Users: checking if session is still valid...");
        Date now = new Date();
        boolean isSessionValid = authUsers.stream().anyMatch(x -> x.getSessionId() == authTicket.getSessionId());
        if (isSessionValid && (now.getTime() - authTicket.getTimestamp().getTime()) > MAX_SESSION_DURATION_MILLIS) {
            error = "Session with ID " + authTicket.getSessionId() + " for user " + authTicket.getUsername() + " has expired (current session time limit is " + MAX_SESSION_DURATION_MILLIS + " millis)! Need to re-login.";
            logInfo(error);
            quitSession(authTicket);
            throw new Exception(error);
        } else {
            logInfo("Session with ID " + authTicket.getSessionId() + " for user " + authTicket.getUsername() + " is valid: authenticated!");
        }
    }

    /**
     * Calls the PolicyManager currently in use to see if the user requesting the action is authorized.
     * Returns silently if user is authorized, otherwise raises an exception with an error message.
     */
    void isUserAuthorized(PermissionType permissionType, AuthTicket authTicket) throws Exception {
        policyManager.checkPermission(new PrinterPermission(permissionType), authTicket);
    }

    /**
     * Creates a new session for the user specified by the authTicket
     *
     * @param authTicket
     * @return
     */
    private AuthTicket authenticateUser(AuthTicket authTicket) {
        //Increment current session ID or start from 1 if it's the first time
        int sessionId = authTicket.getSessionId() != AuthTicket.INVALID_SESSION_ID ? authTicket.getSessionId() + 1 : 1;
        Date now = new Date();
        AuthUser authUser = new AuthUser(authTicket.getUsername(), sessionId);
        authUsers.add(authUser);
        logInfo("User " + authUser.getUsername() + " is logged in with session ID " + authUser.getSessionId() + "!");

        //Set parameters on auth ticket to be sent back to client.Client
        authTicket.setSessionId(sessionId);
        authTicket.setTimestamp(now);
        return authTicket;
    }

    @Override
    public void quitSession(AuthTicket authTicket) {
        String username = authTicket.getUsername();
        boolean found = authUsers.removeIf(x -> x.getUsername().equals(username));
        if (found) {
            logInfo(username + "'s session is expired or was shut down. Automatically logged off! Please login again");
        }
    }

    private static void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }
}
