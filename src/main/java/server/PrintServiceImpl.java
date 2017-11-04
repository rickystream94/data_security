package server;

import common.AuthTicket;
import common.IPrintService;
import common.Util;

import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ricky on 01/11/2017.
 */
public class PrintServiceImpl implements IPrintService {

    private static final String TAG = "[*** PrintService ***]: ";
    private List<AuthUser> authUsers;
    private int MAX_SESSION_DURATION_MILLIS = 60000; //1 minute
    private DbmsManager dbmsManager;

    protected PrintServiceImpl(DbmsManager dbmsManager) {
        super();
        authUsers = new ArrayList<>();
        this.dbmsManager = dbmsManager;
    }

    @Override
    public AuthTicket print(String filename, String printer, AuthTicket authTicket) {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "PRINT REQUEST for file " + filename + " on printer " + printer + " requested from user " + authTicket.getUsername();
        } else {
            message = "UNAUTHORIZED PRINT INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket queue(AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "QUEUE REQUEST requested from user " + authTicket.getUsername();
        } else {
            message = "UNAUTHORIZED QUEUE INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket topQueue(int job, AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "TOP_QUEUE REQUEST for job " + job + " requested from user " + authTicket.getUsername();
            logInfo(message);
            authTicket.setMessage(message);
        } else {
            message = "UNAUTHORIZED TOP_QUEUE INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket start(AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "START REQUEST requested from user " + authTicket.getUsername();
        } else {
            message = "UNAUTHORIZED START INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket stop(AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "STOP REQUEST requested from user " + authTicket.getUsername();
        } else {
            message = "UNAUTHORIZED STOP INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket restart(AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "RESTART REQUEST requested from user " + authTicket.getUsername();
        } else {
            message = "UNAUTHORIZED RESTART INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket status(AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "STATUS REQUEST requested from user " + authTicket.getUsername();
        } else {
            message = "UNAUTHORIZED STATUS INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket readConfig(String parameter, AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "READ_CONFIG REQUEST for parameter " + parameter + " requested from user " + authTicket.getUsername();
        } else {
            message = "UNAUTHORIZED READ_CONFIG INVOCATION: session expired or invalid login.";
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket setConfig(String parameter, String value, AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "SET_CONFIG REQUEST for parameter " + parameter + " with value " + value + " requested from user " + authTicket.getUsername();
            logInfo(message);
            authTicket.setMessage(message);
        } else {
            message = "UNAUTHORIZED SET_CONFIG ATTEMPT BLOCKED: session expired or invalid login.";
            logInfo(message);
            authTicket.setMessage(message);
        }
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket login(AuthTicket authTicket) {
        try {
            //Check if user exists, otherwise create new user
            if (!userExists(authTicket.getUsername()))
                return signUp(authTicket);

            //Check if user is already authenticated
            if (isUserAuthenticated(authTicket)) {
                authTicket.setMessage(TAG + "User " + authTicket.getUsername() + " is already logged in with session " + authTicket.getSessionId());
                return authTicket;
            } else {
                //First step: verify authentication to respect of password correctness
                if (!passwordIsVerified(authTicket)) {
                    authTicket.setMessage(TAG + " UNAUTHORIZED ACCESS ATTEMPT! Login was unsuccessful because of wrong password.");
                    return authTicket;
                }

                //If password is correct, user must be authenticated
                authTicket = authenticateUser(authTicket);
                authTicket.setMessage(TAG + "Welcome " + authTicket.getUsername() + "! You're now logged in with session ID " + authTicket.getSessionId() + ". Are you ready to print_service?");
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

    @Override
    public AuthTicket signUp(AuthTicket authTicket) {
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
     * Checks if specified user is already authenticated
     *
     * @param authTicket
     * @return
     */
    private boolean isUserAuthenticated(AuthTicket authTicket) {
        boolean isAuth = false;
        //1) Verify if user is already authenticated
        if (authUsers.stream().anyMatch(x -> x.getUsername().equals(authTicket.getUsername()))) {
            logInfo("User " + authTicket.getUsername() + " was found among Authenticated Users: checking if session is still valid...");
            //2) Verify if his session has not expired
            Date now = new Date();
            boolean isSessionValid = authUsers.stream().anyMatch(x -> x.getSessionId() == authTicket.getSessionId());
            if (isSessionValid && (now.getTime() - authTicket.getTimestamp().getTime()) > MAX_SESSION_DURATION_MILLIS) {
                logInfo("Session with ID " + authTicket.getSessionId() + " for user " + authTicket.getUsername() + " has expired (current session time limit is " + MAX_SESSION_DURATION_MILLIS + " millis)! Need to re-login.");
                quitSession(authTicket);
            } else {
                logInfo("Session with ID " + authTicket.getSessionId() + " for user " + authTicket.getUsername() + " is valid: authenticated!");
                isAuth = true;
            }
        }
        return isAuth;
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
