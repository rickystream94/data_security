package server;

import common.AuthTicket;
import common.IPrintService;
import common.Util;

/**
 * Created by ricky on 01/11/2017.
 */
public class PrintServiceImpl implements IPrintService {

    private Authenticator authenticator;
    private static final String TAG = "[*** PrintService ***]: ";

    public PrintServiceImpl(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public AuthTicket print(String filename, String printer, AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.PRINT, authTicket);

        //Specific method code should be placed here...

        String message = "PRINT REQUEST for file " + filename + " on printer " + printer + " requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket queue(AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.QUEUE, authTicket);

        //Specific method code should be placed here...

        String message = "QUEUE REQUEST requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket topQueue(int job, AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.TOP_QUEUE, authTicket);

        //Specific method code should be placed here...

        String message = "TOP_QUEUE REQUEST for job " + job + " requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket start(AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.START, authTicket);

        //Specific method code should be placed here...

        String message = "START REQUEST requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket stop(AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.STOP, authTicket);

        //Specific method code should be placed here...

        String message = "STOP REQUEST requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket restart(AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.RESTART, authTicket);

        //Specific method code should be placed here...

        String message = "RESTART REQUEST requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket status(AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.STATUS, authTicket);

        //Specific method code should be placed here...

        String message = "STATUS REQUEST requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket readConfig(String parameter, AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.READ_CONFIG, authTicket);

        //Specific method code should be placed here...

        String message = "READ_CONFIG REQUEST for parameter " + parameter + " requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    @Override
    public AuthTicket setConfig(String parameter, String value, AuthTicket authTicket) throws Exception {
        authenticator.isUserAuthenticated(authTicket);
        authenticator.isUserAuthorized(PermissionType.SET_CONFIG, authTicket);

        //Specific method code should be placed here...

        String message = "SET_CONFIG REQUEST for parameter " + parameter + " and value " + value + " requested from user " + authTicket.getUsername();
        logInfo(message);
        authTicket.setMessage(message);
        return authTicket;
    }

    private static void logInfo(String message) {
        System.out.println(TAG + Util.getCurrentTime() + message);
    }
}
