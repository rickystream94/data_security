package print;

import com.github.windpapi4j.WinAPICallFailedException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Created by ricky on 01/11/2017.
 */
public class PrintServerImpl implements IPrintServer {

    private static final int PORT = 9000;
    private static final String REMOTE_OBJECT_NAME = "PrintServer";
    private static final String TAG = "[*** PrintServer ***]: ";
    private static List<AuthUser> authUsers;
    private static int MAX_SESSION_DURATION_MILLIS = 60000; //1 minute
    private static DBMS dbms;
    private static Scanner input = new Scanner(System.in);

    protected PrintServerImpl() {
        super();
        authUsers = new ArrayList<>();
    }

    public static void main(String args[]) {
        try {
            //Init WinDPAPI
            PasswordManager.checkWinDPAPI();

            //Init RMI connection
            PrintServerImpl printServer = new PrintServerImpl();
            Registry registry = LocateRegistry.createRegistry(PORT);
            IPrintServer stub = (IPrintServer) UnicastRemoteObject.exportObject(printServer, PORT);
            registry.rebind(REMOTE_OBJECT_NAME, stub);
            logInfo(REMOTE_OBJECT_NAME + " is now bound on registry with port " + PORT);

            //Initializing print.DBMS
            dbms = new DBMS();
            dbCredentials();
            dbms.init();

            logInfo("Waiting for client invocations...");
        } catch (Exception e) {
            System.err.println(TAG + " Exception occurred:");
            e.printStackTrace();
        }
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
    public void setConfig(String parameter, String value, AuthTicket authTicket) throws RemoteException {
        String message;
        if (isUserAuthenticated(authTicket)) {
            message = "SET_CONFIG REQUEST for parameter " + parameter + " with value " + value + " requested from user " + authTicket.getUsername();
            logInfo(message);
            authTicket.setMessage(message);
        } else {
            message = "UNAUTHORIZED PRINT ATTEMPT BLOCKED: session expired or invalid login.";
            logInfo(message);
            authTicket.setMessage(message);
        }
        logInfo(message);
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
        return dbms.isPasswordMatching(authTicket.getUsername(), authTicket.getHashedPassword());
    }

    private boolean userExists(String username) throws SQLException {
        return dbms.userExists(username);
    }

    @Override
    public AuthTicket signUp(AuthTicket authTicket) {
        String username = authTicket.getUsername();
        String hashedPassword = authTicket.getHashedPassword();
        logInfo("Signing up new user " + username + "...");
        try {
            dbms.registerUser(username, hashedPassword);
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

        //Set parameters on auth ticket to be sent back to print.Client
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

    private static void dbCredentials() throws WinAPICallFailedException {
        logInfo("Enter Database Host:");
        String host = input.nextLine();
        logInfo("Enter Database Name:");
        String db = input.nextLine();
        logInfo("Enter Database Username:");
        String user = input.nextLine();
        logInfo("Enter Database Password:");
        String password = input.nextLine();
        logInfo("Enter Database Port:");
        int port = input.nextInt();

        logInfo("Encrypting DB properties...");
        String jsonDbProperties = dbms.createJsonFromDbProperties(host, db, user, password, port);
        String encryptedProperties = PasswordManager.encrypt(jsonDbProperties);
        DBMS.setEncryptedProperties(encryptedProperties);
        logInfo("Properties encrypted --> " + encryptedProperties);
    }
}
