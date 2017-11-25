package server;

import com.github.windpapi4j.WinAPICallFailedException;
import common.IAuthenticator;
import common.IPrintService;
import common.Util;
import crypto.CryptoManager;

import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.io.*;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;

public class ApplicationServer {

    private static final int PORT = 9000;
    private static final String PRINT_SERVICE = "PrintService";
    private static final String AUTHENTICATOR = "Authenticator";
    private static final String TAG = "[*** ApplicationServer ***]: ";
    private static DbmsManager dbmsManager;
    private static PolicyManager policyManager;

    public static void main(String args[]) {
        try {
            //Set SSL properties
            setProperties();

            //Init WinDPAPI
            if (!CryptoManager.checkWinDPAPI())
                return;

            //Initializing DbmsManager
            initDbms();

            //Initializing PolicyManager
            initPolicyManager();

            //Init RMI connection
            initRmiConnection();

            logInfo("Waiting for client invocations...");
        } catch (Exception e) {
            System.err.println(TAG + " Exception occurred:");
            e.printStackTrace();
        }
    }

    private static void setProperties() {
        String pass = "password"; //Hardcoded password for key/trust stores
        System.setProperty("javax.net.ssl.keyStore", "ssl/keystore-server.jks");
        System.setProperty("javax.net.ssl.keyStorePassword", pass);
    }

    private static void initDbms() throws SQLException, WinAPICallFailedException, IOException {
        dbmsManager = new DbmsManager();
        dbmsManager.init();
    }

    //TODO: choose dynamically
    private static void initPolicyManager() {
        policyManager = new AclPolicyManager();
        //policyManager = new RoleBasedPolicyManager();
    }

    /**
     * Binds both remote objects to the RMI registry
     *
     * @throws RemoteException
     * @throws AlreadyBoundException
     */
    private static void initRmiConnection() throws RemoteException, AlreadyBoundException {
        Authenticator authenticator = new Authenticator(dbmsManager, policyManager);
        IPrintService printServer = new PrintServiceImpl(authenticator);
        Registry registry = LocateRegistry.createRegistry(PORT, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        IPrintService printServiceStub = (IPrintService) UnicastRemoteObject.exportObject(printServer, PORT, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        IAuthenticator authenticatorStub = (IAuthenticator) UnicastRemoteObject.exportObject(authenticator, PORT, new SslRMIClientSocketFactory(), new SslRMIServerSocketFactory());
        registry.rebind(PRINT_SERVICE, printServiceStub);
        registry.rebind(AUTHENTICATOR, authenticatorStub);
        logInfo(PRINT_SERVICE + " is now bound on registry with port " + PORT);
        logInfo(AUTHENTICATOR + " is now bound on registry with port " + PORT);
    }

    private static void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }
}
