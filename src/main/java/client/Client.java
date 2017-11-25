package client;

import common.IAuthenticator;
import crypto.CryptoManager;
import common.AuthTicket;
import common.IPrintService;
import common.Util;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

/**
 * Created by ricky on 01/11/2017.
 */
public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 9000;
    private static final String PRINT_SERVER = "PrintService";
    private static final String AUTHENTICATOR = "Authenticator";
    private static final String TAG = "[*** Client ***]: ";
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static String username;
    private static String password;
    private static Scanner input = new Scanner(System.in);
    private static IPrintService printServer;
    private static IAuthenticator authenticator;
    private static AuthTicket authTicket;

    public static void main(String args[]) {
        //Set SSL Properties
        setProperties();

        //Connect to server and if successful shows client's menu
        connectToServer();
    }

    private static void setProperties() {
        String pass = "password"; //Hardcoded password for key/trust stores
        System.setProperty("javax.net.ssl.trustStore", "ssl/truststore-client.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", pass);
    }

    /**
     * Establishes connection to remote print_service server and logs in the user
     */
    private static void connectToServer() {
        try {
            //Credentials validation
            insertCredentials();
            while (!validateUsername(username) || !validatePassword(password)) {
                logInfo("Invalid username/passoword! (Too weak?)");
                insertCredentials();
            }

            //RMI connection
            initRmiConnection();

            //Client-side password hashing
            logInfo("Hashing password...");
            //AuthTicket will never contain original password, but always a hashed version
            String hashedPassword = CryptoManager.getHashedPassword(password);
            logInfo("Done: " + password + " ---> " + hashedPassword);

            //Login by generating fresh Authentication Ticket
            logInfo("Logging in using fresh authentication ticket with provided credentials...");
            authTicket = authenticator.login(new AuthTicket(username, hashedPassword));
            System.out.println(authTicket.getMessage());
            showMenu();
        } catch (NotBoundException | RemoteException e) {
            e.printStackTrace();
        }
    }

    private static void initRmiConnection() throws RemoteException, NotBoundException {
        Registry registry = LocateRegistry.getRegistry(HOST, PORT, new SslRMIClientSocketFactory());
        logInfo("Looking up in RMI registry for remote object with name " + PRINT_SERVER + "...");
        printServer = (IPrintService) registry.lookup(PRINT_SERVER);
        logInfo("Found remote object " + PRINT_SERVER + " bound in registry!");
        logInfo("Looking up in RMI registry for remote object with name " + AUTHENTICATOR + "...");
        authenticator = (IAuthenticator) registry.lookup(AUTHENTICATOR);
        logInfo("Found remote object " + AUTHENTICATOR + " bound in registry!");
    }

    private static void insertCredentials() {
        logInfo("Insert your nickname:");
        username = input.nextLine();
        logInfo("Insert your password:");
        password = input.nextLine();
    }

    private static boolean validatePassword(String password) {
        return password.length() >= MIN_PASSWORD_LENGTH;
    }

    private static boolean validateUsername(String username) {
        return username.length() >= MIN_USERNAME_LENGTH && username.matches("^[a-zA-Z][a-zA-Z_0-9]{2,15}$");
    }

    private static void showMenu() throws RemoteException {
        int choice;
        do {
            do {
                logInfo("\n\n*** Choose your next action: ***");
                System.out.println("0) Re-login\n1) Print\n2) Show print_service queue\n3) Move job to top\n4) Start print_service server\n5) Stop print_service server\n6) Restart print_service server\n7) Get print_service server status\n8) Read config\n9) Set config\n10) Exit");
                choice = input.nextInt();
            } while (choice < 0 || choice > 11);

            try {
                switch (choice) {
                    case 0:
                        authTicket = authenticator.login(authTicket);
                        break;
                    case 1:
                        authTicket = printServer.print("FILENAME", "DTU101", authTicket);
                        break;
                    case 2:
                        authTicket = printServer.queue(authTicket);
                        break;
                    case 3:
                        authTicket = printServer.topQueue(1, authTicket);
                        break;
                    case 4:
                        authTicket = printServer.start(authTicket);
                        break;
                    case 5:
                        authTicket = printServer.stop(authTicket);
                        break;
                    case 6:
                        authTicket = printServer.restart(authTicket);
                        break;
                    case 7:
                        authTicket = printServer.status(authTicket);
                        break;
                    case 8:
                        authTicket = printServer.readConfig("PLACEHOLDER_PARAMETER", authTicket);
                        break;
                    case 9:
                        authTicket = printServer.setConfig("PLACEHOLDER_PARAMETER", "NEW_VALUE", authTicket);
                        break;
                    case 10:
                        authenticator.quitSession(authTicket);
                        logInfo("Closing application... Bye!");
                        return;
                    default:
                        logInfo("Unsupported operation...");
                        break;
                }
                logInfo(authTicket.getMessage());
            } catch (Exception e) {
                logInfo(e.getMessage());
            }
        } while (true);
    }

    private static void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }
}
